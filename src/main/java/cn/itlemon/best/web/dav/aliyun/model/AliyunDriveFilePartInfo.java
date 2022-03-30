package cn.itlemon.best.web.dav.aliyun.model;

import cn.hutool.core.annotation.Alias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-30
 */
@Getter
@Setter
@ToString
public class AliyunDriveFilePartInfo {

    @Alias("part_number")
    private Integer partNumber;

    @Alias("upload_url")
    private String uploadUrl;

    @Alias("internal_upload_url")
    private String internalUploadUrl;

    @Alias("content_type")
    private String contentType;

}
