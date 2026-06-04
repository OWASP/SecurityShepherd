package servlets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class SetupTest {

  @Test
  public void validateHostPort_bothEmpty_isValid() {
    assertNull(Setup.validateHostPort("", ""));
  }

  @Test
  public void validateHostPort_bothProvided_isValid() {
    assertNull(Setup.validateHostPort("localhost", "3306"));
  }

  @Test
  public void validateHostPort_onlyHostProvided_isInvalid() {
    assertNotNull(Setup.validateHostPort("localhost", ""));
  }

  @Test
  public void validateHostPort_onlyPortProvided_isInvalid() {
    assertNotNull(Setup.validateHostPort("", "3306"));
  }

  @Test
  public void validateHostPort_bothNull_isValid() {
    assertNull(Setup.validateHostPort(null, null));
  }

  @Test
  public void validateHostPort_hostNullPortProvided_isInvalid() {
    assertNotNull(Setup.validateHostPort(null, "3306"));
  }

  @Test
  public void validateHostPort_hostProvidedPortNull_isInvalid() {
    assertNotNull(Setup.validateHostPort("localhost", null));
  }
}
