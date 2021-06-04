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
package org.springframework.batch.core.step;

import org.junit.jupiter.api.Test;

import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.StepExecution;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Dave Syer
 *
 */
public class ThreadStepInterruptionPolicyTests {

	ThreadStepInterruptionPolicy policy = new ThreadStepInterruptionPolicy();
	private StepExecution context = new StepExecution("stepSupport", null);

	/**
	 * Test method for {@link org.springframework.batch.core.step.ThreadStepInterruptionPolicy#checkInterrupted(StepExecution)}.
	 * @throws Exception 
	 */
	@Test
 public void testCheckInterruptedNotComplete() throws Exception {
		policy.checkInterrupted(context);
		// no exception
	}

	/**
	 * Test method for {@link org.springframework.batch.core.step.ThreadStepInterruptionPolicy#checkInterrupted(StepExecution)}.
	 * @throws Exception 
	 */
	@Test
 public void testCheckInterruptedComplete() throws Exception {
		context.setTerminateOnly();
		try {
			policy.checkInterrupted(context);
			fail("Expected StepInterruptedException");
		} catch (JobInterruptedException e) {
			// expected
			assertTrue(e.getMessage().indexOf("interrupt")>=0);
		}
	}

}
