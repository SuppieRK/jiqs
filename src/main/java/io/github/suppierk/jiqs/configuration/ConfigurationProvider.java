package io.github.suppierk.jiqs.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.suppierk.inject.Provides;
import io.github.suppierk.jiqs.configuration.values.ServerPort;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.flywaydb.core.Flyway;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.exceptions.GestaltException;

@Singleton
public class ConfigurationProvider {
  private final Gestalt gestalt;

  @Inject
  public ConfigurationProvider(Gestalt gestalt) {
    this.gestalt = gestalt;
  }

  @Provides
  @Singleton
  public ServerPort serverPort() throws GestaltException {
    return new ServerPort(gestalt.getConfig("server.port", Integer.class));
  }

  @Provides
  @Singleton
  public HikariDataSource dataSource() throws GestaltException {
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
