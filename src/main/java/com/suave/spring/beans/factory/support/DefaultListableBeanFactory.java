package com.suave.spring.beans.factory.support;

import com.suave.spring.beans.factory.BeanDefinition;
import com.suave.spring.beans.factory.BeanFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Suave
 * @since 2023/08/18 15:44
 */
public class DefaultListableBeanFactory implements BeanFactory {

    public Map<String, BeanDefinition> beanDefinitionMap = new HashMap<String,BeanDefinition>();

    public void doRegisterBeanDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        for (BeanDefinition beanDefinition : beanDefinitions) {
            if(this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exists!!!");
            }
            this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
    }

    /**
     * 通过Bean的名称从IoC容器中获取Bean
     *
     * @param beanName Bean的名称
     * @return Bean对象
     */
    @Override
    public Object getBean(String beanName) {
        return null;
    }

    /**
     * 通过Bean对象的类从IoC容器中获取Bean
     *
     * @param requiredType Bean对象的类
     * @return Bean对象
     */
    @Override
    public <T> T getBean(Class<T> requiredType) {
        return null;
    }
}
