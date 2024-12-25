package io.github.suppierk.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.time.Duration;
import org.flywaydb.core.Flyway;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;

/** Abstract functionality which other tests can extend to verify database related operations. */
public abstract class AbstractDatabaseTest {
  private static final PostgreSQLContainer<?> POSTGRESQL =
      new PostgreSQLContainer<>("postgres:16-alpine");

  private static HikariDataSource dataSource;
  private static DSLContext dsl;

  @BeforeAll
  static void beforeAll() {
    // Kick off the database container
    POSTGRESQL.setCommand("postgres", "-c", "fsync=off", "-c", "log_statement=all");
    POSTGRESQL.start();

    // Wait until the database container is up
    Awaitility.await()
        .pollDelay(Duration.ofMillis(250))
        .pollInterval(Duration.ofMillis(250))
        .atMost(Duration.ofSeconds(10))
        .until(POSTGRESQL::isRunning);

    // Run migration against container
    Flyway.configure()
        .dataSource(POSTGRESQL.getJdbcUrl(), POSTGRESQL.getUsername(), POSTGRESQL.getPassword())
        .locations("classpath:db/migration")
        .load()
        .migrate();

    // Create database connection pool for tests
    HikariConfig hikariReadWriteConfig = new HikariConfig();
    hikariReadWriteConfig.setDriverClassName(POSTGRESQL.getDriverClassName());
    hikariReadWriteConfig.setJdbcUrl(POSTGRESQL.getJdbcUrl());
    hikariReadWriteConfig.setUsername(POSTGRESQL.getUsername());
    hikariReadWriteConfig.setPassword(POSTGRESQL.getPassword());
    dataSource = new HikariDataSource(hikariReadWriteConfig);

    // Create DSL context for direct database querying in tests
    dsl = DSL.using(dataSource, SQLDialect.POSTGRES);
  }

  @AfterAll
  static void afterAll() {
    dataSource.close();
    POSTGRESQL.stop();
  }

  protected static String getDriverClassName() {
    return POSTGRESQL.getDriverClassName();
  }

  protected static String getJdbcUrl() {
    return POSTGRESQL.getJdbcUrl();
  }

  protected static String getDatabaseName() {
    return POSTGRESQL.getDatabaseName();
  }

  protected static String getUsername() {
    return POSTGRESQL.getUsername();
  }

  protected static String getPassword() {
    return POSTGRESQL.getPassword();
  }

  protected static DSLContext testDsl() {
    return dsl;
  }
}
