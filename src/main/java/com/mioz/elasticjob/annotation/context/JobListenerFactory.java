package com.mioz.elasticjob.annotation.context;

import com.dangdang.ddframe.job.exception.JobConfigurationException;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 作业监听器工厂.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobListenerFactory {
    
    /**
     * 获取作业监听器实例.
     * 
     * @param jobListenerClassName 作业监听器类名
     * @return 作业监听器实例
     */
    public static ElasticJobListener getStrategy(final String jobListenerClassName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jobListenerClassName), " ej.jobListenerClassName.* 值为空。请检查 job.properties 中的配置");
        try {
            Class<?> jobListenerClass = Class.forName(jobListenerClassName);
            if (!ElasticJobListener.class.isAssignableFrom(jobListenerClass)) {
                throw new JobConfigurationException("类 '%s' 不是 ElasticJobListener 接口的实现。请检查 job.properties 中  ej.jobListenerClassName.* 的配置", jobListenerClassName);
            }
            return (ElasticJobListener) jobListenerClass.newInstance();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new JobConfigurationException("获取作业监听器 '%s' 实例出现异常，异常信息为 '%s'", jobListenerClassName, ex.getCause());
        }
    }
}
