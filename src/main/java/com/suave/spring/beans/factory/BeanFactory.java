package com.suave.spring.beans.factory;

/**
 * 用于访问 Spring Bean 容器的根接口
 *
 * @author Suave
 * @since 2023/08/18 15:35
 */
public interface BeanFactory {
    /**
     * 通过Bean的名称从IoC容器中获取Bean
     *
     * @param beanName Bean的名称
     * @return Bean对象
     */
    Object getBean(String beanName);

    /**
     * 通过Bean对象的类从IoC容器中获取Bean
     *
     * @param requiredType Bean对象的类
     * @return Bean对象
     */
	<T> T getBean(Class<T> requiredType);
}
