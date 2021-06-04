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
package org.springframework.batch.repeat.listener;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatListener;
import org.springframework.batch.repeat.context.RepeatContextSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dave Syer
 * 
 */
public class CompositeRepeatListenerTests {

	private CompositeRepeatListener listener = new CompositeRepeatListener();
	private RepeatContext context = new RepeatContextSupport(null);

	private List<Object> list = new ArrayList<>();

	/**
	 * Test method for {@link CompositeRepeatListener#setListeners(RepeatListener[])}.
	 */
	@Test
 public void testSetListeners() {
		listener.setListeners(new RepeatListener[] { new RepeatListenerSupport() {
            @Override
			public void open(RepeatContext context) {
				list.add("fail");
			}
		}, new RepeatListenerSupport() {
            @Override
			public void open(RepeatContext context) {
				list.add("continue");
			}
		} });
		listener.open(context);
		assertEquals(2, list.size());
	}

	/**
	 * Test method for
	 * {@link CompositeRepeatListener#register(RepeatListener)}.
	 */
	@Test
 public void testSetListener() {
		listener.register(new RepeatListenerSupport() {
            @Override
			public void before(RepeatContext context) {
				list.add("fail");
			}
		});
		listener.before(context);
		assertEquals(1, list.size());
	}

	@Test
 public void testClose() {
		listener.register(new RepeatListenerSupport() {
            @Override
			public void close(RepeatContext context) {
				list.add("foo");
			}
		});
		listener.close(context);
		assertEquals(1, list.size());
	}

	@Test
 public void testOnError() {
		listener.register(new RepeatListenerSupport() {
            @Override
			public void onError(RepeatContext context, Throwable e) {
				list.add(e);
			}
		});
		listener.onError(context, new RuntimeException("foo"));
		assertEquals(1, list.size());
	}

}
