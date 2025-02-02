package platform.primitives;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Supplier;

public final class ServerPort implements Supplier<Integer> {
  private final int port;

  public ServerPort(int port) {
    this.port = port;
  }

  @Override
  public Integer get() {
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
        .add("port=" + port)
        .toString();
  }
}
