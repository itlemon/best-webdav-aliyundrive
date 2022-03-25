package cn.itlemon.best.web.dav.aliyun.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.itlemon.best.web.dav.aliyun.client.AliyunDriveClient;
import cn.itlemon.best.web.dav.aliyun.constant.AliyunDriveConstant;
import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFile;
import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFileListResult;
import cn.itlemon.best.web.dav.aliyun.model.request.FileListRequest;
import cn.itlemon.best.web.dav.aliyun.store.AliyunDriveWebDavStore;
import lombok.extern.slf4j.Slf4j;
import net.sf.webdav.exceptions.WebdavException;
import okhttp3.HttpUrl;
import okhttp3.Response;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-23
 */
@Slf4j
@Service
public class AliyunDriveWebDavService {

    /**
     * 根目录
     */
    private static final String ROOT_PATH = "/";

    /**
     * 预设文件大小：10M
     */
    private static final int CHUNK_SIZE = 10 * 1024 * 1024;

    private AliyunDriveFile aliyunDriveFile;

    private final AliyunDriveClient aliyunDriveClient;
    private final AliyunDriveFileVirtualService aliyunDriveFileVirtualService;

    /**
     * 缓存
     */
    private final LoadingCache<String, Set<AliyunDriveFile>> ALIYUN_DRIVE_FILE_CACHE = CacheBuilder.newBuilder()
            .initialCapacity(128)
            .maximumSize(1024)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            // 目前这里仅仅添加一个打印日志的监听器
            .removalListener((RemovalListener<String, Set<AliyunDriveFile>>) notification -> log.info(
                    "AliyunDriveWebDavService remove cache, cacheId: {}, AliyunDriveFile: {}",
                    notification.getKey(), notification.getValue()))
            .build(new CacheLoader<>() {
                @Override
                public Set<AliyunDriveFile> load(@Nonnull String fileId) {
                    // 获取真实的文件列表
                    return getChildrenFilesFromAliyunDrive(fileId);
                }
            });

    public AliyunDriveWebDavService(AliyunDriveClient aliyunDriveClient,
            AliyunDriveFileVirtualService aliyunDriveFileVirtualService) {
        this.aliyunDriveClient = aliyunDriveClient;
        this.aliyunDriveFileVirtualService = aliyunDriveFileVirtualService;
    }

    @PostConstruct
    public void init() {
        AliyunDriveWebDavStore.setAliyunDriveWebDavService(this);
    }


    /**
     * 创建文件夹
     *
     * @param folderUri 文件夹uri
     */
    public void createFolder(String folderUri) {

    }

    /**
     * 创建资源，阿里云盘暂无创建资源的能力，所以这里不再实现相关功能
     *
     * @param resourceUri 资源uri
     */
    public void createResource(String resourceUri) {
        // do nothing
    }

    /**
     * 下载文件
     *
     * @param resourceUri 资源uri
     * @param request 请求
     * @param size 文件大小
     * @return 响应体
     */
    public Response download(String resourceUri, HttpServletRequest request, long size) {
        return null;
    }

    public void uploadPre(String resourceUri, long contentLength, InputStream content) {
        resourceUri = checkResourceUri(resourceUri);
        PathInfo pathInfo = getPathInfo(resourceUri);
        TFile parent = getTFileByPath(pathInfo.getParentPath());
        if (parent == null) {
            return;
        }
        // 如果已存在，先删除
        TFile tfile = getTFileByPath(resourceUri);
        if (tfile != null) {
            if (tfile.getSize() == size) {
                //如果文件大小一样，则不再上传
                return;
            }
            remove(path);
        }


        int chunkCount = (int) Math.ceil(((double) size) / chunkSize); // 进1法

        UploadPreRequest uploadPreRequest = new UploadPreRequest();
        //        uploadPreRequest.setContent_hash(UUID.randomUUID().toString());
        uploadPreRequest.setDrive_id(client.getDriveId());
        uploadPreRequest.setName(pathInfo.getName());
        uploadPreRequest.setParent_file_id(parent.getFile_id());
        uploadPreRequest.setSize(size);
        List<UploadPreRequest.PartInfo> part_info_list = new ArrayList<>();
        for (int i = 0; i < chunkCount; i++) {
            UploadPreRequest.PartInfo partInfo = new UploadPreRequest.PartInfo();
            partInfo.setPart_number(i + 1);
            part_info_list.add(partInfo);
        }
        uploadPreRequest.setPart_info_list(part_info_list);

        LOGGER.info("开始上传文件，文件名：{}，总大小：{}, 文件块数量：{}", resourceUri, size, chunkCount);

        String json = client.post("/file/create_with_proof", uploadPreRequest);
        UploadPreResult uploadPreResult = JsonUtil.readValue(json, UploadPreResult.class);
        List<UploadPreRequest.PartInfo> partInfoList = uploadPreResult.getPart_info_list();
        if (partInfoList != null) {
            if (size > 0) {
                virtualTFileService.createTFile(parent.getFile_id(), uploadPreResult);
            }
            LOGGER.info("文件预处理成功，开始上传。文件名：{}，上传URL数量：{}", resourceUri, partInfoList.size());

            byte[] buffer = new byte[chunkSize];
            for (int i = 0; i < partInfoList.size(); i++) {
                UploadPreRequest.PartInfo partInfo = partInfoList.get(i);

                long expires = Long.parseLong(
                        Objects.requireNonNull(Objects.requireNonNull(HttpUrl.parse(partInfo.getUpload_url()))
                                .queryParameter("x-oss-expires")));
                if (System.currentTimeMillis() / 1000 + 10 >= expires) {
                    // 已过期，重新置换UploadUrl
                    RefreshUploadUrlRequest refreshUploadUrlRequest = new RefreshUploadUrlRequest();
                    refreshUploadUrlRequest.setDrive_id(client.getDriveId());
                    refreshUploadUrlRequest.setUpload_id(uploadPreResult.getUpload_id());
                    refreshUploadUrlRequest.setFile_id(uploadPreResult.getFile_id());
                    refreshUploadUrlRequest.setPart_info_list(part_info_list);
                    String refreshJson = client.post("/file/get_upload_url", refreshUploadUrlRequest);
                    UploadPreResult refreshResult = JsonUtil.readValue(refreshJson, UploadPreResult.class);
                    for (int j = i; j < partInfoList.size(); j++) {
                        UploadPreRequest.PartInfo oldInfo = partInfoList.get(j);
                        UploadPreRequest.PartInfo newInfo = refreshResult.getPart_info_list().stream()
                                .filter(p -> p.getPart_number().equals(oldInfo.getPart_number())).findAny()
                                .orElseThrow(NullPointerException::new);
                        oldInfo.setUpload_url(newInfo.getUpload_url());
                    }
                }

                try {
                    int read = IOUtils.read(inputStream, buffer, 0, buffer.length);
                    if (read == -1) {
                        LOGGER.info("文件上传结束。文件名：{}，当前进度：{}/{}", resourceUri, (i + 1), partInfoList.size());
                        return;
                    }
                    client.upload(partInfo.getUpload_url(), buffer, 0, read);
                    virtualTFileService.updateLength(parent.getFile_id(), uploadPreResult.getFile_id(), buffer.length);
                    LOGGER.info("文件正在上传。文件名：{}，当前进度：{}/{}", path, (i + 1), partInfoList.size());
                } catch (IOException e) {
                    virtualTFileService.remove(parent.getFile_id(), uploadPreResult.getFile_id());
                    throw new WebdavException(e);
                }
            }
        }


        UploadFinalRequest uploadFinalRequest = new UploadFinalRequest();
        uploadFinalRequest.setFile_id(uploadPreResult.getFile_id());
        uploadFinalRequest.setDrive_id(client.getDriveId());
        uploadFinalRequest.setUpload_id(uploadPreResult.getUpload_id());

        client.post("/file/complete", uploadFinalRequest);
        virtualTFileService.remove(parent.getFile_id(), uploadPreResult.getFile_id());
        LOGGER.info("文件上传成功。文件名：{}", path);
        clearCache();
    }

    /**
     * 获取阿里云网盘文件夹或者文件信息
     *
     * @param folderUri uri
     * @return 获取阿里云网盘文件夹或者文件信息
     */
    public AliyunDriveFile getAliyunDriveFile(String folderUri) {
        return null;
    }

    /**
     * 根据文件夹ID获取该文件夹的子文件（可能含有目录）
     *
     * @param fileId 文件夹ID
     * @return 子文件列表
     */
    public Set<AliyunDriveFile> getChildrenFiles(String fileId) {
        // 这里使用getUnchecked方法，因为返回的结果一定可以通过检测，所以这里无需检测
        Set<AliyunDriveFile> aliyunDriveFiles = ALIYUN_DRIVE_FILE_CACHE.getUnchecked(fileId);
        Set<AliyunDriveFile> result = new LinkedHashSet<>(aliyunDriveFiles);
        // 获取上传中的文件列表（这里是虚拟出来的）
        List<AliyunDriveFile> virtualAliyunDriveFiles = aliyunDriveFileVirtualService.list(fileId);
        result.addAll(virtualAliyunDriveFiles);
        return result;
    }

    private Set<AliyunDriveFile> getChildrenFilesFromAliyunDrive(String fileId) {
        List<AliyunDriveFile> aliyunDriveFiles = getFileListFromAliyunDrive(fileId, null, new ArrayList<>());
        // 按最新时间排序
        aliyunDriveFiles.sort(Comparator.comparing(AliyunDriveFile::getUpdatedAt).reversed());
        Set<AliyunDriveFile> aliyunDriveFileSet = new LinkedHashSet<>();
        for (AliyunDriveFile file : aliyunDriveFiles) {
            if (!aliyunDriveFileSet.add(file)) {
                log.info("Current folder: {} already has file: {}, size: {}", fileId, file.getName(), file.getSize());
            }
        }
        return aliyunDriveFileSet;
    }

    /**
     * 调用阿里云API获取文件列表
     *
     * @param fileId 文件夹ID
     * @param marker 标记
     * @param aliyunDriveFileResult 列表结果容器
     * @return 列表结果容器
     */
    private List<AliyunDriveFile> getFileListFromAliyunDrive(String fileId, String marker,
            List<AliyunDriveFile> aliyunDriveFileResult) {
        try {
            FileListRequest requestParam = new FileListRequest();
            requestParam.setDriveId(aliyunDriveClient.getDriveId());
            requestParam.setParentFileId(fileId);
            requestParam.setMarker(marker);
            String result = aliyunDriveClient.post(AliyunDriveConstant.ALIYUN_DRIVE_FILE_LIST_API, requestParam);
            AliyunDriveFileListResult fileListResult = JSONUtil.toBean(result, AliyunDriveFileListResult.class);
            aliyunDriveFileResult.addAll(fileListResult.getItems());
            if (StrUtil.isBlank(fileListResult.getNextMarker())) {
                return aliyunDriveFileResult;
            }
            // 递归获取
            return getFileListFromAliyunDrive(fileId, fileListResult.getNextMarker(), aliyunDriveFileResult);
        } catch (Exception e) {
            // 这里需要catch异常，当因为网络加载失败的话，这样可以返回部分数据或者空列表，不至于返回错误
            log.error("AliyunDriveWebDavService.getFileListFromAliyunDrive fail.", e);
            return aliyunDriveFileResult;
        }
    }

    /**
     * 删除指定资源
     *
     * @param uri uri
     */
    public void remove(String uri) {

    }

    /**
     * 检查资源的uri是否正确，且纠正为正确的uri
     *
     * @param resourceUri uri
     * @return uri
     */
    private String checkResourceUri(String resourceUri) {
        resourceUri = resourceUri.replaceAll("//", "/");
        if (resourceUri.endsWith("/")) {
            resourceUri = resourceUri.substring(0, resourceUri.length() - 1);
        }
        return resourceUri;
    }
}
