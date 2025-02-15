package service;

import platform.ApplicationWrapper;
import service.endpoints.UsersEndpointGroup;
import service.services.UsersService;

/** Starting point. */
public class Application {
  /**
   * In this simple example we leave this field visibility at package-default level for easier
   * access in tests.
   *
   * <p>It could be a good idea to expose this field via public getter - just make sure that your
   * implementation of {@link ApplicationWrapper} is immutable.
   */
  final ApplicationWrapper wrapper;

  /**
   * Default constructor.
   *
   * <p>Also registers all necessary dependencies.
   */
  public Application() {
    this.wrapper =
        ApplicationWrapper.createApplication()
            .add(UsersService.class, UsersEndpointGroup.class)
            .build();
  }

  /**
   * Default main method.
   *
   * @param args to pass to the app
   */
  public static void main(String[] args) {
    final var application = new Application();

    // Add shutdown hook for graceful shutdown
    Runtime.getRuntime().addShutdownHook(new Thread(application.wrapper::close));

    // Start the web server
    application.wrapper.start();
  }
}
