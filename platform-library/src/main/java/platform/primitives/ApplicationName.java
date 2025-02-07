package platform.primitives;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Supplier;

/**
 * Wraps application name.
 *
 * <p>Required to make {@link io.github.suppierk.inject.Injector} work, because it relies on the
 * object type for injection.
 */
public final class ApplicationName implements Supplier<String> {
  private final String name;

  /**
   * Default constructor.
   *
   * @param name to set
   * @throws IllegalArgumentException if application name is {@code null} or blank
   */
  public ApplicationName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Application name cannot be null or blank");
    }

    this.name = name;
  }

  /** {@inheritDoc} */
  @Override
  public String get() {
    return name;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ApplicationName that)) return false;
    return Objects.equals(name, that.name);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return new StringJoiner(", ", ApplicationName.class.getSimpleName() + "[", "]")
        .add("name='" + name + "'")
        .toString();
  }
}
