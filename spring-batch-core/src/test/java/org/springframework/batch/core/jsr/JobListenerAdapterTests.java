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

import javax.batch.api.listener.JobListener;
import javax.batch.operations.BatchRuntimeException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class JobListenerAdapterTests {

	private JobListenerAdapter adapter;
	@Mock
	private JobListener delegate;

	@BeforeEach
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		adapter = new JobListenerAdapter(delegate);
	}

	@Test
	public void testCreateWithNull() {
	 assertThrows(IllegalArgumentException.class, () -> {
		adapter = new JobListenerAdapter(null);
	 });
	}

	@Test
	public void testBeforeJob() throws Exception {
		adapter.beforeJob(null);

		verify(delegate).beforeJob();
	}

	@Test
	public void testBeforeJobException() throws Exception {
	 assertThrows(BatchRuntimeException.class, () -> {
		doThrow(new Exception("expected")).when(delegate).beforeJob();

		adapter.beforeJob(null);
	 });
	}

	@Test
	public void testAfterJob() throws Exception {
		adapter.afterJob(null);

		verify(delegate).afterJob();
	}

	@Test
	public void testAfterJobException() throws Exception {
	 assertThrows(BatchRuntimeException.class, () -> {
		doThrow(new Exception("expected")).when(delegate).afterJob();

		adapter.afterJob(null);
	 });
	}
}
