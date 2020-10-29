package servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;

import utils.Hash;
import utils.ShepherdLogManager;
import utils.UserKicker;
import dbProcs.Getter;

/**
 * Control class for the authentication procedure. <br/>
 * <br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Mark Denihan
 *
 */
public class ACS extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(ACS.class);

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("**** servlets.ACS ***");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		HttpSession ses = request.getSession(true);

		Object language = ses.getAttribute("lang");

		String errorMessage = new String();

		request.setCharacterEncoding("UTF-8");

		response.setContentType("text/plain");

		log.debug("Getting ApplicationRoot");
		String ApplicationRoot = getServletContext().getRealPath("");
		log.debug("Servlet root = " + ApplicationRoot);

		boolean mustRedirect = false;
		boolean ssoValid = false;

		// Start SAML processing

		log.debug("Processing SAML data...");

		Auth auth;
		try {
			auth = new Auth(request, response);
		} catch (SettingsException e) {
			throw new RuntimeException("SAML not configured: " + e.toString());
		} catch (Error e) {
			throw new RuntimeException("SAML error : " + e.toString());
		}

		try {
			auth.processResponse();
		} catch (Exception e) {
			throw new RuntimeException("SAML error when processing response: " + e.toString());
		}

		log.debug("SAML data processed");

		if (!auth.isAuthenticated()) {
			log.debug("User not authenticated");

			errorMessage += "Not authenticated";
			ses.setAttribute("loginFailed", errorMessage);
			response.sendRedirect("../login.jsp");

		} else {
			log.debug("User authenticated");
			List<String> errors = auth.getErrors();
			if (!errors.isEmpty()) {
				errorMessage += StringUtils.join(errors, ", ");
				log.debug("SAML errors found: " + StringUtils.join(errors, ", "));
			} else {
				Map<String, List<String>> attributes = auth.getAttributes();
				String nameId = auth.getNameId();
				String nameIdFormat = auth.getNameIdFormat();
				String sessionIndex = auth.getSessionIndex();
				String nameidNameQualifier = auth.getNameIdNameQualifier();
				String nameidSPNameQualifier = auth.getNameIdSPNameQualifier();

				ses.setAttribute("attributes", attributes);
				ses.setAttribute("nameId", nameId);
				ses.setAttribute("nameIdFormat", nameIdFormat);
				ses.setAttribute("sessionIndex", sessionIndex);

				ses.setAttribute("nameidNameQualifier", nameidNameQualifier);
				ses.setAttribute("nameidSPNameQualifier", nameidSPNameQualifier);

				if (attributes.isEmpty()) {
					errorMessage += "You don't have any attributes";
					log.debug("No SAML attributes found");

				} else {
					log.debug("Unpacking SAML attributes...");

					String ssoName = null;
					String userName = null;
					String userRole = null;

					ClassLoader classLoader = getClass().getClassLoader();

					log.debug("Loading saml unpack properties file");

					String unpackFileName = "sso.properties";

					try (InputStream inputStream = classLoader.getResourceAsStream(unpackFileName)) {
						if (inputStream != null) {
							Properties prop = new Properties();
							prop.load(inputStream);
							if (prop != null) {

								log.debug("Saml unpack properties file loaded, unpacking saml data");

								// Get id and name from SAML data

								String ssoNameKey = prop.getProperty("sso.saml.ssoName");

								ssoName = attributes.get(ssoNameKey).get(0);

								log.debug("ssoName = " + ssoName);

								String userNameKey = prop.getProperty("sso.saml.userName");

								userName = attributes.get(userNameKey).get(0);

								log.debug("userName = " + userName);

								String affiliationKey = prop.getProperty("sso.saml.affiliation");

								List<String> affiliations = attributes.get(affiliationKey);

								String adminAffiliation = prop.getProperty("sso.saml.adminAffiliation");
								String playerAffiliation = prop.getProperty("sso.saml.playerAffiliation");

								List<String> adminAffiliations = Arrays.asList(adminAffiliation.split(",[ ]*"));
								List<String> playerAffiliations = Arrays.asList(playerAffiliation.split(",[ ]*"));

								boolean foundAdmin = false;
								boolean foundPlayer = false;

								for (String affiliation : adminAffiliations) {
									if (affiliations.contains(affiliation)) {
										foundAdmin = true;
									}
								}

								if (!foundAdmin) {
									for (String affiliation : playerAffiliations) {
										if (affiliations.contains(affiliation)) {
											foundPlayer = true;
										}
									}
								}

								if (foundAdmin) {
									userRole = "admin";
									ssoValid = true;
								} else if (foundPlayer) {
									userRole = "player";
									ssoValid = true;
								} else {
									ssoValid = false;

									errorMessage += "Authorization failed. Please ensure that you are a member of one of the following groups: ";

									for (String affiliation : adminAffiliations) {
										errorMessage += affiliation + ", ";
									}

									for (String affiliation : playerAffiliations) {
										errorMessage += affiliation + ", ";
									}
								}

								log.debug("userRole = " + userRole);

							}
						} else {
							String errorMsg = "SAML unpack properties file '" + unpackFileName
									+ "' not found in the classpath";
							log.error(errorMsg);
							throw new RuntimeException(errorMsg);
						}
					} catch (IOException e) {
						String errorMsg = "SAML unpack properties file '" + unpackFileName + "' cannot be loaded";

						log.error(errorMsg);
						throw new RuntimeException(errorMsg);

					}

					if (ssoValid) {

						if (ssoName == null || userName == null || userRole == null) {
							String errorMsg = "Unknown error occured when unpacking SAML properties";

							log.error(errorMsg);
							throw new RuntimeException(errorMsg);
						}

						log.debug("Saml userdata loaded, calling authUserSSO");

						String user[] = Getter.authUserSSO(ApplicationRoot, null, userName, ssoName, userRole);

						if (user != null && !user[0].isEmpty()) {

							// Kill Session and Create a new one with user logged in
							log.debug("Creating new session for " + user[2] + " " + user[1]);
							ses.invalidate();
							ses = request.getSession(true);
							ses.setAttribute("userStamp", user[0]);
							ses.setAttribute("userName", user[1]);
							ses.setAttribute("userRole", user[2]);
							ses.setAttribute("lang", language);
							log.debug("userClassId = " + user[4]);

							ses.setAttribute("userClass", user[4]);

							if (user[5].equalsIgnoreCase("true")) {
								log.debug("Temporary Username Detected, user will be prompted to change");
								ses.setAttribute("ChangeUsername", "true");
							}

							log.debug("Setting CSRF cookie");
							Cookie token = new Cookie("token", Hash.randomString());
							if (request.getRequestURL().toString().startsWith("https"))// If Requested over HTTPs
								token.setSecure(true);

							// We must set the path because the ACS servlet is in a subdir...
							token.setPath("/");
							response.addCookie(token);

							mustRedirect = true;

							// Removing user from kick list. If they were on it before, their suspension
							// must have ended if their authentication Succeeded
							UserKicker.removeFromKicklist(user[1]);
						}

						if (mustRedirect) {
							response.sendRedirect("../index.jsp");
						} else {
							ssoValid = false;
						}
					}

					if (!ssoValid) {
						log.debug("Could not authenticate");

						errorMessage += "SSO login failed.";
						ses.setAttribute("loginFailed", errorMessage);
						// Lagging Response
						try {
							Thread.sleep(2000);
						} catch (InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
						response.sendRedirect("../login.jsp");
					}
				}
			}
		}

		log.debug("**** End servlets.ACS ***");

	}

	/**
	 * Redirects user to index.jsp
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("../index.jsp");
	}
}
