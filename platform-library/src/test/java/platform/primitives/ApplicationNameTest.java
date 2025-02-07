package platform.primitives;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ApplicationNameTest {
  @Test
  void pojoTest() {
    EqualsVerifier.forClass(ApplicationName.class).verify();
  }

  @Test
  void constructorTest() {
    assertThrows(IllegalArgumentException.class, () -> new ApplicationName(null));
    assertThrows(IllegalArgumentException.class, () -> new ApplicationName("    "));
    assertDoesNotThrow(() -> new ApplicationName("test-app"));
  }
}
