/*
 * Copyright 2013-2014 the original author or authors.
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

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>
 * Test cases around {@link JsrXmlApplicationContext}.
 * </p>
 *
 * @author Chris Schaefer
 */
public class JsrXmlApplicationContextTests {
	private static final String JOB_PARAMETERS_BEAN_DEFINITION_NAME = "jsr_jobParameters";

	@Test
	@SuppressWarnings("resource")
	public void testNullProperties() {
		JsrXmlApplicationContext applicationContext = new JsrXmlApplicationContext(null);

		BeanDefinition beanDefinition = applicationContext.getBeanDefinition(JOB_PARAMETERS_BEAN_DEFINITION_NAME);
		Properties properties = (Properties) beanDefinition.getConstructorArgumentValues().getGenericArgumentValue(Properties.class).getValue();

		assertNotNull(properties, "Properties should not be null");
		assertTrue(properties.isEmpty(), "Properties should be empty");
	}

	@Test
	@SuppressWarnings("resource")
	public void testWithProperties() {
		Properties properties = new Properties();
		properties.put("prop1key", "prop1val");

		JsrXmlApplicationContext applicationContext = new JsrXmlApplicationContext(properties);

		BeanDefinition beanDefinition = applicationContext.getBeanDefinition(JOB_PARAMETERS_BEAN_DEFINITION_NAME);
		Properties storedProperties = (Properties) beanDefinition.getConstructorArgumentValues().getGenericArgumentValue(Properties.class).getValue();

		assertNotNull(storedProperties, "Properties should not be null");
		assertFalse(storedProperties.isEmpty(), "Properties not be empty");
		assertEquals("prop1val", storedProperties.getProperty("prop1key"));
	}
}
