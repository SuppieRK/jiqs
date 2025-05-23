package platform.test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.inject.Singleton;
import java.time.Duration;
import org.flywaydb.core.Flyway;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ClassPathConfigSourceBuilder;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import platform.ApplicationWrapper;
import platform.contracts.ConfigurationReader;
import platform.dependencies.ConfigurationReaderProvider;

/** Wraps PostgreSQL TestContainer to verify the application against real PostgreSQL instance. */
public abstract class AbstractApplicationTest {
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

  /**
   * Re-wraps initial {@link ApplicationWrapper} making sure that it uses our TestContainers
   * database.
   *
   * @param applicationWrapper to modify
   * @return initial {@link ApplicationWrapper} with replaced database connection properties
   */
  protected static ApplicationWrapper useTestDatabase(ApplicationWrapper applicationWrapper) {
    return applicationWrapper
        .modifyDependencies()
        .replace(ConfigurationReaderProvider.class, TestConfigurationReaderProvider.class)
        .build();
  }

  /**
   * @return jOOQ {@link DSLContext} to be used in tests independently from {@link
   *     ApplicationWrapper} context to be able to perform the necessary operations like truncating
   *     tables.
   */
  protected static DSLContext testDsl() {
    return dsl;
  }

  /**
   * An override of the {@link ConfigurationReaderProvider} for tests, enabling us to create {@link
   * #useTestDatabase(ApplicationWrapper)} functionality.
   *
   * <p><b>NOTE</b>: we still have to load an initial configuration file here to pull in other
   * configurations, except the database.
   */
  @Singleton
  static class TestConfigurationReaderProvider extends ConfigurationReaderProvider {
    @Override
    public ConfigurationReader configurationReader() throws Exception {
      final ConfigSourcePackage configSourcePackage =
          MapConfigSourceBuilder.builder()
              .addCustomConfig("database.url", POSTGRESQL.getJdbcUrl())
              .addCustomConfig("database.username", POSTGRESQL.getUsername())
              .addCustomConfig("database.password", POSTGRESQL.getPassword())
              .build();

      final Gestalt gestalt =
          new GestaltBuilder()
              .addSource(
                  ClassPathConfigSourceBuilder.builder().setResource(CONFIGURATION_FILE).build())
              .addSource(configSourcePackage)
              .build();

      gestalt.loadConfigs();

      return new ConfigurationReader() {
        @Override
        public <T> T read(String path, Class<T> clazz) {
          try {
            return gestalt.getConfig(path, clazz);
          } catch (GestaltException e) {
            throw new IllegalStateException(e);
          }
        }
      };
    }
  }
}
