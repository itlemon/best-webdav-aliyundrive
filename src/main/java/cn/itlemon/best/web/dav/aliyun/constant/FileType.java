package cn.itlemon.best.web.dav.aliyun.constant;

import lombok.Getter;
import lombok.ToString;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-24
 */
@Getter
@ToString
public enum FileType {

    /**
     * 文件类型
     */
    FOLDER("folder"),

    FILE("file");

    private final String type;

    FileType(String type) {
        this.type = type;
    }

}
