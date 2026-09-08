package com.schedio;

import javax.sql.DataSource;

import jakarta.persistence.EntityManager;

import com.jayway.jsonpath.JsonPath;
import com.schedio.business.Business;
import com.schedio.business.BusinessRepository;
import com.schedio.user.UserAccount;
import com.schedio.user.UserAccountRepository;
import com.schedio.user.UserRole;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import testsupport.auth.AuthenticationTestController;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties =
	"schedio.auth.jwt-secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
@AutoConfigureMockMvc
@Import(AuthenticationTestController.class)
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

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtDecoder jwtDecoder;

	@Autowired
	private MockMvc mockMvc;

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
	void appliesDatabaseMigrations() {
		var currentMigration = flyway.info().current();

		assertThat(currentMigration).isNotNull();
		assertThat(currentMigration.getVersion().getVersion()).isEqualTo("2");
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

		assertThat(jdbcTemplate.queryForList("""
			SELECT table_name
			FROM information_schema.tables
			WHERE table_schema = DATABASE()
			  AND table_name IN ('users', 'refresh_tokens')
			ORDER BY table_name
			""", String.class)).containsExactly("refresh_tokens", "users");
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

	@Test
	@Transactional
	void persistsTenantUserAndEnforcesBusinessScope() {
		var business = businessRepository.saveAndFlush(
			new Business("Authentication Persistence Demo", "Europe/Istanbul")
		);
		var passwordHash = passwordEncoder.encode("secure-password");
		var user = userAccountRepository.saveAndFlush(new UserAccount(
			business.getId(),
			"Owner@Example.com ",
			passwordHash,
			UserRole.BUSINESS_OWNER
		));

		entityManager.clear();

		var persistedUser = userAccountRepository.findByEmail("owner@example.com").orElseThrow();

		assertThat(persistedUser.getId()).isEqualTo(user.getId());
		assertThat(persistedUser.getBusinessId()).isEqualTo(business.getId());
		assertThat(persistedUser.getRole()).isEqualTo(UserRole.BUSINESS_OWNER);
		assertThat(persistedUser.isActive()).isTrue();
		assertThat(passwordEncoder.matches("secure-password", persistedUser.getPasswordHash())).isTrue();
		assertThat(persistedUser.getCreatedAt()).isNotNull();
		assertThat(persistedUser.getUpdatedAt()).isNotNull();

		assertThatThrownBy(() -> jdbcTemplate.update("""
			INSERT INTO users (business_id, email, password_hash, role)
			VALUES (NULL, 'owner-without-business@example.com', 'hash', 'BUSINESS_OWNER')
			""")).isInstanceOf(DataAccessException.class);

		assertThatThrownBy(() -> jdbcTemplate.update("""
			INSERT INTO users (business_id, email, password_hash, role)
			VALUES (?, 'scoped-admin@example.com', 'hash', 'PLATFORM_ADMIN')
			""", business.getId())).isInstanceOf(DataAccessException.class);
	}

	@Test
	void loginIssuesJwtAndStoresOnlyHashedRefreshToken() throws Exception {
		var user = createOwner("login-owner@example.com", "correct-password");

		var loginJson = login("LOGIN-OWNER@EXAMPLE.COM", "correct-password");
		String accessToken = JsonPath.read(loginJson, "$.accessToken");
		String refreshToken = JsonPath.read(loginJson, "$.refreshToken");
		var jwt = jwtDecoder.decode(accessToken);

		assertThat(jwt.getIssuer().toString()).isEqualTo("https://api.schedio.local");
		assertThat(jwt.getSubject()).isEqualTo(user.getId().toString());
		assertThat(jwt.getClaimAsString("email")).isEqualTo("login-owner@example.com");
		assertThat(jwt.getClaimAsStringList("roles")).containsExactly("BUSINESS_OWNER");
		assertThat(jwt.<Long>getClaim("business_id")).isEqualTo(user.getBusinessId());

		var storedHash = jdbcTemplate.queryForObject("""
			SELECT token_hash
			FROM refresh_tokens
			WHERE user_id = ?
			ORDER BY id DESC
			LIMIT 1
			""", String.class, user.getId());
		assertThat(storedHash).hasSize(64).isNotEqualTo(refreshToken);

		mockMvc.perform(get("/test/auth/owner")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(content().string("owner"));
	}

	@Test
	void rejectsInvalidLoginWithoutRevealingAccountDetails() throws Exception {
		createOwner("invalid-login@example.com", "correct-password");

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"invalid-login@example.com","password":"wrong-password"}
					"""))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value("Invalid email or password."))
			.andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
	}

	@Test
	void rotatesRefreshTokensAndRevokesFamilyOnReuse() throws Exception {
		createOwner("rotation-owner@example.com", "correct-password");
		String firstRefreshToken = JsonPath.read(
			login("rotation-owner@example.com", "correct-password"),
			"$.refreshToken"
		);

		var refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(refreshBody(firstRefreshToken)))
			.andExpect(status().isOk())
			.andReturn();
		String secondRefreshToken = JsonPath.read(
			refreshResult.getResponse().getContentAsString(),
			"$.refreshToken"
		);

		assertThat(secondRefreshToken).isNotEqualTo(firstRefreshToken);

		mockMvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(refreshBody(firstRefreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value("Invalid refresh token."));

		mockMvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(refreshBody(secondRefreshToken)))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value("Invalid refresh token."));
	}

	@Test
	void logoutRevokesRefreshTokenAndIsIdempotentForUnknownTokens() throws Exception {
		createOwner("logout-owner@example.com", "correct-password");
		String refreshToken = JsonPath.read(
			login("logout-owner@example.com", "correct-password"),
			"$.refreshToken"
		);

		mockMvc.perform(post("/api/v1/auth/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.content(refreshBody(refreshToken)))
			.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/auth/refresh")
				.contentType(MediaType.APPLICATION_JSON)
				.content(refreshBody(refreshToken)))
			.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/auth/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.content(refreshBody("unknown-refresh-token")))
			.andExpect(status().isNoContent());
	}

	@Test
	void validatesAuthenticationRequests() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"not-an-email","password":""}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Validation failed."))
			.andExpect(jsonPath("$.validationErrors[*].field")
				.value(org.hamcrest.Matchers.containsInAnyOrder("email", "password")));
	}

	@Test
	void rejectsMalformedBearerTokenWithStandardErrorResponse() throws Exception {
		mockMvc.perform(get("/test/auth/owner")
				.header(HttpHeaders.AUTHORIZATION, "Bearer malformed-token"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message").value("Authentication is required."))
			.andExpect(jsonPath("$.path").value("/test/auth/owner"));
	}

	private UserAccount createOwner(String email, String password) {
		var business = businessRepository.saveAndFlush(
			new Business("Auth Test " + email, "Europe/Istanbul")
		);
		return userAccountRepository.saveAndFlush(new UserAccount(
			business.getId(),
			email,
			passwordEncoder.encode(password),
			UserRole.BUSINESS_OWNER
		));
	}

	private String login(String email, String password) throws Exception {
		var result = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"email":"%s","password":"%s"}
					""".formatted(email, password)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.refreshToken").isNotEmpty())
			.andExpect(jsonPath("$.user.role").value("BUSINESS_OWNER"))
			.andReturn();
		return result.getResponse().getContentAsString();
	}

	private String refreshBody(String refreshToken) {
		return """
			{"refreshToken":"%s"}
			""".formatted(refreshToken);
	}
}
