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

package org.springframework.batch.sample.validation;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.support.ListItemWriter;
import org.springframework.batch.sample.validation.domain.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Mahmoud Ben Hassine
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ValidationSampleConfiguration.class})
public class ValidationSampleFunctionalTests {

	@Autowired
	private Job job;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private ListItemWriter<Person> listItemWriter;

	@Test
	public void testItemValidation() throws Exception {
		// given
		JobParameters jobParameters = new JobParameters();

		// when
		JobExecution jobExecution = this.jobLauncher.run(this.job, jobParameters);

		// then
		Assertions.assertEquals(ExitStatus.COMPLETED.getExitCode(), jobExecution.getExitStatus().getExitCode());
		List<? extends Person> writtenItems = this.listItemWriter.getWrittenItems();
		Assertions.assertEquals(1, writtenItems.size());
		Assertions.assertEquals("foo", writtenItems.get(0).getName());
	}
}
