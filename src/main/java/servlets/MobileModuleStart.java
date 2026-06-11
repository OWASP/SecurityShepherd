package servlets;

import dbProcs.Getter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import utils.ShepherdLogManager;

/**
 * Records that an authenticated student has opened a mobile module in the app.
 *
 * <p>{@link MobileFlagGet} will not return a flag for a module until this endpoint has been called
 * for that user/module pair. The Android app calls this automatically when a lesson or challenge
 * fragment becomes visible, so legitimate use is transparent to the student.
 *
 * <p>This two-step requirement (start → get) prevents a student from bulk-fetching all flags via
 * direct API calls immediately after logging in without interacting with any module.
 *
 * <p>For offline-only modules (e.g. Reverse Engineering) that have no entry in {@link
 * MobileModuleFlags#BASE_FLAGS}, this endpoint silently succeeds — the gate in {@code
 * MobileFlagGet} is never reached for those modules anyway.
 *
 * <p>Request (POST, application/x-www-form-urlencoded):
 *
 * <pre>
 *   moduleId â€" Mobile module identifier (e.g. "insecure_comm_lesson")
 * </pre>
 *
 * <p>Authentication via the Shepherd session cookie (JSESSIONID) set during mobile login.
 *
 * <p>Response (application/json):
 *
 * <pre>
 *   {"started":true}   – success (or offline-only module)
 *   {"error":"..."}    – authentication failure or missing parameters
 * </pre>
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
public class MobileModuleStart extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(MobileModuleStart.class);

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.MobileModuleStart ****");

    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    PrintWriter out = response.getWriter();

    String moduleId = request.getParameter("moduleId");

    if (moduleId == null) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      out.write(errorJson("Missing required parameters"));
      return;
    }

    HttpSession ses = request.getSession(false);
    if (ses == null
        || ses.getAttribute("userName") == null
        || ses.getAttribute("userStamp") == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      out.write(errorJson("Not authenticated"));
      return;
    }

    String userId = (String) ses.getAttribute("userStamp");

    String applicationRoot = getServletContext().getRealPath("");

    // Offline-only modules (not in BASE_FLAGS) have no server-side flag to gate.
    // Return success immediately so the app does not need special-case handling.
    if (!MobileModuleFlags.BASE_FLAGS.containsKey(moduleId)) {
      JSONObject result = new JSONObject();
      result.put("started", true);
      out.write(result.toString());
      log.debug(
          "Module start: "
              + MobileModuleFlags.sanitize(moduleId)
              + " is offline-only, silently accepted");
      return;
    }

    // Record in-memory for fast gate checks within this server session.
    MobileModuleProgress.recordStart(userId, moduleId);

    // Also DB-persist via moduleGetHash so the start survives server restarts.
    // getModuleAddress is idempotent: it only inserts a results row if one doesn't exist.
    String dbModuleId = MobileModuleFlags.MODULE_DB_IDS.get(moduleId);
    if (dbModuleId != null) {
      try {
        Getter.getModuleAddress(applicationRoot, dbModuleId, userId);
        log.debug(
            "DB start row ensured for "
                + MobileModuleFlags.sanitize(moduleId)
                + " ("
                + dbModuleId
                + ")");
      } catch (Exception e) {
        // Non-fatal: in-memory record is still set; gate will fall back to DB on next check.
        log.warn(
            "DB start record failed for "
                + MobileModuleFlags.sanitize(moduleId)
                + ": "
                + e.toString());
      }
    }

    log.debug(
        "Module start recorded: " + MobileModuleFlags.sanitize(moduleId) + " for userId " + userId);

    JSONObject result = new JSONObject();
    result.put("started", true);
    out.write(result.toString());
    log.debug("**** END MobileModuleStart ****");
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  private static String errorJson(String message) {
    JSONObject obj = new JSONObject();
    obj.put("error", message);
    return obj.toString();
  }
}
