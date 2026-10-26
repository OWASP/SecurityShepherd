package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import utils.ShepherdLogManager;

/**
 * Tells the mobile app which modules the signed-in user can currently open, so it can grey out the
 * rest instead of letting the student hit a 403 from {@link MobileModuleStart}.
 *
 * <p>The answer comes from {@link MobileModuleGate}, the same check those endpoints enforce, so the
 * app mirrors the platform's open floor / CTF rules: module closed, event not running (admins
 * excepted) and the module held by the module blocker. This endpoint is advisory only; the
 * authoritative check stays on the endpoints that start a module and hand out or accept flags.
 *
 * <p>Request: GET, no parameters. Authentication via the Shepherd session cookie (JSESSIONID).
 *
 * <p>Response (application/json):
 *
 * <pre>
 *   {"available":true,"modules":{"client_side_injection_lesson":{"status":"ALLOWED"},
 *                                "client_side_injection_challenge_1":{"status":"CLOSED",
 *                                "message":"Module is not open"}}}
 *   {"available":false,"message":"..."}   - not authenticated or server error
 * </pre>
 *
 * <p>Modules that have no entry (offline-only modules) are always available.
 *
 * <p>This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 */
public class MobileModuleStatus extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(MobileModuleStatus.class);

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.MobileModuleStatus ****");

    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    PrintWriter out = response.getWriter();

    HttpSession ses = request.getSession(false);
    if (ses == null
        || ses.getAttribute("userName") == null
        || ses.getAttribute("userStamp") == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      out.write(errorJson("Not authenticated"));
      return;
    }

    try {
      String applicationRoot = getServletContext().getRealPath("");
      Map<String, MobileModuleGate.Decision> decisions = new TreeMap<>();
      for (String moduleId : MobileModuleFlags.MODULE_DB_IDS.keySet()) {
        decisions.put(moduleId, MobileModuleGate.check(applicationRoot, moduleId, ses));
      }
      out.write(toJson(decisions));
      log.debug("**** END MobileModuleStatus ****");
    } catch (Exception e) {
      log.error("Failed to build module status: " + e.toString());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      out.write(errorJson("Module status temporarily unavailable"));
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  static String toJson(Map<String, MobileModuleGate.Decision> decisions) {
    JSONObject modules = new JSONObject();
    for (Map.Entry<String, MobileModuleGate.Decision> entry : decisions.entrySet()) {
      JSONObject module = new JSONObject();
      module.put("status", entry.getValue().name());
      if (entry.getValue() != MobileModuleGate.Decision.ALLOWED) {
        module.put("message", entry.getValue().message());
      }
      modules.put(entry.getKey(), module);
    }
    JSONObject result = new JSONObject();
    result.put("available", true);
    result.put("modules", modules);
    return result.toString();
  }

  private static String errorJson(String message) {
    JSONObject obj = new JSONObject();
    obj.put("available", false);
    obj.put("message", message);
    return obj.toString();
  }
}
