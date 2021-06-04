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
package org.springframework.batch.core.launch.support;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.util.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Lucas Ward
 *
 */
public class ScheduledJobParametersFactoryTests {

	ScheduledJobParametersFactory factory;

	DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

	@BeforeEach
	public void setUp() throws Exception {

		factory = new ScheduledJobParametersFactory();
	}

	@Test
 public void testGetParameters() throws Exception {

		String jobKey = "job.key=myKey";
		String scheduleDate = "schedule.date=2008/01/23";
		String vendorId = "vendor.id=33243243";

		String[] args = new String[] { jobKey, scheduleDate, vendorId };

		JobParameters props = factory.getJobParameters(StringUtils.splitArrayElementsIntoProperties(args, "="));
		assertNotNull(props);
		assertEquals("myKey", props.getString("job.key"));
		assertEquals("33243243", props.getString("vendor.id"));
		Date date = dateFormat.parse("01/23/2008");
		assertEquals(date, props.getDate("schedule.date"));
	}

	@Test
 public void testGetProperties() throws Exception {

		JobParameters parameters = new JobParametersBuilder().addDate("schedule.date", dateFormat.parse("01/23/2008"))
				.addString("job.key", "myKey").addString("vendor.id", "33243243").toJobParameters();

		Properties props = factory.getProperties(parameters);
		assertNotNull(props);
		assertEquals("myKey", props.getProperty("job.key"));
		assertEquals("33243243", props.getProperty("vendor.id"));
		assertEquals("2008/01/23", props.getProperty("schedule.date"));
	}

	@Test
 public void testEmptyArgs() {

		JobParameters props = factory.getJobParameters(new Properties());
		assertTrue(props.getParameters().isEmpty());
	}

	@Test
 public void testNullArgs() {
		assertEquals(new JobParameters(), factory.getJobParameters(null));
		assertEquals(new Properties(), factory.getProperties(null));
	}

	@Test
 public void testGetParametersWithDateFormat() throws Exception {

		String[] args = new String[] { "schedule.date=2008/23/01" };

		factory.setDateFormat(new SimpleDateFormat("yyyy/dd/MM"));
		JobParameters props = factory.getJobParameters(StringUtils.splitArrayElementsIntoProperties(args, "="));
		assertNotNull(props);
		Date date = dateFormat.parse("01/23/2008");
		assertEquals(date, props.getDate("schedule.date"));
	}

	@Test
 public void testGetParametersWithBogusDate() throws Exception {

		String[] args = new String[] { "schedule.date=20080123" };

		try {
			factory.getJobParameters(StringUtils.splitArrayElementsIntoProperties(args, "="));
		} catch (IllegalArgumentException e) {
			String message = e.getMessage();
			assertTrue(contains(message, "20080123"), "Message should contain wrong date: " + message);
		}
	}

	private boolean contains(String str, String searchStr) {
		return str.indexOf(searchStr) != -1;
	}
}
