package io.github.suppierk.jiqs;

import static io.github.suppierk.jiqs.db.Tables.USERS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.suppierk.inject.Injector;
import io.github.suppierk.jiqs.configuration.GestaltProvider;
import io.github.suppierk.jiqs.models.requests.UserRequest;
import io.github.suppierk.jiqs.models.responses.UserResponse;
import io.github.suppierk.test.AbstractDatabaseTest;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.testtools.JavalinTest;
import jakarta.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

class ApplicationTest extends AbstractDatabaseTest {
  @Singleton
  static class TestGestaltProvider extends GestaltProvider {
    @Override
    public Gestalt gestalt() throws GestaltException {
      final ConfigSourcePackage configSourcePackage =
          MapConfigSourceBuilder.builder()
              .addCustomConfig("database.url", getJdbcUrl())
              .addCustomConfig("database.username", getUsername())
              .addCustomConfig("database.password", getPassword())
              .build();

      final Gestalt gestalt = new GestaltBuilder().addSource(configSourcePackage).build();
      gestalt.loadConfigs();
      return gestalt;
    }
  }

  static final ObjectMapper MAPPER = new ObjectMapper();

  Injector injector;
  Javalin javalin;

  @BeforeEach
  void setUp() {
    injector =
        new Application()
            .injector
            .copy()
            .replace(GestaltProvider.class, TestGestaltProvider.class)
            .build();

    javalin = injector.get(Javalin.class);
  }

  @AfterEach
  void tearDown() {
    testDsl().truncate(USERS).execute();

    injector.close();

    assertTrue(javalin.jettyServer().server().isStopped());
  }

  @Nested
  class CommonEndpoints {
    @Test
    void must_have_healthcheck_endpoint() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            try (final var healthcheckResponse = client.get("/api/health")) {
              assertEquals(
                  HttpStatus.OK.getCode(), healthcheckResponse.code(), "Response code is not OK");
              assertEquals(
                  "OK",
                  healthcheckResponse.body().string(),
                  "Response body is not text saying 'OK'");
            }
          });
    }

    @Test
    void must_have_openapi_endpoint() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            try (final var metricsResponse = client.get("/openapi?v=default")) {
              assertEquals(
                  HttpStatus.OK.getCode(), metricsResponse.code(), "Response code is not OK");

              final var responseBody = metricsResponse.body().string();

              assertNotNull(responseBody, "Response body is null");
              assertFalse(responseBody.isBlank(), "Response body is blank");
            }

            try (final var metricsResponse = client.get("/swagger")) {
              assertEquals(
                  HttpStatus.OK.getCode(), metricsResponse.code(), "Response code is not OK");

              final var responseBody = metricsResponse.body().string();

              assertNotNull(responseBody, "Response body is null");
              assertFalse(responseBody.isBlank(), "Response body is blank");
            }
          });
    }
  }

  @Nested
  class UsersEndpoints {
    @Test
    void when_create_user_request_is_not_correct_then_400_is_returned() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            try (final var invalidJsonResponse = client.post("/api/users", "bad request")) {
              assertEquals(
                  HttpStatus.BAD_REQUEST.getCode(),
                  invalidJsonResponse.code(),
                  "Response code is not Bad Request");
            }
          });
    }

    @Test
    void when_create_user_request_is_correct_then_new_entity_is_created() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            final var request =
                new UserRequest(
                    RandomStringUtils.randomAlphanumeric(16),
                    RandomStringUtils.randomAlphanumeric(16).toCharArray());

            try (final var response = client.post("/api/users", request)) {
              assertEquals(HttpStatus.OK.getCode(), response.code(), "Response code is not OK");

              final var responseBody = response.body();
              assertNotNull(responseBody, "Response body is null");

              // Check the response
              final var parsedResponse =
                  assertDoesNotThrow(
                      () -> MAPPER.readValue(responseBody.bytes(), UserResponse.class));
              assertEquals(
                  request.username(),
                  parsedResponse.username(),
                  "Response username is not correct");

              // Check the database
              final var optionalRecord =
                  testDsl()
                      .selectFrom(USERS)
                      .where(USERS.ID.eq(parsedResponse.id()))
                      .fetchOptional();
              assertTrue(optionalRecord.isPresent(), "New database record must be created");

              final var databaseRecord = optionalRecord.get();
              assertEquals(
                  request.username(), databaseRecord.getName(), "Database username is not correct");
              assertArrayEquals(
                  request.password(),
                  databaseRecord.getPassword().toCharArray(),
                  "Database password is not correct");
            }
          });
    }

    @Test
    void when_all_users_requested_then_result_is_returned() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            try (final var response = client.get("/api/users")) {
              assertEquals(HttpStatus.OK.getCode(), response.code(), "Response code is not OK");

              final var responseBody = response.body();
              assertNotNull(responseBody, "Response body is null");

              final var parsedResponse =
                  assertDoesNotThrow(
                      () -> MAPPER.readValue(responseBody.bytes(), UserResponse[].class));
              assertEquals(0, parsedResponse.length, "Response body should be empty");
            }
          });
    }

    @Test
    void when_user_requested_with_incorrect_id_then_400_is_returned() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            try (final var response = client.get("/api/users/invalidUuid")) {
              assertEquals(
                  HttpStatus.BAD_REQUEST.getCode(),
                  response.code(),
                  "Response code is not Bad Request");
            }
          });
    }

    @Test
    void when_missing_user_requested_then_404_is_returned() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            try (final var response = client.get("/api/users/" + UUID.randomUUID())) {
              assertEquals(
                  HttpStatus.NOT_FOUND.getCode(),
                  response.code(),
                  "Response code is not Not Found");
            }
          });
    }

    @Test
    void when_existing_user_requested_then_it_is_returned() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            final var request =
                new UserRequest(
                    RandomStringUtils.randomAlphanumeric(16),
                    RandomStringUtils.randomAlphanumeric(16).toCharArray());

            final var userId = new AtomicReference<UUID>(null);

            try (final var response = client.post("/api/users", request)) {
              assertEquals(HttpStatus.OK.getCode(), response.code(), "Response code is not OK");

              final var responseBody = response.body();
              assertNotNull(responseBody, "Response body is null");

              // Check the response
              final var parsedResponse =
                  assertDoesNotThrow(
                      () -> MAPPER.readValue(responseBody.bytes(), UserResponse.class));
              userId.set(parsedResponse.id());
            }

            try (final var response = client.get("/api/users/" + userId.get())) {
              assertEquals(HttpStatus.OK.getCode(), response.code(), "Response code is not OK");

              final var responseBody = response.body();
              assertNotNull(responseBody, "Response body is null");

              // Check the response
              final var parsedResponse =
                  assertDoesNotThrow(
                      () -> MAPPER.readValue(responseBody.bytes(), UserResponse.class));
              assertEquals(userId.get(), parsedResponse.id(), "IDs do not match");
              assertEquals(
                  request.username(), parsedResponse.username(), "Username is not correct");
            }
          });
    }

    @Test
    void when_user_update_requested_with_incorrect_id_then_400_is_returned() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            try (final var response = client.patch("/api/users/invalidUuid")) {
              assertEquals(
                  HttpStatus.BAD_REQUEST.getCode(),
                  response.code(),
                  "Response code is not Bad Request");
            }
          });
    }

    @Test
    void when_missing_user_update_requested_then_400_is_returned() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            final var request =
                new UserRequest(
                    RandomStringUtils.randomAlphanumeric(16),
                    RandomStringUtils.randomAlphanumeric(16).toCharArray());

            try (final var response = client.patch("/api/users/" + UUID.randomUUID(), request)) {
              assertEquals(
                  HttpStatus.NOT_FOUND.getCode(),
                  response.code(),
                  "Response code is not Not Found");
            }
          });
    }

    @Test
    void when_existing_user_update_requested_with_invalid_body_then_it_is_returned() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            final var request =
                new UserRequest(
                    RandomStringUtils.randomAlphanumeric(16),
                    RandomStringUtils.randomAlphanumeric(16).toCharArray());

            final var userId = new AtomicReference<UUID>(null);

            try (final var response = client.post("/api/users", request)) {
              assertEquals(HttpStatus.OK.getCode(), response.code(), "Response code is not OK");

              final var responseBody = response.body();
              assertNotNull(responseBody, "Response body is null");

              // Check the response
              final var parsedResponse =
                  assertDoesNotThrow(
                      () -> MAPPER.readValue(responseBody.bytes(), UserResponse.class));
              userId.set(parsedResponse.id());
            }

            try (final var response = client.patch("/api/users/" + userId.get(), "bad request")) {
              assertEquals(
                  HttpStatus.BAD_REQUEST.getCode(),
                  response.code(),
                  "Response code is not Bad Request");
            }
          });
    }

    @Test
    void when_existing_user_update_requested_with_empty_body_then_nothing_changes() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            final var request =
                new UserRequest(
                    RandomStringUtils.randomAlphanumeric(16),
                    RandomStringUtils.randomAlphanumeric(16).toCharArray());

            final var userId = new AtomicReference<UUID>(null);

            try (final var response = client.post("/api/users", request)) {
              assertEquals(HttpStatus.OK.getCode(), response.code(), "Response code is not OK");

              final var responseBody = response.body();
              assertNotNull(responseBody, "Response body is null");

              // Check the response
              final var parsedResponse =
                  assertDoesNotThrow(
                      () -> MAPPER.readValue(responseBody.bytes(), UserResponse.class));
              userId.set(parsedResponse.id());
            }

            try (final var response = client.patch("/api/users/" + userId.get(), "{}")) {
              assertEquals(HttpStatus.OK.getCode(), response.code(), "Response code is not OK");

              final var responseBody = response.body();
              assertNotNull(responseBody, "Response body is null");

              // Check the response
              final var parsedResponse =
                  assertDoesNotThrow(
                      () -> MAPPER.readValue(responseBody.bytes(), UserResponse.class));
              assertEquals(userId.get(), parsedResponse.id(), "IDs do not match");
              assertEquals(
                  request.username(), parsedResponse.username(), "Username is not correct");

              // Check the database
              final var optionalRecord =
                  testDsl()
                      .selectFrom(USERS)
                      .where(USERS.ID.eq(parsedResponse.id()))
                      .fetchOptional();
              assertTrue(optionalRecord.isPresent(), "New database record must be created");

              final var databaseRecord = optionalRecord.get();
              assertEquals(
                  request.username(), databaseRecord.getName(), "Database username is not correct");
              assertArrayEquals(
                  request.password(),
                  databaseRecord.getPassword().toCharArray(),
                  "Database password is not correct");
            }
          });
    }

    @Test
    void when_existing_user_update_requested_then_it_is_returned() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            final var request =
                new UserRequest(
                    RandomStringUtils.randomAlphanumeric(16),
                    RandomStringUtils.randomAlphanumeric(16).toCharArray());

            final var userId = new AtomicReference<UUID>(null);

            try (final var response = client.post("/api/users", request)) {
              assertEquals(HttpStatus.OK.getCode(), response.code(), "Response code is not OK");

              final var responseBody = response.body();
              assertNotNull(responseBody, "Response body is null");

              // Check the response
              final var parsedResponse =
                  assertDoesNotThrow(
                      () -> MAPPER.readValue(responseBody.bytes(), UserResponse.class));
              userId.set(parsedResponse.id());
            }

            final var patchRequest =
                new UserRequest(
                    RandomStringUtils.randomAlphanumeric(16),
                    RandomStringUtils.randomAlphanumeric(16).toCharArray());

            try (final var response = client.patch("/api/users/" + userId.get(), patchRequest)) {
              assertEquals(HttpStatus.OK.getCode(), response.code(), "Response code is not OK");

              final var responseBody = response.body();
              assertNotNull(responseBody, "Response body is null");

              // Check the response
              final var parsedResponse =
                  assertDoesNotThrow(
                      () -> MAPPER.readValue(responseBody.bytes(), UserResponse.class));
              assertEquals(userId.get(), parsedResponse.id(), "IDs do not match");
              assertEquals(
                  patchRequest.username(), parsedResponse.username(), "Username is not correct");

              // Check the database
              final var optionalRecord =
                  testDsl()
                      .selectFrom(USERS)
                      .where(USERS.ID.eq(parsedResponse.id()))
                      .fetchOptional();
              assertTrue(optionalRecord.isPresent(), "New database record must be created");

              final var databaseRecord = optionalRecord.get();
              assertEquals(
                  patchRequest.username(),
                  databaseRecord.getName(),
                  "Database username is not correct");
              assertArrayEquals(
                  patchRequest.password(),
                  databaseRecord.getPassword().toCharArray(),
                  "Database password is not correct");
            }
          });
    }

    @Test
    void when_user_delete_requested_with_incorrect_id_then_400_is_returned() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            try (final var response = client.delete("/api/users/invalidUuid")) {
              assertEquals(
                  HttpStatus.BAD_REQUEST.getCode(),
                  response.code(),
                  "Response code is not Bad Request");
            }
          });
    }

    @Test
    void when_existing_user_delete_requested_then_it_is_returned() {
      JavalinTest.test(
          javalin,
          (server, client) -> {
            final var request =
                new UserRequest(
                    RandomStringUtils.randomAlphanumeric(16),
                    RandomStringUtils.randomAlphanumeric(16).toCharArray());

            final var userId = new AtomicReference<UUID>(null);

            try (final var response = client.post("/api/users", request)) {
              assertEquals(HttpStatus.OK.getCode(), response.code(), "Response code is not OK");

              final var responseBody = response.body();
              assertNotNull(responseBody, "Response body is null");

              // Check the response
              final var parsedResponse =
                  assertDoesNotThrow(
                      () -> MAPPER.readValue(responseBody.bytes(), UserResponse.class));
              userId.set(parsedResponse.id());
            }

            try (final var response = client.delete("/api/users/" + userId.get())) {
              assertEquals(
                  HttpStatus.NO_CONTENT.getCode(), response.code(), "Response code is not OK");

              // Check the database
              final var optionalRecord =
                  testDsl().selectFrom(USERS).where(USERS.ID.eq(userId.get())).fetchOptional();
              assertTrue(optionalRecord.isEmpty(), "Database record must be deleted");
            }
          });
    }
  }
}
