package com.mioz.elasticjob.annotation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mioz.elasticjob.annotation.context.ScheduleBuilder;

/**
  * className: MainClass
  * description: Spring方式配置的 mainClass 的 bean
  * @author Bingjie Wei
  * date 2017年10月11日 上午9:53:01
  */
public class MainClass {
	
	private static final Logger logger = LoggerFactory.getLogger(MainClass.class);

	@PostConstruct
	public void init() {
		logger.info("开始初始化作业-----");
		ScheduleBuilder.build();
		logger.info("初始化作业完成-----");
	}

	@PreDestroy
	public void destroy() throws Exception {
		// TODO 保留方法，留待新版本实现
	}

}
