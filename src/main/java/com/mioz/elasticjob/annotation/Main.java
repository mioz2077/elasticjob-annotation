package com.mioz.elasticjob.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mioz.elasticjob.annotation.context.ScheduleBuilder;

/**
  * ClassName: Main
  * Description: 作业启动主类
  * @author Bingjie Wei
  * Date 2017年9月30日 上午9:56:35
  */
public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	/**
	 * Declare main: 构建作业. 
	 */
	public static void main(String[] args) {
		logger.info("开始初始化作业-----");
		ScheduleBuilder.build();
		logger.info("初始化作业完成-----");
	}
}
