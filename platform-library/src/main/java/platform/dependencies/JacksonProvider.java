package platform.dependencies;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.suppierk.inject.Provides;
import io.javalin.json.JavalinJackson;
import jakarta.inject.Singleton;

/**
 * Provides singletons to manage {@link ObjectMapper} instances.
 *
 * <p>This class is {@code final} which prohibits its replacement in the {@link
 * io.github.suppierk.inject.Injector} exposed to the consumers.
 */
@Singleton
public final class JacksonProvider {
  /**
   * A good place to apply some common configuration, for example, date formatting.
   *
   * @return a {@link ObjectMapper} instance
   */
  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  /**
   * @param objectMapper from {@link #objectMapper()} autowired by {@link
   *     io.github.suppierk.inject.Injector}
   * @return a {@link JavalinJackson} instance to make sure Javalin and the rest of the codebase use
   *     the same {@link ObjectMapper}
   */
  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public JavalinJackson javalinJackson(ObjectMapper objectMapper) {
    return new JavalinJackson(objectMapper, true);
  }
}
