/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.core.configuration.xml;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * @author Dave Syer
 *
 */
@ContextConfiguration
@ExtendWith(SpringExtension.class)
public class JobStepParserTests {

	@Autowired
	@Qualifier("job1")
	private Job job1;

	@Autowired
	@Qualifier("job2")
	private Job job2;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private MapJobRepositoryFactoryBean mapJobRepositoryFactoryBean;

	@BeforeEach
	public void setUp() {
		mapJobRepositoryFactoryBean.clear();
	}

	@Test
	public void testFlowStep() throws Exception {
		assertNotNull(job1);
		JobExecution jobExecution = jobRepository.createJobExecution(job1.getName(), new JobParameters());
		job1.execute(jobExecution);
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		List<String> stepNames = getStepNames(jobExecution);
		assertEquals(3, stepNames.size());
		assertEquals("[s1, job1.flow, s4]", stepNames.toString());
	}

	@Test
	public void testFlowExternalStep() throws Exception {
		assertNotNull(job2);
		JobExecution jobExecution = jobRepository.createJobExecution(job2.getName(), new JobParameters());
		job2.execute(jobExecution);
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		List<String> stepNames = getStepNames(jobExecution);
		assertEquals(3, stepNames.size());
		assertEquals("[job2.s1, job2.flow, job2.s4]", stepNames.toString());
	}

	private List<String> getStepNames(JobExecution jobExecution) {
		List<String> list = new ArrayList<>();
		for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
			list.add(stepExecution.getStepName());
		}
		return list;
	}

}
