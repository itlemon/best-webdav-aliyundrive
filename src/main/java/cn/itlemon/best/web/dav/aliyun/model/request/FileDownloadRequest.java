package cn.itlemon.best.web.dav.aliyun.model.request;

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
public class FileDownloadRequest {

    @Alias("drive_id")
    private String driveId;

    @Alias("file_id")
    private String fileId;

    @Alias("expire_sec")
    private Integer expireSec = 14400;

}
