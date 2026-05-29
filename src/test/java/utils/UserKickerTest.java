package utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserKickerTest {

  @BeforeEach
  void resetKickList() throws Exception {
    Field field = UserKicker.class.getDeclaredField("kickTheseUsers");
    field.setAccessible(true);
    ((List<?>) field.get(null)).clear();
  }

  @Test
  void shouldKickUser_emptyList() {
    assertFalse(UserKicker.shouldKickUser("alice"));
  }

  @Test
  void addAndCheck_userIsOnList() {
    UserKicker.addUserToKickList("bob");
    assertTrue(UserKicker.shouldKickUser("bob"));
  }

  @Test
  void addAndCheck_otherUserNotOnList() {
    UserKicker.addUserToKickList("bob");
    assertFalse(UserKicker.shouldKickUser("alice"));
  }

  @Test
  void removeFromKicklist_removesUser() {
    UserKicker.addUserToKickList("carol");
    assertTrue(UserKicker.shouldKickUser("carol"));
    UserKicker.removeFromKicklist("carol");
    assertFalse(UserKicker.shouldKickUser("carol"));
  }

  @Test
  void removeFromKicklist_nonexistentUserNoOp() {
    UserKicker.removeFromKicklist("nobody");
    assertFalse(UserKicker.shouldKickUser("nobody"));
  }

  @Test
  void multipleUsers_independentTracking() {
    UserKicker.addUserToKickList("alice");
    UserKicker.addUserToKickList("bob");
    assertTrue(UserKicker.shouldKickUser("alice"));
    assertTrue(UserKicker.shouldKickUser("bob"));
    UserKicker.removeFromKicklist("alice");
    assertFalse(UserKicker.shouldKickUser("alice"));
    assertTrue(UserKicker.shouldKickUser("bob"));
  }
}
