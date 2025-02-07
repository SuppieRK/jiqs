package platform;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.utils.Memoized;
import io.javalin.Javalin;
import java.io.Closeable;
import platform.dependencies.ConfigurationProvider;
import platform.dependencies.ConfigurationReaderProvider;
import platform.dependencies.JacksonProvider;
import platform.dependencies.JavalinProvider;
import platform.dependencies.JooqProvider;
import platform.endpoints.HealthcheckEndpointGroup;
import platform.primitives.ServerPort;

/**
 * Provides functionality similar to SpringBootApplication, wrapping application internals and
 * preparing a majority of the functionality for use.
 */
public final class ApplicationWrapper implements Closeable {
  private final Injector injector;
  private final Memoized<Javalin> javalin;

  /**
   * Default constructor.
   *
   * @param injector with application dependencies.
   */
  private ApplicationWrapper(Injector injector) {
    this.injector = injector;
    this.javalin = Memoized.memoizedSupplier(() -> injector.get(Javalin.class));
  }

  /**
   * @return wrapper builder
   */
  public static Builder createApplication() {
    return new Builder();
  }

  /**
   * @return alternative builder used to adjust existing dependencies - mainly for testing
   */
  public Modifier modifyDependencies() {
    return new Modifier(this);
  }

  /**
   * @return Javalin web server to implement tests using {@link io.javalin.testtools.JavalinTest}
   */
  public Javalin javalin() {
    return javalin.get();
  }

  /** Starts Javalin web server. */
  public void start() {
    javalin().start(injector.get(ServerPort.class).get());
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    injector.close();
  }

  /**
   * Primary wrapper builder which registers any platform dependencies upfront and allows consumers
   * to add their dependencies as well.
   */
  public static class Builder {
    private final Injector.Builder injectorBuilder;

    /** Default constructor. */
    private Builder() {
      this.injectorBuilder =
          Injector.injector()
              // Registering default platform dependencies
              .add(ConfigurationReaderProvider.class)
              .add(ConfigurationProvider.class)
              .add(JooqProvider.class)
              .add(JacksonProvider.class)
              .add(HealthcheckEndpointGroup.class);
    }

    /** Delegates to {@link Injector.Builder#add(Object, Object...)} */
    public Builder add(Object object, Object... additionalObjects) {
      injectorBuilder.add(object, additionalObjects);
      return this;
    }

    /** Delegates to {@link Injector.Builder#add(Class, Class[])} */
    public Builder add(Class<?> clazz, Class<?>... additionalClasses) {
      injectorBuilder.add(clazz, additionalClasses);
      return this;
    }

    /**
     * @return {@link ApplicationWrapper} instance
     */
    public ApplicationWrapper build() {
      return new ApplicationWrapper(injectorBuilder.add(JavalinProvider.class).build());
    }
  }

  /**
   * Secondary wrapper builder, which copies all existing dependencies for the sake to provide
   * customers with the ability to replace some dependencies for testing.
   */
  public static class Modifier {
    private final Injector.CopyBuilder builder;

    /**
     * Default constructor.
     *
     * @param wrapper to copy dependencies from
     */
    private Modifier(ApplicationWrapper wrapper) {
      this.builder = wrapper.injector.copy();
    }

    /** Delegates to {@link Injector.CopyBuilder#replace(Class, Class)} */
    public <F, T extends F> Modifier replace(Class<F> from, Class<T> to) {
      builder.replace(from, to);
      return this;
    }

    /** Delegates to {@link Injector.CopyBuilder#replace(Object, Object)} */
    public <F, T extends F> Modifier replace(F from, T to) {
      builder.replace(from, to);
      return this;
    }

    /**
     * @return {@link ApplicationWrapper} instance
     */
    public ApplicationWrapper build() {
      return new ApplicationWrapper(builder.build());
    }
  }
}
