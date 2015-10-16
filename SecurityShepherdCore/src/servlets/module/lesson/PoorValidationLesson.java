package servlets.module.lesson;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * Poor Validation Lesson
 * <br/><br/>
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Security Shepherd project.  If not, see <http://www.gnu.org/licenses/>. 
 * 
 * @author Mark Denihan
 */
public class PoorValidationLesson extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(PoorValidationLesson.class);
	private static String levelName = "Poor Validation Lesson";
	public static String levelhash = "4d8d50a458ca5f1f7e2506dd5557ae1f7da21282795d0ed86c55fefe41eb874f";
	private static String levelResult = "6680b08b175c9f3d521764b41349fcbd3c0ad0a76655a10d42372ebccdfdb4bb";
	/**
	 * Data is only validated on the client side. No Server Side Validation is Performed
	 * @param userdata data submitted by user
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));

		//Translation Stuff
		Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
		ResourceBundle errors = ResourceBundle.getBundle("i18n.servlets.errors", locale);
		ResourceBundle bundle = ResourceBundle.getBundle("i18n.servlets.lessons.poorValidation", locale);
		
		//Attempting to recover username of session that made request
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug(levelName + " servlet accessed by: " + ses.getAttribute("userName").toString());
			PrintWriter out = response.getWriter();
			out.print(getServletInfo());
			try
			{
				String userData = request.getParameter("userdata");
				log.debug("User Submitted - " + userData);
				String htmlOutput = new String();
				int userNumber = Integer.parseInt(userData);
				if(userNumber < 0)
				{
					// Get key and add it to the output
					String userKey = Hash.generateUserSolution(levelResult, (String)ses.getAttribute("userName"));
					log.debug("Negative Number Submitted");
					htmlOutput = "<h2 class='title'>" + bundle.getString("result.validationBypassed") + "</h2><p>" + bundle.getString("result.youDidIt") + ". " + bundle.getString("result.resultKey") + ": <a>" + userKey + "</a></p>";
				}
				else
				{
					log.debug("Valid Number Submitted");
					htmlOutput = "<h2 class='title'>" + bundle.getString("response.validNumber") + "</h2><p>" + bundle.getString("response.theNumber") + " " + userNumber + " " + bundle.getString("response.valid") + ".";
				}
				log.debug("Outputting HTML");
				out.write(htmlOutput);
			}
			catch(Exception e)
			{
				out.write(errors.getString("error.funky"));
				log.fatal(levelName + " - " + e.toString());
			}
		}
		else
		{
			log.error(levelName + " servlet accessed with no session");
		}
	}
}