package com.suave.spring.beans.factory.support;

import com.suave.spring.annotation.Component;
import com.suave.spring.annotation.Controller;
import com.suave.spring.annotation.Repository;
import com.suave.spring.annotation.Service;
import com.suave.spring.beans.factory.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Suave
 * @since 2023/08/18 15:48
 */
public class BeanDefinitionReader {

    public static final String SCAN_PACKAGE = "scanPackage";

    public static final String CLASS_PATH = "classpath:";

    public static final String CLASS_SUFFIX = ".class";

    /**
     * 保存用户配置好的配置文件
     */
    private Properties contextConfig = new Properties();

    /**
     * 缓存从包路径下扫描的全类名, 需要被注册地BeanClass们
     */
    private List<String> registerBeanClasses = new ArrayList<>();

    public BeanDefinitionReader(String... locations) {
        // 1、加载Properties文件
        doLoadConfig(locations[0]);

        // 2、扫描相关的类
        doScanner(contextConfig.getProperty(SCAN_PACKAGE));

    }

    public List<BeanDefinition> loadBeanDefinitions() {
        List<BeanDefinition> result = new ArrayList<>();

        try {
            for (String className : registerBeanClasses) {
                Class<?> beanClass = Class.forName(className);

                // beanClass本身是接口的话，不做处理
                if (beanClass.isInterface()) {
                    continue;
                }

                // beanClass本身没有Component、Controller、Service、Repository注解就跳过
                if (!beanClass.isAnnotationPresent(Component.class) &&
                        !beanClass.isAnnotationPresent(Controller.class) &&
                        !beanClass.isAnnotationPresent(Service.class) &&
                        !beanClass.isAnnotationPresent(Repository.class)
                ) {
                    continue;
                }

                // 1、默认类名首字母小写的情况
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));

                // 2、如果是接口，就用实现类
                for (Class<?> i : beanClass.getInterfaces()) {
                    result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private BeanDefinition doCreateBeanDefinition(String factoryBeanName, String factoryClassName) {
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setFactoryBeanName(factoryBeanName);
        beanDefinition.setBeanClassName(factoryClassName);
        return beanDefinition;
    }


    /**
     * 根据contextConfigLocation的路径去ClassPath下找到对应的配置文件
     *
     * @param contextConfigLocation
     */
    private void doLoadConfig(String contextConfigLocation) {
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replaceAll(CLASS_PATH, ""))) {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描ClassPath下符合包路径规则所有的Class文件
     *
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());

        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                // 取反，减少代码嵌套
                if (!file.getName().endsWith(CLASS_SUFFIX)) {
                    continue;
                }

                // 包名.类名  比如： com.suave.spring.DemoAction
                String className = (scanPackage + "." + file.getName().replace(CLASS_SUFFIX, ""));
                // 实例化，要用到Class.forName(className);
                registerBeanClasses.add(className);
            }

        }

    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
