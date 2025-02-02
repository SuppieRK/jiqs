package service.models.responses;

import java.util.UUID;
import service.db.tables.records.UsersRecord;

public record UserResponse(UUID id, String username) {
  public UserResponse(UsersRecord databaseRecord) {
    this(databaseRecord.getId(), databaseRecord.getName());
  }
}
