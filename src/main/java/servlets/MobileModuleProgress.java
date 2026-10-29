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
 * <p>State is ephemeral: it resets on server restart. The app must call {@link MobileModuleStart}
 * again after a restart (it does so whenever a module screen becomes visible) before {@link
 * MobileFlagGet} will return a flag.
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

  private static final ConcurrentHashMap<String, Long> STARTED = new ConcurrentHashMap<>();

  /** Records that {@code userId} has opened {@code moduleId} in the app. */
  static void recordStart(String userId, String moduleId) {
    STARTED.put(userId + "|" + moduleId, System.currentTimeMillis());
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
