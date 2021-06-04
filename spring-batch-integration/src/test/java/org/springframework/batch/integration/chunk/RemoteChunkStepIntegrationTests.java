package org.springframework.batch.integration.chunk;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration
@ExtendWith(SpringExtension.class)
public class RemoteChunkStepIntegrationTests {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private Job job;

	@Test
	public void testSunnyDaySimpleStep() throws Exception {
		JobExecution jobExecution = jobLauncher.run(job, new JobParameters(Collections.singletonMap("item.three",
				new JobParameter("3"))));
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
		assertEquals(9, stepExecution.getReadCount());
		assertEquals(9, stepExecution.getWriteCount());
	}

	@Test
	public void testFailedStep() throws Exception {
		JobExecution jobExecution = jobLauncher.run(job, new JobParameters(Collections.singletonMap("item.three",
				new JobParameter("fail"))));
		assertEquals(BatchStatus.FAILED, jobExecution.getStatus());
		StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
		assertEquals(9, stepExecution.getReadCount());
		// In principle the write count could be more than 2 and less than 9...
		assertEquals(7, stepExecution.getWriteCount());
	}

}
