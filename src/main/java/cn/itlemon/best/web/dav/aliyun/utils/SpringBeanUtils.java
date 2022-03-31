package cn.itlemon.best.web.dav.aliyun.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Bean的相关工具类
 *
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-31
 */
@Slf4j
@Component
public final class SpringBeanUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Autowired
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringBeanUtils.applicationContext == null) {
            log.info("SpringBeanUtils start to set applicationContext: {}", applicationContext);
            SpringBeanUtils.applicationContext = applicationContext;
            log.info("SpringBeanUtils finished set applicationContext: {}", SpringBeanUtils.applicationContext);
        }
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 定义一个获取已经实例化bean的方法
     */
    public static <T> T getBean(Class<T> clazz) {
        log.info("SpringBeanUtils.getBean({}), applicationContext: {}", clazz, SpringBeanUtils.applicationContext);
        return SpringBeanUtils.applicationContext.getBean(clazz);
    }
}
