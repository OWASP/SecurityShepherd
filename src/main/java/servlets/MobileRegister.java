package servlets;

import dbProcs.Getter;
import dbProcs.Setter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import utils.OpenRegistration;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Stateless registration endpoint for the Security Shepherd mobile app. <br>
 * <br>
 * Unlike the web {@link Register} servlet, this endpoint does not require a session or CSRF token
 * because registration is always initiated by a human using the mobile app's sign-in screen.
 * All standard input validation (username format, password strength, email format) is still
 * enforced.
 *
 * <p>Request (POST, application/x-www-form-urlencoded):
 *
 * <pre>
 *   userName  – Desired username
 *   passWord  – Desired password
 *   userAddress – Email address (optional but validated if provided)
 * </pre>
 *
 * <p>Response (application/json):
 *
 * <pre>
 *   {"success":true}
 *   {"success":false,"message":"..."}
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
public class MobileRegister extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(MobileRegister.class);

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    ShepherdLogManager.setRequestIp(
        request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.MobileRegister ****");

    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    PrintWriter out = response.getWriter();

    if (!OpenRegistration.isEnabled()) {
      log.debug("Registration is closed");
      out.write(errorJson("Registration is currently closed on this server"));
      return;
    }

    String userName = request.getParameter("userName");
    String passWord = request.getParameter("passWord");
    String userAddress = request.getParameter("userAddress");

    if (userName == null || userName.isEmpty() || passWord == null || passWord.isEmpty()) {
      out.write(errorJson("Username and password are required"));
      return;
    }

    if (userAddress == null) {
      userAddress = "";
    }

    boolean validAddress = userAddress.isEmpty()
        || Validate.isValidEmailAddress(userAddress);

    boolean userValidate;
    if (validAddress && !userAddress.isEmpty()) {
      userValidate = Validate.isValidUser(userName, passWord, userAddress);
    } else {
      userValidate = Validate.isValidUser(userName, passWord);
    }

    if (!userValidate) {
      out.write(errorJson("Invalid username or password format"));
      return;
    }

    if (!validAddress) {
      out.write(errorJson("Invalid email address format"));
      return;
    }

    String applicationRoot = getServletContext().getRealPath("");
    try {
      String defaultClass = Getter.getDefaultClass(applicationRoot);
      String classId = (defaultClass == null || defaultClass.isEmpty()) ? null : defaultClass;
      Setter.userCreate(applicationRoot, classId, userName, passWord, "player", userAddress, false);
      log.debug("Mobile registration successful for: " + userName);
      JSONObject result = new JSONObject();
      result.put("success", true);
      out.write(result.toString());
    } catch (SQLException e) {
      log.error("Registration DB error: " + e.toString());
      out.write(errorJson("Registration failed — username may already be taken"));
    } catch (Exception e) {
      log.error("Registration error: " + e.toString());
      out.write(errorJson("Server error"));
    }

    log.debug("**** END MobileRegister ****");
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  private static String errorJson(String message) {
    JSONObject obj = new JSONObject();
    obj.put("success", false);
    obj.put("message", message);
    return obj.toString();
  }
}
