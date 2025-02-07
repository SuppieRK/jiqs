package platform.dependencies;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.flywaydb.core.Flyway;
import platform.contracts.ConfigurationReader;
import platform.primitives.ApplicationName;
import platform.primitives.ServerPort;

/**
 * Initializes common configuration used by other components.
 *
 * <p>This class is {@code final} which prohibits its replacement in the {@link
 * io.github.suppierk.inject.Injector} exposed to the consumers.
 */
@Singleton
public final class ConfigurationProvider {
  private final ConfigurationReader reader;

  /**
   * Default constructor.
   *
   * @param reader to getch configuration with
   */
  @Inject
  public ConfigurationProvider(ConfigurationReader reader) {
    this.reader = reader;
  }

  /**
   * @return a {@link ServerPort} instance to start the app with
   */
  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public ServerPort serverPort() {
    return new ServerPort(reader.read("port", Integer.class));
  }

  /**
   * @return an {@link ApplicationName} to set in the Swagger API
   */
  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public ApplicationName applicationName() {
    return new ApplicationName(reader.read("name", String.class));
  }

  /**
   * Creates database connection and applies Flyway migrations.
   *
   * <p><b>NOTE</b>: this has to be {@link HikariDataSource} and not {@link javax.sql.DataSource},
   * because {@link HikariDataSource} is {@link java.io.Closeable} - which will allow {@link
   * io.github.suppierk.inject.Injector} to automatically release its resources.
   *
   * @return a {@link HikariDataSource} to query the database
   */
  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public HikariDataSource dataSource() {
    final var config = new HikariConfig();
    config.setDriverClassName(org.postgresql.Driver.class.getName());
    config.setJdbcUrl(reader.read("database.url", String.class));
    config.setUsername(reader.read("database.username", String.class));
    config.setPassword(reader.read("database.password", String.class));
    final var dataSource = new HikariDataSource(config);

    // Apply database migration
    Flyway.configure().dataSource(dataSource).load().migrate();

    return dataSource;
  }
}
