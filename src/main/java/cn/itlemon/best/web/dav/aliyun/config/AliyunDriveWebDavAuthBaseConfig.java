package cn.itlemon.best.web.dav.aliyun.config;

/**
 * 阿里云盘接入WebDav的认证信息
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-21
 */
public interface AliyunDriveWebDavAuthBaseConfig {

    /**
     * 是否启用认证信息
     *
     * @return 是否开启认证
     */
    Boolean getEnabled();

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    String getUsername();

    /**
     * 获取密码
     *
     * @return 密码
     */
    String getPassword();

}
