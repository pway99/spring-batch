/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.batch.core.jsr;

import javax.batch.api.listener.StepListener;
import javax.batch.operations.BatchRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StepListenerAdapterTests {

	private StepListenerAdapter adapter;
	@Mock
	private StepListener delegate;
	@Mock
	private StepExecution execution;

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		adapter = new StepListenerAdapter(delegate);
	}

	@Test
	public void testCreateWithNull() {
	 assertThrows(IllegalArgumentException.class, () -> {
		adapter = new StepListenerAdapter(null);
	 });
	}

	@Test
	public void testBeforeStep() throws Exception {
		adapter.beforeStep(null);

		verify(delegate).beforeStep();
	}

	@Test
	public void testBeforeStepException() throws Exception {
	 assertThrows(BatchRuntimeException.class, () -> {
		doThrow(new Exception("expected")).when(delegate).beforeStep();

		adapter.beforeStep(null);
	 });
	}

	@Test
	public void testAfterStep() throws Exception {
		ExitStatus exitStatus = new ExitStatus("complete");
		when(execution.getExitStatus()).thenReturn(exitStatus);

		assertEquals(exitStatus, adapter.afterStep(execution));

		verify(delegate).afterStep();
	}

	@Test
	public void testAfterStepException() throws Exception {
	 assertThrows(BatchRuntimeException.class, () -> {
		doThrow(new Exception("expected")).when(delegate).afterStep();

		adapter.afterStep(null);
	 });
	}
}
