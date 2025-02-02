package platform.dependencies;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.flywaydb.core.Flyway;
import org.github.gestalt.config.Gestalt;
import platform.primitives.ApplicationName;
import platform.primitives.ServerPort;

@Singleton
public final class ConfigurationProvider {
  private final Gestalt gestalt;

  @Inject
  public ConfigurationProvider(Gestalt gestalt) {
    this.gestalt = gestalt;
  }

  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public ServerPort serverPort() throws Exception {
    return new ServerPort(gestalt.getConfig("port", Integer.class));
  }

  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public ApplicationName applicationName() throws Exception {
    return new ApplicationName(gestalt.getConfig("name", String.class));
  }

  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public HikariDataSource dataSource() throws Exception {
    final var config = new HikariConfig();
    config.setDriverClassName(org.postgresql.Driver.class.getName());
    config.setJdbcUrl(gestalt.getConfig("database.url", String.class));
    config.setUsername(gestalt.getConfig("database.username", String.class));
    config.setPassword(gestalt.getConfig("database.password", String.class));
    final var dataSource = new HikariDataSource(config);

    // Apply database migration
    Flyway.configure().dataSource(dataSource).load().migrate();

    return dataSource;
  }
}
