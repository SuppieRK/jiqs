package platform.dependencies;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.json.JavalinJackson;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Set;
import java.util.stream.Collectors;
import platform.primitives.ApplicationName;

/**
 * Everything necessary for the web server in the app.
 *
 * <p>This class is {@code final} which prohibits its replacement in the {@link
 * io.github.suppierk.inject.Injector} exposed to the consumers.
 */
@Singleton
public final class JavalinProvider implements AutoCloseable {
  private final ApplicationName applicationName;
  private final JavalinJackson javalinJackson;
  private final Set<EndpointGroup> endpoints;

  private final Javalin javalin;

  /**
   * Default constructor.
   *
   * <p>In this particular case, unlike {@link JacksonProvider} we need internal state to be able to
   * close web server.
   *
   * @param applicationName to set in Swagger API
   * @param javalinJackson to use during requests / responses handling
   * @param injector to find {@link EndpointGroup}s to register in the web server
   */
  @Inject
  public JavalinProvider(
      ApplicationName applicationName, JavalinJackson javalinJackson, Injector injector) {
    this.applicationName = applicationName;
    this.javalinJackson = javalinJackson;

    this.endpoints =
        injector.findAll(EndpointGroup.class).stream()
            .map(injector::get)
            .collect(Collectors.toUnmodifiableSet());

    this.javalin = initializeJavalin();
  }

  /**
   * @return the web server instance to start and test
   */
  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public Javalin javalin() {
    return javalin;
  }

  /** This will allow {@link Injector} to automatically shut down the web server */
  @Override
  public void close() {
    javalin.stop();
  }

  /**
   * Abstracting away the logic to instantiate {@link Javalin} web server.
   *
   * @return fully initialized {@link Javalin} instance
   */
  private Javalin initializeJavalin() {
    return Javalin.create(
        config -> {
          // Disable banner in logs
          config.showJavalinBanner = false;

          config.useVirtualThreads = true;

          config.jsonMapper(javalinJackson);

          // Registering OpenAPI
          config.registerPlugin(new SwaggerPlugin());
          config.registerPlugin(
              new OpenApiPlugin(
                  pluginConfig ->
                      pluginConfig.withDefinitionConfiguration(
                          (version, definition) ->
                              definition.withInfo(info -> info.setTitle(applicationName.get())))));

          // Registering endpoints
          for (EndpointGroup endpoint : endpoints) {
            config.router.apiBuilder(endpoint);
          }
        });
  }
}
