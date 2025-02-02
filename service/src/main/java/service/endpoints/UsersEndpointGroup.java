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
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import service.models.requests.UserRequest;
import service.models.responses.UserResponse;
import service.services.UsersService;

@Singleton
public class UsersEndpointGroup implements EndpointGroup, CrudHandler {
  private static final String USER_ID = "userId";
  private static final String PLURAL_USERS_ENDPOINT = "/api/users";
  private static final String SINGULAR_USER_ENDPOINT = "/api/users/{userId}";

  private final UsersService service;

  @Inject
  public UsersEndpointGroup(UsersService service) {
    this.service = service;
  }

  @Override
  public void addEndpoints() {
    crud(SINGULAR_USER_ENDPOINT, this);
  }

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
