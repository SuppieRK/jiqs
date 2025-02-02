package platform.plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Defines common properties for plugin. */
public class PlatformExtension {
  /** Provides the name of this extension. */
  public static final String EXTENSION_NAME = "platform";

  /** Provides default PostgreSQL database driver class name. */
  @Nonnull public final String databaseDriverClassName;

  /** Provides default PostgreSQL jOOQ database generator class name. */
  @Nonnull public final String databaseJooqGeneratorClassName;

  /** Provides user-defined name of the destination package for generated database classes. */
  @Nullable public String generatedDatabaseClassesPackage;

  /** Default constructor. */
  public PlatformExtension() {
    this.databaseDriverClassName = "org.postgresql.Driver";
    this.databaseJooqGeneratorClassName = "org.jooq.meta.postgres.PostgresDatabase";
  }

  /**
   * Null-safe getter for user-defined property.
   *
   * @return non-null user-defined name of the destination package for generated database classes
   * @throws IllegalStateException if the name was not set
   */
  public @Nonnull String getGeneratedDatabaseClassesPackage() {
    if (generatedDatabaseClassesPackage == null || generatedDatabaseClassesPackage.isEmpty()) {
      throw new IllegalStateException("Package for generated database classes is not set");
    }

    return generatedDatabaseClassesPackage;
  }
}
