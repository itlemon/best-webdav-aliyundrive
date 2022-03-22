package cn.itlemon.best.web.dav.aliyun.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.RateLimiter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import cn.itlemon.best.web.dav.aliyun.config.AliyunDriveConfig;
import cn.itlemon.best.web.dav.aliyun.constant.AliyunDriveConstant;
import lombok.extern.slf4j.Slf4j;
import net.sf.webdav.exceptions.WebdavException;
import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/**
 * 封装http接口的client
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-21
 */
@Slf4j
@Component
class AliyunDriveApiClient {

    private static final RateLimiter LOG_RATE_LIMITER = RateLimiter.create(1);

    private final AliyunDriveConfig aliyunDriveConfig;

    private final OkHttpClient httpClient;

    public AliyunDriveApiClient(AliyunDriveConfig aliyunDriveConfig) {
        // 构建一个okHttpClient
        this.httpClient = new Builder()
                // 新增一个拦截器，设置请求头等信息
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    request = request.newBuilder()
                            // 添加浏览器代理信息和认证信息
                            .addHeader(AliyunDriveConstant.USER_AGENT_HEADER_NAME, aliyunDriveConfig.getAgent())
                            .addHeader(AliyunDriveConstant.AUTHORIZATION_HEADER_NAME,
                                    aliyunDriveConfig.getAuthorization())
                            .build();
                    return chain.proceed(request);
                })
                // 添加认证器
                .authenticator(new Authenticator() {
                    @Nullable
                    @Override
                    public Request authenticate(@Nullable Route route, Response response) throws IOException {
                        // 访问阿里云网盘任何接口，在没有登录的情况下，会返回401状态码以及如下信息
                        // {
                        //    "code": "AccessTokenInvalid",
                        //    "message": "AccessToken is invalid. ErrValidateTokenFailed"
                        // }
                        if (response.code() == HttpStatus.HTTP_UNAUTHORIZED && response.body() != null
                                && response.body().string()
                                .contains(AliyunDriveConstant.PART_OF_API_ERROR_MESSAGE)) {
                            String aliyunDriveUserInfo;
                            try {
                                // 根据refresh token来获取用户信息
                                aliyunDriveUserInfo = post(AliyunDriveConstant.ALIYUN_DRIVE_REFRESH_USER_INFO_API,
                                        Collections.singletonMap(AliyunDriveConstant.REFRESH_TOKEN_KEY,
                                                readRefreshToken()));
                            } catch (Exception e) {
                                // 如果更新token失败，先清空原token文件，再尝试一次
                                log.warn("AliyunDriveApiClient update refresh token fail, try again now.");
                                deleteRefreshTokenFile();
                                aliyunDriveUserInfo = post(AliyunDriveConstant.ALIYUN_DRIVE_REFRESH_USER_INFO_API,
                                        Collections.singletonMap(AliyunDriveConstant.REFRESH_TOKEN_KEY,
                                                readRefreshToken()));
                            }
                            // 获取accessToken
                            String accessToken = AliyunDriveApiClient.toString(
                                    JSONUtil.parseObj(aliyunDriveUserInfo).get(AliyunDriveConstant.ACCESS_TOKEN_KEY));
                            String refreshToken = AliyunDriveApiClient.toString(
                                    JSONUtil.parseObj(aliyunDriveUserInfo).get(AliyunDriveConstant.REFRESH_TOKEN_KEY));
                            Preconditions.checkArgument(StrUtil.isBlank(accessToken),
                                    "AliyunDriveApiClient getAccessToken fail.");
                            Preconditions.checkArgument(StrUtil.isBlank(refreshToken),
                                    "AliyunDriveApiClient getRefreshToken fail.");

                            // 更新refreshToken
                            aliyunDriveConfig.setAuthorization(accessToken);
                            writeRefreshToken2File(refreshToken);
                            return response.request().newBuilder()
                                    .header(AliyunDriveConstant.AUTHORIZATION_HEADER_NAME, accessToken)
                                    .build();
                        }
                        return null;
                    }
                })
                // 设置超时时间均为1min
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .connectTimeout(1, TimeUnit.MINUTES)
                .build();
        this.aliyunDriveConfig = aliyunDriveConfig;
    }

    @PostConstruct
    public void init() {
        if (StrUtil.isBlank(aliyunDriveConfig.getDriveId())) {
            String result = post("/user/get", Collections.emptyMap());
            String driveId = toString(JSONUtil.parseObj(result).get(AliyunDriveConstant.DEFAULT_DRIVE_ID_KEY));
            aliyunDriveConfig.setDriveId(driveId);
        }
    }

    public String getDriveId() {
        return aliyunDriveConfig.getDriveId();
    }

    /**
     * 下载文件
     *
     * @param url 请求url
     * @param httpServletRequest 请求
     * @param size 文件大小
     * @return 返回体
     */
    public Response download(String url, HttpServletRequest httpServletRequest, long size) {
        Request.Builder builder = new Request.Builder().header(AliyunDriveConstant.REFERER_HEADER_NAME,
                AliyunDriveConstant.ALIYUN_DRIVE_MAIN_URL);
        String range = httpServletRequest.getHeader(AliyunDriveConstant.RANGE_HEADER_NAME);
        if (StrUtil.isNotBlank(range)) {
            // 超过size的range后部分删除
            String[] split = range.split("-");
            if (split.length == 2) {
                String end = split[1];
                if (Long.parseLong(end) >= size) {
                    range = range.substring(0, range.lastIndexOf('-') + 1);
                }
            }
            builder.header(AliyunDriveConstant.RANGE_HEADER_NAME, range);
        }

        String ifRange = httpServletRequest.getHeader(AliyunDriveConstant.IF_RANGE_HEADER_NAME);
        if (StrUtil.isNotBlank(ifRange)) {
            builder.header(AliyunDriveConstant.IF_RANGE_HEADER_NAME, ifRange);
        }

        Request request = builder.url(url).build();
        Response response;
        try {
            response = httpClient.newCall(request).execute();
            return response;
        } catch (IOException e) {
            throw new WebdavException(e);
        }
    }

    /**
     * 上传文件
     *
     * @param url 请求url
     * @param bytes 字节数组
     * @param offset 偏移量
     * @param byteCount 字节数量
     */
    public void upload(String url, byte[] bytes, final int offset, final int byteCount) {
        // 构建请求
        Request request = new Request.Builder()
                .put(RequestBody.create(MediaType.parse(""), bytes, offset, byteCount))
                .url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            log.info("AliyunDriveApiClient post: {} upload, response code: {}, response body: {}", url, response.code(),
                    response.body());
            if (!response.isSuccessful()) {
                log.error("AliyunDriveApiClient post fail, url: {}, response code: {}, response body: {}", url,
                        response.code(), response.body());
                throw new WebdavException("AliyunDriveApiClient post fail: " + url);
            }
        } catch (Exception e) {
            throw new WebdavException(e);
        }
    }

    /**
     * 发送post请求
     *
     * @param url url或者uri
     * @param body 请求体
     * @return 响应体
     */
    public String post(String url, Object body) {
        String bodyAsJson = JSONUtil.toJsonStr(body);
        Request request = new Request.Builder()
                .post(RequestBody.create(MediaType.parse(AliyunDriveConstant.DEFAULT_CONTENT_TYPE), bodyAsJson))
                .url(wrapUrl(url)).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (LOG_RATE_LIMITER.tryAcquire()) {
                log.info("AliyunDriveApiClient call post url: {}, response body: {}, response code: {}", url,
                        bodyAsJson, response.code());
            }
            if (!response.isSuccessful()) {
                log.error("AliyunDriveApiClient call post fail，url: {}, response body: {}, response code: {}", url,
                        response.body(), response.code());
                throw new WebdavException("AliyunDriveApiClient call post fail: " + url);
            }
            return toString(response.body());
        } catch (Exception e) {
            throw new WebdavException(e);
        }
    }

    /**
     * 发起put请求
     *
     * @param url url
     * @param body 请求体
     * @return 请求结果
     */
    public String put(String url, Object body) {
        Request request = new Request.Builder()
                .put(RequestBody.create(MediaType.parse(AliyunDriveConstant.DEFAULT_CONTENT_TYPE),
                        JSONUtil.toJsonStr(body)))
                .url(wrapUrl(url)).build();
        try (Response response = httpClient.newCall(request).execute()) {
            log.info("AliyunDriveApiClient call put: {}, response code: {}, response body: {}", url, response.code(),
                    response.body());
            if (!response.isSuccessful()) {
                log.error("AliyunDriveApiClient call put fail, url: {}, response code: {}, response body: {}", url,
                        response.code(), response.body());
                throw new WebdavException("AliyunDriveApiClient call put fail: " + url);
            }
            return toString(response.body());
        } catch (Exception e) {
            throw new WebdavException(e);
        }
    }

    /**
     * 发起get请求
     *
     * @param url url
     * @param params 参数
     * @return 请求结果
     */
    public String get(String url, Map<String, String> params) {
        try {
            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(wrapUrl(url))).newBuilder();
            params.forEach(urlBuilder::addQueryParameter);
            HttpUrl httpUrl = urlBuilder.build();

            Request request = new Request.Builder().get().url(httpUrl).build();
            try (Response response = httpClient.newCall(request).execute()) {
                log.info("AliyunDriveApiClient call get: {}, response code: {}, response body: {}", httpUrl,
                        response.code(), response.body());
                if (!response.isSuccessful()) {
                    throw new WebdavException("AliyunDriveApiClient call get fail: " + httpUrl);
                }
                return toString(response.body());
            }
        } catch (Exception e) {
            throw new WebdavException(e);
        }

    }

    /**
     * 将对象转化成String
     *
     * @param object 对象
     * @return 字符串
     */
    private static String toString(Object object) {
        if (object == null) {
            return null;
        }
        return Objects.toString(object);
    }

    /**
     * 获取完整的url
     *
     * @param url url
     * @return 完整的url
     */
    private String wrapUrl(String url) {
        if (url.startsWith(AliyunDriveConstant.HTTP_PREFIX)) {
            return url;
        }
        return aliyunDriveConfig.getAliyunDriveUrl() + url;
    }

    /**
     * 从工作目录中删除refresh_token文件
     */
    private void deleteRefreshTokenFile() {
        Path path = Paths.get(getRefreshTokenFileFullName());
        try {
            Files.delete(path);
        } catch (Exception e) {
            log.error("AliyunDriveApiClient deleteRefreshTokenFile {} fail.", getRefreshTokenFileFullName(), e);
        }
    }

    /**
     * 读取refresh token
     *
     * @return refresh token
     */
    private String readRefreshToken() {
        Path path = Paths.get(getRefreshTokenFileFullName());

        try {
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                // 创建目录：/etc/aliyun-drive/
                Files.createDirectories(path.getParent());
                // 创建文件：refresh_token
                Files.createFile(path);
            }
            // 读取文件
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length != 0 && bytes.length == aliyunDriveConfig.getRefreshToken().length()) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("AliyunDriveApiClient create file {} fail", getRefreshTokenFileFullName(), e);
        }
        // 将refreshToken设置到文件中
        return writeRefreshToken2File(aliyunDriveConfig.getRefreshToken());
    }

    /**
     * 将refreshToken写入文件并返回
     *
     * @param refreshToken refreshToken
     * @return refreshToken
     */
    private String writeRefreshToken2File(String refreshToken) {
        try {
            Files.write(Paths.get(getRefreshTokenFileFullName()), refreshToken.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // 记录日志，但不fast-fail
            log.error("AliyunDriveApiClient writeRefreshToken {} fail.", refreshToken, e);
        }
        // 更新配置中的refreshToken
        aliyunDriveConfig.setRefreshToken(refreshToken);
        return refreshToken;
    }

    /**
     * 默认文件路径：/etc/aliyun-drive/refresh_token
     *
     * @return 文件路径
     */
    private String getRefreshTokenFileFullName() {
        return aliyunDriveConfig.getWorkDir() + "refresh_token";
    }

}
