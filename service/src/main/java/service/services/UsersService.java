package service.services;

import static service.db.Tables.USERS;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import service.models.requests.UserRequest;
import service.models.responses.UserResponse;

/** Defines data operations in database. */
@Singleton
public class UsersService {
  private final DSLContext dsl;

  /**
   * Default constructor.
   *
   * @param dsl to talk to the database
   */
  @Inject
  public UsersService(DSLContext dsl) {
    this.dsl = dsl;
  }

  /**
   * Creates new user.
   *
   * @param userRequest to set parameters from
   * @return new {@link UserResponse}
   */
  @SuppressWarnings("squid:S2129")
  public UserResponse create(UserRequest userRequest) {
    final var newRecord = dsl.newRecord(USERS);
    newRecord.setId(UuidCreator.getTimeOrderedEpoch());
    newRecord.setCreatedAt(OffsetDateTime.now());
    newRecord.setName(userRequest.username());
    newRecord.setPassword(new String(userRequest.password()));
    newRecord.insert();

    return new UserResponse(newRecord);
  }

  /**
   * Fetches all available users.
   *
   * @return a {@link List} of available users
   */
  public List<UserResponse> getAll() {
    return dsl.selectFrom(USERS).fetchStream().map(UserResponse::new).toList();
  }

  /**
   * Fetch a single user.
   *
   * @param userId to fetch
   * @return an empty {@link Optional} if user was not found or {@link UserResponse} with the user
   *     data
   */
  public Optional<UserResponse> getOne(UUID userId) {
    return dsl.selectFrom(USERS).where(USERS.ID.eq(userId)).fetchOptional().map(UserResponse::new);
  }

  /**
   * Updates single user.
   *
   * <p>Uses jOOQ transactional capability.
   *
   * @param userId to update
   * @param userRequest to get values for update from
   * @return an empty {@link Optional} if user was not found or {@link UserResponse} with updated
   *     user data
   */
  @SuppressWarnings("squid:S2129")
  public Optional<UserResponse> update(UUID userId, UserRequest userRequest) {
    return dsl.transactionResult(
        ctx ->
            ctx.dsl()
                .selectFrom(USERS)
                .where(USERS.ID.eq(userId))
                .fetchOptional()
                .map(
                    databaseRecord -> {
                      if (userRequest.username() != null) {
                        databaseRecord.setName(userRequest.username());
                      }

                      if (userRequest.password() != null) {
                        databaseRecord.setPassword(new String(userRequest.password()));
                      }

                      databaseRecord.update();
                      return new UserResponse(databaseRecord);
                    }));
  }

  /**
   * Delete user.
   *
   * @param userId to delete
   */
  public void delete(UUID userId) {
    dsl.deleteFrom(USERS).where(USERS.ID.eq(userId)).execute();
  }
}
