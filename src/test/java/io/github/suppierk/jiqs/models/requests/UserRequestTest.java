package io.github.suppierk.jiqs.models.requests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

class UserRequestTest {
  @Test
  void verify_basic_object_properties() {
    EqualsVerifier.forClass(UserRequest.class).verify();
  }

  @Test
  void verify_constructor() {
    final var username = RandomStringUtils.randomAlphabetic(10);
    final var password = RandomStringUtils.randomAlphabetic(10);
    final var passwordArray = password.toCharArray();

    final var userProperties = assertDoesNotThrow(() -> new UserRequest(username, passwordArray));

    assertNotNull(userProperties.toString());
    assertTrue(userProperties.toString().contains(username));
    assertFalse(userProperties.toString().contains("password"));
    assertFalse(userProperties.toString().contains(password));
  }
}
