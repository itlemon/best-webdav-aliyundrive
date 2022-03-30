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
public class FileCreateRequest {

    @Alias("check_name_mode")
    private String checkNameMode = "refuse";

    @Alias("drive_id")
    private String driveId;

    private String name;

    @Alias("parent_file_id")
    private String parentFileId;

    private String type;

}
