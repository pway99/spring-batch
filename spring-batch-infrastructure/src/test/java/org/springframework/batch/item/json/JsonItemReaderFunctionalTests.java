/*
 * Copyright 2018-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.item.json;

import java.math.BigDecimal;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.item.json.domain.Trade;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mahmoud Ben Hassine
 */
public abstract class JsonItemReaderFunctionalTests {

	protected abstract JsonObjectReader<Trade> getJsonObjectReader();

	protected abstract Class<? extends Exception> getJsonParsingException();

	@Test
	public void testJsonReading() throws Exception {
		JsonItemReader<Trade> itemReader = new JsonItemReaderBuilder<Trade>()
				.jsonObjectReader(getJsonObjectReader())
				.resource(new ClassPathResource("org/springframework/batch/item/json/trades.json"))
				.name("tradeJsonItemReader")
				.build();

		itemReader.open(new ExecutionContext());

		Trade trade = itemReader.read();
		Assertions.assertNotNull(trade);
		Assertions.assertEquals("123", trade.getIsin());
		Assertions.assertEquals("foo", trade.getCustomer());
		Assertions.assertEquals(new BigDecimal("1.2"), trade.getPrice());
		Assertions.assertEquals(1, trade.getQuantity());

		trade = itemReader.read();
		Assertions.assertNotNull(trade);
		Assertions.assertEquals("456", trade.getIsin());
		Assertions.assertEquals("bar", trade.getCustomer());
		Assertions.assertEquals(new BigDecimal("1.4"), trade.getPrice());
		Assertions.assertEquals(2, trade.getQuantity());

		trade = itemReader.read();
		Assertions.assertNotNull(trade);
		Assertions.assertEquals("789", trade.getIsin());
		Assertions.assertEquals("foobar", trade.getCustomer());
		Assertions.assertEquals(new BigDecimal("1.6"), trade.getPrice());
		Assertions.assertEquals(3, trade.getQuantity());

		trade = itemReader.read();
		Assertions.assertNotNull(trade);
		Assertions.assertEquals("100", trade.getIsin());
		Assertions.assertEquals("barfoo", trade.getCustomer());
		Assertions.assertEquals(new BigDecimal("1.8"), trade.getPrice());
		Assertions.assertEquals(4, trade.getQuantity());

		trade = itemReader.read();
		Assertions.assertNull(trade);
	}

	@Test
	public void testEmptyResource() throws Exception {
		JsonItemReader<Trade> itemReader = new JsonItemReaderBuilder<Trade>()
				.jsonObjectReader(getJsonObjectReader())
				.resource(new ByteArrayResource("[]".getBytes()))
				.name("tradeJsonItemReader")
				.build();

		itemReader.open(new ExecutionContext());

		Trade trade = itemReader.read();
		Assertions.assertNull(trade);
	}

	@Test
	public void testInvalidResourceFormat() {
	 Exception exception = assertThrows(ItemStreamException.class, () -> {
		JsonItemReader<Trade> itemReader = new JsonItemReaderBuilder<Trade>()
		.jsonObjectReader(getJsonObjectReader())
		.resource(new ByteArrayResource("{}, {}".getBytes()))
		.name("tradeJsonItemReader")
		.build();

		itemReader.open(new ExecutionContext());
	 }, "Failed to initialize the reader");
	 assertThat(exception.getCause(), instanceOf(IllegalStateException.class));
	}

	@Test
	public void testInvalidResourceContent() throws Exception {
	 Exception exception = assertThrows(ParseException.class, () -> {
		JsonItemReader<Trade> itemReader = new JsonItemReaderBuilder<Trade>()
		.jsonObjectReader(getJsonObjectReader())
		.resource(new ByteArrayResource("[{]".getBytes()))
		.name("tradeJsonItemReader")
		.build();
		itemReader.open(new ExecutionContext());

		itemReader.read();
	 });
	 assertThat(exception.getCause(), Matchers.instanceOf(getJsonParsingException()));
	}
}
