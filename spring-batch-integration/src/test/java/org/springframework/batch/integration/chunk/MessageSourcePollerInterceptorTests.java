package org.springframework.batch.integration.chunk;

import org.junit.jupiter.api.Test;

import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MessageSourcePollerInterceptorTests {

	@Test
	public void testMandatoryPropertiesUnset() throws Exception {
	 assertThrows(IllegalStateException.class, () -> {
		MessageSourcePollerInterceptor interceptor = new MessageSourcePollerInterceptor();
		interceptor.afterPropertiesSet();
	 });
	}

	@Test
	public void testMandatoryPropertiesSetViaConstructor() throws Exception {
		MessageSourcePollerInterceptor interceptor = new MessageSourcePollerInterceptor(new TestMessageSource("foo"));
		interceptor.afterPropertiesSet();
	}

	@Test
	public void testMandatoryPropertiesSet() throws Exception {
		MessageSourcePollerInterceptor interceptor = new MessageSourcePollerInterceptor();
		interceptor.setMessageSource(new TestMessageSource("foo"));
		interceptor.afterPropertiesSet();
	}

	@Test
	public void testPreReceive() throws Exception {
		MessageSourcePollerInterceptor interceptor = new MessageSourcePollerInterceptor(new TestMessageSource("foo"));
		QueueChannel channel = new QueueChannel();
		assertTrue(interceptor.preReceive(channel));
		assertEquals("foo", channel.receive(10L).getPayload());
	}

	private static class TestMessageSource implements MessageSource<String> {

		private final String payload;

		public TestMessageSource(String payload) {
			super();
			this.payload = payload;
		}

		public Message<String> receive() {
			return new GenericMessage<>(payload);
		}
	}

}
