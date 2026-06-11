package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
 * Intentionally insecure mobile API endpoint used by the Insecure Communication challenges.
 *
 * <p>This endpoint simulates a legacy backend session API that transmits a sensitive session token
 * over plain HTTP. Students configure a proxy (e.g. Burp Suite, mitmproxy) on their device and
 * intercept the HTTP response to extract the {@code session_token} field, which is the flag for the
 * relevant module.
 *
 * <p>Request (GET, plain HTTP):
 *
 * <pre>
 *   moduleId â€" Mobile module identifier (e.g. "insecure_comm_challenge")
 * </pre>
 *
 * <p>Authentication via the Shepherd session cookie (JSESSIONID), which is transmitted in plaintext
 * over HTTP â€" demonstrating session-hijacking via unencrypted transport.
 *
 * <p>Response (application/json):
 *
 * <pre>
 *   {
 *     "status":        "ok",
 *     "user_id":       "u_&lt;hash&gt;",
 *     "session_token": "&lt;user-specific flag&gt;",
 *     "role":          "user",
 *     "server":        "legacy-api/1.0",
 *     "expires":       "&lt;ISO-8601 timestamp&gt;"
 *   }
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
public class MobileInsecureApi extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(MobileInsecureApi.class);

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.MobileInsecureApi ****");

    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    // Intentionally omit security headers to simulate a legacy insecure service
    response.setHeader("Server", "legacy-api/1.0");
    response.setHeader("X-Powered-By", "Apache-Coyote/1.1");

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

    String userName = (String) ses.getAttribute("userName");

    String baseFlag = MobileModuleFlags.BASE_FLAGS.get(moduleId);
    if (baseFlag == null) {
      log.debug("Unknown mobile module ID: " + MobileModuleFlags.sanitize(moduleId));
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      out.write(errorJson("Module not found"));
      return;
    }

    String sessionToken;
    try {
      // Same HMAC derivation as MobileFlagGet / MobileFlagSubmit — unique per user per session
      sessionToken = Hash.generateUserSolutionKeyOnly(baseFlag, userName);
    } catch (Exception e) {
      log.error("Flag generation error: " + e.toString());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      out.write(errorJson("Server error"));
      return;
    }

    // Build a realistic-looking session response with the flag embedded as session_token
    String userId = "u_" + String.format("%04x", userName.hashCode() & 0xffff);
    String expiresAt = Instant.now().plus(1, ChronoUnit.HOURS).toString();

    JSONObject json = new JSONObject();
    json.put("status", "ok");
    json.put("user_id", userId);
    json.put("session_token", sessionToken);
    json.put("role", "user");
    json.put("server", "legacy-api/1.0");
    json.put("expires", expiresAt);

    log.debug(
        "MobileInsecureApi: served session token for "
            + MobileModuleFlags.sanitize(moduleId)
            + " to "
            + MobileModuleFlags.sanitize(userName));
    response.setStatus(HttpServletResponse.SC_OK);
    out.write(json.toString());
  }

  private static String errorJson(String message) {
    return new JSONObject().put("error", message).toString();
  }
}
