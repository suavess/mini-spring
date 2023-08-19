package com.suave.demo;

import com.suave.demo.controller.PersonController;
import com.suave.spring.context.ApplicationContext;

/**
 * @author Suave
 * @since 2023/08/19 12:50
 */
public class SpringApplicationTest {
    public static void main(String[] args) {
        ApplicationContext applicationContext = new ApplicationContext("classpath:application.yml");
        System.out.println(applicationContext.getBean(PersonController.class));
    }
}
