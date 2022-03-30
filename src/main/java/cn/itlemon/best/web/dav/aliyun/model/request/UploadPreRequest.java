package cn.itlemon.best.web.dav.aliyun.model.request;

import java.util.List;

import cn.hutool.core.annotation.Alias;
import cn.itlemon.best.web.dav.aliyun.constant.FileType;
import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFilePartInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 正式上传前的请求
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-30
 */
@Getter
@Setter
@ToString
public class UploadPreRequest {

    @Alias("check_name_mode")
    private String checkNameMode = "refuse";

    @Alias("content_hash")
    private String contentHash;

    /**
     * web页面上是sha1，这里暂时设置为none
     */
    @Alias("content_hash_name")
    private String contentHashName = "none";

    @Alias("drive_id")
    private String driveId;

    private String name;

    @Alias("parent_file_id")
    private String parentFileId;

    @Alias("part_info_list")
    private List<AliyunDriveFilePartInfo> partInfoList;

    @Alias("proof_code")
    private String proofCode;

    @Alias("proof_version")
    private String proofVersion = "v1";

    private Long size;

    /**
     * 类型：文件或者文件夹，这里默认为文件，当需要上传文件夹的时候，要设置为folder
     */
    private String type = FileType.FILE.getType();

}
