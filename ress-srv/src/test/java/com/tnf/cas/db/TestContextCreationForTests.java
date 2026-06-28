package com.tnf.cas.db;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestContextCreationForTests {

	private ClassPathXmlApplicationContext mApp;

	@Test
	public void test() {

		mApp = new ClassPathXmlApplicationContext();
		mApp.setConfigLocations( //
				"classpath:test-beans.xml" //
		);
		mApp.refresh();

		for (String name : mApp.getBeanDefinitionNames()) {
			System.out.println(name);
		}
		Map<String, PropertyPlaceholderConfigurer> map = mApp.getBeansOfType(PropertyPlaceholderConfigurer.class);

		for (Entry<String, PropertyPlaceholderConfigurer> e : map.entrySet()) {
			PropertyPlaceholderConfigurer cfg = e.getValue();
			System.out.println(cfg);
		}
		ImsiResolver r = mApp.getBean(ImsiResolver.class);
		assertTrue(r instanceof DummyImsiResolver);
	}
}
