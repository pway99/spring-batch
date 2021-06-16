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
package org.springframework.batch.core.job.flow;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.flow.FlowExecution;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dave Syer
 * 
 */
public class FlowExecutionTests {

	@Test
	public void testBasicProperties() throws Exception {
		FlowExecution execution = new FlowExecution("foo", new FlowExecutionStatus("BAR"));
		assertEquals("foo",execution.getName());
		assertEquals("BAR",execution.getStatus().getName());
	}

	@Test
	public void testAlphaOrdering() throws Exception {
		FlowExecution first = new FlowExecution("foo", new FlowExecutionStatus("BAR"));
		FlowExecution second = new FlowExecution("foo", new FlowExecutionStatus("SPAM"));
		assertTrue(first.compareTo(second) < 0, "Should be negative");
		assertTrue(second.compareTo(first) > 0, "Should be positive");
	}

	@Test
	public void testEnumOrdering() throws Exception {
		FlowExecution first = new FlowExecution("foo", FlowExecutionStatus.COMPLETED);
		FlowExecution second = new FlowExecution("foo", FlowExecutionStatus.FAILED);
		assertTrue(first.compareTo(second) < 0, "Should be negative");
		assertTrue(second.compareTo(first) > 0, "Should be positive");
	}

	@Test
	public void testEnumStartsWithOrdering() throws Exception {
		FlowExecution first = new FlowExecution("foo", new FlowExecutionStatus("COMPLETED.BAR"));
		FlowExecution second = new FlowExecution("foo", new FlowExecutionStatus("FAILED.FOO"));
		assertTrue(first.compareTo(second) < 0, "Should be negative");
		assertTrue(second.compareTo(first) > 0, "Should be positive");
	}

	@Test
	public void testEnumStartsWithAlphaOrdering() throws Exception {
		FlowExecution first = new FlowExecution("foo", new FlowExecutionStatus("COMPLETED.BAR"));
		FlowExecution second = new FlowExecution("foo", new FlowExecutionStatus("COMPLETED.FOO"));
		assertTrue(first.compareTo(second) < 0, "Should be negative");
		assertTrue(second.compareTo(first) > 0, "Should be positive");
	}

	@Test
	public void testEnumAndAlpha() throws Exception {
		FlowExecution first = new FlowExecution("foo", new FlowExecutionStatus("ZZZZZ"));
		FlowExecution second = new FlowExecution("foo", new FlowExecutionStatus("FAILED.FOO"));
		assertTrue(first.compareTo(second) < 0, "Should be negative");
		assertTrue(second.compareTo(first) > 0, "Should be positive");
	}

}
