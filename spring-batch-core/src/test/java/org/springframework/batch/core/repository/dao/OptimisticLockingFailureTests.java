/*
 * Copyright 2014-2020 the original author or authors.
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

package org.springframework.batch.core.repository.dao;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.lang.Nullable;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OptimisticLockingFailureTests {

	private static final Set<BatchStatus> END_STATUSES =
			EnumSet.of(BatchStatus.COMPLETED, BatchStatus.FAILED, BatchStatus.STOPPED);

	@Test
	public void testAsyncStopOfStartingJob() throws Exception {
		ApplicationContext applicationContext =
				new ClassPathXmlApplicationContext("org/springframework/batch/core/repository/dao/OptimisticLockingFailureTests-context.xml");
		Job job = applicationContext.getBean(Job.class);
		JobLauncher jobLauncher = applicationContext.getBean(JobLauncher.class);
		JobOperator jobOperator = applicationContext.getBean(JobOperator.class);
		JobRepository jobRepository = applicationContext.getBean(JobRepository.class);

		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("test", 1L)
				.toJobParameters();
		JobExecution jobExecution = jobLauncher.run(job, jobParameters);

		Thread.sleep(1000);

		jobOperator.stop(jobExecution.getId());

		JobExecution lastJobExecution = jobRepository.getLastJobExecution("locking", jobParameters);
		while (lastJobExecution != null && !END_STATUSES.contains(lastJobExecution.getStatus())) {
			lastJobExecution = jobRepository.getLastJobExecution("locking", jobParameters);
		}

		int numStepExecutions = jobExecution.getStepExecutions().size();
		StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
		String stepName = stepExecution.getStepName();
		BatchStatus stepExecutionStatus = stepExecution.getStatus();
		BatchStatus jobExecutionStatus = jobExecution.getStatus();

		assertTrue(numStepExecutions == 1, "Should only be one StepExecution but got: " + numStepExecutions);
		assertTrue("step1".equals(stepName), "Step name for execution should be step1 but got: " + stepName);
		assertTrue(stepExecutionStatus.equals(BatchStatus.STOPPED), "Step execution status should be STOPPED but got: " + stepExecutionStatus);
		assertTrue(jobExecutionStatus.equals(BatchStatus.STOPPED), "Job execution status should be STOPPED but got:" + jobExecutionStatus);

		JobExecution restartJobExecution = jobLauncher.run(job, jobParameters);

		Thread.sleep(1000);

		lastJobExecution = jobRepository.getLastJobExecution("locking", jobParameters);
		while (lastJobExecution != null && !END_STATUSES.contains(lastJobExecution.getStatus())) {
			lastJobExecution = jobRepository.getLastJobExecution("locking", jobParameters);
		}

		int restartNumStepExecutions = restartJobExecution.getStepExecutions().size();
		assertTrue(restartNumStepExecutions == 2, "Should be two StepExecution's on restart but got: " + restartNumStepExecutions);

		for(StepExecution restartStepExecution : restartJobExecution.getStepExecutions()) {
			BatchStatus restartStepExecutionStatus = restartStepExecution.getStatus();

			assertTrue(
			restartStepExecutionStatus.equals(BatchStatus.COMPLETED), "Step execution status should be COMPLETED but got: " + restartStepExecutionStatus);
		}

		BatchStatus restartJobExecutionStatus = restartJobExecution.getStatus();
		assertTrue(
		restartJobExecutionStatus.equals(BatchStatus.COMPLETED), "Job execution status should be COMPLETED but got:" + restartJobExecutionStatus);
	}

	public static class Writer implements ItemWriter<String> {
		@Override
		public void write(List<? extends String> items) throws Exception {
			for(String item : items) {
				System.out.println(item);
			}
		}
	}

	public static class SleepingTasklet implements Tasklet {
		@Nullable
		@Override
		public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
			Thread.sleep(2000L);
			return RepeatStatus.FINISHED;
		}
	}
}
