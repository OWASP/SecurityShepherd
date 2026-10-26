package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servlets.MobileModuleGate.Decision;
import utils.ModuleBlock;

class MobileModuleGateTest {

  @BeforeEach
  @AfterEach
  void resetBlocker() {
    ModuleBlock.reset();
  }

  @Test
  void openRunningUnblocked_isAllowed() {
    assertEquals(Decision.ALLOWED, MobileModuleGate.decide(true, true, false, false));
  }

  @Test
  void closedModule_isDeniedEvenForAdmin() {
    assertEquals(Decision.CLOSED, MobileModuleGate.decide(false, true, false, false));
    assertEquals(Decision.CLOSED, MobileModuleGate.decide(false, false, true, false));
  }

  @Test
  void eventNotRunning_deniesPlayersButNotAdmins() {
    assertEquals(Decision.NOT_RUNNING, MobileModuleGate.decide(true, false, false, false));
    assertEquals(Decision.ALLOWED, MobileModuleGate.decide(true, false, true, false));
  }

  @Test
  void blockedModule_isDenied() {
    assertEquals(Decision.BLOCKED, MobileModuleGate.decide(true, true, false, true));
  }

  @Test
  void isBlocked_onlyMatchesTheBlockedModuleWhenEnabled() {
    ModuleBlock.blockerId = "abc";
    ModuleBlock.blockerEnabled = false;
    assertFalse(MobileModuleGate.isBlocked("abc"));
    ModuleBlock.blockerEnabled = true;
    assertTrue(MobileModuleGate.isBlocked("abc"));
    assertFalse(MobileModuleGate.isBlocked("other"));
  }

  @Test
  void isAdmin_requiresAdminRole() {
    HttpSession admin = mock(HttpSession.class);
    when(admin.getAttribute("userRole")).thenReturn("admin");
    HttpSession player = mock(HttpSession.class);
    when(player.getAttribute("userRole")).thenReturn("player");
    assertTrue(MobileModuleGate.isAdmin(admin));
    assertFalse(MobileModuleGate.isAdmin(player));
    assertFalse(MobileModuleGate.isAdmin(null));
  }

  @Test
  void offlineOnlyModule_isNeverGated() {
    assertEquals(
        Decision.ALLOWED, MobileModuleGate.check(null, "not_a_server_module", null));
  }
}
