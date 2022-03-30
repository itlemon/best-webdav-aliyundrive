package cn.itlemon.best.web.dav.aliyun.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.itlemon.best.web.dav.aliyun.model.AliyunDriveFile;
import cn.itlemon.best.web.dav.aliyun.service.AliyunDriveWebDavService;
import lombok.extern.slf4j.Slf4j;

/**
 * 这里启用一个定时任务，
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-21
 */
@Slf4j
@Component
public class AliyunDriveRefreshTask {

    private final AliyunDriveWebDavService aliyunDriveWebDavService;

    public AliyunDriveRefreshTask(AliyunDriveWebDavService aliyunDriveWebDavService) {
        this.aliyunDriveWebDavService = aliyunDriveWebDavService;
    }

    /**
     * 每次间隔5min中请求一次，用来主动更新token
     */
    @Scheduled(initialDelay = 60000L, fixedDelay = 300000L)
    public void refresh() {
        log.info("AliyunDriveRefreshTask refresh token.");
        try {
            AliyunDriveFile root = aliyunDriveWebDavService.getAliyunDriveFile("/");
            aliyunDriveWebDavService.getChildrenFiles(root.getFileId());
        } catch (Throwable e) {
            // nothing
            log.error("AliyunDriveRefreshTask refresh fail.");
        }
    }

}
