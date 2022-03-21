package cn.itlemon.best.web.dav.aliyun.config;

/**
 * 定义阿里云基本配置规范
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-21
 */
public interface AliyunDriveBaseConfig {

    /**
     * 获取阿里云网盘接口API
     *
     * @return 阿里云网盘API接口
     */
    String getAliyunDriveUrl();

    /**
     * 获取阿里云网盘接口认证信息
     *
     * @return 认证信息
     */
    String getAuthorization();

    /**
     * 获取RefreshToken，很重要
     *
     * @return token
     */
    String getRefreshToken();

    /**
     * 获取工作目录
     *
     * @return 工作目录
     */
    String getWorkDir();

    /**
     * 获取浏览器用户代理，用于模拟浏览器行为
     *
     * @return 用户代理
     */
    String getAgent();

    /**
     * 获取网盘ID
     *
     * @return 网盘ID
     */
    String getDriveId();

    /**
     * 获取阿里云网盘的认证信息
     *
     * @return 认证信息
     */
    AliyunDriveWebDavAuthBaseConfig getAuth();

}
