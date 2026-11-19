package org.enoch.snark;

import org.enoch.snark.db.entity.ModuleEntity;
import org.enoch.snark.db.entity.ThreadEntity;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.DatabaseThreadBeanFactory;
import org.enoch.snark.instance.si.module.ThreadMap;
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
	private DatabaseThreadBeanFactory threadBeanFactory;

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
		String mainTime = jdbcTemplate.queryForObject("""
				SELECT t.time
				FROM threads t JOIN modules m ON m.id = t.module_id
				WHERE m.name = 'global' AND t.name = 'main'
				""", String.class);
		String consumerPause = jdbcTemplate.queryForObject("""
				SELECT t.pause
				FROM threads t JOIN modules m ON m.id = t.module_id
				WHERE m.name = 'global' AND t.name = 'consumer'
				""", String.class);
		Integer commonConfigCount = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM config
				WHERE "key" IN ('time', 'pause', 'debug', 'command_limit', 'conditions', 'name', 'type')
				""", Integer.class);

		assertEquals("on", mainTime);
		assertEquals("0.4S", consumerPause);
		assertEquals(0, commonConfigCount);
	}

}
