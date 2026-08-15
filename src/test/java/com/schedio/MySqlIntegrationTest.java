package com.schedio;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class MySqlIntegrationTest {

	@Container
	@ServiceConnection
	static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4.11")
		.withDatabaseName("schedio")
		.withUsername("schedio")
		.withPassword("schedio_test_password");

	@Autowired
	private DataSource dataSource;

	@Autowired
	private Flyway flyway;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void connectsToMySqlAndInitializesFlyway() throws Exception {
		try (var connection = dataSource.getConnection()) {
			assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("MySQL");
		}

		assertThat(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
		assertThat(flyway.info()).isNotNull();
		assertThat(jdbcTemplate.queryForObject("""
			SELECT COUNT(*)
			FROM information_schema.tables
			WHERE table_schema = DATABASE()
			  AND table_name = 'flyway_schema_history'
			""", Integer.class)).isEqualTo(1);
	}
}
