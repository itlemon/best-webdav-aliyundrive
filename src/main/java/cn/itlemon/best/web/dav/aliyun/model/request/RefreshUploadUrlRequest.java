package cn.itlemon.best.web.dav.aliyun.model.request;

import java.util.List;

import cn.hutool.core.annotation.Alias;
import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFilePartInfo;
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
public class RefreshUploadUrlRequest {

    @Alias("drive_id")
    private String driveId;

    @Alias("part_info_list")
    private List<AliyunDriveFilePartInfo> partInfoList;

    @Alias("file_id")
    private String fileId;

    @Alias("upload_id")
    private String uploadId;

}
