package servlets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MobileModuleProgressTest {

  @BeforeEach
  @AfterEach
  void reset() {
    MobileModuleProgress.clear();
  }

  @Test
  void unknownPair_hasNotStarted() {
    assertFalse(MobileModuleProgress.hasStarted("u1", "m1"));
  }

  @Test
  void recordedStart_isReported() {
    MobileModuleProgress.recordStart("u1", "m1");
    assertTrue(MobileModuleProgress.hasStarted("u1", "m1"));
  }

  @Test
  void start_isScopedToUserAndModule() {
    MobileModuleProgress.recordStart("u1", "m1");
    assertFalse(MobileModuleProgress.hasStarted("u2", "m1"));
    assertFalse(MobileModuleProgress.hasStarted("u1", "m2"));
  }

  @Test
  void expiredStart_isNotReportedAndIsRemoved() {
    long expired = System.currentTimeMillis() - MobileModuleProgress.expiryMs() - 1000;
    MobileModuleProgress.recordStartAt("u1", "m1", expired);
    assertFalse(MobileModuleProgress.hasStarted("u1", "m1"));

    // Starting again after expiry works.
    MobileModuleProgress.recordStart("u1", "m1");
    assertTrue(MobileModuleProgress.hasStarted("u1", "m1"));
  }
}
