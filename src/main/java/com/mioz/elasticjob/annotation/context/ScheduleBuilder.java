package com.mioz.elasticjob.annotation.context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import com.mioz.elasticjob.annotation.utils.SecurityUtil;
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
import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.executor.handler.JobProperties.JobPropertiesEnum;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mioz.elasticjob.annotation.context.annotation.LiteJobConfig;
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
	 * 注册中心相关缺省配置
	 */
	private final static int DEFAULT_ZK_BASE_SLEEP_TIMEMS = 1000;
	private final static int DEFAULT_ZK_MAX_SLEEP_TIMEMS = 3000;
	private final static int DEFAULT_ZK_MAXRETRIES = 3;
	private final static int DEFAULT_ZK_SESSIONTIMEOUTMS = 60000;
	private final static int DEFAULT_ZK_CONNECTIONTIMEOUTMS = 15000;
	
	/**
	 * Declare build: 构建作业. 
	 * Date 2017年9月30日 下午4:23:14 void
	 */
	public static void build() {
		String _package = null;
		Properties prop = null;
		try(InputStream in = ScheduleBuilder.class.getClassLoader().getResourceAsStream(SCHEDULER_CONFIG_PATH)) {
			prop = new Properties();
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
		
//		Set<Class<?>> zkconfigClasses = ComponentScan.filter(RegistryCenterConfig.class);
//		Preconditions.checkArgument(zkconfigClasses.size() > 0, "没有找到作业注册中心配置");
//		Class<?> configClass = zkconfigClasses.iterator().next();
//		RegistryCenterConfig registryCenterConfig = configClass.getAnnotation(RegistryCenterConfig.class);
		CoordinatorRegistryCenter registryCenter = createRegistryCenter(prop);
		
//		EventTraceDSConfig eventTraceDSConfig = configClass.getAnnotation(EventTraceDSConfig.class);
		JobEventConfiguration jobEventConfig = createJobEventConf(prop);
//		if (eventTraceDSConfig != null) {
//			jobEventConfig = createJobEventConf(eventTraceDSConfig);
//		}
		
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
				if (Strings.isNullOrEmpty(jobConfig.scriptCommandLine())) {
					throw new JobConfigurationException("Script类型作业 %s 没有配置 scriptCommandLine 属性，请检查并重新配置该作业", jobConfig.jobName());
				}
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
			JobScheduler jobScheduler = null;
			if (jobEventConfig == null) {
				if (jobListeners == null || jobListeners.length <= 0) {
					jobScheduler = new JobScheduler(registryCenter, liteJobConfig);
				} else {
					jobScheduler = new JobScheduler(registryCenter, liteJobConfig, jobListeners);
				}
			} else {
				if (jobListeners == null || jobListeners.length <= 0) {
					jobScheduler = new JobScheduler(registryCenter, liteJobConfig, jobEventConfig);
				} else {
					jobScheduler = new JobScheduler(registryCenter, liteJobConfig, jobEventConfig, jobListeners);
				}
			}
			jobScheduler.init();  // 初始化作业
		}
	}
	
	/**
	 * Declare createRegistryCenter: 创建注册中心配置. 
	 * @author Bingjie Wei 
	 * Date 2017年9月29日 下午3:31:20
	 * @param prop
	 * @return CoordinatorRegistryCenter
	 */
	private static CoordinatorRegistryCenter createRegistryCenter(Properties prop) {
		ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(prop.getProperty("zk.serverLists"), prop.getProperty("zk.namespace"));
		
		int baseSleepTimeMilliseconds = Strings.isNullOrEmpty(prop.getProperty("zk.baseSleepTimeMs")) ? DEFAULT_ZK_BASE_SLEEP_TIMEMS : Integer.valueOf(prop.getProperty("zk.baseSleepTimeMs"));
		zkConfig.setBaseSleepTimeMilliseconds(baseSleepTimeMilliseconds);
		int maxSleepTimeMilliseconds = Strings.isNullOrEmpty(prop.getProperty("zk.maxSleepTimeMs")) ? DEFAULT_ZK_MAX_SLEEP_TIMEMS : Integer.valueOf(prop.getProperty("zk.maxSleepTimeMs"));;
		zkConfig.setMaxSleepTimeMilliseconds(maxSleepTimeMilliseconds);
		int maxRetries = Strings.isNullOrEmpty(prop.getProperty("zk.maxRetries")) ? DEFAULT_ZK_MAXRETRIES : Integer.valueOf(prop.getProperty("zk.maxRetries"));
		zkConfig.setMaxRetries(maxRetries);
		int sessionTimeoutMs = Strings.isNullOrEmpty(prop.getProperty("zk.sessionTimeoutMs")) ? DEFAULT_ZK_SESSIONTIMEOUTMS : Integer.valueOf(prop.getProperty("zk.sessionTimeoutMs"));;
		zkConfig.setSessionTimeoutMilliseconds(sessionTimeoutMs);
		int connectionTimeoutMs = Strings.isNullOrEmpty(prop.getProperty("zk.connectionTimeoutMs")) ? DEFAULT_ZK_CONNECTIONTIMEOUTMS : Integer.valueOf(prop.getProperty("zk.connectionTimeoutMs"));;
		zkConfig.setConnectionTimeoutMilliseconds(connectionTimeoutMs);
		String digest = prop.getProperty("zk.digest");
		if (!Strings.isNullOrEmpty(digest)) {
			zkConfig.setDigest(digest);
		}
		
		CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
		regCenter.init();
		return regCenter;
	}
	
	/**
	 * Declare createJobEventConf: 定义日志数据库事件溯源配置. 
	 * @author Bingjie Wei 
	 * Date 2017年9月29日 下午3:28:34
	 * @param prop
	 * @return JobEventConfiguration
	 */
	private static JobEventConfiguration createJobEventConf(Properties prop) {
		boolean isEncrypted = false;
		if (!Strings.isNullOrEmpty(prop.getProperty("event.rdb.passwd.encrypted"))) {
            isEncrypted = Boolean.valueOf(prop.getProperty("event.rdb.passwd.encrypted"));
        }

		BasicDataSource result = new BasicDataSource();
		String driver = prop.getProperty("event.rdb.driver");
		String url = prop.getProperty("event.rdb.url");
		String username = prop.getProperty("event.rdb.username");
		String passwd = prop.getProperty("event.rdb.passwd");

        if (isEncrypted) {
            String publicKey = prop.getProperty("event.rdb.passwd.publickey");
            if (Strings.isNullOrEmpty(publicKey)) {
                passwd = SecurityUtil.decrypt(passwd);
            } else {
                passwd = SecurityUtil.decrypt(publicKey, passwd);
            }
        }

		if ((driver == null) || (url == null) || (username == null) || (passwd == null)) {
			return null;
		}
		result.setDriverClassName(driver);
		result.setUrl(url);
		result.setUsername(username);
		result.setPassword(passwd);
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
