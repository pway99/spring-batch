/*
 * Copyright 2018-2019 the original author or authors.
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
package org.springframework.batch.sample;

import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.sample.config.JobRunnerConfiguration;
import org.springframework.batch.sample.remotechunking.ManagerConfiguration;
import org.springframework.batch.sample.remotechunking.WorkerConfiguration;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * The manager step of the job under test will read data and send chunks to the worker
 * (started in {@link RemoteChunkingJobFunctionalTests#setUp()}) for processing and writing.
 *
 * @author Mahmoud Ben Hassine
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JobRunnerConfiguration.class, ManagerConfiguration.class})
@PropertySource("classpath:remote-chunking.properties")
public class RemoteChunkingJobFunctionalTests {

	private static final String BROKER_DATA_DIRECTORY = "target/activemq-data";

	@Value("${broker.url}")
	private String brokerUrl;

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	private BrokerService brokerService;

	private AnnotationConfigApplicationContext workerApplicationContext;

	@BeforeEach
	public void setUp() throws Exception {
		this.brokerService = new BrokerService();
		this.brokerService.addConnector(this.brokerUrl);
		this.brokerService.setDataDirectory(BROKER_DATA_DIRECTORY);
		this.brokerService.start();
		this.workerApplicationContext = new AnnotationConfigApplicationContext(WorkerConfiguration.class);
	}

	@AfterEach
	public void tearDown() throws Exception {
		this.workerApplicationContext.close();
		this.brokerService.stop();
	}

	@Test
	public void testRemoteChunkingJob() throws Exception {
		// when
		JobExecution jobExecution = this.jobLauncherTestUtils.launchJob();

		// then
		Assertions.assertEquals(ExitStatus.COMPLETED.getExitCode(), jobExecution.getExitStatus().getExitCode());
		Assertions.assertEquals(
				"Waited for 2 results.", // the manager sent 2 chunks ({1, 2, 3} and {4, 5, 6}) to workers
				jobExecution.getExitStatus().getExitDescription());
	}

}
