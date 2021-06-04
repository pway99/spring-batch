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

package org.springframework.batch.repeat.context;

import org.junit.jupiter.api.Test;

import org.springframework.batch.repeat.RepeatContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RepeatContextCounterTests {

	RepeatContext parent = new RepeatContextSupport(null);
	RepeatContext context = new RepeatContextSupport(parent);

	@Test
 public void testAttributeCreated() {
		new RepeatContextCounter(context, "FOO");
		assertTrue(context.hasAttribute("FOO"));
	}

	@Test
 public void testAttributeCreatedWithNullParent() {
		new RepeatContextCounter(parent, "FOO", true);
		assertTrue(parent.hasAttribute("FOO"));
	}

	@Test
 public void testVanillaIncrement() throws Exception {
		RepeatContextCounter counter = new RepeatContextCounter(context, "FOO");
		assertEquals(0, counter.getCount());
		counter.increment(1);
		assertEquals(1, counter.getCount());
		counter.increment(2);
		assertEquals(3, counter.getCount());
	}

	@Test
 public void testAttributeCreatedInParent() throws Exception {
		new RepeatContextCounter(context, "FOO", true);
		assertFalse(context.hasAttribute("FOO"));
		assertTrue(parent.hasAttribute("FOO"));
	}

	@Test
 public void testParentIncrement() throws Exception {
		RepeatContextCounter counter = new RepeatContextCounter(context, "FOO", true);
		assertEquals(0, counter.getCount());
		counter.increment(1);
		// now get new context with same parent
		counter = new RepeatContextCounter(new RepeatContextSupport(parent), "FOO", true);
		assertEquals(1, counter.getCount());
		counter.increment(2);
		assertEquals(3, counter.getCount());
	}

}
