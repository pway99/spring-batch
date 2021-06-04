/*
 * Copyright 2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.batch.core.test.ldif.builder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ldif.LdifReader;
import org.springframework.batch.item.ldif.RecordCallbackHandler;
import org.springframework.batch.item.ldif.builder.LdifReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapAttributes;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Glenn Renfro
 */
@ExtendWith(SpringExtension.class)
public class LdifReaderBuilderTests {

	@Autowired
	private ApplicationContext context;

	private LdifReader ldifReader;

	private String callbackAttributeName;

	@AfterEach
	public void tearDown() {
		this.callbackAttributeName = null;
		if (this.ldifReader != null) {
			this.ldifReader.close();
		}
	}

	@Test
	public void testSkipRecord() throws Exception {
		this.ldifReader = new LdifReaderBuilder().recordsToSkip(1).resource(context.getResource("classpath:/test.ldif"))
				.name("foo").build();
		LdapAttributes ldapAttributes = firstRead();
		assertEquals(
		"cn=Bjorn Jensen, ou=Accounting, dc=airius, dc=com", ldapAttributes.getName().toString(), "The attribute name for the second record did not match expected result");
	}

	@Test
	public void testBasicRead() throws Exception {
		this.ldifReader = new LdifReaderBuilder().resource(context.getResource("classpath:/test.ldif")).name("foo").build();
		LdapAttributes ldapAttributes = firstRead();
		assertEquals(
		"cn=Barbara Jensen, ou=Product Development, dc=airius, dc=com", ldapAttributes.getName().toString(), "The attribute name for the first record did not match expected result");
	}

	@Test
	public void testCurrentItemCount() throws Exception {
		this.ldifReader = new LdifReaderBuilder().currentItemCount(3)
				.resource(context.getResource("classpath:/test.ldif")).name("foo").build();
		LdapAttributes ldapAttributes = firstRead();
		assertEquals(
		"cn=Gern Jensen, ou=Product Testing, dc=airius, dc=com", ldapAttributes.getName().toString(), "The attribute name for the third record did not match expected result");
	}

	@Test
	public void testMaxItemCount() throws Exception {
		this.ldifReader = new LdifReaderBuilder().maxItemCount(1).resource(context.getResource("classpath:/test.ldif"))
				.name("foo").build();
		LdapAttributes ldapAttributes = firstRead();
		assertEquals(
		"cn=Barbara Jensen, ou=Product Development, dc=airius, dc=com", ldapAttributes.getName().toString(), "The attribute name for the first record did not match expected result");
		ldapAttributes = this.ldifReader.read();
		assertNull(ldapAttributes, "The second read should have returned null");
	}

	@Test
	public void testSkipRecordCallback() throws Exception {
		this.ldifReader = new LdifReaderBuilder().recordsToSkip(1).skippedRecordsCallback(new TestCallBackHandler())
				.resource(context.getResource("classpath:/test.ldif")).name("foo").build();
		firstRead();
		assertEquals(
		"cn=Barbara Jensen, ou=Product Development, dc=airius, dc=com", this.callbackAttributeName, "The attribute name from the callback handler did not match the  expected result");
	}

	@Test
	public void testSaveState() throws Exception {
		this.ldifReader = new LdifReaderBuilder().resource(context.getResource("classpath:/test.ldif")).name("foo").build();
		ExecutionContext executionContext = new ExecutionContext();
		firstRead(executionContext);
		this.ldifReader.update(executionContext);
		assertEquals(1,
		executionContext.getInt("foo.read.count"), "foo.read.count did not have the expected result");
	}

	@Test
	public void testSaveStateDisabled() throws Exception {
		this.ldifReader = new LdifReaderBuilder().saveState(false).resource(context.getResource("classpath:/test.ldif"))
				.build();
		ExecutionContext executionContext = new ExecutionContext();
		firstRead(executionContext);
		this.ldifReader.update(executionContext);
		assertEquals(0, executionContext.size(), "ExecutionContext should have been empty");
	}

	@Test
	public void testStrict() {
		// Test that strict when enabled will throw an exception.
		try {
			this.ldifReader = new LdifReaderBuilder().resource(context.getResource("classpath:/teadsfst.ldif")).name("foo").build();
			this.ldifReader.open(new ExecutionContext());
			fail("IllegalStateException should have been thrown, because strict was set to true");
		}
		catch (ItemStreamException ise) {
			assertEquals(
			"Failed to initialize the reader", ise.getMessage(), "IllegalStateException message did not match the expected result.");
		}
		// Test that strict when disabled will still allow the ldap resource to be opened.
		this.ldifReader = new LdifReaderBuilder().strict(false)
				.resource(context.getResource("classpath:/teadsfst.ldif")).name("foo").build();
		this.ldifReader.open(new ExecutionContext());
	}

	private LdapAttributes firstRead() throws Exception {
		return firstRead(new ExecutionContext());
	}

	private LdapAttributes firstRead(ExecutionContext executionContext) throws Exception {
		this.ldifReader.open(executionContext);
		return this.ldifReader.read();
	}

	@Configuration
	public static class LdifConfiguration {

	}

	public class TestCallBackHandler implements RecordCallbackHandler {

		@Override
		public void handleRecord(LdapAttributes attributes) {
			callbackAttributeName = attributes.getName().toString();
		}
	}
}
