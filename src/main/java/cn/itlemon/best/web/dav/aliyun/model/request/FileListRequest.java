package cn.itlemon.best.web.dav.aliyun.model.request;

import cn.hutool.core.annotation.Alias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 获取文件列表的参数
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-24
 */
@Getter
@Setter
@ToString
public class FileListRequest {

    /**
     * 是否查询所有
     */
    private boolean all = false;

    /**
     * 网盘ID
     */
    @Alias("drive_id")
    private String driveId;

    /**
     * 字段
     */
    private String fields = "*";

    @Alias("image_thumbnail_process")
    private String imageThumbnailProcess = "image/resize,w_400/format,jpeg";

    @Alias("image_url_process")
    private String imageUrlProcess = "image/resize,w_1920/format,jpeg";

    /**
     * 查询最大数
     */
    private int limit = 100;

    @Alias("order_by")
    private String OrderBy = "updated_at";

    @Alias("order_direction")
    private String orderDirection = "DESC";

    @Alias("parent_file_id")
    private String parentFileId;

    @Alias("url_expire_sec")
    private int urlExpireSec = 1600;

    @Alias("video_thumbnail_process")
    private String videoThumbnailProcess = "video/snapshot,t_1000,f_jpg,ar_auto,w_300";

    /**
     * 该参数用于动态加载内容，这里设置的limit是100，首先加载100后暂停加载，并返回一个next_marker值，这个值作为下一次加载的marker值
     * 该值类似：WyI2MTUyNjVjYmMxYTY4OGI4ZGUxOTRjMDA4MjdhOTNiNWRhOGJlMTM2IiwibiIsIm4iLDEsMCwxNjMyNzkwODY4MTkzLCI2MTUyNjk1MTU2ZTIxZDE5ODJlNDQ5MmRhNTQ5NWRhOTI1MjgzNTJiIl0=
     */
    private String marker;

}
