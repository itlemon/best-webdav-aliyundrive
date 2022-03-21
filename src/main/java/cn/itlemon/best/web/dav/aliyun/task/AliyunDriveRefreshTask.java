package cn.itlemon.best.web.dav.aliyun.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.itlemon.best.web.dav.aliyun.client.AliyunDriveClient;
import lombok.extern.slf4j.Slf4j;

/**
 * 这里启用一个定时任务，
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-21
 */
@Slf4j
@Component
public class AliyunDriveRefreshTask {

    @Autowired
    private AliyunDriveClient aliyunDriveClient;

    /**
     * 每次间隔5min中请求一次，用来主动更新token
     */
    @Scheduled(initialDelay = 60000L, fixedDelay = 300000L)
    public void refresh() {
        log.info("AliyunDriveRefreshTask refresh token.");

    }

}
