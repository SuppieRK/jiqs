package service.models.requests;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class UserRequestTest {
  @Test
  void pojoTest() {
    EqualsVerifier.forClass(UserRequest.class).verify();
  }
}
