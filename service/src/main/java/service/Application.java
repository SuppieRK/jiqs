package service;

import platform.ApplicationWrapper;
import service.endpoints.UsersEndpointGroup;
import service.services.UsersService;

public class Application {
  final ApplicationWrapper wrapper;

  public Application() {
    this.wrapper =
        ApplicationWrapper.createApplication()
            .add(UsersService.class, UsersEndpointGroup.class)
            .build();
  }

  public static void main(String[] args) {
    final var application = new Application();

    // Add shutdown hook for graceful shutdown
    Runtime.getRuntime().addShutdownHook(new Thread(application.wrapper::close));

    // Start the web server
    application.wrapper.start();
  }
}
