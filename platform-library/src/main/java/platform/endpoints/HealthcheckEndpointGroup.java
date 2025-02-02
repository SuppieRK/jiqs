package platform.endpoints;

import static io.javalin.apibuilder.ApiBuilder.get;

import io.javalin.apibuilder.EndpointGroup;
import jakarta.inject.Singleton;

@Singleton
public final class HealthcheckEndpointGroup implements EndpointGroup {
  @Override
  public void addEndpoints() {
    get("/api/health", ctx -> ctx.result("OK"));
  }
}
