package com.mioz.elasticjob.annotation.scan;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 扫描组件，并返回符合要求的集合
 */
public class ComponentScan {

	private static final Logger LOG = LoggerFactory.getLogger(ComponentScan.class);
	
	private static Set<Class<?>> classes;
	
	/**
	 * 返回所有带有参数中的注解的类
	 * @param annotationClass 注解类
	 * @return 过滤后的类
	 */
	public static Set<Class<?>> filter(Class<? extends Annotation> annotationClass) {
		if(classes == null) 
			classes = new LinkedHashSet<Class<?>>();
		
		if(classes.size() > 0) {
			Set<Class<?>> annClasses = new LinkedHashSet<Class<?>>();
			for(Class<?> clz : classes) {
				if(clz.isAnnotationPresent(annotationClass)) 
					annClasses.add(clz);
			}

			return annClasses;
		}
		
		return Collections.emptySet();
		
	}
	
	/**
	 * 获取指定包路径下的所有类
	 * @param packageName
	 * @return
	 */
	public static void scan(String packageName) {
		if(packageName == null || packageName.isEmpty()) {
			LOG.warn("没有设置packageName, 跳过扫描");
			return ;
		}
		
		if(classes == null)
			classes = new LinkedHashSet<Class<?>>();
		
		classes.addAll(getClasses(packageName));
	}

	/**
     * Return a set of all classes contained in the given package.
     *
     * @param packageName the package has to be analyzed.
     * @return a set of all classes contained in the given package.
     */
    private static Set<Class<?>> getClasses(String packageName) {
    	if(LOG.isDebugEnabled())
    		LOG.debug("开始扫描包: " + packageName);
    	
        return getClasses(new ResolverUtil.IsA(Object.class), packageName);
    }

    /**
     * Return a set of all classes contained in the given package that match with
     * the given test requirement.
     *
     * @param test the class filter on the given package.
     * @param packageName the package has to be analyzed.
     * @return a set of all classes contained in the given package.
     */
    private static Set<Class<?>> getClasses(ResolverUtil.Test test, String packageName) {
    	if(test == null)
    		throw new IllegalArgumentException("Parameter 'test' must not be null");
    	
    	if(packageName == null)
    		throw new IllegalArgumentException("Parameter 'packageName' must not be null");
    	
        return new ResolverUtil<Object>().find(test, packageName).getClasses();
    }
}
