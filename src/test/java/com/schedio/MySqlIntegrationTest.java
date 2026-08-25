package com.schedio;

import javax.sql.DataSource;

import jakarta.persistence.EntityManager;

import com.schedio.business.Business;
import com.schedio.business.BusinessRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

	@Autowired
	private BusinessRepository businessRepository;

	@Autowired
	private EntityManager entityManager;

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

	@Test
	void appliesInitialBusinessMigration() {
		var currentMigration = flyway.info().current();

		assertThat(currentMigration).isNotNull();
		assertThat(currentMigration.getVersion().getVersion()).isEqualTo("1");
		assertThat(jdbcTemplate.queryForList("""
			SELECT column_name
			FROM information_schema.columns
			WHERE table_schema = DATABASE()
			  AND table_name = 'businesses'
			ORDER BY ordinal_position
			""", String.class)).containsExactly(
			"id",
			"name",
			"timezone",
			"created_at",
			"updated_at"
		);
	}

	@Test
	void businessesTableEnforcesRequiredValuesAndCreatesTimestamps() {
		jdbcTemplate.update(
			"INSERT INTO businesses (name, timezone) VALUES (?, ?)",
			"Schedio Demo",
			"Europe/Istanbul"
		);

		assertThat(jdbcTemplate.queryForObject("""
			SELECT COUNT(*)
			FROM businesses
			WHERE name = ?
			  AND timezone = ?
			  AND created_at IS NOT NULL
			  AND updated_at IS NOT NULL
			""", Integer.class, "Schedio Demo", "Europe/Istanbul")).isEqualTo(1);

		assertThatThrownBy(() -> jdbcTemplate.update(
			"INSERT INTO businesses (name, timezone) VALUES (?, ?)",
			" ",
			"Europe/Istanbul"
		)).isInstanceOf(DataAccessException.class);

		assertThatThrownBy(() -> jdbcTemplate.update(
			"INSERT INTO businesses (name, timezone) VALUES (?, ?)",
			"Schedio Demo",
			" "
		)).isInstanceOf(DataAccessException.class);
	}

	@Test
	@Transactional
	void persistsAndLoadsBusinessThroughRepository() {
		var business = businessRepository.saveAndFlush(
			new Business("Schedio Repository Demo", "Europe/Istanbul")
		);

		entityManager.clear();

		var persistedBusiness = businessRepository.findById(business.getId()).orElseThrow();

		assertThat(persistedBusiness.getName()).isEqualTo("Schedio Repository Demo");
		assertThat(persistedBusiness.getTimezone()).isEqualTo("Europe/Istanbul");
		assertThat(persistedBusiness.getCreatedAt()).isNotNull();
		assertThat(persistedBusiness.getUpdatedAt()).isNotNull();
	}
}
