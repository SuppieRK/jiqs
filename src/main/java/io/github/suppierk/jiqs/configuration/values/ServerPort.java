package io.github.suppierk.jiqs.configuration.values;

import java.util.Objects;
import java.util.StringJoiner;

public final class ServerPort {
  private final int port;

  public ServerPort(Integer port) {
    if (port == null) {
      throw new IllegalStateException("Server port must not be null");
    }

    this.port = port;
  }

  public int get() {
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ServerPort that)) return false;
    return port == that.port;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(port);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ServerPort.class.getSimpleName() + "[", "]")
        .add(Integer.toString(port))
        .toString();
  }
}
