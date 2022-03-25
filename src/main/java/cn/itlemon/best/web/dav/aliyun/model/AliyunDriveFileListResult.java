package cn.itlemon.best.web.dav.aliyun.model;

import java.util.List;

import cn.hutool.core.annotation.Alias;
import lombok.Getter;
import lombok.Setter;

/**
 * 查询阿里云网盘文件列表的返回体
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-25
 */
@Getter
@Setter
public class AliyunDriveFileListResult {

    private List<AliyunDriveFile> items;

    @Alias("next_marker")
    private String nextMarker;
}
