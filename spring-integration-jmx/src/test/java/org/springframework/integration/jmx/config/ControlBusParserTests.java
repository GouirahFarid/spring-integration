/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.integration.jmx.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Mark Fisher
 * @author Gary Russell
 * @author Artem Bilan
 *
 * @since 2.0
 */
@RunWith(SpringRunner.class)
@DirtiesContext
public class ControlBusParserTests {

	@Autowired
	private ApplicationContext context;

	@Test
	public void testControlMessageToChannelMetrics() throws InterruptedException {
		MessageChannel control = this.context.getBean("controlChannel", MessageChannel.class);
		MessagingTemplate messagingTemplate = new MessagingTemplate();
		Object value = messagingTemplate.convertSendAndReceive(control,
				"@integrationMbeanExporter.getChannelSendRate('testChannel').count", Object.class);
		assertEquals(0, value);
		MBeanExporter exporter = this.context.getBean(MBeanExporter.class);
		exporter.destroy();
	}

}
