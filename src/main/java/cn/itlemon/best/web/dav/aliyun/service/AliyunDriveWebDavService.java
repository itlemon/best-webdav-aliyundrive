package cn.itlemon.best.web.dav.aliyun.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
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
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.itlemon.best.web.dav.aliyun.client.AliyunDriveClient;
import cn.itlemon.best.web.dav.aliyun.constant.AliyunDriveConstant;
import cn.itlemon.best.web.dav.aliyun.constant.FileType;
import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFile;
import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFileListResult;
import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFilePartInfo;
import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveResourceInfo;
import cn.itlemon.best.web.dav.aliyun.model.request.FileCreateRequest;
import cn.itlemon.best.web.dav.aliyun.model.request.FileDownloadRequest;
import cn.itlemon.best.web.dav.aliyun.model.request.FileListRequest;
import cn.itlemon.best.web.dav.aliyun.model.request.FileRemoveRequest;
import cn.itlemon.best.web.dav.aliyun.model.request.RefreshUploadUrlRequest;
import cn.itlemon.best.web.dav.aliyun.model.request.UploadFinishRequest;
import cn.itlemon.best.web.dav.aliyun.model.request.UploadPreRequest;
import cn.itlemon.best.web.dav.aliyun.model.response.UploadPreResponse;
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
     * ?????????
     */
    private static final String ROOT_PATH = "/";

    /**
     * ?????????????????????10M
     */
    private static final int CHUNK_SIZE = 10 * 1024 * 1024;

    /**
     * ???????????????
     */
    private AliyunDriveFile rootAliyunDriveFile;

    private final AliyunDriveClient aliyunDriveClient;
    private final AliyunDriveFileVirtualService aliyunDriveFileVirtualService;

    /**
     * ??????
     */
    private final LoadingCache<String, Set<AliyunDriveFile>> ALIYUN_DRIVE_FILE_CACHE = CacheBuilder.newBuilder()
            .initialCapacity(128)
            .maximumSize(1024)
            .expireAfterWrite(60, TimeUnit.SECONDS)
            // ??????????????????????????????????????????????????????
            .removalListener((RemovalListener<String, Set<AliyunDriveFile>>) notification -> log.info(
                    "AliyunDriveWebDavService remove cache, cacheId: {}, AliyunDriveFile: {}",
                    notification.getKey(), notification.getValue()))
            .build(new CacheLoader<>() {
                @Override
                public Set<AliyunDriveFile> load(@Nonnull String fileId) {
                    // ???????????????????????????
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
        // ????????????????????????
        rootAliyunDriveFile = new AliyunDriveFile();
        rootAliyunDriveFile.setName("/");
        rootAliyunDriveFile.setFileId("root");
        rootAliyunDriveFile.setCreateAt(new Date());
        rootAliyunDriveFile.setUpdatedAt(new Date());
        rootAliyunDriveFile.setType(FileType.FOLDER.getType());
    }


    /**
     * ???????????????
     *
     * @param folderUri ?????????uri
     */
    public void createFolder(String folderUri) {
        folderUri = checkResourceUri(folderUri);
        AliyunDriveResourceInfo resourceInfo = getResourceInfo(folderUri);
        AliyunDriveFile parent = getAliyunDriveFile(resourceInfo.getParentPath());
        if (parent == null) {
            log.error("create folder fail, parent folder: {} is not exist.", resourceInfo.getParentPath());
            return;
        }

        FileCreateRequest fileCreateRequest = new FileCreateRequest();
        fileCreateRequest.setDriveId(aliyunDriveClient.getDriveId());
        fileCreateRequest.setName(resourceInfo.getName());
        fileCreateRequest.setParentFileId(parent.getFileId());
        fileCreateRequest.setType(FileType.FOLDER.getType());
        String createFolderResponse =
                aliyunDriveClient.post("https://api.aliyundrive.com/adrive/v2/file/createWithFolders",
                        fileCreateRequest);
        AliyunDriveFile createdFolder = JSONUtil.toBean(createFolderResponse, AliyunDriveFile.class);

        // ????????????
        clearCache();

        // ??????if???????????????????????????????????????????????????
        if (createdFolder.getFileName() == null || "available".equals(createdFolder.getStatus())) {
            log.error("create folder: {} fail, response: {}", folderUri, createFolderResponse);
            throw new WebdavException(String.format("create folder %s fail", folderUri));
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param resourceUri ??????uri
     */
    public void createResource(String resourceUri) {
        // do nothing
    }

    /**
     * ????????????
     *
     * @param resourceUri ??????uri
     * @param request ??????
     * @param size ????????????
     * @return ?????????
     */
    public Response download(String resourceUri, HttpServletRequest request, long size) {
        AliyunDriveFile aliyunDriveFile = getAliyunDriveFile(resourceUri);
        FileDownloadRequest downloadRequest = new FileDownloadRequest();
        downloadRequest.setDriveId(aliyunDriveClient.getDriveId());
        downloadRequest.setFileId(aliyunDriveFile.getFileId());
        String responseString =
                aliyunDriveClient.post("https://api.aliyundrive.com/v2/file/get_download_url", downloadRequest);

        // ??????????????????????????????url
        JSONObject response = JSONUtil.parseObj(responseString);
        String downloadUrl = response.getStr("url");
        log.info("download file: {}, url: {}", resourceUri, downloadUrl);

        return aliyunDriveClient.download(downloadUrl, request, size);
    }


    public void uploadPre(String resourceUri, long contentLength, InputStream content) {
        resourceUri = checkResourceUri(resourceUri);
        AliyunDriveResourceInfo resourceInfo = getResourceInfo(resourceUri);
        AliyunDriveFile parent = getAliyunDriveFile(resourceInfo.getParentPath());
        if (parent == null) {
            return;
        }

        AliyunDriveFile resource = getAliyunDriveFile(resourceUri);
        if (resource != null) {
            if (resource.getSize() == contentLength) {
                // ??????????????????????????????????????????????????????????????????????????????
                log.info("resource: {} already exist.", resource);
                return;
            }
            // ???????????????????????????????????????
            remove(resourceUri);
        }

        int chunkCount = (int) Math.ceil(((double) contentLength) / CHUNK_SIZE);

        UploadPreRequest uploadPreRequest = new UploadPreRequest();
        uploadPreRequest.setDriveId(aliyunDriveClient.getDriveId());
        uploadPreRequest.setName(resourceInfo.getName());
        uploadPreRequest.setParentFileId(parent.getFileId());
        uploadPreRequest.setSize(contentLength);
        List<AliyunDriveFilePartInfo> requestPartInfoList = new ArrayList<>();
        for (int i = 0; i < chunkCount; i++) {
            AliyunDriveFilePartInfo partInfo = new AliyunDriveFilePartInfo();
            partInfo.setPartNumber(i + 1);
            requestPartInfoList.add(partInfo);
        }
        uploadPreRequest.setPartInfoList(requestPartInfoList);

        log.info("start to upload file, file name: {}, size: {}, part number of file: {}", uploadPreRequest.getName(),
                contentLength, chunkCount);

        // ?????????content_hash???content_hash_name???????????????????????????API?????????????????????????????????url??????
        String response = aliyunDriveClient.post("https://api.aliyundrive.com/adrive/v2/file/createWithFolders",
                uploadPreRequest);
        UploadPreResponse uploadPreResponse = JSONUtil.toBean(response, UploadPreResponse.class);

        List<AliyunDriveFilePartInfo> responsePartInfoList = uploadPreRequest.getPartInfoList();
        if (responsePartInfoList != null) {
            if (contentLength > 0) {
                aliyunDriveFileVirtualService.createAliyunDriveFile(parent.getFileId(), uploadPreResponse);
            }
            log.info("pre upload process success, resource uri: {}, upload url size: {}", resourceUri,
                    responsePartInfoList.size());

            byte[] buffer = new byte[CHUNK_SIZE];
            for (int i = 0; i < responsePartInfoList.size(); i++) {
                AliyunDriveFilePartInfo partInfo = responsePartInfoList.get(i);

                // ?????????url?????????https://bj29.cn-beijing.data.alicloudccp.com/8M6tfnwf%2F1917514%2F62443df02bc12fad773045609e965e7b767b1e68%2F62443df0c10354348e174d408092573451ea6c4b?partNumber=6&uploadId=004BC5A6B969423EAA1DAF12263D8FB3&x-oss-access-key-id=LTAIsE5mAn2F493Q&x-oss-expires=1648643073&x-oss-signature=9oYcAI8DB%2FQpIFDeu214gmbiipxnPrH3%2FcfqhdDRZ3w%3D&x-oss-signature-version=OSS2
                // ???????????????URL????????????????????????
                long expires = Long.parseLong(
                        Objects.requireNonNull(Objects.requireNonNull(HttpUrl.parse(partInfo.getUploadUrl()))
                                .queryParameter("x-oss-expires")));

                // ??????url????????????????????????????????????
                if (System.currentTimeMillis() / 1000 + 10 >= expires) {
                    // ????????????????????????UploadUrl
                    RefreshUploadUrlRequest refreshUploadUrlRequest = new RefreshUploadUrlRequest();
                    refreshUploadUrlRequest.setDriveId(aliyunDriveClient.getDriveId());
                    refreshUploadUrlRequest.setUploadId(uploadPreResponse.getUploadId());
                    refreshUploadUrlRequest.setFileId(uploadPreResponse.getFileId());
                    refreshUploadUrlRequest.setPartInfoList(requestPartInfoList);
                    String refreshResult =
                            aliyunDriveClient.post("https://api.aliyundrive.com/v2/file/get_upload_url",
                                    refreshUploadUrlRequest);
                    UploadPreResponse refreshResponse = JSONUtil.toBean(refreshResult, UploadPreResponse.class);

                    // ?????????????????????????????????
                    for (int j = i; j < responsePartInfoList.size(); j++) {
                        AliyunDriveFilePartInfo oldPartInfo = responsePartInfoList.get(j);
                        AliyunDriveFilePartInfo newPartInfo = refreshResponse.getPartInfoList().stream()
                                .filter(item -> item.getPartNumber().equals(oldPartInfo.getPartNumber())).findAny()
                                .orElseThrow(NullPointerException::new);
                        // ??????????????????????????????partInfo???
                        oldPartInfo.setUploadUrl(newPartInfo.getUploadUrl());
                    }
                }

                try {
                    int read = IOUtils.read(content, buffer, 0, buffer.length);
                    if (read == -1) {
                        log.info("finish upload file: {}, current speed of progress??? {}/{}", resourceUri, (i + 1),
                                responsePartInfoList.size());
                        return;
                    }
                    aliyunDriveClient.upload(partInfo.getUploadUrl(), buffer, 0, read);

                    // ????????????????????????
                    aliyunDriveFileVirtualService.updateLength(parent.getFileId(), uploadPreResponse.getFileId(),
                            buffer.length);

                    log.info("file is uploading, file name: {}, current speed of progress??? {}/{}", resourceUri, (i + 1),
                            responsePartInfoList.size());
                } catch (IOException e) {
                    log.error("upload file got error.", e);
                    aliyunDriveFileVirtualService.remove(parent.getFileId(), uploadPreResponse.getFileId());
                    throw new WebdavException(e);
                }
            }
        }

        UploadFinishRequest uploadFinishRequest = new UploadFinishRequest();
        uploadFinishRequest.setFileId(uploadPreResponse.getFileId());
        uploadFinishRequest.setDriveId(aliyunDriveClient.getDriveId());
        uploadFinishRequest.setUploadId(uploadPreResponse.getUploadId());

        // ???????????????????????????????????????
        String completeResponse =
                aliyunDriveClient.post("https://api.aliyundrive.com/v2/file/complete", uploadFinishRequest);
        aliyunDriveFileVirtualService.remove(parent.getFileId(), uploadPreResponse.getFileId());
        log.info("finish upload file: {}, completeResponse: {}", resourceUri, completeResponse);

        // ???????????????????????????
        clearCache();
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param resourceUri ??????uri
     * @return ????????????????????????????????????????????????
     */
    public AliyunDriveFile getAliyunDriveFile(String resourceUri) {
        resourceUri = checkResourceUri(resourceUri);

        if (StrUtil.isBlank(resourceUri)) {
            resourceUri = ROOT_PATH;
        }
        if (resourceUri.equals(ROOT_PATH)) {
            // ?????????????????????
            return rootAliyunDriveFile;
        }

        // ????????????
        AliyunDriveResourceInfo resourceInfo = getResourceInfo(resourceUri);
        AliyunDriveFile parentFile = getAliyunDriveFile(resourceInfo.getParentPath());
        if (parentFile == null) {
            return null;
        }
        return getNodeFileByParentFileId(parentFile.getFileId(), resourceInfo.getName());
    }

    private AliyunDriveFile getNodeFileByParentFileId(String parentFileId, String name) {
        Set<AliyunDriveFile> aliyunDriveFileSet = getChildrenFiles(parentFileId);
        for (AliyunDriveFile file : aliyunDriveFileSet) {
            if (file.getName().equals(name)) {
                return file;
            }
        }
        return null;
    }

    /**
     * ???????????????ID??????????????????????????????????????????????????????
     *
     * @param fileId ?????????ID
     * @return ???????????????
     */
    public Set<AliyunDriveFile> getChildrenFiles(String fileId) {
        // ????????????getUnchecked?????????????????????????????????????????????????????????????????????????????????
        Set<AliyunDriveFile> aliyunDriveFiles = ALIYUN_DRIVE_FILE_CACHE.getUnchecked(fileId);
        Set<AliyunDriveFile> result = new LinkedHashSet<>(aliyunDriveFiles);
        // ????????????????????????????????????????????????????????????
        List<AliyunDriveFile> virtualAliyunDriveFiles = aliyunDriveFileVirtualService.list(fileId);
        result.addAll(virtualAliyunDriveFiles);
        return result;
    }

    private Set<AliyunDriveFile> getChildrenFilesFromAliyunDrive(String fileId) {
        List<AliyunDriveFile> aliyunDriveFiles = getFileListFromAliyunDrive(fileId, null, new ArrayList<>());
        // ?????????????????????
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
     * ???????????????API??????????????????
     *
     * @param fileId ?????????ID
     * @param marker ??????
     * @param aliyunDriveFileResult ??????????????????
     * @return ??????????????????
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
            // ????????????
            return getFileListFromAliyunDrive(fileId, fileListResult.getNextMarker(), aliyunDriveFileResult);
        } catch (Exception e) {
            // ????????????catch??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            log.error("AliyunDriveWebDavService.getFileListFromAliyunDrive fail.", e);
            return aliyunDriveFileResult;
        }
    }

    /**
     * ??????????????????
     *
     * @param resourceUri ??????uri
     */
    public void remove(String resourceUri) {
        resourceUri = checkResourceUri(resourceUri);
        AliyunDriveFile aliyunDriveFile = getAliyunDriveFile(resourceUri);
        if (aliyunDriveFile == null) {
            return;
        }

        FileRemoveRequest removeRequest = new FileRemoveRequest();
        removeRequest.setDriveId(aliyunDriveClient.getDriveId());
        removeRequest.setFileId(aliyunDriveFile.getFileId());

        aliyunDriveClient.post("https://api.aliyundrive.com/v2/recyclebin/trash", removeRequest);
        clearCache();
    }

    /**
     * ???????????????uri????????????????????????????????????uri
     *
     * @param resourceUri uri
     * @return uri
     */
    private String checkResourceUri(String resourceUri) {
        resourceUri = resourceUri.replaceAll("//", "/");
        if (!ROOT_PATH.equals(resourceUri) && resourceUri.endsWith("/")) {
            resourceUri = resourceUri.substring(0, resourceUri.length() - 1);
        }
        return resourceUri;
    }

    /**
     * ???????????????????????????
     *
     * @param resourceUri ??????uri
     * @return ????????????
     */
    private AliyunDriveResourceInfo getResourceInfo(String resourceUri) {
        resourceUri = checkResourceUri(resourceUri);
        AliyunDriveResourceInfo aliyunDriveResourceInfo = new AliyunDriveResourceInfo();
        if (ROOT_PATH.equals(resourceUri)) {
            // rootPath
            aliyunDriveResourceInfo.setPath(resourceUri);
            aliyunDriveResourceInfo.setName(resourceUri);
            return aliyunDriveResourceInfo;
        }
        // ???rootPath
        int index = resourceUri.lastIndexOf("/");
        String parentPath = resourceUri.substring(0, index + 1);
        String name = resourceUri.substring(index + 1);
        aliyunDriveResourceInfo.setPath(resourceUri);
        aliyunDriveResourceInfo.setParentPath(parentPath);
        aliyunDriveResourceInfo.setName(name);
        return aliyunDriveResourceInfo;
    }

    private void clearCache() {
        ALIYUN_DRIVE_FILE_CACHE.invalidateAll();
    }

}
