package platform.primitives;

import java.util.StringJoiner;
import java.util.function.Supplier;

/**
 * Wraps server port.
 *
 * <p>Required to make {@link io.github.suppierk.inject.Injector} work, because it relies on the
 * object type for injection.
 */
public final class ServerPort implements Supplier<Integer> {
  private static final int MIN_PORT = 1024;
  private static final int MAX_PORT = 49151;
  private static final String PORT_OUTSIDE_OF_RANGE = "Port %s is outside of range [%s-%s]";

  private final int port;

  /**
   * Default constructor.
   *
   * <p>Makes sure that the port number is within the expected port range.
   *
   * @param port to set
   * @throws IllegalArgumentException if port is outside the expected range
   * @see <a href="https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers">List of TCP and
   *     UDP port numbers</a>
   */
  public ServerPort(int port) {
    if (port < MIN_PORT || port > MAX_PORT) {
      throw new IllegalArgumentException(PORT_OUTSIDE_OF_RANGE.formatted(port, MIN_PORT, MAX_PORT));
    }

    this.port = port;
  }

  /** {@inheritDoc} */
  @Override
  public Integer get() {
    return port;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ServerPort that)) return false;
    return port == that.port;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return port;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return new StringJoiner(", ", ServerPort.class.getSimpleName() + "[", "]")
        .add("port=" + port)
        .toString();
  }
}
