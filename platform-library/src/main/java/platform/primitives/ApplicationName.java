package platform.primitives;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Supplier;

public final class ApplicationName implements Supplier<String> {
  private final String name;

  public ApplicationName(String name) {
    this.name = name;
  }

  @Override
  public String get() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ApplicationName that)) return false;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ApplicationName.class.getSimpleName() + "[", "]")
        .add("name='" + name + "'")
        .toString();
  }
}
