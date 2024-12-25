package io.github.suppierk.jiqs;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.jiqs.configuration.ConfigurationProvider;
import io.github.suppierk.jiqs.configuration.GestaltProvider;
import io.github.suppierk.jiqs.configuration.JavalinProvider;
import io.github.suppierk.jiqs.configuration.JooqProvider;
import io.github.suppierk.jiqs.configuration.values.ServerPort;
import io.github.suppierk.jiqs.endpoints.HealthcheckEndpointGroup;
import io.github.suppierk.jiqs.endpoints.UsersEndpointGroup;
import io.github.suppierk.jiqs.services.UsersService;
import io.javalin.Javalin;

public final class Application {
  final Injector injector;

  public Application() {
    this.injector =
        Injector.injector()
            .add(GestaltProvider.class, ConfigurationProvider.class)
            .add(JooqProvider.class)
            .add(JavalinProvider.class)
            .add(HealthcheckEndpointGroup.class)
            .add(UsersEndpointGroup.class, UsersService.class)
            .build();
  }

  public static void main(String[] args) {
    final var app = new Application();
    final var javalin = app.injector.get(Javalin.class);

    // Add shutdown hook for graceful shutdown
    Runtime.getRuntime().addShutdownHook(new Thread(app.injector::close));

    // Start the web server
    javalin.start(app.injector.get(ServerPort.class).get());
  }
}
