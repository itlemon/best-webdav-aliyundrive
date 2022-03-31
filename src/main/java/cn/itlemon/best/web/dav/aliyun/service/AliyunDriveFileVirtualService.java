package cn.itlemon.best.web.dav.aliyun.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import cn.itlemon.best.web.dav.aliyun.constant.FileType;
import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFile;
import cn.itlemon.best.web.dav.aliyun.model.response.UploadPreResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-24
 */
@Slf4j
@Service
public class AliyunDriveFileVirtualService {

    private final Map<String, Map<String, AliyunDriveFile>> virtualAliyunDriveFileMap = new ConcurrentHashMap<>();

    /**
     * 获取上传中的文件列表（这里是虚拟出来的）
     *
     * @param fileId fileID
     * @return 列表
     */
    public List<AliyunDriveFile> list(String fileId) {
        Map<String, AliyunDriveFile> aliyunDriveFileMap = virtualAliyunDriveFileMap.get(fileId);
        if (aliyunDriveFileMap == null) {
            log.info("parent file id: {} not exist virtual child file.", fileId);
            return Collections.emptyList();
        }
        return new ArrayList<>(aliyunDriveFileMap.values());
    }

    /**
     * 创建文件，这里是虚拟出来的，用于上传的时候展示使用
     *
     * @param fileId 文件ID
     * @param uploadPreResponse 上传结果
     */
    public void createAliyunDriveFile(String fileId, UploadPreResponse uploadPreResponse) {
        Map<String, AliyunDriveFile> aliyunDriveFileMap =
                virtualAliyunDriveFileMap.computeIfAbsent(fileId, s -> new ConcurrentHashMap<>());
        aliyunDriveFileMap.put(uploadPreResponse.getFileId(), convert(uploadPreResponse));
    }

    /**
     * 更新虚拟文件大小
     *
     * @param parentFileId 父文件夹ID
     * @param fileId 文件ID
     * @param length 新增文件大小
     */
    public void updateLength(String parentFileId, String fileId, int length) {
        Map<String, AliyunDriveFile> aliyunDriveFileMap = virtualAliyunDriveFileMap.get(parentFileId);
        if (aliyunDriveFileMap == null) {
            log.warn("parent file id: {} not exist child file.", parentFileId);
            return;
        }
        AliyunDriveFile aliyunDriveFile = aliyunDriveFileMap.get(fileId);
        if (aliyunDriveFile == null) {
            log.warn("file id: {} is not exist.", fileId);
            return;
        }
        aliyunDriveFile.setSize(aliyunDriveFile.getSize() + length);
        aliyunDriveFile.setUpdatedAt(new Date());
    }

    /**
     * 移除虚拟文件
     *
     * @param parentFileId 父文件夹ID
     * @param fileId 文件ID
     */
    public void remove(String parentFileId, String fileId) {
        Map<String, AliyunDriveFile> aliyunDriveFileMap = virtualAliyunDriveFileMap.get(parentFileId);
        if (aliyunDriveFileMap == null) {
            log.warn("parent file id: {} not exist child file.", parentFileId);
            return;
        }
        aliyunDriveFileMap.remove(fileId);
    }

    private AliyunDriveFile convert(UploadPreResponse uploadPreResponse) {
        AliyunDriveFile aliyunDriveFile = new AliyunDriveFile();
        aliyunDriveFile.setCreateAt(new Date());
        aliyunDriveFile.setFileId(uploadPreResponse.getFileId());
        aliyunDriveFile.setName(uploadPreResponse.getFileName());
        aliyunDriveFile.setType(FileType.FILE.getType());
        aliyunDriveFile.setUpdatedAt(new Date());
        aliyunDriveFile.setSize(0L);
        return aliyunDriveFile;
    }
}
