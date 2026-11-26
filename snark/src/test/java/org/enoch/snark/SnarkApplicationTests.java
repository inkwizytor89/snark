package org.enoch.snark;

import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ThreadBeanFactory;
import org.enoch.snark.instance.si.module.update.UpdateThread;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class SnarkApplicationTests {

	@Autowired
	private ThreadBeanFactory threadBeanFactory;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void createsSeparateSpringWiredThreadInstances() {
		AbstractThread first = threadBeanFactory.create(UpdateThread.class);
		AbstractThread second = threadBeanFactory.create(UpdateThread.class);
		try {
			assertNotSame(first, second);
		} finally {
			threadBeanFactory.destroy(first);
			threadBeanFactory.destroy(second);
		}
	}

	@Test
	void migratesCommonThreadConfigurationOutOfConfigRows() {
		Integer commonConfigCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM config
				WHERE "key" IN ('time', 'pause', 'debug', 'command_limit', 'conditions', 'name', 'type')
				""", Integer.class);

		assertEquals(0, commonConfigCount);
	}

}
