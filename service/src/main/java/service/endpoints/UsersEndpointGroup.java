package service.endpoints;

import static io.javalin.apibuilder.ApiBuilder.crud;

import com.github.f4b6a3.uuid.UuidCreator;
import com.github.f4b6a3.uuid.util.UuidValidator;
import io.javalin.apibuilder.CrudHandler;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.openapi.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import service.models.requests.UserRequest;
import service.models.responses.UserResponse;
import service.services.UsersService;

/**
 * This is essentially a controller, which defines endpoints to manipulate data in the app.
 *
 * <p>Be careful with business logic, as this class also defines a lot of OpenAPI annotations.
 */
@Singleton
public class UsersEndpointGroup implements EndpointGroup, CrudHandler {
  private static final String USER_ID = "userId";
  private static final String PLURAL_USERS_ENDPOINT = "/api/users";
  private static final String SINGULAR_USER_ENDPOINT = "/api/users/{userId}";

  private final UsersService service;

  /**
   * Default constructor.
   *
   * <p>Because of the way how {@link io.github.suppierk.inject.Injector} is implemented,
   * constructor parameter will never be {@code null}.
   *
   * @param service to invoke database operations.
   */
  @Inject
  public UsersEndpointGroup(UsersService service) {
    this.service = service;
  }

  /** {@inheritDoc} */
  @Override
  public void addEndpoints() {
    crud(SINGULAR_USER_ENDPOINT, this);
  }

  /** {@inheritDoc} */
  @Override
  @OpenApi(
      summary = "Create new user",
      operationId = "createUser",
      path = PLURAL_USERS_ENDPOINT,
      methods = HttpMethod.POST,
      tags = {"User"},
      requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = UserRequest.class)}),
      responses = {
        @OpenApiResponse(status = "200", content = @OpenApiContent(from = UserResponse.class))
      })
  public void create(Context context) {
    final var request = context.bodyValidator(UserRequest.class).get();
    final var response = service.create(request);
    context.json(response);
  }

  /** {@inheritDoc} */
  @Override
  @OpenApi(
      summary = "Get all users",
      operationId = "getUsers",
      path = PLURAL_USERS_ENDPOINT,
      methods = HttpMethod.GET,
      tags = {"User"},
      responses = {
        @OpenApiResponse(status = "200", content = @OpenApiContent(from = UserResponse[].class))
      })
  public void getAll(Context context) {
    context.json(service.getAll());
  }

  /** {@inheritDoc} */
  @Override
  @OpenApi(
      summary = "Get user by ID",
      operationId = "getUserById",
      path = SINGULAR_USER_ENDPOINT,
      methods = HttpMethod.PATCH,
      pathParams = {
        @OpenApiParam(
            name = USER_ID,
            type = UUID.class,
            description = "The user ID",
            required = true)
      },
      tags = {"User"},
      responses = {
        @OpenApiResponse(status = "200", content = @OpenApiContent(from = UserResponse.class)),
        @OpenApiResponse(status = "400"),
        @OpenApiResponse(status = "404")
      })
  public void getOne(Context context, String resourceId) {
    if (!UuidValidator.isValid(resourceId)) {
      throw new BadRequestResponse(
          HttpStatus.BAD_REQUEST.getMessage(), Map.of(USER_ID, resourceId));
    }

    final var userId = UuidCreator.fromString(resourceId);
    final var response =
        service
            .getOne(userId)
            .orElseThrow(
                () ->
                    new NotFoundResponse(
                        HttpStatus.NOT_FOUND.getMessage(), Map.of(USER_ID, userId.toString())));
    context.json(response);
  }

  /** {@inheritDoc} */
  @Override
  @OpenApi(
      summary = "Update user by ID",
      operationId = "updateUserById",
      path = SINGULAR_USER_ENDPOINT,
      methods = HttpMethod.PATCH,
      pathParams = {
        @OpenApiParam(
            name = USER_ID,
            type = UUID.class,
            description = "The user ID",
            required = true)
      },
      tags = {"User"},
      requestBody = @OpenApiRequestBody(content = {@OpenApiContent(from = UserRequest.class)}),
      responses = {
        @OpenApiResponse(status = "204", content = @OpenApiContent(from = UserResponse.class)),
        @OpenApiResponse(status = "400"),
        @OpenApiResponse(status = "404")
      })
  public void update(Context context, String resourceId) {
    if (!UuidValidator.isValid(resourceId)) {
      throw new BadRequestResponse(
          HttpStatus.BAD_REQUEST.getMessage(), Map.of(USER_ID, resourceId));
    }

    final var userId = UuidCreator.fromString(resourceId);
    final var request = context.bodyValidator(UserRequest.class).get();
    final var response =
        service
            .update(userId, request)
            .orElseThrow(
                () ->
                    new NotFoundResponse(
                        HttpStatus.NOT_FOUND.getMessage(), Map.of(USER_ID, userId.toString())));
    context.json(response);
  }

  /** {@inheritDoc} */
  @Override
  @OpenApi(
      summary = "Delete user by ID",
      operationId = "deleteUserById",
      path = SINGULAR_USER_ENDPOINT,
      methods = HttpMethod.DELETE,
      pathParams = {
        @OpenApiParam(
            name = USER_ID,
            type = UUID.class,
            description = "The user ID",
            required = true)
      },
      tags = {"User"},
      responses = {
        @OpenApiResponse(status = "204"),
        @OpenApiResponse(status = "400"),
      })
  public void delete(Context context, String resourceId) {
    if (!UuidValidator.isValid(resourceId)) {
      throw new BadRequestResponse(
          HttpStatus.BAD_REQUEST.getMessage(), Map.of(USER_ID, resourceId));
    }

    final var userId = UuidCreator.fromString(resourceId);
    service.delete(userId);
    context.status(HttpStatus.NO_CONTENT);
  }
}
