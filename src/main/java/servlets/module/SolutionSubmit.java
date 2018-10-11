package servlets.module;

import java.io.IOException;
import java.io.PrintWriter;
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

import utils.FeedbackStatus;
import utils.Hash;
import utils.ShepherdLogManager;
import utils.Validate;
import dbProcs.Getter;
import dbProcs.Setter;

/**
 * Control class that returns a feedback form for users if they submit the correct solution
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
 * @author Mark Denihan
 *
 */
public class SolutionSubmit extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(SolutionSubmit.class);
	/**
	 * Initiated by a dynamic form in index.jsp this method checks the existence of the submitted module identifier before ensuring that the submission is correct
	 * If the submission is found to be valid then the user is returned with a feedback form.
	 * @param mouleId The identifier of the module that the solution is been submitted for
	 * @param solutionKey The solution key for the proposed module
	 * @param csrfToken
	 */

	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("&&& servlets.module.SolutionSubmit &&&");
		PrintWriter out = response.getWriter();  
		HttpSession ses = request.getSession(true);
		if(Validate.validateSession(ses))
		{
			ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), ses.getAttribute("userName").toString());
			log.debug("Current User: " + ses.getAttribute("userName").toString());
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				boolean notNull = false;
				String storedResult = null;
				try
				{
					log.debug("Getting ApplicationRoot");
					String ApplicationRoot = getServletContext().getRealPath("");
					log.debug("Servlet root = " + ApplicationRoot );
					
					log.debug("Getting Parameters");
					String moduleId = (String)request.getParameter("moduleId");;
					log.debug("moduleId = " + moduleId.toString());
					String solutionKey = (String)request.getParameter("solutionKey");;
					log.debug("solutionKey = " + solutionKey.toString());
					
					log.debug("Getting session parameters");
					String userId = (String)ses.getAttribute("userStamp");
					String userName = (String)ses.getAttribute("userName");
					log.debug("userId = " + userId);
					
					//Validation
					notNull = (moduleId != null && solutionKey != null);
					if(notNull)
					{
						storedResult = Getter.getModuleResult(ApplicationRoot, moduleId);
					}
					boolean moduleOpen = false;
					if(notNull && storedResult != null)
					{
						moduleOpen = Getter.isModuleOpen(ApplicationRoot, moduleId);
					}
					if(notNull && storedResult != null && moduleOpen)
					{
						boolean validKey = false;
						//Identify if solution is a user Specific key (Does it need to be decrypted?)
						if(Getter.getModuleKeyType(ApplicationRoot, moduleId))
							validKey = storedResult.compareTo(solutionKey) == 0;
						else
						{
							//User has submitted a string. Lets see if it matches a freshly computed Key
							storedResult = Hash.generateUserSolutionKeyOnly(Getter.getModuleResult(ApplicationRoot, moduleId), userName);
							validKey = storedResult.compareTo(solutionKey) == 0;
							log.debug("Submitted Key: " + storedResult);
							log.debug("Expected Key : " + solutionKey);
						}
						if(validKey)
						{
							log.debug("Correct key submitted, checking that module not already completed");
							String result = Getter.checkPlayerResult(ApplicationRoot, moduleId, userId);
							if(result != null)
							{
								//If Feedback is enabled, the user must complete another step. This step is continued in FeedbackSubmit.java
								if(FeedbackStatus.isEnabled())
								{
								log.debug("Returning Feedback Form for module: " + result);
								out.write("<h2 class=\"title\">Solution Submission Success</h2><br>" +
										"<p> You are one step away from completing <a>" +
										Encode.forHtml(result) + "</a>! To complete the level please submit your feedback!" +
										"</p><br/>" +
										generateFeedbackForm(moduleId, (String)tokenParmeter, solutionKey));
								}
								else //Feedback is disabled
								{
									log.debug("Feedback is disabled, Marking as completed");
									String htmlOutput = new String();
									result = Setter.updatePlayerResult(ApplicationRoot, moduleId, userId, "Feedback is Disabled", 1, 1, 1);
									if(result != null)
									{
										ResourceBundle bundle = ResourceBundle.getBundle("i18n.moduleGenerics.moduleNames", new Locale(Validate.validateLanguage(request.getSession())));
										String compltedModuleLocalName = bundle.getString(result);
										log.debug("Solution Submission for module " + compltedModuleLocalName + " succeeded");
										htmlOutput = new String("<h2 class=\"title\">Solution Submission Success</h2><br>" +
												"<p>" +
												compltedModuleLocalName + " completed! Congratulations.");
										htmlOutput += "</p>";
										//Refresh Side Menu
										htmlOutput += FeedbackSubmit.refreshMenuScript(Encode.forHtml((String)tokenParmeter), "Refresh Error");
										log.debug("Resetting user's Bad Submisison count to 0");
										Setter.resetBadSubmission(ApplicationRoot, userId);
										out.write(htmlOutput);
									}
									else
									{
										htmlOutput = new String("Could not update user result");
										out.print("<h2 class=\"title\">Solution Submission Failure</h2><br>" +
												"<p><font color=\"red\">" +
												"Sorry but an error Occurred!" +
												"</font></p>");
									}
								}
							}
							else
							{
								log.error("User has completed this module before. Returning Error");
								out.write("<h2 class=\"title\">Haven't You Done This Already?</h2><br>" +
										"<p>" +
										"Our records say you have already completed this module! Go try another one!" +
										"</p>");
							}
						}
						else
						{
							log.error("Incorrect key submitted, returning error");
							out.print("<h2 class=\"title\">Solution Submission Failure</h2><br>" +
									"<p><font color=\"red\">" +
									"Incorrect Solution Key Submitted.<br><br>You have limited amounts of incorrect key submissions before you will loose 10% of your points. Contact the OWASP Security Shepherd if you think you have found the correct key but it is failing you." +
									"</font></p>");
							
							log.error("Invoking Bad Submission procedure...");
							Setter.incrementBadSubmission(ApplicationRoot, userId);
							log.error(userName + " has been warned and potentially has lost points");
						}
					}
					else
					{
						//Validation Error Responses
						String errorMessage = "An Error Occurred: ";
						if(!notNull)
						{
							log.error("Null values detected");
							errorMessage += "Invalid Request. Please try again";
						}
						else if(storedResult == null)
						{
							log.error("Module not found");
							errorMessage += "Module Not Found. Please try again";
						} 
						else 
						{
							log.fatal("Module Not Open");
							errorMessage += "Module Not Open. Please try a level that is marked as open. ";
						}
						out.print("<h2 class=\"title\">Solution Submission Failure</h2><br>" +
								"<p><font color=\"red\">" +
								Encode.forHtml(errorMessage) +
								"</font><p>");
					}
				}
				catch (Exception e)
				{
					log.error("Solution Submission Error: " + e.toString());
					out.print("<h2 class=\"title\">Solution Submission Failure</h2><br>" +
							"<p>" +
							"<font color=\"red\">An error Occurred! Please try again.</font>" +
							"<p>");
				}
			}
			else
			{
				log.debug("CSRF Tokens did not match");
				out.print("<h2 class=\"title\">Solution Submission Failure</h2><br>" +
						"<p>" +
						"<font color=\"red\">An error Occurred! Please try again.</font>" +
						"<p>");
			}
		}
		else
		{
			out.print("<h2 class=\"title\">Solution Submission Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error Occurred! Please Log in!</font>" +
					"<p>");
		}
		log.debug("&&& END SolutionSubmit &&&");
	}
	/**
	 * The method responsible for generating the feedback form based on the valid input submitted  by the user when initiating the doPost() method.
	 * This data will have to be re-assessed when it is used to submit feedback as the user has an opportunity to modify the parameters they are sending.
	 * @param moduleId The identifier of the module been completed
	 * @param csrfToken 
	 * @param theKey The submitted and valid solution key for the module been completed
	 * @return
	 */
	private static String generateFeedbackForm (String moduleId, String csrfToken, String theKey)
	{
		return feedbackForm + Encode.forHtml(moduleId) + feedbackForm21 + Encode.forHtml(theKey) 
			+ feedbackForm22 + Encode.forHtml(csrfToken) + feedbackForm3;
	}
	
	//To be written to the user
	private static String feedbackForm = "" +
	"<div id=\"badData\"></div>\n" +
	"<div id=\"formDiv\">\n" +
	"Please note that this feedback will be used to improve the Security Shepherd Project. " +
	"The results of this feedback will be evaluated using anonymous user names. " +
	"If you wish to enquire for more information about this survey please email the project leader at markdenihan@gmail.com..<br/>\n" +
	"<form action=\"javascript:;\" id=\"leForm2\">\n" +
	"	<input type=\"radio\" name=\"difficulty\" id=\"difficulty\" value=\"0\" style=\"display: none;\"/>\n" +
	"	<input type=\"radio\" name=\"before\" value=\"0\" style=\"display: none;\"/>\n" +
	"	<input type=\"radio\" name=\"after\" value=\"0\" style=\"display: none;\"/>\n" +
	"	<table>\n" +
	"		<tr>\n" +
	"			<td colspan=\"5\">\n" +
	"				<p>Please rate the <a>difficulty</a> of this module</p>\n" +
	"			</td>\n" +
	"		</tr>\n" +
	"		<tr>\n" +
	"			<td>Very Easy: <input type=\"radio\" name=\"difficulty\" id=\"difficulty\" value=\"1\"/></td>\n" +
	"			<td>Easy: <input type=\"radio\" name=\"difficulty\" id=\"difficulty\" value=\"2\"/></td>\n" +
	"			<td>Fair: <input type=\"radio\" name=\"difficulty\" id=\"difficulty\" value=\"3\"/></td>\n" +
	"			<td>Hard: <input type=\"radio\" name=\"difficulty\" id=\"difficulty\" value=\"4\"/></td>\n" +
	"			<td>Very Hard: <input type=\"radio\" name=\"difficulty\" id=\"difficulty\" value=\"5\"/></td>\n" +
	"		</tr>\n" +
	"		<tr>\n" +
	"			<td colspan=\"5\">\n" +
	"				<p>What was your knowlege of the module's topic <a>before</a> you started this module</p>\n" +
	"			</td>\n" +
	"		</tr>\n" +
	"		<tr>\n" +
	"			<td>None: <input type=\"radio\" name=\"before\" id=\"before\" value=\"1\"/></td>\n" +
	"			<td>Little: <input type=\"radio\" name=\"before\" id=\"before\" value=\"2\"/></td>\n" +
	"			<td>Average: <input type=\"radio\" name=\"before\" id=\"before\" value=\"3\"/></td>\n" +
	"			<td>Advanced: <input type=\"radio\" name=\"before\" id=\"before\" value=\"4\"/></td>\n" +
	"			<td>Expert: <input type=\"radio\" name=\"before\" id=\"before\" value=\"5\"/></td>\n" +
	"		</tr>\n" +
	"		<tr>\n" +
	"			<td colspan=\"5\">\n" +
	"				<p>What was your knowlege of the module's topic <a>after</a> you completed this module</p>\n" +
	"			</td>\n" +
	"		</tr>\n" +
	"		<tr>\n" +
	"			<td>None: <input type=\"radio\" name=\"after\" id=\"after\" value=\"1\"/></td>\n" +
	"			<td>Little: <input type=\"radio\" name=\"after\" id=\"after\" value=\"2\"/></td>\n" +
	"			<td>Average: <input type=\"radio\" name=\"after\" id=\"after\" value=\"3\"/></td>\n" +
	"			<td>Advanced: <input type=\"radio\" name=\"after\" id=\"after\" value=\"4\"/></td>\n" +
	"			<td>Expert: <input type=\"radio\" name=\"after\" id=\"after\" value=\"5\"/></td>\n" +
	"		</tr>\n" +
	"		<tr>\n" +
	"			<td colspan=\"5\">\n" +
	"				<p>Additional thoughts of the module experience?</p>\n" +
	"			</td>\n" +
	"		</tr>\n" +
	"		<tr>\n" +
	"			<td colspan=\"5\">\n" +
	"				<textarea style=\"width: 525px; height: 200px;\" id=\"extraInfo\"></textarea>\n" +
	"			</td>\n" +
	"		</tr>\n" +
	"		<tr><td colspan=\"5\" align=\"center\" >\n" +
	"			<input type=\"submit\" value=\"Submit Feedback\">\n" +
	"		</td></tr>\n" +
	"	</table>\n" +
	"</form>\n" +
	"</div>\n" +
	"<div id=\"loadingSign\" style=\"display: none;\"><p>Loading...</p></div>\n" +
	"<div id=\"resultDiv\" style=\"display: none;\"><div>\n" +
	"<script>\n" +
	"var theDifficulty = 0;\n" +
	"var theBefore = 0;\n" +
	"var theAfter = 0;\n" +
	"var theModuleId = \"";
	private static String feedbackForm21 = "\";\n var theKey = \"";
	private static String feedbackForm22 =	"\";\n" +
	"\n" +
	"$(\"input[name='difficulty']\").change(function(){\n" +
	"	theDifficulty = $(\"input[name='difficulty']:checked\").val();\n" +
	"});" +
	"\n" +
	"$(\"input[name='before']\").change(function(){\n" +
	"	theBefore = $(\"input[name='before']:checked\").val();\n" +
	"});\n" +
	"\n" +
	"$(\"input[name='after']\").change(function(){\n" +
	"	theAfter = $(\"input[name='after']:checked\").val();\n" +
	"});\n" +
	"\n" +
	"$(\"#leForm2\").submit(function(){\n" +
	"	$(\"#badData\").hide(\"fast\");\n" +
	"	\n" +
	"	theAdditionalInfo = $(\"#extraInfo\").val();\n" +
	"	if(theDifficulty != 0 && theBefore != 0 && theAfter != 0)\n" +
	"	{\n" +
	"		$(\"#loadingSign\").show(\"slow\");\n" +
	"		$(\"#formDiv\").hide(\"slow\", function(){\n" +
	"			var ajaxCall = $.ajax({\n" +
	"				dataType: \"text\",\n" +
	"				type: \"POST\",\n" +
	"				url: \"feedbackSubmit\",\n" +
	"				data: {\n" +
	"					moduleId: theModuleId,\n" +
	"					solutionKey: theKey,\n" +
	"					extra: theAdditionalInfo,\n" +
	"					difficulty: theDifficulty,\n" +
	"					before: theBefore,\n" +
	"					after: theAfter,\n" +
	"					csrfToken: \"";
	private static String feedbackForm3 = "\"\n" +
	"				},\n" +
	"				async: false\n" +
	"			});\n" +
	"			if(ajaxCall.status == 200)\n" +
	"			{\n" +
	"				$(\"#resultDiv\").html(ajaxCall.responseText);\n" +
	"				$(\"#resultDiv\").show(\"slow\");\n" +
	"			}\n" +
	"			else\n" +
	"			{\n" +
	"				$(\"#badData\").html(\"<p> An Error Occurred: \" + ajaxCall.status + \" \" + ajaxCall.statusText + \"</p>\");\n" +
	"				$(\"#badData\").show(\"slow\");\n" +
	"				$(\"#formDiv\").show(\"fast\");\n" +
	"			}\n" +
	"			$(\"#loadingSign\").hide(\"fast\");\n" +
	"		});\n" +
	"	}\n" +
	"	else\n" +
	"	{\n" +
	"		$(\"#badData\").html(\"<font color='red'>Please complete the feedback</font>\");\n" +
	"		$(\"#badData\").show(\"slow\");\n" +
	"	}\n" +
	"});\n" +
	"</script>";
}
