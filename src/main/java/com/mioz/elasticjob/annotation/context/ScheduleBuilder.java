package com.mioz.elasticjob.annotation.context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.script.ScriptJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.executor.handler.JobProperties.JobPropertiesEnum;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mioz.elasticjob.annotation.context.annotation.EventTraceDSConfig;
import com.mioz.elasticjob.annotation.context.annotation.LiteJobConfig;
import com.mioz.elasticjob.annotation.context.annotation.RegistryCenterConfig;
import com.mioz.elasticjob.annotation.scan.ComponentScan;

/**
  * ClassName: ScheduleBuilder
  * Description: 作业构建类
  * @author Bingjie Wei
  * Date 2017年9月29日 上午9:09:18
  */
public class ScheduleBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduleBuilder.class);
	
	public static final String BASE_PACKAGE = "context.scan.base-package";
	public static final String SCHEDULER_CONFIG_PATH = "job.properties";
	
	/**
	 * Declare build: 构建作业. 
	 * @author Bingjie Wei 
	 * Date 2017年9月30日 下午4:23:14 void
	 */
	public static void build() {
		String _package = null;
		try {
			InputStream in = ScheduleBuilder.class.getClassLoader().getResourceAsStream(SCHEDULER_CONFIG_PATH);
			Properties prop = new Properties();
			prop.load(in);
			_package = prop.get(BASE_PACKAGE).toString();
		} catch (FileNotFoundException e) {
			logger.error("没有找到job.properties 配置文件", e);
		} catch (IOException e) {
			logger.error(" job.properties 文件读取异常", e);
		}
		
		Preconditions.checkArgument(!Strings.isNullOrEmpty(_package), String.format("Property '%s' must not be null.", BASE_PACKAGE));
		
		String[] packages = _package.split(",");
		for(String pkg : packages) {
			String _pkg;
			if(pkg != null && !(_pkg = pkg.trim()).isEmpty()) {
				ComponentScan.scan(_pkg);
			}
		}
		
		Set<Class<?>> zkconfigClasses = ComponentScan.filter(RegistryCenterConfig.class);
		Preconditions.checkArgument(zkconfigClasses.size() > 0, "没有找到作业注册中心配置");
		Class<?> configClass = zkconfigClasses.iterator().next();
		RegistryCenterConfig registryCenterConfig = configClass.getAnnotation(RegistryCenterConfig.class);
		CoordinatorRegistryCenter registryCenter = createRegistryCenter(registryCenterConfig);
		
		EventTraceDSConfig eventTraceDSConfig = configClass.getAnnotation(EventTraceDSConfig.class);
		JobEventConfiguration jobEventConfig = null;
		if (eventTraceDSConfig != null) {
			jobEventConfig = createJobEventConf(eventTraceDSConfig);
		}
		
		Set<Class<?>> JobClasses = ComponentScan.filter(LiteJobConfig.class);
		if(JobClasses.size() <= 0) {
			logger.info("看起来没有需要运行的作业呢……");
			return;
		}
		for (Class<?> clz : JobClasses) {
			Preconditions.checkArgument(ElasticJob.class.isAssignableFrom(clz), String.format("作业类 %s 必须是 com.dangdang.ddframe.job.api.ElasticJob.class 的实现", clz));
			LiteJobConfig jobConfig = clz.getAnnotation(LiteJobConfig.class);
			JobCoreConfiguration.Builder coreConfigBuilder = JobCoreConfiguration.newBuilder(jobConfig.jobName(), jobConfig.cron(), jobConfig.shardingTotalCount());
			coreConfigBuilder.shardingItemParameters(jobConfig.shardingItemParameters());
			coreConfigBuilder.jobParameter(jobConfig.jobParameter());
			coreConfigBuilder.failover(jobConfig.failover());
			coreConfigBuilder.misfire(jobConfig.misfire());
			coreConfigBuilder.description(jobConfig.description());
			coreConfigBuilder.jobProperties(JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), jobConfig.jobExceptionHandler());
			coreConfigBuilder.jobProperties(JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), jobConfig.executorServiceHandler());
			JobCoreConfiguration coreConfig = coreConfigBuilder.build();
			
			JobTypeConfiguration jobTypeConfig = null;
			if (SimpleJob.class.isAssignableFrom(clz)) {
				jobTypeConfig = new SimpleJobConfiguration(coreConfig, clz.getCanonicalName());
			} else if (DataflowJob.class.isAssignableFrom(clz)) {
				jobTypeConfig = new DataflowJobConfiguration(coreConfig, clz.getCanonicalName(), jobConfig.streamingProcess());
			} else if (ScriptJob.class.isAssignableFrom(clz)) {
				jobTypeConfig = new ScriptJobConfiguration(coreConfig, jobConfig.scriptCommandLine());
			} else {
				// you'll never reach here
			}
			
			LiteJobConfiguration.Builder liteJobConfigBuilder = LiteJobConfiguration.newBuilder(jobTypeConfig);
			liteJobConfigBuilder.monitorExecution(jobConfig.monitorExecution());
			liteJobConfigBuilder.jobShardingStrategyClass(jobConfig.jobShardingStrategyClass());
			liteJobConfigBuilder.reconcileIntervalMinutes(jobConfig.reconcileIntervalMinutes());
			liteJobConfigBuilder.disabled(jobConfig.disabled());
			liteJobConfigBuilder.overwrite(jobConfig.overwrite());
			LiteJobConfiguration liteJobConfig = liteJobConfigBuilder.build();
			
			ElasticJobListener[] jobListeners = createJobListeners(jobConfig);
			if (jobEventConfig == null) {
				if (jobListeners == null) {
					new JobScheduler(registryCenter, liteJobConfig).init();;
				} else {
					new JobScheduler(registryCenter, liteJobConfig, jobListeners).init();
				}
			} else {
				if (jobListeners == null) {
					new JobScheduler(registryCenter, liteJobConfig, jobEventConfig).init();
				} else {
					new JobScheduler(registryCenter, liteJobConfig, jobEventConfig, jobListeners).init();
				}
			}
		}
	}
	
	/**
	 * Declare createRegistryCenter: 创建注册中心配置. 
	 * @author Bingjie Wei 
	 * Date 2017年9月29日 下午3:31:20
	 * @param registryCenterConfig
	 * @return CoordinatorRegistryCenter
	 */
	private static CoordinatorRegistryCenter createRegistryCenter(RegistryCenterConfig registryCenterConfig) {
		ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(registryCenterConfig.serverLists(), registryCenterConfig.nameSpace());
		
		zkConfig.setBaseSleepTimeMilliseconds(registryCenterConfig.baseSleepTimeMilliseconds());
		zkConfig.setMaxSleepTimeMilliseconds(registryCenterConfig.maxSleepTimeMilliseconds());
		zkConfig.setMaxRetries(registryCenterConfig.maxRetries());
		zkConfig.setSessionTimeoutMilliseconds(registryCenterConfig.sessionTimeoutMilliseconds());
		zkConfig.setConnectionTimeoutMilliseconds(registryCenterConfig.connectionTimeoutMilliseconds());
		zkConfig.setDigest(registryCenterConfig.digest());
		
		CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
		regCenter.init();
		return regCenter;
	}
	
	/**
	 * Declare createJobEventConf: 定义日志数据库事件溯源配置. 
	 * @author Bingjie Wei 
	 * Date 2017年9月29日 下午3:28:34
	 * @param eventTraceDSConfig
	 * @return JobEventConfiguration
	 */
	private static JobEventConfiguration createJobEventConf(EventTraceDSConfig eventTraceDSConfig) {
		BasicDataSource result = new BasicDataSource();
		result.setDriverClassName(eventTraceDSConfig.rdbDriver());
		result.setUrl(eventTraceDSConfig.rdbUrl());
		result.setUsername(eventTraceDSConfig.rdbUsername());
		result.setPassword(eventTraceDSConfig.rdbPasswd());
		return new JobEventRdbConfiguration(result);
	}
	
	/**
	 * Declare createJobListeners: 创建作业监听器. 
	 * @author Bingjie Wei 
	 * Date 2017年9月29日 下午5:21:27
	 * @param jobConfig
	 * @return ElasticJobListener[]
	 */
	private static ElasticJobListener[] createJobListeners(LiteJobConfig jobConfig) {
		String jobListenerClassName = jobConfig.jobListenerClassName();
		if (Strings.isNullOrEmpty(jobListenerClassName)) {
			return null;
		}
		String[] classNames = jobListenerClassName.split(",");
		ElasticJobListener[] jobListeners = new ElasticJobListener[classNames.length];
		for (int i = 0; i < classNames.length; i++) {
			jobListeners[i] = JobListenerFactory.getStrategy(classNames[i]);
		}
		return jobListeners;
	}
	
}
