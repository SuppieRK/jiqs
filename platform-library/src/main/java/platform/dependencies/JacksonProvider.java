package platform.dependencies;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.suppierk.inject.Provides;
import io.javalin.json.JavalinJackson;
import jakarta.inject.Singleton;

@Singleton
public final class JacksonProvider {
  private final ObjectMapper mapper;

  public JacksonProvider() {
    this.mapper = new ObjectMapper();
  }

  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public ObjectMapper objectMapper() {
    return mapper;
  }

  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public JavalinJackson javalinJackson() {
    return new JavalinJackson(mapper, true);
  }
}
