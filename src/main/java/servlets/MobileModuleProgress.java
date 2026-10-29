package servlets;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * In-memory tracking of which mobile modules each authenticated user has opened via the app.
 *
 * <p>Used by {@link MobileFlagGet} to gate flag delivery — a flag is only returned once the student
 * has navigated to the corresponding module through the app. This prevents a student from
 * bulk-fetching all flags via direct API calls immediately after logging in.
 *
 * <p>State is ephemeral: it resets on server restart. Students who re-open a module after a server
 * restart will automatically re-trigger {@link MobileModuleStart}, re-recording the start.
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
    Long ts = STARTED.get(userId + "|" + moduleId);
    if (ts == null) return false;
    if (System.currentTimeMillis() - ts > EXPIRY_MS) {
      STARTED.remove(userId + "|" + moduleId);
      return false;
    }
    return true;
  }
}
