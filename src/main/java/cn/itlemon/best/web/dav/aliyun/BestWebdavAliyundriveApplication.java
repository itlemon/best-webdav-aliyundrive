package cn.itlemon.best.web.dav.aliyun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import cn.itlemon.best.web.dav.aliyun.config.AliyunDriveConfig;

/**
 * 阿里云网盘WEB-DAV服务启动类
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-20
 */
@SpringBootApplication
@EnableConfigurationProperties(AliyunDriveConfig.class)
public class BestWebdavAliyundriveApplication {

    public static void main(String[] args) {
        SpringApplication.run(BestWebdavAliyundriveApplication.class, args);
    }

}
