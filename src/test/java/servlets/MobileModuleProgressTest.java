package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

  @Test
  void elapsedSinceStart_isMinusOneWhenNeverStarted() {
    assertEquals(-1, MobileModuleProgress.elapsedSinceStartMs("u1", "m1"));
  }

  @Test
  void elapsedSinceStart_reportsTimeSinceStart() {
    MobileModuleProgress.recordStartAt("u1", "m1", System.currentTimeMillis() - 5_000);
    long elapsed = MobileModuleProgress.elapsedSinceStartMs("u1", "m1");
    assertTrue(elapsed >= 5_000 && elapsed < 15_000, "elapsed was " + elapsed);
  }

  @Test
  void elapsedSinceStart_isMinusOneWhenExpired() {
    long expired = System.currentTimeMillis() - MobileModuleProgress.expiryMs() - 1000;
    MobileModuleProgress.recordStartAt("u1", "m1", expired);
    assertEquals(-1, MobileModuleProgress.elapsedSinceStartMs("u1", "m1"));
  }

  @Test
  void restart_keepsEarliestStart() {
    MobileModuleProgress.recordStartAt("u1", "m1", System.currentTimeMillis() - 20_000);
    MobileModuleProgress.recordStart("u1", "m1");
    assertTrue(MobileModuleProgress.elapsedSinceStartMs("u1", "m1") >= 20_000);
  }

  @Test
  void restart_afterExpiry_replacesStart() {
    long expired = System.currentTimeMillis() - MobileModuleProgress.expiryMs() - 1000;
    MobileModuleProgress.recordStartAt("u1", "m1", expired);
    MobileModuleProgress.recordStart("u1", "m1");
    long elapsed = MobileModuleProgress.elapsedSinceStartMs("u1", "m1");
    assertTrue(elapsed >= 0 && elapsed < 10_000, "elapsed was " + elapsed);
  }
}
