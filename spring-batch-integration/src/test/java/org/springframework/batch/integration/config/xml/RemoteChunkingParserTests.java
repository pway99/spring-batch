/*
 * Copyright 2014-2019 the original author or authors.
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
package org.springframework.batch.integration.config.xml;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.batch.core.step.item.ChunkProcessor;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.integration.chunk.ChunkHandler;
import org.springframework.batch.integration.chunk.ChunkMessageChannelItemWriter;
import org.springframework.batch.integration.chunk.ChunkProcessorChunkHandler;
import org.springframework.batch.integration.chunk.RemoteChunkHandlerFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.config.ServiceActivatorFactoryBean;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageChannel;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * <p>
 * Test cases for the {@link RemoteChunkingWorkerParser}
 * and {@link RemoteChunkingManagerParser}.
 * </p>
 *
 * @author Chris Schaefer
 * @author Mahmoud Ben Hassine
 * @since 3.1
 */
@SuppressWarnings("unchecked")
public class RemoteChunkingParserTests {

	/* TODO delete the following deprecated tests when related APIs are removed
	 * /!\ Deliberately not using parametrized tests as it will be easier to delete
	 * the following tests afterwards
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void testRemoteChunkingSlaveParserWithProcessorDefined() {
		ApplicationContext applicationContext =
				new ClassPathXmlApplicationContext("/org/springframework/batch/integration/config/xml/RemoteChunkingSlaveParserTests.xml");

		ChunkHandler chunkHandler = applicationContext.getBean(ChunkProcessorChunkHandler.class);
		ChunkProcessor chunkProcessor = (SimpleChunkProcessor) TestUtils.getPropertyValue(chunkHandler, "chunkProcessor");
		assertNotNull(chunkProcessor, "ChunkProcessor must not be null");

		ItemWriter<String> itemWriter = (ItemWriter<String>) TestUtils.getPropertyValue(chunkProcessor, "itemWriter");
		assertNotNull(itemWriter, "ChunkProcessor ItemWriter must not be null");
		assertTrue(itemWriter instanceof Writer, "Got wrong instance of ItemWriter");

		ItemProcessor<String, String> itemProcessor = (ItemProcessor<String, String>) TestUtils.getPropertyValue(chunkProcessor, "itemProcessor");
		assertNotNull(itemProcessor, "ChunkProcessor ItemWriter must not be null");
		assertTrue(itemProcessor instanceof Processor, "Got wrong instance of ItemProcessor");

		FactoryBean serviceActivatorFactoryBean = applicationContext.getBean(ServiceActivatorFactoryBean.class);
		assertNotNull(serviceActivatorFactoryBean, "ServiceActivatorFactoryBean must not be null");
		assertNotNull(TestUtils.getPropertyValue(serviceActivatorFactoryBean, "outputChannelName"), "Output channel name must not be null");

		MessageChannel inputChannel = applicationContext.getBean("requests", MessageChannel.class);
		assertNotNull(inputChannel, "Input channel must not be null");

		String targetMethodName = (String) TestUtils.getPropertyValue(serviceActivatorFactoryBean, "targetMethodName");
		assertNotNull(targetMethodName, "Target method name must not be null");
		assertTrue("handleChunk".equals(targetMethodName), "Target method name must be handleChunk, got: " + targetMethodName);

		ChunkHandler targetObject = (ChunkHandler) TestUtils.getPropertyValue(serviceActivatorFactoryBean, "targetObject");
		assertNotNull(targetObject, "Target object must not be null");
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testRemoteChunkingSlaveParserWithProcessorNotDefined() {
		ApplicationContext applicationContext =
				new ClassPathXmlApplicationContext("/org/springframework/batch/integration/config/xml/RemoteChunkingSlaveParserNoProcessorTests.xml");

		ChunkHandler chunkHandler = applicationContext.getBean(ChunkProcessorChunkHandler.class);
		ChunkProcessor chunkProcessor = (SimpleChunkProcessor) TestUtils.getPropertyValue(chunkHandler, "chunkProcessor");
		assertNotNull(chunkProcessor, "ChunkProcessor must not be null");

		ItemProcessor<String, String> itemProcessor = (ItemProcessor<String, String>) TestUtils.getPropertyValue(chunkProcessor, "itemProcessor");
		assertNotNull(itemProcessor, "ChunkProcessor ItemWriter must not be null");
		assertTrue(itemProcessor instanceof PassThroughItemProcessor, "Got wrong instance of ItemProcessor");
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testRemoteChunkingMasterParser() {
		ApplicationContext applicationContext =
				new ClassPathXmlApplicationContext("/org/springframework/batch/integration/config/xml/RemoteChunkingMasterParserTests.xml");

		ItemWriter itemWriter = applicationContext.getBean("itemWriter", ChunkMessageChannelItemWriter.class);
		assertNotNull(TestUtils.getPropertyValue(itemWriter, "messagingGateway"), "Messaging template must not be null");
		assertNotNull(TestUtils.getPropertyValue(itemWriter, "replyChannel"), "Reply channel must not be null");

		FactoryBean<ChunkHandler> remoteChunkingHandlerFactoryBean = applicationContext.getBean(RemoteChunkHandlerFactoryBean.class);
		assertNotNull(TestUtils.getPropertyValue(remoteChunkingHandlerFactoryBean, "chunkWriter"), "Chunk writer must not be null");
		assertNotNull(TestUtils.getPropertyValue(remoteChunkingHandlerFactoryBean, "step"), "Step must not be null");
	}

	@Test
	public void testRemoteChunkingMasterIdAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingMasterParserMissingIdAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The id attribute must be specified".equals(iae.getMessage()), "Expected: " + "The id attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingMasterMessageTemplateAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingMasterParserMissingMessageTemplateAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The message-template attribute must be specified".equals(iae.getMessage()), "Expected: " + "The message-template attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingMasterStepAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingMasterParserMissingStepAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The step attribute must be specified".equals(iae.getMessage()), "Expected: " + "The step attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingMasterReplyChannelAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingMasterParserMissingReplyChannelAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The reply-channel attribute must be specified".equals(iae.getMessage()), "Expected: " + "The reply-channel attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingSlaveIdAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingSlaveParserMissingIdAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The id attribute must be specified".equals(iae.getMessage()), "Expected: " + "The id attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingSlaveInputChannelAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingSlaveParserMissingInputChannelAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The input-channel attribute must be specified".equals(iae.getMessage()), "Expected: " + "The input-channel attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingSlaveItemWriterAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingSlaveParserMissingItemWriterAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The item-writer attribute must be specified".equals(iae.getMessage()), "Expected: " + "The item-writer attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingSlaveOutputChannelAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingSlaveParserMissingOutputChannelAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The output-channel attribute must be specified".equals(iae.getMessage()), "Expected: " + "The output-channel attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	/* TODO end of deprecated tests to remove */

	@SuppressWarnings("rawtypes")
	@Test
	public void testRemoteChunkingWorkerParserWithProcessorDefined() {
		ApplicationContext applicationContext =
				new ClassPathXmlApplicationContext("/org/springframework/batch/integration/config/xml/RemoteChunkingWorkerParserTests.xml");

		ChunkHandler chunkHandler = applicationContext.getBean(ChunkProcessorChunkHandler.class);
		ChunkProcessor chunkProcessor = (SimpleChunkProcessor) TestUtils.getPropertyValue(chunkHandler, "chunkProcessor");
		assertNotNull(chunkProcessor, "ChunkProcessor must not be null");

		ItemWriter<String> itemWriter = (ItemWriter<String>) TestUtils.getPropertyValue(chunkProcessor, "itemWriter");
		assertNotNull(itemWriter, "ChunkProcessor ItemWriter must not be null");
		assertTrue(itemWriter instanceof Writer, "Got wrong instance of ItemWriter");

		ItemProcessor<String, String> itemProcessor = (ItemProcessor<String, String>) TestUtils.getPropertyValue(chunkProcessor, "itemProcessor");
		assertNotNull(itemProcessor, "ChunkProcessor ItemWriter must not be null");
		assertTrue(itemProcessor instanceof Processor, "Got wrong instance of ItemProcessor");

		FactoryBean serviceActivatorFactoryBean = applicationContext.getBean(ServiceActivatorFactoryBean.class);
		assertNotNull(serviceActivatorFactoryBean, "ServiceActivatorFactoryBean must not be null");
		assertNotNull(TestUtils.getPropertyValue(serviceActivatorFactoryBean, "outputChannelName"), "Output channel name must not be null");

		MessageChannel inputChannel = applicationContext.getBean("requests", MessageChannel.class);
		assertNotNull(inputChannel, "Input channel must not be null");

		String targetMethodName = (String) TestUtils.getPropertyValue(serviceActivatorFactoryBean, "targetMethodName");
		assertNotNull(targetMethodName, "Target method name must not be null");
		assertTrue("handleChunk".equals(targetMethodName), "Target method name must be handleChunk, got: " + targetMethodName);

		ChunkHandler targetObject = (ChunkHandler) TestUtils.getPropertyValue(serviceActivatorFactoryBean, "targetObject");
		assertNotNull(targetObject, "Target object must not be null");
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testRemoteChunkingWorkerParserWithProcessorNotDefined() {
		ApplicationContext applicationContext =
				new ClassPathXmlApplicationContext("/org/springframework/batch/integration/config/xml/RemoteChunkingWorkerParserNoProcessorTests.xml");

		ChunkHandler chunkHandler = applicationContext.getBean(ChunkProcessorChunkHandler.class);
		ChunkProcessor chunkProcessor = (SimpleChunkProcessor) TestUtils.getPropertyValue(chunkHandler, "chunkProcessor");
		assertNotNull(chunkProcessor, "ChunkProcessor must not be null");

		ItemProcessor<String, String> itemProcessor = (ItemProcessor<String, String>) TestUtils.getPropertyValue(chunkProcessor, "itemProcessor");
		assertNotNull(itemProcessor, "ChunkProcessor ItemWriter must not be null");
		assertTrue(itemProcessor instanceof PassThroughItemProcessor, "Got wrong instance of ItemProcessor");
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testRemoteChunkingManagerParser() {
		ApplicationContext applicationContext =
				new ClassPathXmlApplicationContext("/org/springframework/batch/integration/config/xml/RemoteChunkingManagerParserTests.xml");

		ItemWriter itemWriter = applicationContext.getBean("itemWriter", ChunkMessageChannelItemWriter.class);
		assertNotNull(TestUtils.getPropertyValue(itemWriter, "messagingGateway"), "Messaging template must not be null");
		assertNotNull(TestUtils.getPropertyValue(itemWriter, "replyChannel"), "Reply channel must not be null");

		FactoryBean<ChunkHandler> remoteChunkingHandlerFactoryBean = applicationContext.getBean(RemoteChunkHandlerFactoryBean.class);
		assertNotNull(TestUtils.getPropertyValue(remoteChunkingHandlerFactoryBean, "chunkWriter"), "Chunk writer must not be null");
		assertNotNull(TestUtils.getPropertyValue(remoteChunkingHandlerFactoryBean, "step"), "Step must not be null");
	}

	@Test
	public void testRemoteChunkingManagerIdAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingManagerParserMissingIdAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The id attribute must be specified".equals(iae.getMessage()), "Expected: " + "The id attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingManagerMessageTemplateAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingManagerParserMissingMessageTemplateAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The message-template attribute must be specified".equals(iae.getMessage()), "Expected: " + "The message-template attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingManagerStepAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingManagerParserMissingStepAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The step attribute must be specified".equals(iae.getMessage()), "Expected: " + "The step attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingManagerReplyChannelAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingManagerParserMissingReplyChannelAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The reply-channel attribute must be specified".equals(iae.getMessage()), "Expected: " + "The reply-channel attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingWorkerIdAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingWorkerParserMissingIdAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The id attribute must be specified".equals(iae.getMessage()), "Expected: " + "The id attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingWorkerInputChannelAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingWorkerParserMissingInputChannelAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The input-channel attribute must be specified".equals(iae.getMessage()), "Expected: " + "The input-channel attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingWorkerItemWriterAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingWorkerParserMissingItemWriterAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The item-writer attribute must be specified".equals(iae.getMessage()), "Expected: " + "The item-writer attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	@Test
	public void testRemoteChunkingWorkerOutputChannelAttrAssert() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
		applicationContext.setValidating(false);
		applicationContext.setConfigLocation("/org/springframework/batch/integration/config/xml/RemoteChunkingWorkerParserMissingOutputChannelAttrTests.xml");

		try {
			applicationContext.refresh();
			fail();
		} catch (BeanDefinitionStoreException e) {
			assertTrue(e.getCause() instanceof IllegalArgumentException, "Nested exception must be of type IllegalArgumentException");

			IllegalArgumentException iae = (IllegalArgumentException) e.getCause();

			assertTrue(
			"The output-channel attribute must be specified".equals(iae.getMessage()), "Expected: " + "The output-channel attribute must be specified" + " but got: " + iae.getMessage());
		}
	}

	private static class Writer implements ItemWriter<String> {
		@Override
		public void write(List<? extends String> items) throws Exception {
			//
		}
	}

	private static class Processor implements ItemProcessor<String, String> {
		@Nullable
		@Override
		public String process(String item) throws Exception {
			return item;
		}
	}
}
