package com.mioz.elasticjob.annotation.context.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
  * ClassName EventTraceDSConfig
  * Description 事件追踪记录数据源配置 —— 用于记录作业事件
  * <p> Tips: 建议使用MySQL，数据库需使用utf8字符集 </p>
  * @author Bingjie Wei
  * Date 2017年9月28日 下午2:42:01
  */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface EventTraceDSConfig {
	
	/**
	 * Declare rdbDriver: 数据源驱动类
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:50:43
	 * @return String
	 */
	String rdbDriver();
	
	/**
	 * Declare rdbUrl: 数据库连接字符串
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:51:33
	 * @return String
	 */
	String rdbUrl();
	
	/**
	 * Declare rdbUsername: 用户名
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:51:51
	 * @return String
	 */
	String rdbUsername();
	
	/**
	 * Declare rdbPasswd: 密码
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午2:51:58
	 * @return String
	 */
	String rdbPasswd();

}
