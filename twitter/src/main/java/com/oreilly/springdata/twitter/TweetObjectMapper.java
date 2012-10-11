package com.oreilly.springdata.twitter;

import java.lang.reflect.Constructor;

import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;

public class TweetObjectMapper extends ObjectMapper implements InitializingBean, BeanClassLoaderAware {

	private volatile ClassLoader classLoader = null;

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void afterPropertiesSet() throws Exception {
		Class<?> twitterModuleClass = Class.forName(
				"org.springframework.social.twitter.api.impl.TwitterModule", false, this.classLoader);
		Constructor<?> ctor = twitterModuleClass.getConstructor(new Class<?>[0]);
		ctor.setAccessible(true);
		this.registerModule((Module) ctor.newInstance());
	}

}
