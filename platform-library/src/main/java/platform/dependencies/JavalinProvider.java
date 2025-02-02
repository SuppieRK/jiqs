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

@Singleton
public final class JavalinProvider implements AutoCloseable {
  private final ApplicationName applicationName;
  private final JavalinJackson javalinJackson;
  private final Set<EndpointGroup> endpoints;

  private final Javalin javalin;

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

  @Provides
  @Singleton
  @SuppressWarnings("unused")
  public Javalin javalin() {
    return javalin;
  }

  @Override
  public void close() {
    javalin.stop();
  }

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
