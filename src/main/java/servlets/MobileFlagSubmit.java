package servlets;

import dbProcs.Getter;
import dbProcs.Setter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import utils.Hash;
import utils.ShepherdLogManager;

/**
 * Stateless flag-validation endpoint for the Security Shepherd mobile app.
 *
 * <p>Validates the flag the student discovered inside a challenge by recomputing the same
 * user-specific HMAC that {@link MobileFlagGet} produced. Because validation is HMAC-based rather
 * than a static hash comparison, each student has a unique flag and flags cannot be shared or
 * precomputed from the APK.
 *
 * <p>Request (POST, application/x-www-form-urlencoded):
 *
 * <pre>
 *   moduleId - Module identifier string (e.g. "client_side_injection_lesson")
 *   flag     - Flag string discovered by the student
 * </pre>
 *
 * <p>Authentication via the Shepherd session cookie (JSESSIONID) set during mobile login.
 *
 * <p>Response (application/json):
 *
 * <pre>
 *   {"correct":true}
 *   {"correct":false,"message":"..."}
 * </pre>
 *
 * <p>This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see http://www.gnu.org/licenses/.
 *
 * @author Sean Duggan
 */
public class MobileFlagSubmit extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(MobileFlagSubmit.class);

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.MobileFlagSubmit ****");

    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    PrintWriter out = response.getWriter();

    String moduleId = request.getParameter("moduleId");
    String flag = request.getParameter("flag");

    if (moduleId == null || flag == null) {
      log.debug("Missing required parameters");
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      out.write(errorJson("Missing required parameters"));
      return;
    }

    HttpSession ses = request.getSession(false);
    if (!MobileSession.isAuthenticated(ses)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      out.write(errorJson("Not authenticated"));
      return;
    }

    String userId = (String) ses.getAttribute("userStamp");
    String userName = (String) ses.getAttribute("userName");

    String applicationRoot = getServletContext().getRealPath("");

    String baseFlag = MobileModuleFlags.BASE_FLAGS.get(moduleId);
    if (baseFlag == null) {
      log.debug("Unknown mobile module ID: " + MobileModuleFlags.sanitize(moduleId));
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      out.write(errorJson("Module not found"));
      return;
    }

    MobileModuleGate.Decision gate = MobileModuleGate.check(applicationRoot, moduleId, ses);
    if (gate != MobileModuleGate.Decision.ALLOWED) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      out.write(errorJson(gate.message()));
      return;
    }

    // Recompute the same HMAC that MobileFlagGet produced for this user.
    // Hash.generateUserSolutionKeyOnly uses HmacSHA512 keyed with the server`s
    // ephemeral key, which is constant for the lifetime of the server process.
    String expectedFlag = Hash.generateUserSolutionKeyOnly(baseFlag, userName);
    boolean correct = expectedFlag != null && expectedFlag.equalsIgnoreCase(flag.trim());

    log.debug(
        "Flag submission for "
            + MobileModuleFlags.sanitize(moduleId)
            + " by "
            + MobileModuleFlags.sanitize(userName)
            + ": "
            + (correct ? "correct" : "incorrect"));

    if (correct) {
      // Record completion and award points if this module has a DB entry
      String dbModuleId = MobileModuleFlags.MODULE_DB_IDS.get(moduleId);
      if (dbModuleId != null) {
        try {
          // Mobile users never go through GetModule, so there may be no results row.
          // Calling getModuleAddress triggers moduleGetHash which inserts one if missing.
          Getter.getModuleAddress(applicationRoot, dbModuleId, userId);
          // checkPlayerResult returns the module name when in-progress (not yet completed).
          // Returns null when already completed or when there was no row (both safe to skip).
          String inProgress = Getter.checkPlayerResult(applicationRoot, dbModuleId, userId);
          if (inProgress != null) {
            // updatePlayerResult returns null when the CTF isn't running or on a SQL error.
            if (Setter.updatePlayerResult(
                    applicationRoot, dbModuleId, userId, "Mobile App Submission", 1, 1, 1)
                == null) {
              throw new IllegalStateException("updatePlayerResult did not record the result");
            }
            log.debug(
                "Score recorded for "
                    + MobileModuleFlags.sanitize(moduleId)
                    + " ("
                    + dbModuleId
                    + ") by "
                    + MobileModuleFlags.sanitize(userName));
          } else {
            log.debug(
                "Module "
                    + MobileModuleFlags.sanitize(moduleId)
                    + " already completed by "
                    + MobileModuleFlags.sanitize(userName)
                    + ", no score change");
          }
        } catch (Exception e) {
          // Don't report a correct flag as success when the score wasn't saved. A non-200 makes
          // the app fall back to offline validation, and a retry is safe: scoring is idempotent
          // because checkPlayerResult returns null once the module is completed.
          log.error(
              "Failed to record score for "
                  + MobileModuleFlags.sanitize(moduleId)
                  + ": "
                  + e.toString());
          response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
          out.write(errorJson("Flag is correct but the score could not be recorded. Try again."));
          return;
        }
      }
    }

    JSONObject result = new JSONObject();
    result.put("correct", correct);
    out.write(result.toString());
    log.debug("**** END MobileFlagSubmit ****");
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  private static String errorJson(String message) {
    JSONObject obj = new JSONObject();
    obj.put("correct", false);
    obj.put("message", message);
    return obj.toString();
  }
}
