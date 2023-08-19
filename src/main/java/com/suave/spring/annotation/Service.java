package com.suave.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标识一个需要被注入IoC中的Bean
 * @author Suave
 * @since 2023/08/18 15:30
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
    /**
     * 用于标识注入IoC中的Bean的名称
     * @return
     */
    String value() default "";
}
