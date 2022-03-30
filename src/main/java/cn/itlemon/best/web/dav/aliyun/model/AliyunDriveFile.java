package cn.itlemon.best.web.dav.aliyun.model;

import java.util.Date;

import cn.hutool.core.annotation.Alias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 阿里云盘文件模型，包含文件和文件夹
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-23
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AliyunDriveFile {

    /**
     * 是否已经被删除
     */
    private boolean trashed;

    /**
     * 创建时间，原始值是：2022-03-22T17:43:43.315Z
     */
    @Alias("created_at")
    private Date createAt;

    /**
     * 目测是存储桶ID，例如：bj29
     */
    @Alias("domain_id")
    private String domainId;

    /**
     * 网盘ID，例如：1917513
     */
    @Alias("drive_id")
    private String driveId;

    /**
     * 加密方式，没有加密则为none
     */
    @Alias("encrypt_mode")
    private String encryptMode;

    /**
     * 文件后缀
     */
    @Alias("file_extension")
    private String fileExtension;

    /**
     * 文件ID或者文件夹ID
     */
    @Alias("file_id")
    private String fileId;

    /**
     * 媒体类型，例如：application/vnd.ms-powerpoint text/plain; charset=iso-8859-1等
     */
    @Alias("mime_type")
    private String mimeType;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 桶链接
     */
    private String url;

    /**
     * 是否被隐藏
     */
    private boolean hidden;

    /**
     * 文件名称或者文件夹名称
     */
    private String name;

    @Alias("file_name")
    private String fileName;

    /**
     * 父文件夹ID，根目录下的文件父目录ID是root
     */
    @Alias("parent_file_id")
    private String parentFileId;

    /**
     * 未知参数，尚不清楚作用
     */
    private boolean starred;

    /**
     * 状态，如：available
     */
    private String status;

    /**
     * 类型，folder或者file
     */
    private String type;

    /**
     * 更新时间，原始值是：2022-03-22T17:43:43.315Z
     */
    @Alias("updated_at")
    private Date updatedAt;

    @Alias("user_meta")
    private AliyunDriveFileMetaData userMeta;

}
