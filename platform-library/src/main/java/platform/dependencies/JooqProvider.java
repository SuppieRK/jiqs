package platform.dependencies;

import com.zaxxer.hikari.HikariDataSource;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

/**
 * Everything necessary to query the database in the app.
 *
 * <p>This class is {@code final} which prohibits its replacement in the {@link
 * io.github.suppierk.inject.Injector} exposed to the consumers.
 */
@Singleton
public final class JooqProvider {
  /**
   * Creates jOOQ {@link DSLContext} to be able to leverage SQL-like code to work with the database.
   *
   * <p><b>NOTE</b>: this implementation is hardcoded to PostgreSQL, and there is freedom of not
   * only configuration, but also creation of additional connections as needed.
   *
   * @param dataSource to use to connect to the database
   * @return jOOQ {@link DSLContext}
   */
  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public DSLContext dsl(HikariDataSource dataSource) {
    return DSL.using(dataSource, SQLDialect.POSTGRES);
  }
}
