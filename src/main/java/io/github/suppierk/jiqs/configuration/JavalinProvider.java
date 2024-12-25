package io.github.suppierk.jiqs.configuration;

import io.github.suppierk.inject.Provides;
import io.github.suppierk.jiqs.endpoints.HealthcheckEndpointGroup;
import io.github.suppierk.jiqs.endpoints.UsersEndpointGroup;
import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JavalinProvider implements AutoCloseable {
  private final HealthcheckEndpointGroup healthcheckEndpointGroup;
  private final UsersEndpointGroup usersEndpointGroup;

  private final Javalin javalin;

  @Inject
  public JavalinProvider(
      HealthcheckEndpointGroup healthcheckEndpointGroup, UsersEndpointGroup usersEndpointGroup) {
    this.healthcheckEndpointGroup = healthcheckEndpointGroup;
    this.usersEndpointGroup = usersEndpointGroup;

    this.javalin = initializeJavalin();
  }

  @Provides
  @Singleton
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

          // Registering OpenAPI
          config.registerPlugin(new SwaggerPlugin());
          config.registerPlugin(
              new OpenApiPlugin(
                  pluginConfig ->
                      pluginConfig.withDefinitionConfiguration(
                          (version, definition) ->
                              definition.withInfo(info -> info.setTitle("JIQS stack example")))));

          // Registering common endpoints
          config.router.apiBuilder(healthcheckEndpointGroup);

          // Registering business endpoints
          config.router.apiBuilder(usersEndpointGroup);
        });
  }
}
