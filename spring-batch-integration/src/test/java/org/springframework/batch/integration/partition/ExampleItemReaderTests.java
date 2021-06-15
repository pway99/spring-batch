package org.springframework.batch.integration.partition;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class ExampleItemReaderTests {

	private ExampleItemReader reader = new ExampleItemReader();

	@BeforeEach
	@AfterEach
	public void ensureFailFlagUnset() {
		ExampleItemReader.fail = false;
	}

	@Test
	public void testRead() throws Exception {
		int count = 0;
		while (reader.read()!=null) {
			count++;
		}
		assertEquals(8, count);
	}

	@Test
	public void testOpen() throws Exception {
		ExecutionContext context = new ExecutionContext();
		for (int i=0; i<4; i++) {
			reader.read();
		}
		reader.update(context);
		reader.open(context);
		int count = 0;
		while (reader.read()!=null) {
			count++;
		}
		assertEquals(4, count);
	}

	@Test
	public void testFailAndRestart() throws Exception {
		ExecutionContext context = new ExecutionContext();
		ExampleItemReader.fail = true;
		for (int i=0; i<4; i++) {
			reader.read();
			reader.update(context);
		}
		try {
			reader.read();
			reader.update(context);
			fail("Expected Exception");
		}
		catch (Exception e) {
			// expected
			assertEquals("Planned failure", e.getMessage());
		}
		assertFalse(ExampleItemReader.fail);
		reader.open(context);
		int count = 0;
		while (reader.read()!=null) {
			count++;
		}
		assertEquals(4, count);
	}

}
