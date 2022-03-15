package servlets.module.challenge;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Level : Poor Validation 1 <br>
 * <br>
 *
 * <p>This file is part of the Security Shepherd Project.
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
public class PoorValidation1 extends HttpServlet {

  private static final String levelName = "Poor Validation 2";
  private static String levelSolution =
      "d30475881612685092e5ec469317dcc5ccc1f548a97bfdb041236b5bba7627bf";
  public static String levelHash =
      "ca0e89caf3c50dbf9239a0b3c6f6c17869b2a1e2edc3aa6f029fd30925d66c7e";
  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger(PoorValidation1.class);

  /** Shopping cart addition algorithm does not check for negative numbers on amounts */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Setting IpAddress To Log and taking header for original IP if forwarded from
    // proxy
    ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
    HttpSession ses = request.getSession(true);
    if (Validate.validateSession(ses)) {
      // Translation Stuff
      Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
      ResourceBundle bundle =
          ResourceBundle.getBundle(
              "i18n.servlets.challenges.poorValidation.poorValidationStrings", locale);

      String currentUser = ses.getAttribute("userName").toString();
      ShepherdLogManager.setRequestIp(
          request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), currentUser);
      log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
      PrintWriter out = response.getWriter();
      out.print(getServletInfo());
      String htmlOutput = new String();
      try {
        int pineappleAmount = Integer.parseInt(request.getParameter("pineappleAmount"));
        log.debug("pineappleAmount - " + pineappleAmount);
        int orangeAmount = Integer.parseInt(request.getParameter("orangeAmount"));
        log.debug("orangeAmount - " + orangeAmount);
        int appleAmount = Integer.parseInt(request.getParameter("appleAmount"));
        log.debug("appleAmount - " + appleAmount);
        int bananaAmount = Integer.parseInt(request.getParameter("bananaAmount"));
        log.debug("bananaAmount - " + bananaAmount);

        // Working out costs
        int pineappleCost = pineappleAmount * 30;
        int orangeCost = orangeAmount * 3000;
        int appleCost = appleAmount * 45;
        int bananaCost = bananaAmount * 15;

        htmlOutput = new String();

        // Work Out Final Cost
        int finalCost = pineappleCost + appleCost + bananaCost + orangeCost;

        // Output Order
        htmlOutput =
            "<h3 class='title'>"
                + bundle.getString("poorValidation.orderComplete")
                + "</h3>"
                + "<p>"
                + bundle.getString("poorValidation.orderComplete.message")
                + "<br></p>"
                + "<p>"
                + bundle.getString("poorValidation.orderTotal")
                + " <a><strong>$"
                + finalCost
                + "</strong></a></p>";
        if (finalCost <= 0 && orangeAmount > 0) {
          htmlOutput +=
              "<br><p>"
                  + bundle.getString("poorValidation.freeOranges")
                  + " - "
                  + Hash.generateUserSolution(levelSolution, currentUser)
                  + "</p>";
        }

      } catch (Exception e) {
        log.debug("Didn't complete order: " + e.toString());
        htmlOutput += "<p>" + bundle.getString("poorValidation.badOrder") + "</p>";
      }
      try {
        Thread.sleep(1000);
      } catch (Exception e) {
        log.error("Failed to Pause: " + e.toString());
      }
      out.write(htmlOutput);
    } else {
      log.error(levelName + " servlet accessed with no session");
    }
  }
}
