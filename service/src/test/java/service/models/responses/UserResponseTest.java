package service.models.responses;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UserResponseTest {
  @Test
  void pojoTest() {
    EqualsVerifier.forClass(UserResponse.class).verify();
  }
}
