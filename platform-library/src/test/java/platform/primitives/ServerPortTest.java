package platform.primitives;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ServerPortTest {
  @Test
  void pojoTest() {
    EqualsVerifier.forClass(ServerPort.class).verify();
  }

  @Test
  void constructorTest() {
    assertThrows(IllegalArgumentException.class, () -> new ServerPort(-1));
    assertThrows(IllegalArgumentException.class, () -> new ServerPort(Integer.MAX_VALUE));
    assertDoesNotThrow(() -> new ServerPort(8080));
  }
}
