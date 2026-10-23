package servlets;

import dbProcs.Getter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pac4j.saml.profile.SAML2Profile;
import utils.Hash;
import utils.LoginMethod;
import utils.SamlSso;
import utils.ShepherdLogManager;
import utils.SsoUserMapper;
import utils.UserKicker;

/**
 * Control class for the authentication procedure. <br>
 * <br>
 * This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.<br>
 *
 * <p>The Security Shepherd project is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.<br>
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Mark Denihan
 */
public class ACS extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(ACS.class);

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (!LoginMethod.isSaml()) {
      // SSO is off (sso.enabled in sso.properties): this endpoint must not authenticate anyone,
      // even if a SAML configuration is present
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    log.debug("**** servlets.ACS ***");
    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");
    HttpSession ses = request.getSession(true);

    Object language = ses.getAttribute("lang");

    response.setContentType("text/plain");

    log.debug("Getting ApplicationRoot");
    String ApplicationRoot = getServletContext().getRealPath("");
    log.debug("Servlet root = " + ApplicationRoot);

    log.debug("Processing SAML data...");
    Optional<SAML2Profile> profile = SamlSso.authenticate(request, response);
    log.debug("SAML data processed");

    if (!profile.isPresent()) {
      log.debug("User not authenticated");
      ses.setAttribute("loginFailed", "Not authenticated");
      response.sendRedirect("../login.jsp");
      log.debug("**** End servlets.ACS ***");
      return;
    }

    log.debug("User authenticated, unpacking SAML attributes...");
    SsoUserMapper.Result ssoUser =
        SsoUserMapper.map(profile.get().getAttributes(), loadSsoProperties());
    String errorMessage = "";
    boolean loggedIn = false;

    if (ssoUser.isValid()) {
      log.debug("ssoName = " + ssoUser.ssoName);
      log.debug("userName = " + ssoUser.userName);
      log.debug("userRole = " + ssoUser.userRole);
      log.debug("Saml userdata loaded, calling authUserSSO");

      String user[] =
          Getter.authUserSSO(
              ApplicationRoot, null, ssoUser.userName, ssoUser.ssoName, ssoUser.userRole);

      if (user != null && !user[0].isEmpty()) {

        // Kill Session and Create a new one with user logged in
        log.debug("Creating new session for " + user[2] + " " + user[1]);
        ses.invalidate();
        ses = request.getSession(true);
        ses.setAttribute("userStamp", user[0]);
        ses.setAttribute("userName", user[1]);
        ses.setAttribute("userRole", user[2]);
        ses.setAttribute("lang", language);
        // Kept for single logout, which needs the NameID and session index
        ses.setAttribute(SamlSso.PROFILE_SESSION_ATTRIBUTE, profile.get());
        // Lets an IdP-initiated logout end this session
        SamlSso.registerLoginSession(ses, profile.get());
        log.debug("userClassId = " + user[4]);

        ses.setAttribute("userClass", user[4]);

        if (user[5].equalsIgnoreCase("true")) {
          log.debug("Temporary Username Detected, user will be prompted to change");
          ses.setAttribute("ChangeUsername", "true");
        }

        log.debug("Setting CSRF cookie");
        Cookie token = new Cookie("token", Hash.randomString());
        if (request.getRequestURL().toString().startsWith("https")) // If Requested over HTTPs
        {
          token.setSecure(true);
        }

        // We must set the path because the ACS servlet is in a subdir...
        token.setPath("/");
        response.addCookie(token);

        // Removing user from kick list. If they were on it before, their suspension
        // must have ended if their authentication Succeeded
        UserKicker.removeFromKicklist(user[1]);
        loggedIn = true;
      }
    } else {
      log.debug("SSO attributes rejected: " + ssoUser.error);
      errorMessage = ssoUser.error;
    }

    if (loggedIn) {
      response.sendRedirect("../index.jsp");
    } else {
      log.debug("Could not authenticate");
      ses.setAttribute("loginFailed", errorMessage + "SSO login failed.");
      // Lagging Response
      try {
        Thread.sleep(2000);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
      response.sendRedirect("../login.jsp");
    }

    log.debug("**** End servlets.ACS ***");
  }

  /** Redirects user to index.jsp */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.sendRedirect("../index.jsp");
  }

  private Properties loadSsoProperties() {
    String unpackFileName = "sso.properties";
    log.debug("Loading saml unpack properties file");
    try (InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream(unpackFileName)) {
      if (inputStream == null) {
        String errorMsg =
            "SAML unpack properties file '" + unpackFileName + "' not found in the classpath";
        log.error(errorMsg);
        throw new RuntimeException(errorMsg);
      }
      Properties prop = new Properties();
      prop.load(inputStream);
      return prop;
    } catch (IOException e) {
      String errorMsg = "SAML unpack properties file '" + unpackFileName + "' cannot be loaded";
      log.error(errorMsg);
      throw new RuntimeException(errorMsg);
    }
  }
}
