/*
 * Copyright 2006-2013 the original author or authors.
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
package org.springframework.batch.core.job.flow.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.flow.FlowExecution;
import org.springframework.batch.core.job.flow.FlowExecutionException;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.FlowExecutor;
import org.springframework.batch.core.job.flow.State;
import org.springframework.batch.core.job.flow.StateSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Dave Syer
 * @author Michael Minella
 *
 */
public class SimpleFlowTests {

	protected SimpleFlow flow;

	protected FlowExecutor executor = new JobFlowExecutorSupport();

	@BeforeEach
	public void setUp() {
		flow = new SimpleFlow("job");
	}

	@Test
	public void testEmptySteps() throws Exception {
	 assertThrows(IllegalArgumentException.class, () -> {
		flow.setStateTransitions(Collections.<StateTransition>emptyList());
		flow.afterPropertiesSet();
	 });
	}

	@Test
	public void testNoNextStepSpecified() throws Exception {
	 assertThrows(IllegalArgumentException.class, () -> {
		flow.setStateTransitions(Collections.singletonList(StateTransition.createStateTransition(new StateSupport(
		"step"), "foo")));
		flow.afterPropertiesSet();
	 });
	}

	@Test
	public void testStepLoop() throws Exception {
		flow.setStateTransitions(collect(StateTransition.createStateTransition(new StateSupport("step"),
				ExitStatus.FAILED.getExitCode(), "step"), StateTransition.createEndStateTransition(new StateSupport("step"))));
		flow.afterPropertiesSet();
		FlowExecution execution = flow.start(executor);
		assertEquals(FlowExecutionStatus.COMPLETED, execution.getStatus());
		assertEquals("step", execution.getName());
	}

	@Test
	public void testNoEndStep() throws Exception {
	 assertThrows(IllegalArgumentException.class, () -> {
		flow.setStateTransitions(Collections.singletonList(StateTransition.createStateTransition(new StateSupport(
		"step"), ExitStatus.FAILED.getExitCode(), "step")));
		flow.afterPropertiesSet();
	 });
	}

	@Test
	public void testUnconnectedSteps() throws Exception {
		flow.setStateTransitions(collect(StateTransition.createEndStateTransition(new StubState("step1")),
				StateTransition.createEndStateTransition(new StubState("step2"))));
		flow.afterPropertiesSet();
		FlowExecution execution = flow.start(executor);
		assertEquals(FlowExecutionStatus.COMPLETED, execution.getStatus());
		assertEquals("step1", execution.getName());
	}

	@Test
	public void testNoMatchForNextStep() throws Exception {
		flow.setStateTransitions(collect(StateTransition.createStateTransition(new StubState("step1"), "FOO", "step2"),
				StateTransition.createEndStateTransition(new StubState("step2"))));
		flow.afterPropertiesSet();
		try {
			flow.start(executor);
			fail("Expected JobExecutionException");
		}
		catch (FlowExecutionException e) {
			// expected
			String message = e.getMessage();
			assertTrue(message.toLowerCase().contains("next state not found"), "Wrong message: " + message);
		}
	}

	@Test
	public void testOneStep() throws Exception {
		flow.setStateTransitions(Collections.singletonList(StateTransition.createEndStateTransition(new StubState(
				"step1"))));
		flow.afterPropertiesSet();
		FlowExecution execution = flow.start(executor);
		assertEquals(FlowExecutionStatus.COMPLETED, execution.getStatus());
		assertEquals("step1", execution.getName());
	}

	@Test
	public void testOneStepWithListenerCallsClose() throws Exception {
		flow.setStateTransitions(Collections.singletonList(StateTransition.createEndStateTransition(new StubState(
				"step1"))));
		flow.afterPropertiesSet();
		final List<FlowExecution> list = new ArrayList<>();
		executor = new JobFlowExecutorSupport() {
			@Override
			public void close(FlowExecution result) {
				list.add(result);
			}
		};
		FlowExecution execution = flow.start(executor);
		assertEquals(1, list.size());
		assertEquals(FlowExecutionStatus.COMPLETED, execution.getStatus());
		assertEquals("step1", execution.getName());
	}

	@Test
	public void testExplicitStartStep() throws Exception {
		flow.setStateTransitions(collect(StateTransition.createStateTransition(new StubState("step"),
				ExitStatus.FAILED.getExitCode(), "step"), StateTransition.createEndStateTransition(new StubState("step"))));
		flow.afterPropertiesSet();
		FlowExecution execution = flow.start(executor);
		assertEquals(FlowExecutionStatus.COMPLETED, execution.getStatus());
		assertEquals("step", execution.getName());
	}

	@Test
	public void testTwoSteps() throws Exception {
		flow.setStateTransitions(collect(StateTransition.createStateTransition(new StubState("step1"), "step2"),
				StateTransition.createEndStateTransition(new StubState("step2"))));
		flow.afterPropertiesSet();
		FlowExecution execution = flow.start(executor);
		assertEquals(FlowExecutionStatus.COMPLETED, execution.getStatus());
		assertEquals("step2", execution.getName());
	}

	@Test
	public void testResume() throws Exception {
		flow.setStateTransitions(collect(StateTransition.createStateTransition(new StubState("step1"), "step2"),
				StateTransition.createEndStateTransition(new StubState("step2"))));
		flow.afterPropertiesSet();
		FlowExecution execution = flow.resume("step2", executor);
		assertEquals(FlowExecutionStatus.COMPLETED, execution.getStatus());
		assertEquals("step2", execution.getName());
	}

	@Test
	public void testFailedStep() throws Exception {
		flow.setStateTransitions(collect(StateTransition.createStateTransition(new StubState("step1") {
			@Override
			public FlowExecutionStatus handle(FlowExecutor executor) {
				return FlowExecutionStatus.FAILED;
			}
		}, "step2"), StateTransition.createEndStateTransition(new StubState("step2"))));
		flow.afterPropertiesSet();
		FlowExecution execution = flow.start(executor);
		assertEquals(FlowExecutionStatus.COMPLETED, execution.getStatus());
		assertEquals("step2", execution.getName());
	}

	@Test
	public void testBranching() throws Exception {
		flow.setStateTransitions(collect(StateTransition.createStateTransition(new StubState("step1"), "step2"),
				StateTransition.createStateTransition(new StubState("step1"), ExitStatus.COMPLETED.getExitCode(), "step3"),
				StateTransition.createEndStateTransition(new StubState("step2")), StateTransition
				.createEndStateTransition(new StubState("step3"))));
		flow.setStateTransitionComparator(new DefaultStateTransitionComparator());
		flow.afterPropertiesSet();
		FlowExecution execution = flow.start(executor);
		assertEquals(FlowExecutionStatus.COMPLETED, execution.getStatus());
		assertEquals("step3", execution.getName());
	}

	@Test
	public void testGetStateExists() throws Exception {
		flow.setStateTransitions(Collections.singletonList(StateTransition.createEndStateTransition(new StubState(
				"step1"))));
		flow.afterPropertiesSet();
		State state = flow.getState("step1");
		assertNotNull(state);
		assertEquals("step1", state.getName());
	}

	@Test
	public void testGetStateDoesNotExist() throws Exception {
		flow.setStateTransitions(Collections.singletonList(StateTransition.createEndStateTransition(new StubState(
				"step1"))));
		flow.afterPropertiesSet();
		State state = flow.getState("bar");
		assertNull(state);
	}

	protected List<StateTransition> collect(StateTransition... states) {
		List<StateTransition> list = new ArrayList<>();

		for (StateTransition stateTransition : states) {
			list.add(stateTransition);
		}

		return list;
	}

	/**
	 * @author Dave Syer
	 *
	 */
	protected static class StubState extends StateSupport {

		/**
		 * @param string
		 */
		public StubState(String string) {
			super(string);
		}

	}

}
