package cn.itlemon.best.web.dav.aliyun.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

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


    /**
     * 获取上传中的文件列表（这里是虚拟出来的）
     *
     * @param fileId fileID
     * @return 列表
     */
    public List<AliyunDriveFile> list(String fileId) {
        return null;
    }

    /**
     * 创建文件，这里是虚拟出来的，用于上传的时候展示使用
     *
     * @param fileId 文件ID
     * @param uploadPreResponse 上传结果
     */
    public void createAliyunDriveFile(String fileId, UploadPreResponse uploadPreResponse) {

    }

    /**
     * 更新虚拟文件大小
     *
     * @param parentFileId 父文件夹ID
     * @param fileId 文件ID
     * @param length 新增文件大小
     */
    public void updateLength(String parentFileId, String fileId, int length) {

    }

    /**
     * 移除虚拟文件
     *
     * @param parentFileId 父文件夹ID
     * @param fileId 文件ID
     */
    public void remove(String parentFileId, String fileId) {

    }
}
