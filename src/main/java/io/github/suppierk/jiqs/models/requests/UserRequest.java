package io.github.suppierk.jiqs.models.requests;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public record UserRequest(String username, char[] password) {
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UserRequest that)) return false;
    return Objects.equals(username, that.username) && Objects.deepEquals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, Arrays.hashCode(password));
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", UserRequest.class.getSimpleName() + "[", "]")
        .add("username='" + username + "'")
        .toString();
  }
}
