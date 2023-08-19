package com.suave.spring.context;

import com.suave.spring.annotation.Autowired;
import com.suave.spring.annotation.Controller;
import com.suave.spring.annotation.Service;
import com.suave.spring.beans.BeanWrapper;
import com.suave.spring.beans.factory.BeanDefinition;
import com.suave.spring.beans.factory.BeanFactory;
import com.suave.spring.beans.factory.ObjectFactory;
import com.suave.spring.beans.factory.support.BeanDefinitionReader;
import com.suave.spring.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Suave
 * @since 2023/08/18 15:42
 */
public class ApplicationContext implements BeanFactory {

    private DefaultListableBeanFactory registry = new DefaultListableBeanFactory();

    /**
     * 用于记录当前正在创建的BeanName，循环依赖时使用
     */
    private final Set<String> singletonsCurrentlyInCreation = new HashSet<>();

    /**
     * 一级缓存，保存成熟的Bean
     */
    private final Map<String, Object> singletonObjects = new HashMap<>();

    /**
     * 二级缓存，保存实例化完，但没有初始化的Bean，可能为代理类
     */
    private final Map<String, Object> earlySingletonObjects = new HashMap<>();

    /**
     * 三级缓存，保存创建对象的工厂
     */
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>();

    /**
     * 保存成熟的Bean
     * Key可能为BeanName、类名
     * Value为Bean对象
     */
    private final Map<String, Object> factoryBeanObjectCache = new HashMap<>();


    private final BeanDefinitionReader reader;


    /**
     * 通过Bean的名称从IoC容器中获取Bean
     *
     * @param beanName Bean的名称
     * @return Bean对象
     */
    @Override
    public Object getBean(String beanName) {
        Object singleton = getSingleton(beanName);
        if (singleton != null) {
            // 拿到了直接返回
            return singleton;
        }
        // 1、拿到BeanDefinition配置信息
        BeanDefinition beanDefinition = registry.beanDefinitionMap.get(beanName);
        return getSingleton(beanName, () -> createBean(beanName, beanDefinition));
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        // 标记bean正在创建
        if (!isSingletonCurrentlyInCreation(beanName)) {
            singletonsCurrentlyInCreation.add(beanName);
        }
        // 2、反射实例化对象
        BeanWrapper beanWrapper = instantiateBean(beanName, beanDefinition);

        if (beanDefinition.isSingleton() && isSingletonCurrentlyInCreation(beanName)) {
            // 3、单例且正在创建，说明有循环依赖，加入三级缓存
            addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, beanDefinition, beanWrapper.getWrappedInstance()));
        }

        // 4、执行依赖注入
        populateBean(beanName, beanDefinition, beanWrapper);

        // 5、保存到IoC容器中
        this.factoryBeanObjectCache.put(beanName, beanWrapper.getWrappedInstance());

        return beanWrapper.getWrappedInstance();
    }

    private void populateBean(String beanName, BeanDefinition beanDefinition, BeanWrapper beanWrapper) {

        Object instance = beanWrapper.getWrappedInstance();

        Class<?> clazz = beanWrapper.getWrappedClass();

        if (!(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class))) {
            return;
        }

        // 获取所有属性
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }

            Autowired autowired = field.getAnnotation(Autowired.class);
            String autowiredBeanName = autowired.value().trim();
            if (autowiredBeanName.isEmpty()) {
                autowiredBeanName = field.getType().getName();
            }

            // 强制访问
            field.setAccessible(true);

            try {
                field.set(instance, getBean(autowiredBeanName));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * TODO 根据是否需要代理来决定返回代理对象或本身
     *
     * @param beanName       Bean名称
     * @param beanDefinition Bean包装信息
     * @param bean           Bean对象
     * @return Bean对象或其代理对象
     */
    private Object getEarlyBeanReference(String beanName, BeanDefinition beanDefinition, Object bean) {
        Object exposedObject = bean;
        return exposedObject;
    }

    /**
     * 添加到三级缓存中
     *
     * @param beanName         Bean名称
     * @param singletonFactory 三级缓存的值，创建Bean对象的匿名方法
     */
    private void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
            }
        }
    }

    /**
     * 通过反射实例化
     *
     * @param beanName       Bean的名称
     * @param beanDefinition Bean的包装信息
     * @return 实例化完成的Bean包装对象
     */
    private BeanWrapper instantiateBean(String beanName, BeanDefinition beanDefinition) {
        if (beanDefinition.isSingleton() && this.factoryBeanObjectCache.containsKey(beanName)) {
            return new BeanWrapper(this.factoryBeanObjectCache.get(beanName));
        }

        String className = beanDefinition.getBeanClassName();
        Object instance = null;
        try {

            Class<?> clazz = Class.forName(className);

            instance = clazz.newInstance();

            // TODO 生成代理类

            this.factoryBeanObjectCache.put(beanName, instance);
            this.factoryBeanObjectCache.put(clazz.getName(), instance);
            for (Class<?> i : clazz.getInterfaces()) {
                this.factoryBeanObjectCache.put(i.getName(), instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BeanWrapper(instance);
    }

    /**
     * 根据名称获取Bean
     *
     * @param beanName Bean对象名称
     * @return Bean对象
     */
    private Object getSingleton(String beanName) {
        return getSingleton(beanName, true);
    }

    /**
     * 根据名称获取Bean
     *
     * @param beanName            Bean对象名称
     * @param allowEarlyReference 是否允许创建早期引用，为true则当二级缓存中不存在时会调用三级缓存中的对象工厂创建
     * @return
     */
    private Object getSingleton(String beanName, boolean allowEarlyReference) {
        // 1.从一级缓存中获取对象，一级缓存中存放的完整的bean，即初始化完成的bean
        // 从一级缓存中获取（用于避免获取不完整bean的时候来避免反复创建bean）
        Object singletonObject = this.singletonObjects.get(beanName);
        // 1.1 如果一级缓存中没有，并且正在创建，说明存在循环依赖
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (this.singletonObjects) {
                // 2. 从二级缓存中获取，如果此时bean中存在AOP，则获取到的就是代理的只实例化而未初始化的bean（不完整bean）
                // 如果不存在AOP，则返回普通的不完整bean，其作用就是避免某一个bean存在多次循环依赖而创建多次代理bean的情况
                singletonObject = this.earlySingletonObjects.get(beanName);
                // 3.如果二级缓存中不存在，则从三级缓存中获取bean
                if (singletonObject == null && allowEarlyReference) {
                    // 3.1 根据beanName从三级缓存中获取，此处不是获取bean对象
                    ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        // 3.2 三级缓存中value存放的并不是bean对象，而是函数式接口，用于回调
                        // 此处调用getObject()方法，就是判断是否要创建动态代理，是，则实例化代理bean，否，则实例化普通bean
                        singletonObject = singletonFactory.getObject();
                        // 3.3 将创建的不完整bean放入二级缓存中，下次先在二级缓存中拿，避免重复
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        // 3.4 三级缓存的回调作用已完成，进行移除
                        this.singletonFactories.remove(beanName);
                    }
                }
            }
        }
        return singletonObject;
    }

    /**
     * 根据名称获取Bean
     *
     * @param beanName         Bean名称
     * @param singletonFactory Bean不存在时则会调用该函数式接口进行创建
     * @return Bean对象
     */
    private Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        synchronized (this.singletonObjects) {
            // 1.先判断一级缓存中是否存在该bean，如果存在，不做任何操作，直接返回bean对象
            Object singletonObject = this.singletonObjects.get(beanName);
            // 1.1 如果一级缓存中没有
            if (singletonObject == null) {
                // 2.根据beanName判断是否在正在创建列表中，即是否正在创建
                if (isSingletonCurrentlyInCreation(beanName)) {
                    throw new RuntimeException("该bean正在被创建中！");
                }
                // 3.标记bean [A]正在创建
                // 将 beanName 添加到 Set 容器 singletonsCurrentlyInCreation 中，这个集合中存放的都是正在实例化的 bean
                // 标记bean正在创建
                singletonsCurrentlyInCreation.add(beanName);
                boolean newSingleton = false;
                try {
                    // 4.调用函数式接口的getObject()方法，即调用外层的createBean方法
                    singletonObject = singletonFactory.getObject();
                    newSingleton = true;
                } catch (IllegalStateException ex) {
                    singletonObject = this.singletonObjects.get(beanName);
                    if (singletonObject == null) {
                        throw ex;
                    }
                } finally {
                    singletonsCurrentlyInCreation.remove(beanName);
                }
                if (newSingleton) {
                    // 5.将bean加入到缓存中
                    addSingleton(beanName, singletonObject);
                }
            }
            return singletonObject;
        }
    }

    /**
     * 添加单例Bean对象到一级缓存，移除二三级缓存
     *
     * @param beanName        Bean名称
     * @param singletonObject 单例Bean对象
     */
    private void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
        }
    }

    /**
     * 该Bean对象是否正在创建中
     *
     * @param beanName Bean名称
     * @return 是否正在创建中
     */
    private boolean isSingletonCurrentlyInCreation(String beanName) {
        return this.singletonsCurrentlyInCreation.contains(beanName);
    }

    /**
     * 通过Bean对象的类从IoC容器中获取Bean
     *
     * @param requiredType Bean对象的类
     * @return Bean对象
     */
    @Override
    public <T> T getBean(Class<T> requiredType) {
        List<String> beanNameList = new ArrayList<>();
        for (BeanDefinition beanDefinition : this.registry.beanDefinitionMap.values()) {
            if (beanDefinition.getBeanClassName().equals(requiredType.getName())) {
                beanNameList.add(beanDefinition.getFactoryBeanName());
            }
        }
        if (beanNameList.size() > 1) {
            throw new RuntimeException(String.format("有%d个该类型的对象！", beanNameList.size()));
        }
        return (T) getBean(beanNameList.get(0));
    }

    /**
     * 构造方法，IoC容器启动的入口
     *
     * @param configLocations 配置文件
     */
    public ApplicationContext(String... configLocations) {
        // 1、加载配置文件
        reader = new BeanDefinitionReader(configLocations);

        try {
            // 2、解析配置文件，将所有的配置信息封装成BeanDefinition对象
            List<BeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
            // 3、所有的配置信息缓存起来
            this.registry.doRegisterBeanDefinition(beanDefinitions);
            // 4、加载非延时加载的所有的Bean
            doLoadInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据BeanDefinitionMap创建所有Bean对象
     */
    private void doLoadInstance() {
        // 循环调用getBean()方法
        for (Map.Entry<String, BeanDefinition> entry : this.registry.beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            if (!entry.getValue().isLazyInit()) {
                getBean(beanName);
            }
        }

    }
}
