package io.github.suppierk.jiqs.services;

import static io.github.suppierk.jiqs.db.Tables.USERS;

import com.github.f4b6a3.uuid.UuidCreator;
import io.github.suppierk.jiqs.models.requests.UserRequest;
import io.github.suppierk.jiqs.models.responses.UserResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;

@Singleton
public class UsersService {
  private final DSLContext dsl;

  @Inject
  public UsersService(DSLContext dsl) {
    this.dsl = dsl;
  }

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

  public List<UserResponse> getAll() {
    return dsl.selectFrom(USERS).fetchStream().map(UserResponse::new).toList();
  }

  public Optional<UserResponse> getOne(UUID userId) {
    return dsl.selectFrom(USERS).where(USERS.ID.eq(userId)).fetchOptional().map(UserResponse::new);
  }

  @SuppressWarnings("squid:S2129")
  public Optional<UserResponse> update(UUID userId, UserRequest userRequest) {
    return dsl.selectFrom(USERS)
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
            });
  }

  public void delete(UUID userId) {
    dsl.deleteFrom(USERS).where(USERS.ID.eq(userId)).execute();
  }
}
