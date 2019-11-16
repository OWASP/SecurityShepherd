package servlets.module;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.encoder.Encode;

import dbProcs.Getter;
import utils.CountdownHandler;
import utils.InvalidCountdownStateException;
import utils.ModulePlan;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Class used to return a fresh incremental menu upon completion of a module in
 * incremental mode. <br/>
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
public class RefreshMenu extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(GetModule.class);

	/**
	 * This servlet returns a fresh version of the module menu bar. This is used
	 * when completing levels, changing the floor plan or when opening/closing
	 * challenges.
	 * 
	 * @param csrfToken
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Setting IpAddress To Log and taking header for original IP if forwarded from
		// proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		boolean isRunning = false;
		boolean isAdmin = false;
		log.debug("&&& servlets.module.RefreshMenu &&&");
		PrintWriter out = response.getWriter();
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);

		// Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.text", locale);

		if (Validate.validateSession(ses)) {
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
					ses.getAttribute("userName").toString());
			log.debug("Current User: " + ses.getAttribute("userName").toString());
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");

			try {
				isRunning = CountdownHandler.isRunning();
			} catch (InvalidCountdownStateException e) {
				String message = "Countdown is in an invalid state: " + e.toString();
				log.error(message);
				throw new RuntimeException(e);
			}

			if (Validate.validateAdminSession(ses, tokenCookie, tokenParmeter)) {
				ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
						ses.getAttribute("userName").toString());
				log.debug("Current User: " + ses.getAttribute("userName").toString());
				if (Validate.validateTokens(tokenCookie, tokenParmeter)) {
					isAdmin = true;
				}
			}

			// Check if ctf has started/ended
			if (isRunning || isAdmin) {

				if (Validate.validateTokens(tokenCookie, tokenParmeter)) {

					String ApplicationRoot = getServletContext().getRealPath("");
					String userId = (String) ses.getAttribute("userStamp");
					if (ModulePlan.isIncrementalFloor()) {
						log.debug("Returning CTF Menu");
						out.write(Getter.getIncrementalModules(ApplicationRoot, userId,
								ses.getAttribute("lang").toString(), (String) tokenParmeter)
								+ "<script>startScrollsBars();makeSearchList();</script>");
					} else if (ModulePlan.isOpenFloor()) {
						log.debug("Returning Open Floor Menu");
						try {
							out.write("<li><a id=\"lessonList\" href=\"javascript:;\"><div class=\"menuButton\">"
									+ bundle.getString("generic.text.lessons")
									+ "</div></a><ul id=\"theLessonList\" style=\"display: none;\">"
									+ Getter.getLessons(ApplicationRoot, userId, locale) + "</ul></li><li>"
									+ "<a id=\"challengeList\" href=\"javascript:;\"><div class=\"menuButton\">"
									+ bundle.getString("generic.text.challenges")
									+ "</div></a><ul id=\"theChallengeList\" style=\"display: none;\">"
									+ Getter.getChallenges(ApplicationRoot, userId, locale) + "</ul></li>"
									+ "<script>applyMenuButtonActionsOpenOrTourney('"
									+ Encode.forHtml(tokenParmeter.toString()) + "', \""
									+ Encode.forHtml(bundle.getString("generic.text.sorryError"))
									+ "\");openFloorToggleFunctions();makeSearchList();</script>");
						} catch (SQLException e) {
							String message = "Unable to get challenges: " + e.toString();
							log.error(message);
							throw new RuntimeException(e);
						}
					} else {
						if (ModulePlan.isTournamentFloor())
							log.fatal("Could not Pick ModulePlan to use (All False). Using Tournament Instead");
						log.debug("Returning Tournament Floor Menu");
						out.write(Getter.getTournamentModules(ApplicationRoot, userId, locale)
								+ "<script>applyMenuButtonActionsOpenOrTourney('"
								+ Encode.forHtml(tokenParmeter.toString()) + "', \""
								+ Encode.forHtml(bundle.getString("generic.text.sorryError"))
								+ "\");tournamentToggleFunctions();startScrollsBars();makeSearchList();</script>");
					}
				} else {
					log.debug("CSRF Tokens did not match");
				}
			}

		} else {
			log.error("Invalid Session Detected");
			out.write("Your are logged out! Please sign back in!");
		}
		log.debug("&&& END RefreshMenu &&&");
	}
}
