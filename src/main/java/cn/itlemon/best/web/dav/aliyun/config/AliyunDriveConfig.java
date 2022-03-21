package cn.itlemon.best.web.dav.aliyun.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import cn.itlemon.best.web.dav.aliyun.constant.AliyunDriveConstant;
import lombok.ToString;

/**
 * 阿里云网盘配置类
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-21
 */
@ToString
@ConfigurationProperties(prefix = "aliyundrive.base")
public class AliyunDriveConfig implements AliyunDriveBaseConfig {

    /**
     * 阿里云网盘API URL
     */
    private String aliyunDriveUrl = AliyunDriveConstant.ALIYUN_DRIVE_API;

    /**
     * 认证信息
     */
    private String authorization = AliyunDriveConstant.AUTHORIZATION_HEADER_VALUE;

    /**
     * Refresh Token，可在web版的阿里云网盘中获取
     */
    private String refreshToken;

    /**
     * 工作目录
     */
    private String workDir = AliyunDriveConstant.ALIYUN_DRIVE_WORK_DIR;

    /**
     * 模拟浏览器的用户代理
     */
    private String agent = AliyunDriveConstant.USER_AGENT_HEADER_VALUE;

    /**
     * 网盘ID
     */
    private String driveId;

    /**
     * 阿里云网盘接入web-dav的认证信息
     */
    private AliyunDriveWebDavAuthConfig auth;

    @Override
    public String getAliyunDriveUrl() {
        return aliyunDriveUrl;
    }

    @Override
    public String getAuthorization() {
        return authorization;
    }

    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String getWorkDir() {
        return workDir;
    }

    @Override
    public String getAgent() {
        return agent;
    }

    @Override
    public String getDriveId() {
        return driveId;
    }

    @Override
    public AliyunDriveWebDavAuthConfig getAuth() {
        return auth;
    }

    public void setAliyunDriveUrl(String aliyunDriveUrl) {
        this.aliyunDriveUrl = aliyunDriveUrl;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public void setAuth(AliyunDriveWebDavAuthConfig auth) {
        this.auth = auth;
    }

    @ToString
    public static class AliyunDriveWebDavAuthConfig implements AliyunDriveWebDavAuthBaseConfig {

        /**
         * 是否开启认证，默认true
         */
        private Boolean enabled = true;

        /**
         * 用户名和密码
         */
        private String username;
        private String password;

        @Override
        public Boolean getEnabled() {
            return enabled;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getPassword() {
            return password;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

}
