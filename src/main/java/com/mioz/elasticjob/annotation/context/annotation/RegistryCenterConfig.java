/**
 * @Title: RegistryCenterConfig.java
 * @Package com.ane56.ej.annotation.context.annotation
 * Description: TODO
 * Copyright: Copyright (c) 2014 
 * Company:上海安能聚创供应链管理有限公司
 * 
 * @author Comsys-Bingjie Wei
 * Date 2017年9月28日 下午2:11:32
 * @Version V1.0
 */
package com.mioz.elasticjob.annotation.context.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * ClassName: RegistryCenterConfig
 * Description: 作业注册中心配置 —— 使用zookeeper作注册中心
 * @author Bingjie Wei
 * Date 2017年9月28日 下午2:11:32
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface RegistryCenterConfig {

	/**
	 * Declare serverLists: 连接Zookeeper服务器的列表. 
	 * <p>包括IP地址和端口号. 多个地址用英文逗号分隔。如：<b>host1:2181,host2:2181</b></p>
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:14:39
	 * @return String
	 */
	String serverLists();
	
	/**
	 * Declare nameSpace: 命名空间.
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:16:43
	 * @return String
	 */
	String nameSpace();
	
	/**
	 * Declare baseSleepTimeMilliseconds 等待重试的间隔时间的初始值. 单位毫秒.
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:20:36
	 * @return int
	 */
	int baseSleepTimeMilliseconds() default 1000;
	
	/**
	 * Declare maxSleepTimeMilliseconds: 等待重试的间隔时间的最大值. 单位毫秒.
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:21:31
	 * @return int
	 */
	int maxSleepTimeMilliseconds() default 3000;
	
	/**
	 * Declare maxRetries: 最大重试次数.
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:22:25
	 * @return int
	 */
	int maxRetries() default 3;
	
	/**
	 * Declare sessionTimeoutMilliseconds: 会话超时时间. 单位毫秒.
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:22:56
	 * @return int
	 */
	int sessionTimeoutMilliseconds() default 60000;

	/**
	 * Declare connectionTimeoutMilliseconds: 连接超时时间. 单位毫秒.
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:23:34
	 * @return int
	 */
	int connectionTimeoutMilliseconds() default 15000;
	
	/**
	 * Declare digest: 连接Zookeeper的权限令牌. 缺省为不需要权限验证.
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:24:38
	 * @return String
	 */
	String digest() default "";
}
