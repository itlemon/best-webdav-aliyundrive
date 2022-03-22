package cn.itlemon.best.web.dav.aliyun.client;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

/**
 * 阿里云网盘访问客户端
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-21
 */
@Slf4j
@Component
public class AliyunDriveClient {

    private final AliyunDriveApiClient client;

    public AliyunDriveClient(AliyunDriveApiClient client) {
        this.client = client;
    }

    public String getDriveId() {
        return client.getDriveId();
    }

    public Response download(String url, HttpServletRequest httpServletRequest, long size) {
        return client.download(url, httpServletRequest, size);
    }

    public void upload(String url, byte[] bytes, final int offset, final int byteCount) {
        client.upload(url, bytes, offset, byteCount);
    }

    public String post(String url, Object body) {
        return client.post(url, body);
    }

    public String put(String url, Object body) {
        return client.put(url, body);
    }

    public String get(String url, Map<String, String> params) {
        return client.get(url, params);
    }

}
