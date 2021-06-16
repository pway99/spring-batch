/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.batch.item.support;

import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scripting.bsh.BshScriptEvaluator;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * <p>
 * Test cases around {@link org.springframework.batch.item.support.ScriptItemProcessor}.
 * </p>
 *
 * @author Chris Schaefer
 * @since 3.1
 */
public class ScriptItemProcessorTests {
	private static List<String> availableLanguages = new ArrayList<>();

	@BeforeAll
	public static void populateAvailableEngines() {
		List<ScriptEngineFactory> scriptEngineFactories = new ScriptEngineManager().getEngineFactories();

		for (ScriptEngineFactory scriptEngineFactory : scriptEngineFactories) {
			availableLanguages.addAll(scriptEngineFactory.getNames());
		}
	}

	@Test
	public void testJavascriptScriptSourceSimple() throws Exception {
		assumeTrue(languageExists("javascript"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScriptSource("item.toUpperCase();", "javascript");
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	@Test
	public void testJavascriptScriptSourceFunction() throws Exception {
		assumeTrue(languageExists("javascript"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScriptSource("function process(item) { return item.toUpperCase(); } process(item);", "javascript");
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	@Test
	public void testJRubyScriptSourceSimple() throws Exception {
		assumeTrue(languageExists("jruby"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScriptSource("$item.upcase", "jruby");
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	@Test
	public void testJRubyScriptSourceMethod() throws Exception {
		assumeTrue(languageExists("jruby"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScriptSource("def process(item) $item.upcase end \n process($item)", "jruby");
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	@Test
	public void testBeanShellScriptSourceSimple() throws Exception {
		assumeTrue(languageExists("bsh"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScriptSource("item.toUpperCase();", "bsh");
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	@Test
	public void testBeanShellScriptSourceFunction() throws Exception {
		assumeTrue(languageExists("bsh"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScriptSource("String process(String item) { return item.toUpperCase(); } process(item);", "bsh");
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	@Test
	public void testGroovyScriptSourceSimple() throws Exception {
		assumeTrue(languageExists("groovy"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScriptSource("item.toUpperCase();", "groovy");
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	@Test
	public void testGroovyScriptSourceMethod() throws Exception {
		assumeTrue(languageExists("groovy"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScriptSource("def process(item) { return item.toUpperCase() } \n process(item)", "groovy");
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	@Test
	public void testJavascriptScriptSimple() throws Exception {
		assumeTrue(languageExists("javascript"));

		Resource resource = new ClassPathResource("org/springframework/batch/item/support/processor-test-simple.js");

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScript(resource);
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	@Test
	public void testItemBinding() throws Exception {
		assumeTrue(languageExists("javascript"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScriptSource("foo.contains('World');", "javascript");
		scriptItemProcessor.setItemBindingVariableName("foo");

		scriptItemProcessor.afterPropertiesSet();

		assertEquals(true, scriptItemProcessor.process("Hello World"), "Incorrect transformed value");
	}

	@Test
	public void testNoScriptSet() throws Exception {
	 assertThrows(IllegalStateException.class, () -> {
		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.afterPropertiesSet();
	 });
	}

	@Test
	public void testScriptSourceAndScriptResourceSet() throws Exception {
	 assertThrows(IllegalStateException.class, () -> {
		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScriptSource("blah", "blah");
		scriptItemProcessor.setScript(new ClassPathResource("blah"));
		scriptItemProcessor.afterPropertiesSet();
	 });
	}

	@Test
	public void testNoScriptSetWithoutInitBean() throws Exception {
	 assertThrows(IllegalStateException.class, () -> {
		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.process("blah");
	 });
	}

	@Test
	public void testScriptSourceWithNoLanguage() throws Exception {
	 assertThrows(IllegalArgumentException.class, () -> {
		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setScriptSource("function process(item) { return item.toUpperCase(); } process(item);", null);
		scriptItemProcessor.afterPropertiesSet();
	 });
	}

	@Test
	public void testItemBindingNameChange() throws Exception {
		assumeTrue(languageExists("javascript"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<>();
		scriptItemProcessor.setItemBindingVariableName("someOtherVarName");
		scriptItemProcessor.setScriptSource("function process(param) { return param.toUpperCase(); } process(someOtherVarName);", "javascript");
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	@Test
	public void testBshScriptEvaluator() throws Exception {
		assumeTrue(languageExists("bsh"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<String, Object>();
		scriptItemProcessor.setScriptEvaluator(new BshScriptEvaluator());
		scriptItemProcessor.setScriptSource("String process(String item) { return item.toUpperCase(); } process(item);", "bsh");
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	@Test
	public void testGroovyScriptEvaluator() throws Exception {
		assumeTrue(languageExists("groovy"));

		ScriptItemProcessor<String, Object> scriptItemProcessor = new ScriptItemProcessor<String, Object>();
		scriptItemProcessor.setScriptEvaluator(new GroovyScriptEvaluator());
		scriptItemProcessor.setScriptSource("def process(item) { return item.toUpperCase() } \n process(item)", "groovy");
		scriptItemProcessor.afterPropertiesSet();

		assertEquals("SS", scriptItemProcessor.process("ss"), "Incorrect transformed value");
	}

	private boolean languageExists(String engineName) {
		return availableLanguages.contains(engineName);
	}
}
