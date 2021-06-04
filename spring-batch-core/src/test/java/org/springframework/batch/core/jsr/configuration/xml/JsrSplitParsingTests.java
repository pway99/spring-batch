/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.core.jsr.configuration.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import org.springframework.batch.core.jsr.AbstractJsrTestCase;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class JsrSplitParsingTests extends AbstractJsrTestCase {

	@Test
	public void testOneFlowInSplit() {
		try {
			new ClassPathXmlApplicationContext("/org/springframework/batch/core/jsr/configuration/xml/invalid-split-context.xml");
		} catch (BeanDefinitionParsingException bdpe) {
			assertTrue(bdpe.getMessage().contains("A <split/> must contain at least two 'flow' elements."));
			return;
		}

		fail("Expected exception was not thrown");
	}

	@Test
	public void testUserSpecifiedTaskExecutor() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/org/springframework/batch/core/jsr/configuration/xml/user-specified-split-task-executor-context.xml");
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();
		PropertyValue propertyValue = new JsrSplitParser(null).getSplitTaskExecutorPropertyValue(registry);

		RuntimeBeanReference runtimeBeanReferenceValue = (RuntimeBeanReference) propertyValue.getValue();

		Assertions.assertTrue("jsr352splitTaskExecutor".equals(runtimeBeanReferenceValue.getBeanName()), "RuntimeBeanReference should have a name of jsr352splitTaskExecutor");
		context.close();
	}

	@Test
	public void testDefaultTaskExecutor() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/org/springframework/batch/core/jsr/configuration/xml/default-split-task-executor-context.xml");
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();
		PropertyValue propertyValue = new JsrSplitParser(null).getSplitTaskExecutorPropertyValue(registry);
		Assertions.assertTrue((propertyValue.getValue() instanceof SimpleAsyncTaskExecutor), "Task executor not an instance of SimpleAsyncTaskExecutor");
		context.close();
	}

}
