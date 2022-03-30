package cn.itlemon.best.web.dav.aliyun.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 阿里云网盘Path描述类
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-30
 */
@Getter
@Setter
@ToString
public class AliyunDriveResourceInfo {

    private String path;

    private String parentPath;

    private String name;

}
