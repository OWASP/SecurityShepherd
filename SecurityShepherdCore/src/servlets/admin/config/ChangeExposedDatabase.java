package servlets.admin.config;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import dbProcs.Setter;
import utils.ExposedServer;
import utils.ShepherdLogManager;
import utils.Validate;

/**
 * This class is responsible for requests to configure the applications exposed database sign on information.
 * <br/>
 * <br/>
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
public class ChangeExposedDatabase extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(ChangeExposedDatabase.class);
	
	/**
	 * If this method is called by a valid administrator the site.properties file that contains the database information required to make a connection is updated 
	 * @param csrfToken
	 */
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		//Setting IpAddress To Log and taking header for original IP if forwarded from proxy
		ShepherdLogManager.setRequestIp(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"));
		log.debug("*** servlets.Admin.config.ChangeExposedDatabase ***");
		Encoder encoder = ESAPI.encoder();
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		if(Validate.validateAdminSession(ses))
		{
			log.debug("Current User: " + ses.getAttribute("userName").toString());
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				if(!ExposedServer.getApplicationRoot().isEmpty())
				{
					try
					{
						log.debug("Getting ApplicationRoot");
						String ApplicationRoot = getServletContext().getRealPath("");
						log.debug("Servlet root = " + ApplicationRoot );
						
						log.debug("Getting Parameters");
						String url = Validate.validateParameter(request.getParameter("databaseUrl"), 256);
						log.debug("url = " + url);
						String userName = Validate.validateParameter(request.getParameter("databaseUsername"), 256);
						log.debug("userName = " + userName);
						String password = Validate.validateParameter(request.getParameter("databasePassword"), 256);
						log.debug("password = " + password);
						
						boolean validData = !url.isEmpty() && !userName.isEmpty() && !password.isEmpty();
						if(Setter.setExposedDatabaseInfo(ApplicationRoot, url, userName, password))
						{
							if(Setter.setRemoteExposedDatabaseInfo(url, userName, password))
								out.write("<h2 class='title'>Exposed Database Info Updated</h2>" +
										"<p>The Core Database properties have sucessfully been updated!</p>");
							else
							{
								out.print("<h2 class=\"title\">Exposed Database Info Update Failure</h2><br>" +
										"<p><font color=\"red\">Could not update the database server information on the exposed server. Ether it is a completly remote machine to the core server or the vulnerable application root is set incorrectly.</font><p>");
							}
						}
						else
						{
							//Validation Error Responses
							String errorMessage = "An Error Occured ";
							if(!validData)
							{
								log.error("Invalid Application Address");
								errorMessage += "Invalid Host Address. Please try again";
							}
							else
							{
								log.error("Unexpected Failure");
								errorMessage = "An Error Occured";
							}
							out.print("<h2 class=\"title\">Exposed Database Info Update Failure</h2><br>" +
									"<p><font color=\"red\">" +
									encoder.encodeForHTML(errorMessage) +
									"</font><p>");
						}
					}
					catch (Exception e)
					{
						log.error("Exposed Database Info Update Error: " + e.toString());
						out.print("<h2 class=\"title\">Exposed Database Info Update Failure</h2><br>" +
								"<p>" +
								"<font color=\"red\">An error occured! Please try again.</font>" +
								"<p>");
					}
				}
				else
				{
					log.debug("Vulnerable Application Root not Set");
					out.print("<h1 class=\"title\">Vulnerable Application Root</h1>" +
							"	This functionality will not work until you set your vulnerable " +
							"application server's context. This is available in the vulnerable " +
							"application servers log file. Search the log file for \"Servlet root\". " +
							"This can be modified in the <a>Configuration</a> section of your administrators " +
							"controls<br/>	An example application root is as follows;<br/><br/>	Servlet root = " +
							"<a>C:\\Users\\userName\\Servers\\applicationServers\\tomcatExposed\\temp\\1-ROOT</a>	<br/>	<br/>" +
							"	<div id=\"badData\" style=\"display: none;\"></div>	<div id=\"theStep\">	<form action=\"javascript:;\" id=\"leForm\">" +
							"	Vulnerable Application Root <input type=\"text\" id=\"vAppRoot\" style=\"width: 300px;\"/>" +
							"<input type=\"submit\" id=\"submitButton\" value=\"Set Vulnerable Application Root\"/>	" +
							"<div id=\"loadingSign\" style=\"display: none;\"><p>Loading...</p></div>	</form>	</div>" +
							"	<script>	$(\"#leForm\").submit(function(){		$(\"#badData\").hide(\"fast\");		" +
							"var applicationRoot = $(\"#vAppRoot\").val();		if(applicationRoot.length > 8)		{		" +
							"	$(\"#submitButton\").hide(\"fast\");			$(\"#loadingSign\").show(\"slow\", function(){		" +
							"		var ajaxCall = $.ajax({					dataType: \"text\",					type: \"POST\",		" +
							"			url: \"changeVulnerableAppRoot\",					data: {					" +
							"	vulnerableApplicationRoot: applicationRoot,					" +
							"	csrfToken: \"" +
							encoder.encodeForHTML(tokenParmeter.toString()) +
							"\"					},				" +
							"	async: false				});				$(\"#theStep\").hide(\"fast\", function(){" +
							"					if(ajaxCall.status == 200)					{					" +
							"	$(\"#theStep\").html(ajaxCall.responseText);					}				" +
							"	else					{						" +
							"$(\"#badData\").html(\"<p> An Error Occured: \" + ajaxCall.status + \" \" + ajaxCall.statusText + \"</p>\");" +
							"						$(\"#badData\").show(\"slow\");					}				});		" +
							"		$(\"#loadingSign\").hide(\"fast\", function(){					$(\"#theStep\").show(\"slow\");	" +
							"			});			});		}		else		{			" +
							"$(\"#badData\").html(\"<font color='red'>Invalid Application Root. Too Short.</font>\");" +
							"			$(\"#badData\").show(\"slow\");		}	});	</script>");
				}
			}
			else
			{
				log.debug("CSRF tokens did not match");
				out.print("<h2 class=\"title\">Exposed Database Info Update Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error occured! CSRF Tokens did not match.</font>" +
					"<p>");
			}
		}
		else
		{
			out.print("<h2 class=\"title\">Exposed Database Info Update Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error occured! Please log in or try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** servlets.Admin.config.ChangeExposedDatabase END ***");
	}
}
