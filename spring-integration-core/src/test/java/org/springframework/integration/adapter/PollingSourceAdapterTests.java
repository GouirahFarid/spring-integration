/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import org.springframework.integration.channel.SimpleChannel;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.message.Message;
import org.springframework.integration.message.PollableSource;

/**
 * @author Mark Fisher
 */
public class PollingSourceAdapterTests {

	@Test
	public void testPolledSourceSendsToChannel() {
		TestSource source = new TestSource("testing", 1);
		SimpleChannel channel = new SimpleChannel();
		PollingSourceAdapter<String> adapter = new PollingSourceAdapter<String>(source);
		adapter.setChannel(channel);
		adapter.setPeriod(100);
		adapter.start();
		Message<?> message = channel.receive(1000);
		assertNotNull("message should not be null", message);
		assertEquals("testing.1", message.getPayload());
	}

	@Test
	public void testSendTimeout() {
		TestSource source = new TestSource("testing", 1);
		SimpleChannel channel = new SimpleChannel(1);
		PollingSourceAdapter<String> adapter = new PollingSourceAdapter<String>(source);
		adapter.setChannel(channel);
		adapter.setInitialDelay(10000);
		adapter.setSendTimeout(10);
		adapter.start();
		adapter.processMessages();
		adapter.processMessages();
		adapter.stop();
		Message<?> message1 = channel.receive(1000);
		assertNotNull("message should not be null", message1);
		assertEquals("testing.1", message1.getPayload());
		Message<?> message2 = channel.receive(0);
		assertNull("second message should be null", message2);
		source.resetCounter();
		adapter.start();
		adapter.processMessages();
		Message<?> message3 = channel.receive(100);
		assertNotNull("third message should not be null", message3);
		assertEquals("testing.1", message3.getPayload());
	}

	@Test
	public void testMultipleMessagesPerPoll() {
		TestSource source = new TestSource("testing", 3);
		SimpleChannel channel = new SimpleChannel();
		PollingSourceAdapter<String> adapter = new PollingSourceAdapter<String>(source);
		adapter.setChannel(channel);
		adapter.setInitialDelay(10000);
		adapter.setMaxMessagesPerTask(5);
		adapter.start();
		adapter.processMessages();
		Message<?> message1 = channel.receive(0);
		assertNotNull("message should not be null", message1);
		assertEquals("testing.1", message1.getPayload());
		Message<?> message2 = channel.receive(0);
		assertNotNull("message should not be null", message2);
		assertEquals("testing.2", message2.getPayload());
		Message<?> message3 = channel.receive(0);
		assertNotNull("message should not be null", message3);
		assertEquals("testing.3", message3.getPayload());		
		Message<?> message4 = channel.receive(0);
		assertNull("message should be null", message4);
	}


	private static class TestSource implements PollableSource<String> {

		private String message;

		private int limit;

		private AtomicInteger count = new AtomicInteger();

		public TestSource(String message, int limit) {
			this.message = message;
			this.limit = limit;
		}

		public void resetCounter() {
			this.count.set(0);
		}

		public Message<String> poll() {
			if (count.get() >= limit) {
				return null;
			}
			return new GenericMessage<String>(message + "." + count.incrementAndGet());
		}
	}

}
