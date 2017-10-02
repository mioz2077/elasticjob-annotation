package com.mioz.elasticjob.annotation.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  * ClassName: CoreConfig
  * @author Bingjie Wei
  * Date 2017年9月27日 下午4:03:01
  */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LiteJobConfig {

	/**
	 * Declare jobName: 作业名称
	 * @author Bingjie Wei 
	 * Date 2017年9月27日 下午4:13:05
	 * @return String
	 */
	String jobName();
	/**
	 * Declare cron: cron表达式，用于控制作业触发时间
	 * @author Bingjie Wei 
	 * Date 2017年9月27日 下午4:28:50
	 * @return String
	 */
	String cron();
	/**
	 * Declare shardingTotalCount: 作业分片总数，大于0
	 * @author Bingjie Wei 
	 * Date 2017年9月27日 下午4:29:06
	 * @return String
	 */
	int shardingTotalCount();
	
	/**
	 * Declare shardingItemParameters: 设置分片序列号和个性化参数对照表.
     *
     * <p>
     * 分片序列号和参数用等号分隔, 多个键值对用逗号分隔. 类似map.
     * 分片序列号从0开始, 不可大于或等于作业分片总数.
     * 如: 0=a,1=b,2=c
     * </p>
	 * @author Bingjie Wei 
	 * Date 2017年9月27日 下午4:37:40
	 * @return String
	 */
	String shardingItemParameters() default "";
	
	/**
	 * Declare jobParameter: 作业自定义参数
	 * @author Bingjie Wei 
	 * Date 2017年9月27日 下午4:39:49
	 * @return String
	 */
	String jobParameter() default "";
	
	/**
	 * Declare failover: 是否开启任务执行失效转移
	 * <p>
	 * 开启表示如果作业在一次任务执行中途宕机，允许将该次未完成的任务在另一作业节点上补偿执行
	 * </p>
	 * @author Bingjie Wei 
	 * Date 2017年9月27日 下午4:49:41
	 * @return boolean
	 */
	boolean failover() default false;
	
	
	/**
	 * Declare misfire: 是否开启错过任务重新执行
	 * @author Bingjie Wei 
	 * Date 2017年9月27日 下午4:52:15
	 * @return boolean
	 */
	boolean misfire() default false;
	
	/**
	 * Declare description: 作业描述信息
	 * @author Bingjie Wei 
	 * Date 2017年9月27日 下午4:52:58
	 * @return String
	 */
	String description() default "";
	
	/**
	 * Declare jobExceptionHandler: 扩展作业异常处理器。值为扩展实现类的全限定名
	 * @author Bingjie Wei 
	 * Date 2017年9月27日 下午5:38:58
	 * @return String
	 */
	String jobExceptionHandler() default "com.dangdang.ddframe.job.executor.handler.impl.DefaultJobExceptionHandler";
	
	/**
	 * Declare executorServiceHandler: 扩展线程池服务处理器。值为扩展实现类的全限定名
	 * @author Bingjie Wei 
	 * Date 2017年9月27日 下午5:40:07
	 * @return String
	 */
	String executorServiceHandler() default "com.dangdang.ddframe.job.executor.handler.impl.DefaultExecutorServiceHandler";
	
	/**
	 * Declare monitorExecution: 监控作业执行时状态
	 * <p>
     * 每次作业执行时间和间隔时间均非常短的情况, 建议不监控作业运行时状态以提升效率, 因为是瞬时状态, 所以无必要监控. 请用户自行增加数据堆积监控. 并且不能保证数据重复选取, 应在作业中实现幂等性. 也无法实现作业失效转移.<br>
     * 每次作业执行时间和间隔时间均较长短的情况, 建议监控作业运行时状态, 可保证数据不会重复选取.
     * </p>
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午3:27:32
	 * @return boolean
	 */
	boolean monitorExecution() default true;
	
	/**
	 * Declare streamingProcess: 是否流式处理数据.
	 * <p>
	 * 如果流式处理数据, 则fetchData不返回空结果将持续执行作业<br>
	 * 如果非流式处理数据, 则处理数据完成后作业结束<br>
	 * <b>Tips: dataflow类型作业必须配置次项</b>
	 * </p>
	 * @author Bingjie Wei 
	 * Date 2017年9月29日 下午4:15:47
	 * @return boolean
	 */
	boolean streamingProcess() default false;
	
	/**
	 * Declare scriptCommandLine: 脚本型作业执行命令行.<br>
	 * <b>Tips: Script类型作业必须配置次项 </b>
	 * @author Bingjie Wei 
	 * Date 2017年9月29日 下午4:18:14
	 * @return String
	 */
	String scriptCommandLine() default "";
	
	/**
	 * Declare maxTimeDiffSeconds: 最大容忍的本机与注册中心的时间误差秒数.
     * <p>
     * 如果时间误差超过配置秒数则作业启动时将抛异常.<br>
     * 配置为-1表示不检查时间误差.
     * </p>	
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午3:31:05
	 * @return int
	 */
	int maxTimeDiffSeconds() default -1;
	
	/**
	 * Declare monitorPort: 作业辅助监控端口.
	 * <p>值为 -1 时既不开启作业监控端口</p> 
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午3:32:53
	 * @return int
	 */
	int monitorPort() default -1;
	
	/**
	 * Declare jobShardingStrategyClass: 作业分片策略实现类全路径.
     * <p>
     * 默认使用 {@code com.dangdang.ddframe.job.plugin.sharding.strategy.AverageAllocationJobShardingStrategy}.<br>
     * 既平均分配策略
     * </p> 
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午3:35:25
	 * @return String
	 */
	String jobShardingStrategyClass() default "com.dangdang.ddframe.job.lite.api.strategy.impl.AverageAllocationJobShardingStrategy";
	
	/**
	 * Declare reconcileIntervalMinutes: 修复作业服务器不一致状态服务执行间隔分钟数.
     * <p>
     * 每隔一段时间监视作业服务器的状态，如果不正确则重新分片.
     * </p> 
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午3:40:25
	 * @return int
	 */
	int reconcileIntervalMinutes() default 10;
	
	/**
	 * Declare disabled: 作业是否启动时禁止.
     * <p>
     * 可用于部署作业时, 先在启动时禁止, 部署结束后统一启动.
     * </p>
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午4:49:55
	 * @return boolean
	 */
	boolean disabled() default false;
	
	/**
	 * Declare overwrite: . 
	 * @author Bingjie Wei 
	 * Date 2017年9月28日 下午4:50:51
	 * @return boolean
	 */
	boolean overwrite() default false;
	
	/**
	 * Declare jobListenerClassName: 作业监听器.
	 * <p>
	 * 需继承 {@com.dangdang.ddframe.job.lite.api.listener.AbstractDistributeOnceElasticJobListener}. 类，<br>
	 * 或实现 {@com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener} 接口<br>
	 * 值为扩展的作业监听器实现类的全限定名，多个监听器时以英文“,”分隔
	 * </p> 
	 * @author Bingjie Wei 
	 * Date 2017年9月29日 上午9:53:25
	 * @return String
	 */
	String jobListenerClassName() default "";
}
