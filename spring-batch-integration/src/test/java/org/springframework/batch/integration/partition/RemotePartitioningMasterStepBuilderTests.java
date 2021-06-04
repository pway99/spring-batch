/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.integration.partition;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.StepExecutionAggregator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.getField;

/**
 * @author Mahmoud Ben Hassine
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RemotePartitioningMasterStepBuilderTests.BatchConfiguration.class})
public class RemotePartitioningMasterStepBuilderTests {

	@Autowired
	private JobRepository jobRepository;

	@Test
	public void inputChannelMustNotBeNull() {
	 assertThrows(IllegalArgumentException.class, () -> {

		// when
		new RemotePartitioningMasterStepBuilder("step").inputChannel(null);

		 // then
		 // expected exception
	 }, "inputChannel must not be null");

		// then
		// expected exception
	}

	@Test
	public void outputChannelMustNotBeNull() {
	 assertThrows(IllegalArgumentException.class, () -> {

		// when
		new RemotePartitioningMasterStepBuilder("step").outputChannel(null);

		 // then
		 // expected exception
	 }, "outputChannel must not be null");

		// then
		// expected exception
	}

	@Test
	public void messagingTemplateMustNotBeNull() {
	 assertThrows(IllegalArgumentException.class, () -> {

		// when
		new RemotePartitioningMasterStepBuilder("step").messagingTemplate(null);

		 // then
		 // expected exception
	 }, "messagingTemplate must not be null");

		// then
		// expected exception
	}

	@Test
	public void jobExplorerMustNotBeNull() {
	 assertThrows(IllegalArgumentException.class, () -> {

		// when
		new RemotePartitioningMasterStepBuilder("step").jobExplorer(null);

		 // then
		 // expected exception
	 }, "jobExplorer must not be null");

		// then
		// expected exception
	}

	@Test
	public void pollIntervalMustBeGreaterThanZero() {
	 assertThrows(IllegalArgumentException.class, () -> {

		// when
		new RemotePartitioningMasterStepBuilder("step").pollInterval(-1);

		 // then
		 // expected exception
	 }, "The poll interval must be greater than zero");

		// then
		// expected exception
	}

	@Test
	public void eitherOutputChannelOrMessagingTemplateMustBeProvided() {
	 assertThrows(IllegalStateException.class, () -> {
		// given
		RemotePartitioningMasterStepBuilder builder = new RemotePartitioningMasterStepBuilder("step")
		.outputChannel(new DirectChannel())
		.messagingTemplate(new MessagingTemplate());

		// when
		Step step = builder.build();

		 // then
		 // expected exception
	 }, "You must specify either an outputChannel or a messagingTemplate but not both.");

		// then
		// expected exception
	}

	@Test
	public void testUnsupportedOperationExceptionWhenSpecifyingPartitionHandler() {
	 assertThrows(UnsupportedOperationException.class, () -> {
		// given
		PartitionHandler partitionHandler = Mockito.mock(PartitionHandler.class);

		// when
		new RemotePartitioningMasterStepBuilder("step").partitionHandler(partitionHandler);

		 // then
		 // expected exception
	 }, "When configuring a master step " +
		"for remote partitioning using the RemotePartitioningMasterStepBuilder, " +
		"the partition handler will be automatically set to an instance " +
		"of MessageChannelPartitionHandler. The partition handler must " +
		"not be provided in this case.");

		// then
		// expected exception
	}

	@Test
	public void testMasterStepCreationWhenPollingRepository() {
		// given
		int gridSize = 5;
		int startLimit = 3;
		long timeout = 1000L;
		long pollInterval = 5000L;
		DirectChannel outputChannel = new DirectChannel();
		Partitioner partitioner = Mockito.mock(Partitioner.class);
		StepExecutionAggregator stepExecutionAggregator = (result, executions) -> { };

		// when
		Step step = new RemotePartitioningMasterStepBuilder("masterStep")
				.repository(jobRepository)
				.outputChannel(outputChannel)
				.partitioner("workerStep", partitioner)
				.gridSize(gridSize)
				.pollInterval(pollInterval)
				.timeout(timeout)
				.startLimit(startLimit)
				.aggregator(stepExecutionAggregator)
				.allowStartIfComplete(true)
				.build();

		// then
		Assertions.assertNotNull(step);
		Assertions.assertEquals(getField(step, "startLimit"), startLimit);
		Assertions.assertEquals(getField(step, "jobRepository"), this.jobRepository);
		Assertions.assertEquals(getField(step, "stepExecutionAggregator"), stepExecutionAggregator);
		Assertions.assertTrue((Boolean) getField(step, "allowStartIfComplete"));

		Object partitionHandler = getField(step, "partitionHandler");
		Assertions.assertNotNull(partitionHandler);
		Assertions.assertTrue(partitionHandler instanceof MessageChannelPartitionHandler);
		MessageChannelPartitionHandler messageChannelPartitionHandler = (MessageChannelPartitionHandler) partitionHandler;
		Assertions.assertEquals(getField(messageChannelPartitionHandler, "gridSize"), gridSize);
		Assertions.assertEquals(getField(messageChannelPartitionHandler, "pollInterval"), pollInterval);
		Assertions.assertEquals(getField(messageChannelPartitionHandler, "timeout"), timeout);

		Object messagingGateway = getField(messageChannelPartitionHandler, "messagingGateway");
		Assertions.assertNotNull(messagingGateway);
		MessagingTemplate messagingTemplate = (MessagingTemplate) messagingGateway;
		Assertions.assertEquals(getField(messagingTemplate, "defaultDestination"), outputChannel);
	}

	@Test
	public void testMasterStepCreationWhenAggregatingReplies() {
		// given
		int gridSize = 5;
		int startLimit = 3;
		DirectChannel outputChannel = new DirectChannel();
		Partitioner partitioner = Mockito.mock(Partitioner.class);
		StepExecutionAggregator stepExecutionAggregator = (result, executions) -> { };

		// when
		Step step = new RemotePartitioningMasterStepBuilder("masterStep")
				.repository(jobRepository)
				.outputChannel(outputChannel)
				.partitioner("workerStep", partitioner)
				.gridSize(gridSize)
				.startLimit(startLimit)
				.aggregator(stepExecutionAggregator)
				.allowStartIfComplete(true)
				.build();

		// then
		Assertions.assertNotNull(step);
		Assertions.assertEquals(getField(step, "startLimit"), startLimit);
		Assertions.assertEquals(getField(step, "jobRepository"), this.jobRepository);
		Assertions.assertEquals(getField(step, "stepExecutionAggregator"), stepExecutionAggregator);
		Assertions.assertTrue((Boolean) getField(step, "allowStartIfComplete"));

		Object partitionHandler = getField(step, "partitionHandler");
		Assertions.assertNotNull(partitionHandler);
		Assertions.assertTrue(partitionHandler instanceof MessageChannelPartitionHandler);
		MessageChannelPartitionHandler messageChannelPartitionHandler = (MessageChannelPartitionHandler) partitionHandler;
		Assertions.assertEquals(getField(messageChannelPartitionHandler, "gridSize"), gridSize);

		Object replyChannel = getField(messageChannelPartitionHandler, "replyChannel");
		Assertions.assertNotNull(replyChannel);
		Assertions.assertTrue(replyChannel instanceof QueueChannel);

		Object messagingGateway = getField(messageChannelPartitionHandler, "messagingGateway");
		Assertions.assertNotNull(messagingGateway);
		MessagingTemplate messagingTemplate = (MessagingTemplate) messagingGateway;
		Assertions.assertEquals(getField(messagingTemplate, "defaultDestination"), outputChannel);
	}

	@Configuration
	@EnableBatchProcessing
	public static class BatchConfiguration {

	}
}
