package servlets;

import dbProcs.Getter;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.CountdownHandler;
import utils.ModuleBlock;

/**
 * Applies the same availability rules the web platform enforces in {@code GetModule} to the mobile
 * endpoints: the module must be open, the CTF must be running (admins excepted) and the module must
 * not be the one currently held by the module blocker.
 *
 * <p>Offline-only mobile modules have no database row and are never gated here.
 */
final class MobileModuleGate {

  private static final Logger log = LogManager.getLogger(MobileModuleGate.class);

  enum Decision {
    ALLOWED("Allowed"),
    CLOSED("Module is not open"),
    NOT_RUNNING("The event is not currently running"),
    BLOCKED("Module is temporarily blocked");

    private final String message;

    Decision(String message) {
      this.message = message;
    }

    String message() {
      return message;
    }
  }

  private MobileModuleGate() {}

  static Decision decide(boolean moduleOpen, boolean running, boolean admin, boolean blocked) {
    if (!moduleOpen) {
      return Decision.CLOSED;
    }
    if (!running && !admin) {
      return Decision.NOT_RUNNING;
    }
    if (blocked) {
      return Decision.BLOCKED;
    }
    return Decision.ALLOWED;
  }

  static boolean isAdmin(HttpSession ses) {
    Object role = ses == null ? null : ses.getAttribute("userRole");
    return role != null && "admin".equals(role.toString());
  }

  static boolean isBlocked(String dbModuleId) {
    return ModuleBlock.blockerEnabled
        && ModuleBlock.blockerId != null
        && ModuleBlock.blockerId.equals(dbModuleId);
  }

  /**
   * Checks whether the user may start, fetch a flag for or submit a flag for the given module. Fails
   * closed if the platform state cannot be read.
   */
  static Decision check(String applicationRoot, String moduleId, HttpSession ses) {
    String dbModuleId = MobileModuleFlags.MODULE_DB_IDS.get(moduleId);
    if (dbModuleId == null) {
      return Decision.ALLOWED;
    }
    try {
      boolean open = Getter.isModuleOpen(applicationRoot, dbModuleId);
      boolean running = CountdownHandler.isRunning();
      Decision decision = decide(open, running, isAdmin(ses), isBlocked(dbModuleId));
      if (decision != Decision.ALLOWED) {
        log.debug(
            "Mobile module " + MobileModuleFlags.sanitize(moduleId) + " denied: " + decision);
      }
      return decision;
    } catch (Exception e) {
      log.error("Could not evaluate module availability: " + e.toString());
      return Decision.CLOSED;
    }
  }
}
