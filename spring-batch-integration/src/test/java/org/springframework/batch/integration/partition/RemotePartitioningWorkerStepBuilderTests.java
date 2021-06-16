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

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.integration.channel.DirectChannel;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mahmoud Ben Hassine
 */
public class RemotePartitioningWorkerStepBuilderTests {

	@Mock
	private Tasklet tasklet;

	@Test
	public void inputChannelMustNotBeNull() {
	 assertThrows(IllegalArgumentException.class, () -> {

		// when
		new RemotePartitioningWorkerStepBuilder("step").inputChannel(null);

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
		new RemotePartitioningWorkerStepBuilder("step").outputChannel(null);

		// then
		// expected exception
	 }, "outputChannel must not be null");

		// then
		// expected exception
	}

	@Test
	public void jobExplorerMustNotBeNull() {
	 assertThrows(IllegalArgumentException.class, () -> {

		// when
		new RemotePartitioningWorkerStepBuilder("step").jobExplorer(null);

		// then
		// expected exception
	 }, "jobExplorer must not be null");

		// then
		// expected exception
	}

	@Test
	public void stepLocatorMustNotBeNull() {
	 assertThrows(IllegalArgumentException.class, () -> {

		// when
		new RemotePartitioningWorkerStepBuilder("step").stepLocator(null);

		// then
		// expected exception
	 }, "stepLocator must not be null");

		// then
		// expected exception
	}

	@Test
	public void beanFactoryMustNotBeNull() {
	 assertThrows(IllegalArgumentException.class, () -> {

		// when
		new RemotePartitioningWorkerStepBuilder("step").beanFactory(null);

		// then
		// expected exception
	 }, "beanFactory must not be null");

		// then
		// expected exception
	}

	@Test
	public void testMandatoryInputChannel() {
	 assertThrows(IllegalArgumentException.class, () -> {

		// when
		new RemotePartitioningWorkerStepBuilder("step").tasklet(this.tasklet);

		// then
		// expected exception
	 }, "An InputChannel must be provided");

		// then
		// expected exception
	}

	@Test
	public void testMandatoryJobExplorer() {
	 assertThrows(IllegalArgumentException.class, () -> {
		// given
		DirectChannel inputChannel = new DirectChannel();

		// when
		new RemotePartitioningWorkerStepBuilder("step")
		.inputChannel(inputChannel)
		.tasklet(this.tasklet);

		// then
		// expected exception
	 }, "A JobExplorer must be provided");

		// then
		// expected exception
	}

}
