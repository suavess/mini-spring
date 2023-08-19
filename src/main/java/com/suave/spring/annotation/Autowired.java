package com.suave.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识需要自动注入的属性
 * @author Suave
 * @since 2023/08/18 15:30
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    /**
     * 用于标识注入IoC中的Bean的名称
     * @return
     */
    String value() default "";
}
