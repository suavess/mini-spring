package com.suave.spring.beans;

/**
 * @author Suave
 * @since 2023/08/19 12:28
 */
public class BeanWrapper {
    private Object wrapperedInstance;
    private Class<?> wrappedClass;

    public BeanWrapper(Object instance) {
        this.wrapperedInstance = instance;
        this.wrappedClass = instance.getClass();
    }

    public Object getWrappedInstance() {
        return this.wrapperedInstance;
    }

    public Class<?> getWrappedClass() {
        return this.wrappedClass;
    }
}
