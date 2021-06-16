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
package org.springframework.batch.core.repository.dao;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dave Syer
 *
 */
public class JdbcJobDaoQueryTests {

	JdbcJobExecutionDao jobExecutionDao;

	List<String> list = new ArrayList<>();

	/*
 	 * (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
	@BeforeEach
	public void setUp() throws Exception {

		jobExecutionDao = new JdbcJobExecutionDao();
		jobExecutionDao.setJobExecutionIncrementer(new DataFieldMaxValueIncrementer() {

			@Override
			public int nextIntValue() throws DataAccessException {
				return 0;
			}

			@Override
			public long nextLongValue() throws DataAccessException {
				return 0;
			}

			@Override
			public String nextStringValue() throws DataAccessException {
				return "bar";
			}

		});
	}

	@Test
 public void testTablePrefix() throws Exception {
		jobExecutionDao.setTablePrefix("FOO_");
		jobExecutionDao.setJdbcTemplate(new JdbcTemplate() {
			@Override
			public int update(String sql, Object[] args, int[] argTypes) throws DataAccessException {
				list.add(sql);
				return 1;
			}
		});
		JobExecution jobExecution = new JobExecution(new JobInstance(new Long(11), "testJob"), new JobParameters());

		jobExecutionDao.saveJobExecution(jobExecution);
		assertEquals(1, list.size());
		String query = list.get(0);
		assertTrue(query.indexOf("FOO_") >= 0, "Query did not contain FOO_:" + query);
	}

}
