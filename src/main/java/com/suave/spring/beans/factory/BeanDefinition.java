package com.suave.spring.beans.factory;

/**
 * @author Suave
 * @since 2023/08/18 15:33
 */
public class BeanDefinition {

    /**
     * Bean的名称
     */
    private String factoryBeanName;
    /**
     * 原生类的全类名
     */
    private String beanClassName;

    /**
     * 是否懒加载
     * @return 默认false
     */
    public boolean isLazyInit() {
        return false;
    }

    /**
     * 是否单例
     * @return 默认返回true
     */
    public boolean isSingleton() {
        return true;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }
}
