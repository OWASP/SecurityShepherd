package servlets;

import dbProcs.Getter;
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
import utils.ScoreboardStatus;
import utils.ShepherdLogManager;

/**
 * Stateless scoreboard endpoint for the Security Shepherd mobile app.
 *
 * <p>Mirrors the permission and class-resolution logic of {@link GetJsonScore} (the web scoreboard
 * endpoint), but without a CSRF token requirement, since this endpoint is read-only and
 * authenticated solely via the Shepherd session cookie, matching every other mobile endpoint. It
 * also reports failures as explicit JSON states with HTTP status codes, rather than {@link
 * GetJsonScore}'s approach of writing a literal {@code "ERROR:"}-prefixed string with HTTP 200 (or
 * a {@code null} body on a SQL failure), which made the web scoreboard fail silently.
 *
 * <p>Request: GET, no parameters.
 *
 * <p>Authentication via the Shepherd session cookie (JSESSIONID) set during mobile login. If the
 * scoreboard is configured as public, anonymous (no session) requests are also served.
 *
 * <p>Response (application/json):
 *
 * <pre>
 *   {"available":true,"scoreboard":[...]}   - success (array may be empty)
 *   {"available":false,"message":"..."}     - not authenticated, not permitted, or server error
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
 */
public class MobileScoreboard extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(MobileScoreboard.class);

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.MobileScoreboard ****");

    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    PrintWriter out = response.getWriter();

    HttpSession ses = request.getSession(false);
    boolean loggedIn = MobileSession.isAuthenticated(ses);

    try {
      boolean isPublic = ScoreboardStatus.isPublicScoreboard();

      if (!loggedIn && !isPublic) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        out.write(errorJson("Not authenticated"));
        return;
      }

      String userRole = loggedIn ? (String) ses.getAttribute("userRole") : null;
      boolean canSeeScoreboard = ScoreboardStatus.canSeeScoreboard(userRole);

      if (!canSeeScoreboard && !isPublic) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        out.write(errorJson("Scoreboard is not currently available"));
        return;
      }

      // What class to list? Mirrors GetJsonScore's resolution, guarding for the anonymous
      // (public scoreboard, no session) case GetJsonScore itself does not handle.
      String scoreboardClass;
      if (ScoreboardStatus.getClassSpecificScoreboard()) {
        if (loggedIn && "admin".equals(userRole)) {
          // Admin should get the default class scoreboard
          scoreboardClass = Register.getDefaultClass();
        } else if (loggedIn && ses.getAttribute("userClass") != null) {
          // User should get their class scoreboard
          scoreboardClass = ses.getAttribute("userClass").toString();
        } else {
          scoreboardClass = "";
        }
      } else {
        scoreboardClass = ScoreboardStatus.getScoreboardClass();
      }

      String applicationRoot = getServletContext().getRealPath("");
      String jsonOutput = Getter.getJsonScore(applicationRoot, scoreboardClass);

      if (jsonOutput == null) {
        log.error("Getter.getJsonScore returned null (SQL failure) for class " + scoreboardClass);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        out.write(errorJson("Scoreboard temporarily unavailable"));
        return;
      }

      out.write(
          jsonOutput.isEmpty()
              ? "{\"available\":true,\"scoreboard\":[]}"
              : "{\"available\":true,\"scoreboard\":" + jsonOutput + "}");
      log.debug("**** END MobileScoreboard ****");
    } catch (Exception e) {
      log.error("Failed to build scoreboard: " + e.toString());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      out.write(errorJson("Scoreboard temporarily unavailable"));
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  private static String errorJson(String message) {
    JSONObject obj = new JSONObject();
    obj.put("available", false);
    obj.put("message", message);
    return obj.toString();
  }
}
