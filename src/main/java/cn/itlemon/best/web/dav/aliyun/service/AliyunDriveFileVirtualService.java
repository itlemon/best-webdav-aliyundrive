package cn.itlemon.best.web.dav.aliyun.service;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFile;
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
}
