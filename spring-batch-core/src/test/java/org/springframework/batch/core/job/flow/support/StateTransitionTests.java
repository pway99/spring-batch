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

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.flow.State;
import org.springframework.batch.core.job.flow.StateSupport;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dave Syer
 * @author Michael Minella
 *
 */
public class StateTransitionTests {

	State state = new StateSupport("state1");

	@Test
	public void testIsEnd() {
		StateTransition transition = StateTransition.createEndStateTransition(state, "");
		assertTrue(transition.isEnd());
		assertNull(transition.getNext());
	}

	@Test
	public void testMatchesStar() {
		StateTransition transition = StateTransition.createStateTransition(state, "*", "start");
		assertTrue(transition.matches("CONTINUABLE"));
	}

	@Test
	public void testMatchesNull() {
		StateTransition transition = StateTransition.createStateTransition(state, null, "start");
		assertTrue(transition.matches("CONTINUABLE"));
	}

	@Test
	public void testMatchesEmpty() {
		StateTransition transition = StateTransition.createStateTransition(state, "", "start");
		assertTrue(transition.matches("CONTINUABLE"));
	}

	@Test
	public void testMatchesExact() {
		StateTransition transition = StateTransition.createStateTransition(state, "CONTINUABLE", "start");
		assertTrue(transition.matches("CONTINUABLE"));
	}

	@Test
	public void testMatchesWildcard() {
		StateTransition transition = StateTransition.createStateTransition(state, "CONTIN*", "start");
		assertTrue(transition.matches("CONTINUABLE"));
	}

	@Test
	public void testMatchesPlaceholder() {
		StateTransition transition = StateTransition.createStateTransition(state, "CONTIN???LE", "start");
		assertTrue(transition.matches("CONTINUABLE"));
	}

	@Test
	public void testToString() {
		StateTransition transition = StateTransition.createStateTransition(state, "CONTIN???LE", "start");
		String string = transition.toString();
		assertTrue(string.contains("Transition"), "Wrong string: " + string);
		assertTrue(string.contains("start"), "Wrong string: " + string);
		assertTrue(string.contains("CONTIN???LE"), "Wrong string: " + string);
		assertTrue(string.contains("next="), "Wrong string: " + string);
	}

}
