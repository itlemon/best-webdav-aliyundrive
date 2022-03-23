package cn.itlemon.best.web.dav.aliyun.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;

import cn.itlemon.best.web.dav.aliyun.client.AliyunDriveClient;
import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFile;
import cn.itlemon.best.web.dav.aliyun.model.request.FileListRequest;
import cn.itlemon.best.web.dav.aliyun.store.AliyunDriveWebDavStore;
import lombok.extern.slf4j.Slf4j;
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
    private static final Cache<String, Set<AliyunDriveFile>> ALIYUN_DRIVE_FILE_CACHE = CacheBuilder.newBuilder()
            .initialCapacity(128)
            .maximumSize(1024)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            // 目前这里仅仅添加一个打印日志的监听器
            .removalListener((RemovalListener<String, Set<AliyunDriveFile>>) notification -> log.info(
                    "AliyunDriveWebDavService remove cache, cacheId: {}, AliyunDriveFile: {}",
                    notification.getKey(), notification.getValue()))
            .build();

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
        Set<AliyunDriveFile> aliyunDriveFiles = ALIYUN_DRIVE_FILE_CACHE.get(fileId, key -> {
            // 获取真实的文件列表
            return getChildrenFilesFromAliyunDrive(fileId);
        });
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
        FileListRequest requestParam = new FileListRequest();
        listQuery.setMarker(marker);
        listQuery.setLimit(100);
        listQuery.setOrder_by("updated_at");
        listQuery.setOrder_direction("DESC");
        listQuery.setDrive_id(client.getDriveId());
        listQuery.setParent_file_id(nodeId);
        String json = client.post("/file/list", requestParam);
        TFileListResult<TFile> tFileListResult = JsonUtil.readValue(json, new TypeReference<TFileListResult<TFile>>() {
        });
        all.addAll(tFileListResult.getItems());
        if (!StringUtils.hasLength(tFileListResult.getNext_marker())) {
            return all;
        }
        // 递归获取
        return getFileListFromAliyunDrive(nodeId, tFileListResult.getNext_marker(), all);
    }

    /**
     * 删除指定资源
     *
     * @param uri uri
     */
    public void remove(String uri) {

    }
}
