<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: classProgress.jsp *************************");

/**
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
 
if (request.getSession() != null)
{
HttpSession ses = request.getSession();
Getter get = new Getter();
//Getting CSRF Token from client
Cookie tokenCookie = null;
try
{
	tokenCookie = Validate.getToken(request.getCookies());
}
catch(Exception htmlE)
{
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(classProgress.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateAdminSession ensures a valid session, and valid administrator credentials
// Also, if tokenCookie != null, then the page is good to continue loading
// Token is now validated when accessing admin pages to stop attackers causing other users to tigger logs of access attempts
Object tokenParmeter = request.getParameter("csrfToken");
if(Validate.validateAdminSession(ses, tokenCookie, tokenParmeter))
{
	//Logging Username
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
// Getting Session Variables
//The org.owasp.encoder.Encode class should be used to encode any softcoded data. This should be performed everywhere for safety

String csrfToken = Encode.forHtmlAttribute(tokenCookie.getValue());
String ApplicationRoot = getServletContext().getRealPath("");
ResultSet classList = Getter.getClassInfo(ApplicationRoot);
boolean showClasses = true;
try
{
	showClasses = classList.next();
}
catch(SQLException e)
{
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Could not open classList: " + e.toString());
	showClasses = false;
}
%>
	<div id="formDiv" class="post">
		<h1 class="title">Get Progress</h1>
		<div class="entry">
			<div id="badData"></div>
			<form id="theForm" action="javascript:;">
					<p>Select the class you would like to see the progress of</p>
					<div id="badData"></div>
					<input type="hidden" id="csrfToken" value="<%=csrfToken%>"/>
					<table align="center">
						<tr>
							<td>
							<select id="classId">
								<option value=""></option>
								<%
									if(showClasses)
														{
															try
															{
																do
																{
																	String classId = Encode.forHtmlAttribute(classList.getString(1));
																	String classYearName = Encode.forHtml(classList.getString(3)) + " " + Encode.forHtml(classList.getString(2));
								%>
												<option value="<%=classId%>"><%=classYearName%></option>
											<%
												}
																			while(classList.next());
																		}
																		catch(SQLException e)
																		{
																			ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Error occured when manipulating classList: " + e.toString());
																			showClasses = false;
																		}
																	}
											%>
							</select>
							</td>
						</tr>
						<tr><td align="center">
							<div id="submitButton"><input type="submit" value="Get Progress"/></div>
							<div id="loadingSign" style="display: none;"><p>Loading...</p></div> 
						</td></tr>
					</table>
					<div id="resultDiv">
					
					</div>
					<script>					
					$("#theForm").submit(function(){
						var theCsrfToken = $('#csrfToken').val();
						var theClass = $("#classId").val();
						//The Ajax Operation
						$("#badData").hide("fast");
						$("#loadingSign").show("slow");
						$("#submitButton").hide("fast");
						$("#resultDiv").hide("fast", function(){
							var ajaxCall = $.ajax({
								type: "POST",
								url: "getProgress",
								data: {
									classId: theClass,
									csrfToken: theCsrfToken
								},
								async: false
							});
							if(ajaxCall.status == 200)
							{
								$("#resultDiv").html(ajaxCall.responseText);
								$("#resultDiv").show("fast");
							}
							else
							{
								$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
								$("#badData").show("slow");
							}
							$("#loadingSign").hide("fast", function(){
								$("#submitButton").show("slow");
							});
						});
					});
					</script>
					<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
			</form>
		</div>
	</div>
	<%
}
else
{
response.sendRedirect("../../loggedOutSheep.html");
}
}
else
{
response.sendRedirect("../../loggedOutSheep.html");
}
%>