package cn.itlemon.best.web.dav.aliyun.model.response;

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
public class UploadPreResponse {

    @Alias("domain_id")
    private String domainId;

    @Alias("drive_id")
    private String driveId;

    @Alias("encrypt_mode")
    private String encryptMode;

    @Alias("file_id")
    private String fileId;

    @Alias("file_name")
    private String fileName;

    private String location;

    @Alias("parent_file_id")
    private String parentFileId;

    @Alias("rapid_upload")
    private Boolean rapidUpload;

    private String  type;

    @Alias("upload_id")
    private String uploadId;

    @Alias("part_info_list")
    private List<AliyunDriveFilePartInfo> partInfoList;

}
