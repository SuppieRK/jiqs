package service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static service.db.Tables.USERS;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.testtools.JavalinTest;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import platform.ApplicationWrapper;
import platform.test.AbstractApplicationTest;
import service.models.requests.UserRequest;
import service.models.responses.UserResponse;

class ApplicationTest extends AbstractApplicationTest {
  static final ObjectMapper MAPPER = new ObjectMapper();

  ApplicationWrapper wrapper;
  Javalin javalin;

  @BeforeEach
  void setUp() {
    wrapper = useTestDatabase(new Application().wrapper);

    javalin = wrapper.javalin();
  }

  @AfterEach
  void tearDown() {
    testDsl().truncate(USERS).execute();

    wrapper.close();
    assertTrue(javalin.jettyServer().server().isStopped());
  }

  @Test
  @Disabled(value = "https://github.com/javalin/javalin-openapi/issues/237")
  void hasOpenApiAndSwaggerEndpoints() {
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

  @Test
  void returnsBadRequestForIncorrectUserCreateRequest() {
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
  void createsNewUser() {
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
                request.username(), parsedResponse.username(), "Response username is not correct");

            // Check the database
            final var optionalRecord =
                testDsl().selectFrom(USERS).where(USERS.ID.eq(parsedResponse.id())).fetchOptional();
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
  void returnsAllUsers() {
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
  void returnsBadRequestWhenUserIdToGetUserIsIncorrect() {
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
  void returnsNotFoundWhenThereIsNoSuchUser() {
    JavalinTest.test(
        javalin,
        (server, client) -> {
          try (final var response = client.get("/api/users/" + UUID.randomUUID())) {
            assertEquals(
                HttpStatus.NOT_FOUND.getCode(), response.code(), "Response code is not Not Found");
          }
        });
  }

  @Test
  void returnsUserById() {
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
            assertEquals(request.username(), parsedResponse.username(), "Username is not correct");
          }
        });
  }

  @Test
  void returnsBadRequestWhenUpdatingUserWithIncorrectId() {
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
  void returnsNotFoundWhenUpdatingMissingUser() {
    JavalinTest.test(
        javalin,
        (server, client) -> {
          final var request =
              new UserRequest(
                  RandomStringUtils.randomAlphanumeric(16),
                  RandomStringUtils.randomAlphanumeric(16).toCharArray());

          try (final var response = client.patch("/api/users/" + UUID.randomUUID(), request)) {
            assertEquals(
                HttpStatus.NOT_FOUND.getCode(), response.code(), "Response code is not Not Found");
          }
        });
  }

  @Test
  void returnsBadRequestWhenUpdatingUserWithIncorrectRequestBody() {
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
  void returnsOkIfUserPropertiesDidNotChangeWithUpdate() {
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
            assertEquals(request.username(), parsedResponse.username(), "Username is not correct");

            // Check the database
            final var optionalRecord =
                testDsl().selectFrom(USERS).where(USERS.ID.eq(parsedResponse.id())).fetchOptional();
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
  void returnsNewUserPropertiesWhenUpdated() {
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
                testDsl().selectFrom(USERS).where(USERS.ID.eq(parsedResponse.id())).fetchOptional();
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
  void returnsBadRequestWhenDeletingUserByIncorrectId() {
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
  void returnsNoContentWhenUserWasSuccessfullyDeleted() {
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
