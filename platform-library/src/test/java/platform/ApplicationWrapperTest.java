package platform;

import static io.javalin.apibuilder.ApiBuilder.get;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;
import io.javalin.testtools.JavalinTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import platform.test.AbstractApplicationTest;

class ApplicationWrapperTest extends AbstractApplicationTest {
  @Singleton
  static class CustomEndpoints implements EndpointGroup {
    @Override
    public void addEndpoints() {
      get("/api/custom", ctx -> ctx.result("Hello World!"));
    }
  }

  @Test
  void createsApplicationWrapper() {
    assertDoesNotThrow(() -> ApplicationWrapper.createApplication().build());
  }

  @Test
  void hasDefaultHealthcheck() {
    JavalinTest.test(
        ApplicationWrapper.createApplication().build().javalin(),
        (server, client) -> {
          try (final var healthcheckResponse = client.get("/api/health")) {
            assertEquals(
                HttpStatus.OK.getCode(), healthcheckResponse.code(), "Response code is not OK");
            assertEquals(
                "OK", healthcheckResponse.body().string(), "Response body is not text saying 'OK'");
          }
        });
  }

  @Test
  void registersUserEndpoints() {
    JavalinTest.test(
        ApplicationWrapper.createApplication().add(CustomEndpoints.class).build().javalin(),
        (server, client) -> {
          try (final var customResponse = client.get("/api/custom")) {
            assertEquals(HttpStatus.OK.getCode(), customResponse.code(), "Response code is not OK");
            assertEquals(
                "Hello World!",
                customResponse.body().string(),
                "Response body is not text saying 'Hello World!'");
          }
        });
  }
}
