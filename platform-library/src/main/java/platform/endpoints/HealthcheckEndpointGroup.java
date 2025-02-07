package platform.endpoints;

import static io.javalin.apibuilder.ApiBuilder.get;

import io.javalin.apibuilder.EndpointGroup;
import jakarta.inject.Singleton;

/**
 * Standard healthcheck.
 *
 * <p>As additional idea - this can be extended to inject database connection pool to provide more
 * complete healthcheck or better yet: expose "readiness" endpoint to check the database connection,
 * leaving the current implementation as "liveness" check (or "startup" check).
 *
 * @see <a
 *     href="https://kubernetes.io/docs/concepts/configuration/liveness-readiness-startup-probes">Kubernetes
 *     liveness and readiness probes</a>
 */
@Singleton
public final class HealthcheckEndpointGroup implements EndpointGroup {
  @Override
  public void addEndpoints() {
    get("/api/health", ctx -> ctx.result("OK"));
  }
}
