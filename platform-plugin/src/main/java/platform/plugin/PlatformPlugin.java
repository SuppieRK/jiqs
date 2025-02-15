package platform.plugin;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;
import io.github.suppierk.codegen.GeneratorPlugin;
import java.util.Objects;
import nu.studer.gradle.jooq.JooqEdition;
import nu.studer.gradle.jooq.JooqExtension;
import nu.studer.gradle.jooq.JooqPlugin;
import org.flywaydb.gradle.FlywayExtension;
import org.flywaydb.gradle.FlywayPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;

/** Perform registration of necessary parts. */
@SuppressWarnings("unused")
public class PlatformPlugin implements Plugin<Project> {
  private static final String PLATFORM_LIBRARY_VERSION = "0.0.1";
  private static final String JOOQ_VERSION = "3.19.16";
  private static final String POSTGRESQL_VERSION = "42.7.4";

  /** Default constructor. */
  public PlatformPlugin() {}

  @Override
  public void apply(Project root) {
    // Registering external plugin dependencies
    root.getPluginManager().apply(JavaBasePlugin.class);
    root.getPluginManager().apply(JavaPlugin.class);
    root.getPluginManager().apply(SpotlessPlugin.class);
    root.getPluginManager().apply(FlywayPlugin.class);
    root.getPluginManager().apply(JooqPlugin.class);

    // Extensions
    final var extension =
        root.getExtensions().create(PlatformExtension.EXTENSION_NAME, PlatformExtension.class);

    final var spotlessExtension =
        Objects.requireNonNull(
            root.getExtensions().getByType(SpotlessExtension.class),
            "Cannot retrieve Spotless extension");

    final var flywayExtension =
        Objects.requireNonNull(
            root.getExtensions().getByType(FlywayExtension.class),
            "Cannot retrieve Flyway extension");

    final var jooqExtension =
        Objects.requireNonNull(
            root.getExtensions().getByType(JooqExtension.class), "Cannot retrieve jOOQ extension");

    // Setting extension properties
    root.afterEvaluate(
        project -> {
          // Setting Spotless properties
          spotlessExtension.java(
              javaExtension -> {
                javaExtension.target("**/*.java");

                // Aligns with Intellij IDEA default settings
                javaExtension.toggleOffOn("@formatter:off", "@formatter:on");

                javaExtension.googleJavaFormat();
              });

          spotlessExtension.groovyGradle(
              groovyGradleExtension -> {
                groovyGradleExtension.target("**/*.groovy");

                groovyGradleExtension.greclipse();
              });

          // Setting a correct database driver for the Flyway
          flywayExtension.driver = extension.databaseDriverClassName;

          // Registering additional jOOQ dependencies implicitly
          root.getDependencies()
              .add(
                  "implementation",
                  "io.github.suppierk:platform-library:%s".formatted(PLATFORM_LIBRARY_VERSION));

          // Registering additional database-specific dependency for jOOQ class generator
          root.getDependencies()
              .add("jooqGenerator", "org.postgresql:postgresql:%s".formatted(POSTGRESQL_VERSION));

          // Setting correct properties for jOOQ
          jooqExtension.getVersion().set(JOOQ_VERSION);
          jooqExtension.getEdition().set(JooqEdition.OSS);

          final var jooqGeneratorConfiguration = jooqExtension.getConfigurations().create("main");
          jooqGeneratorConfiguration
              .getJooqConfiguration()
              .getGenerator()
              .getDatabase()
              .setName(extension.databaseJooqGeneratorClassName);
          jooqGeneratorConfiguration
              .getJooqConfiguration()
              .getGenerator()
              .getGenerate()
              .setFluentSetters(true);
          jooqGeneratorConfiguration
              .getJooqConfiguration()
              .getGenerator()
              .getTarget()
              .setPackageName(extension.getGeneratedDatabaseClassesPackage());

          project.getPluginManager().apply(GeneratorPlugin.class);
        });
  }
}
