package platform.dependencies;

import io.github.suppierk.inject.Provides;
import jakarta.inject.Singleton;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ClassPathConfigSourceBuilder;
import platform.contracts.ConfigurationReader;

/**
 * Factory to create an instance of {@link ConfigurationReader}.
 *
 * <p>Here we are using {@link org.github.gestalt.config.Gestalt} and this can be replaced as
 * needed.
 *
 * <p>This class is <b>NOT</b> {@code final} to allow its replacement in the {@link
 * io.github.suppierk.inject.Injector}, for example, during tests to be able to leverage
 * TestContainers as can be seen in {@link platform.test.AbstractApplicationTest}.
 */
@Singleton
public class ConfigurationReaderProvider {
  protected static final String CONFIGURATION_FILE = "application.yml";

  /**
   * Initialize new {@link ConfigurationReader} instance.
   *
   * @return {@link org.github.gestalt.config.Gestalt} as {@link ConfigurationReader}
   * @throws Exception if {@link org.github.gestalt.config.Gestalt} cannot be instantiated
   */
  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public ConfigurationReader configurationReader() throws Exception {
    final var gestalt =
        new GestaltBuilder()
            .addSource(
                ClassPathConfigSourceBuilder.builder().setResource(CONFIGURATION_FILE).build())
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
