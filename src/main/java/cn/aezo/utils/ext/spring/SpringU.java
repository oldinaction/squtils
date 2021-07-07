package cn.aezo.utils.ext.spring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Properties;

/**
 * Spring常用工具
 * @author smalle
 * @date 2020-11-22 21:42
 */
@Slf4j
@Component
public class SpringU implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(SpringU.applicationContext == null) {
            SpringU.applicationContext = applicationContext;
        }
    }

    /**
     * 获取applicationContext
     * @author smalle
     * @since 2020/11/29
     * @return org.springframework.context.ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过name获取 Bean
     * @author smalle
     * @since 2020/11/29
     * @param name
     * @throws
     * @return java.lang.Object
     */
    public static Object getBean(String name){
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean
     * @author smalle
     * @since 2020/11/29
     * @param clazz
     * @return T
     */
    public static <T> T getBean(Class<T> clazz){
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name以及Clazz返回指定的Bean
     * @author smalle
     * @since 2020/11/29
     * @param name
     * @param clazz
     * @return T
     */
    public static <T> T getBean(String name, Class<T> clazz){
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 向Spring容器中注册Bean
     * @author smalle
     * @since 202012/26
     * @param beanName
     * @param singletonObject
     */
    public static void registerBean(String beanName, Object singletonObject) {
        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        DefaultListableBeanFactory defaultListableBeanFactory = genericApplicationContext.getDefaultListableBeanFactory();
        defaultListableBeanFactory.registerSingleton(beanName, singletonObject);
    }

    /**
     * 获取AOP代理的目标对象 TODO AopUtils.getTargetClass 获取类
     * @param proxy 代理对象
     * @return 目标对象
     * @throws Exception
     */
    public static Object getProxyTarget(Object proxy) throws Exception {
        if (!AopUtils.isAopProxy(proxy)) {
            return proxy;
        }
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            proxy = getJdkDynamicProxyTargetObject(proxy);
        } else {
            proxy = getCglibProxyTargetObject(proxy);
        }
        return getProxyTarget(proxy);
    }

    /**
     * 获取properties文件配置信息
     * @param classpath 基于classpath的相对路径，如/test/default.properties
     * @return Properties
     */
    @SneakyThrows
    public static Properties getProperties(String classpath) {
        // 读取配置文件
        Resource resource = new ClassPathResource(classpath);
        return PropertiesLoaderUtils.loadProperties(resource);
    }

    private String getLocalMessage(String keyCode) {
        String localMessage = null;
        Locale locale = null;
        try {
            locale = LocaleContextHolder.getLocale();
            localMessage = SpringU.getBean(MessageSource.class).getMessage(keyCode, null, locale);
        } catch (NoSuchMessageException e1) {
            log.warn("invalid i18n! errorCode: " + keyCode + ", local: " + locale);
        }

        return localMessage;
    }

    private static Object getCglibProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        h.setAccessible(true);
        Object dynamicAdvisedInterceptor = h.get(proxy);
        Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        return ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
    }

    private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy) h.get(proxy);
        Field advised = aopProxy.getClass().getDeclaredField("advised");
        advised.setAccessible(true);
        return ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
    }
}
