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
package org.springframework.batch.core.jsr.step.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.batch.core.step.item.Chunk;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsrChunkProviderTests {

	private JsrChunkProvider<String> provider;

	@BeforeEach
	public void setUp() throws Exception {
		provider = new JsrChunkProvider<>();
	}

	@Test
	public void test() throws Exception {
		Chunk<String> chunk = provider.provide(null);
		assertNotNull(chunk);
		assertEquals(0, chunk.getItems().size());
	}
}
