package servlets;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * In-memory tracking of which mobile modules each authenticated user has opened via the app.
 *
 * <p>Used by {@link MobileFlagGet} to gate flag delivery — a flag is only returned once {@link
 * MobileModuleStart} has been called for the module. This deters accidental or lazy bulk fetching
 * of flags right after login. It is not a security boundary: a deliberate client can call start
 * then get without opening the module.
 *
 * <p>{@link MobileFlagSubmit} also uses the start time to enforce a minimum time between opening a
 * module and a correct submission ({@link #MIN_SOLVE_MS}). This is a deterrent only. The flag is
 * delivered to the device by design, so a student who scripts start, get and submit and waits out
 * the minimum can still score a module without solving it.
 *
 * <p>Known gap, planned for a later release: closing it needs a server-side challenge per module
 * (for example, a server-issued value the student can only derive by completing the exercise)
 * rather than a client-held flag. Until then, mobile scoring is honour-system.
 *
 * <p>State is ephemeral: it resets on server restart. The app must call {@link MobileModuleStart}
 * again after a restart (it does so whenever a module screen becomes visible) before {@link
 * MobileFlagGet} will return a flag. A missing or expired record means no minimum-time check is
 * applied on submit.
 *
 * <p>This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Sean Duggan
 */
final class MobileModuleProgress {

  private MobileModuleProgress() {}

  /** Entries expire after this duration to prevent unbounded heap growth. */
  private static final long EXPIRY_MS = 24L * 60 * 60 * 1000;

  /** Minimum time between opening a module and a correct flag submission being scored. */
  static final long MIN_SOLVE_MS = 30_000L;

  private static final ConcurrentHashMap<String, Long> STARTED = new ConcurrentHashMap<>();

  /**
   * Records that {@code userId} has opened {@code moduleId} in the app. Re-opening a module keeps
   * the earliest unexpired start so the minimum-solve-time clock cannot be reset by calling start
   * again.
   */
  static void recordStart(String userId, String moduleId) {
    long now = System.currentTimeMillis();
    STARTED.merge(
        userId + "|" + moduleId, now, (old, fresh) -> fresh - old > EXPIRY_MS ? fresh : old);
    // Probabilistic cleanup: purge expired entries on ~1% of calls to bound heap usage.
    if (ThreadLocalRandom.current().nextDouble() < 0.01) {
      long cutoff = System.currentTimeMillis() - EXPIRY_MS;
      STARTED.values().removeIf(ts -> ts < cutoff);
    }
  }

  /** Returns {@code true} if {@code userId} has previously opened {@code moduleId}. */
  static boolean hasStarted(String userId, String moduleId) {
    String key = userId + "|" + moduleId;
    Long ts = STARTED.get(key);
    if (ts == null) return false;
    if (System.currentTimeMillis() - ts > EXPIRY_MS) {
      // Conditional remove so a concurrent recordStart's fresh timestamp isn't discarded.
      STARTED.remove(key, ts);
      return false;
    }
    return true;
  }

  /**
   * Returns milliseconds since {@code userId} first opened {@code moduleId}, or -1 if there is no
   * unexpired record (never started, expired, or the server restarted).
   */
  static long elapsedSinceStartMs(String userId, String moduleId) {
    Long ts = STARTED.get(userId + "|" + moduleId);
    if (ts == null) return -1;
    long elapsed = System.currentTimeMillis() - ts;
    return elapsed > EXPIRY_MS ? -1 : Math.max(elapsed, 0);
  }

  /** Test hook: records a start with an explicit timestamp. */
  static void recordStartAt(String userId, String moduleId, long timestampMs) {
    STARTED.put(userId + "|" + moduleId, timestampMs);
  }

  /** Test hook: clears all state. */
  static void clear() {
    STARTED.clear();
  }

  /** Test hook: the expiry window in milliseconds. */
  static long expiryMs() {
    return EXPIRY_MS;
  }
}
