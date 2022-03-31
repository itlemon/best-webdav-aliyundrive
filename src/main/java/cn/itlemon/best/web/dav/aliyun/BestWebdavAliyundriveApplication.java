package cn.itlemon.best.web.dav.aliyun;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import cn.itlemon.best.web.dav.aliyun.config.AliyunDriveConfig;
import cn.itlemon.best.web.dav.aliyun.filter.GlobalErrorFilter;
import cn.itlemon.best.web.dav.aliyun.store.AliyunDriveWebDavStore;
import lombok.extern.slf4j.Slf4j;
import net.sf.webdav.WebdavServlet;

/**
 * 阿里云网盘WEB-DAV服务启动类
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-20
 */
@Slf4j
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AliyunDriveConfig.class)
public class BestWebdavAliyundriveApplication {


    public static void main(String[] args) {
        log.info("BestWebdavAliyundriveApplication start args: {}", Arrays.toString(args));
        SpringApplication.run(BestWebdavAliyundriveApplication.class, args);
        log.info("BestWebdavAliyundriveApplication finished start.");
    }

    @Bean
    public ServletRegistrationBean<WebdavServlet> webdavServlet() {
        ServletRegistrationBean<WebdavServlet> servletRegistrationBean =
                new ServletRegistrationBean<>(new WebdavServlet(), "/*");
        Map<String, String> inits = new LinkedHashMap<>();

        // 这里的配置请参考github：https://github.com/ceefour/webdav-servlet README.md文档
        inits.put("ResourceHandlerImplementation", AliyunDriveWebDavStore.class.getName());
        inits.put("rootpath", "./");
        inits.put("storeDebug", "1");
        servletRegistrationBean.setInitParameters(inits);
        return servletRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean<GlobalErrorFilter> disableSpringBootErrorFilter() {
        FilterRegistrationBean<GlobalErrorFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new GlobalErrorFilter());
        filterRegistrationBean.setEnabled(true);
        return filterRegistrationBean;
    }

}
