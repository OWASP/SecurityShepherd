package testUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Guards the test classpath against the Hamcrest 1.1 that the unused json-simple dependency brought
 * in through JUnit 4.10 (#944). Its {@code Matcher} shadowed Hamcrest 3's, so a failing {@code
 * assertThat} threw {@code NoSuchMethodError} instead of reporting the mismatch.
 */
class HamcrestClasspathTest {

  @Test
  void failingAssertThat_ReportsExpectedAndActual() {
    AssertionError error =
        assertThrows(AssertionError.class, () -> assertThat("actual", equalTo("expected")));

    assertTrue(error.getMessage().contains("Expected: \"expected\""), error.getMessage());
    assertTrue(error.getMessage().contains("but: was \"actual\""), error.getMessage());
  }
}
