package cn.itlemon.best.web.dav.aliyun.constant;

/**
 * 阿里云网盘接口的相关常量，下面这些名称可从https://www.aliyundrive.com/drive/页面中，打开控制台（按F12）来获取
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-21
 */
public interface AliyunDriveConstant {

    /**
     * 阿里云网盘接口默认值，未来可能会变化
     */
    String ALIYUN_DRIVE_API = "https://api.aliyundrive.com/v2";

    /**
     * 通过refresh获取用户信息的接口
     */
    String ALIYUN_DRIVE_REFRESH_USER_INFO_API = "https://websv.aliyundrive.com/token/refresh";

    /**
     * 阿里云网盘主地址
     */
    String ALIYUN_DRIVE_MAIN_URL = "https://www.aliyundrive.com/";

    /**
     * 默认的工作目录
     */
    String ALIYUN_DRIVE_WORK_DIR = "/etc/aliyun-drive/";

    /**
     * 用户代理名称常量
     */
    String USER_AGENT_HEADER_NAME = "user-agent";

    /**
     * 用户代理值
     */
    String USER_AGENT_HEADER_VALUE =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664"
                    + ".110 Safari/537.36";

    /**
     * Content-Type
     */
    String DEFAULT_CONTENT_TYPE = "application/json;charset=utf-8";

    /**
     * 认证名称常量
     */
    String AUTHORIZATION_HEADER_NAME = "authorization";

    /**
     * 默认认证信息为空
     */
    String AUTHORIZATION_HEADER_VALUE = "";

    /**
     * referer
     */
    String REFERER_HEADER_NAME = "referer";

    /**
     * range
     */
    String RANGE_HEADER_NAME = "range";

    /**
     * if-range
     */
    String IF_RANGE_HEADER_NAME = "if-range";

    /**
     * 部分错误信息
     */
    String PART_OF_API_ERROR_MESSAGE = "AccessToken";

    /**
     * refresh & access token key
     */
    String REFRESH_TOKEN_KEY = "refresh_token";
    String ACCESS_TOKEN_KEY = "access_token";

    /**
     * 默认网盘ID的Key
     */
    String DEFAULT_DRIVE_ID_KEY = "default_drive_id";

    /**
     * http 头
     */
    String HTTP_PREFIX = "http";

}
