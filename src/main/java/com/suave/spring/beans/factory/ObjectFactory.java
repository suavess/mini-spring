package com.suave.spring.beans.factory;

/**
 * 对象工厂，调用时返回一个Bean对象，用于三级缓存提前生成Bean
 * @author Suave
 * @since 2023/08/18 15:56
 */
@FunctionalInterface
public interface ObjectFactory<T> {

	/**
	 * 创建一个Bean对象，可能为代理对象
	 * @return Bean对象
	 */
	T getObject();

}
