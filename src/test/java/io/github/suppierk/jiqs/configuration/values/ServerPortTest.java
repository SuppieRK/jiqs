package io.github.suppierk.jiqs.configuration.values;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ThreadLocalRandom;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ServerPortTest {
  @Test
  void verify_basic_object_properties() {
    EqualsVerifier.forClass(ServerPort.class).verify();
  }

  @Test
  void verify_constructor() {
    assertThrows(IllegalStateException.class, () -> new ServerPort(null));

    final var port = ThreadLocalRandom.current().nextInt(0, 65535);
    final var serverPort = assertDoesNotThrow(() -> new ServerPort(port));
    assertEquals(port, serverPort.get());

    assertNotNull(serverPort.toString());
    assertTrue(serverPort.toString().contains(Integer.toString(port)));
  }
}
