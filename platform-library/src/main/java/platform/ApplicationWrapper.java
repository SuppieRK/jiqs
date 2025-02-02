package platform;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.utils.Memoized;
import io.javalin.Javalin;
import java.io.Closeable;
import platform.dependencies.ConfigurationProvider;
import platform.dependencies.GestaltProvider;
import platform.dependencies.JacksonProvider;
import platform.dependencies.JavalinProvider;
import platform.dependencies.JooqProvider;
import platform.endpoints.HealthcheckEndpointGroup;
import platform.primitives.ServerPort;

public final class ApplicationWrapper implements Closeable {
  private final Injector injector;
  private final Memoized<Javalin> javalin;

  private ApplicationWrapper(Injector injector) {
    this.injector = injector;
    this.javalin = Memoized.memoizedSupplier(() -> injector.get(Javalin.class));
  }

  public static Builder createApplication() {
    return new Builder();
  }

  public Modifier modifyDependencies() {
    return new Modifier(this);
  }

  public Javalin javalin() {
    return javalin.get();
  }

  public void start() {
    javalin().start(injector.get(ServerPort.class).get());
  }

  @Override
  public void close() {
    injector.close();
  }

  public static class Builder {
    private final Injector.Builder injectorBuilder;

    private Builder() {
      this.injectorBuilder =
          Injector.injector()
              // Registering default platform dependencies
              .add(GestaltProvider.class)
              .add(ConfigurationProvider.class)
              .add(JooqProvider.class)
              .add(JacksonProvider.class)
              .add(HealthcheckEndpointGroup.class);
    }

    public Builder add(Object object, Object... additionalObjects) {
      injectorBuilder.add(object, additionalObjects);
      return this;
    }

    public Builder add(Class<?> clazz, Class<?>... additionalClasses) {
      injectorBuilder.add(clazz, additionalClasses);
      return this;
    }

    public ApplicationWrapper build() {
      return new ApplicationWrapper(injectorBuilder.add(JavalinProvider.class).build());
    }
  }

  public static class Modifier {
    private final Injector.CopyBuilder builder;

    private Modifier(ApplicationWrapper wrapper) {
      this.builder = wrapper.injector.copy();
    }

    public <F, T extends F> Modifier replace(Class<F> from, Class<T> to) {
      builder.replace(from, to);
      return this;
    }

    public <F, T extends F> Modifier replace(F from, T to) {
      builder.replace(from, to);
      return this;
    }

    public ApplicationWrapper build() {
      return new ApplicationWrapper(builder.build());
    }
  }
}
