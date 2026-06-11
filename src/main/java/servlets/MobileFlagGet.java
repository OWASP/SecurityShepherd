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
import utils.Hash;
import utils.ShepherdLogManager;

/**
 * Returns a user-specific dynamic flag for the requested mobile module. <br>
 * <br>
 * The flag is the HMAC-SHA512 of the module's base flag keyed with the server's ephemeral key and
 * salted with the authenticated user's name — matching exactly what {@link MobileFlagSubmit} will
 * recompute during validation. This means:
 *
 * <ul>
 *   <li>Each student receives a different flag string.
 *   <li>Flags cannot be shared between students.
 *   <li>The base flag value is never sent to the client.
 * </ul>
 *
 * <p>Request (POST, application/x-www-form-urlencoded):
 *
 * <pre>
 *   moduleId â€" Mobile module identifier (e.g. "client_side_injection_lesson")
 * </pre>
 *
 * <p>Authentication via the Shepherd session cookie (JSESSIONID) set during mobile login.
 *
 * <p>Response (application/json):
 *
 * <pre>
 *   {"flag":"&lt;hmac-hex&gt;"}                  – authenticated, module known
 *   {"error":"..."}                          – authentication failure or unknown module
 * </pre>
 *
 * <p>This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.<br>
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Sean Duggan
 */
public class MobileFlagGet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(MobileFlagGet.class);

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.MobileFlagGet ****");

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
    String userName = (String) ses.getAttribute("userName");

    String applicationRoot = getServletContext().getRealPath("");

    String baseFlag = MobileModuleFlags.BASE_FLAGS.get(moduleId);
    if (baseFlag == null) {
      log.debug("Unknown mobile module ID: " + MobileModuleFlags.sanitize(moduleId));
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      out.write(errorJson("Module not found"));
      return;
    }

    // Gate: only deliver a flag if the student has opened this module via the app.
    // This prevents bulk flag farming by calling this endpoint directly after login.
    boolean started = MobileModuleProgress.hasStarted(userId, moduleId);
    if (!started) {
      // In-memory cache miss — could be a server restart. Fall back to DB.
      String dbModuleId = MobileModuleFlags.MODULE_DB_IDS.get(moduleId);
      if (dbModuleId != null) {
        started = Getter.hasPlayerStarted(applicationRoot, dbModuleId, userId);
        if (started) {
          // Warm the in-memory cache so subsequent calls don't hit the DB.
          MobileModuleProgress.recordStart(userId, moduleId);
          log.debug(
              "Gate: DB start record found for "
                  + MobileModuleFlags.sanitize(moduleId)
                  + ", cache warmed");
        }
      }
    }
    if (!started) {
      log.debug(
          "Flag requested for "
              + MobileModuleFlags.sanitize(moduleId)
              + " by "
              + MobileModuleFlags.sanitize(userName)
              + " but module not started");
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      out.write(errorJson("Module not started — open it in the app first"));
      return;
    }

    // Derive a user-specific flag. Hash.generateUserSolutionKeyOnly uses HmacSHA512
    // keyed with the server's ephemeral key — same computation as MobileFlagSubmit.
    String dynamicFlag = Hash.generateUserSolutionKeyOnly(baseFlag, userName);
    log.debug(
        "Returning dynamic flag for "
            + MobileModuleFlags.sanitize(moduleId)
            + " to "
            + MobileModuleFlags.sanitize(userName));

    JSONObject result = new JSONObject();
    result.put("flag", dynamicFlag);
    out.write(result.toString());
    log.debug("**** END MobileFlagGet ****");
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
