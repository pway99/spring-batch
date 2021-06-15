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

package org.springframework.batch.repeat.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.context.RepeatContextSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepeatSynchronizationManagerTests {

	private RepeatContext context = new RepeatContextSupport(null);

 @BeforeEach
	public void setUp() throws Exception {
		RepeatSynchronizationManager.clear();
	}

 @AfterEach
	public void tearDown() throws Exception {
		RepeatSynchronizationManager.clear();
	}

	@Test
 public void testGetContext() {
		RepeatSynchronizationManager.register(context);
		assertEquals(context, RepeatSynchronizationManager.getContext());
	}

	@Test
 public void testSetSessionCompleteOnly() {
		assertNull(RepeatSynchronizationManager.getContext());
		RepeatSynchronizationManager.register(context);
		assertFalse(RepeatSynchronizationManager.getContext().isCompleteOnly());
		RepeatSynchronizationManager.setCompleteOnly();
		assertTrue(RepeatSynchronizationManager.getContext().isCompleteOnly());
	}

	@Test
 public void testSetSessionCompleteOnlyWithParent() {
		assertNull(RepeatSynchronizationManager.getContext());
		RepeatContext child = new RepeatContextSupport(context);
		RepeatSynchronizationManager.register(child);
		assertFalse(child.isCompleteOnly());
		RepeatSynchronizationManager.setAncestorsCompleteOnly();
		assertTrue(child.isCompleteOnly());
		assertTrue(context.isCompleteOnly());
	}

	@Test
 public void testClear() {
		RepeatSynchronizationManager.register(context);
		assertEquals(context, RepeatSynchronizationManager.getContext());
		RepeatSynchronizationManager.clear();
		assertEquals(null, RepeatSynchronizationManager.getContext());
	}

}
