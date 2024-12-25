package io.github.suppierk.jiqs.models.responses;

import io.github.suppierk.jiqs.db.tables.records.UsersRecord;
import java.util.UUID;

public record UserResponse(UUID id, String username) {
  public UserResponse(UsersRecord databaseRecord) {
    this(databaseRecord.getId(), databaseRecord.getName());
  }
}
