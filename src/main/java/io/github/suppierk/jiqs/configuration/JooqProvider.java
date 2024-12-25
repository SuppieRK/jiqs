package io.github.suppierk.jiqs.configuration;

import com.zaxxer.hikari.HikariDataSource;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

@Singleton
public class JooqProvider {
  private final HikariDataSource dataSource;

  @Inject
  public JooqProvider(HikariDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Provides
  @Singleton
  public DSLContext dsl() {
    return DSL.using(dataSource, SQLDialect.POSTGRES);
  }
}
