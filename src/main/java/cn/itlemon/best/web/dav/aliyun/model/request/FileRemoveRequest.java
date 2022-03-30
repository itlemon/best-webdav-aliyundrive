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
public class FileRemoveRequest {

    @Alias("drive_id")
    private String driveId;

    @Alias("file_id")
    private String fileId;


}
