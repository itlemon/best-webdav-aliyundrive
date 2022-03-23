package cn.itlemon.best.web.dav.aliyun.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 阿里云网盘文件元信息
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-24
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AliyunDriveFileMetaData {

    /**
     * 分享码，如：https://www.aliyundrive.com/s/Rd419RWgUYo 中的Rd419RWgUYo，多次分享有多个分享码
     */
    private List<String> shares;

    /**
     * 文件或者文件夹来自那个客户端，例如：web、desktop、IOS等
     */
    private String client;

}
