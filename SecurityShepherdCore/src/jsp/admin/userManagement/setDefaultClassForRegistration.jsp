<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: setDefaultClassForRegistration.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(setDefaultClassForRegistration.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateAdminSession ensures a valid session, and valid administrator credentials
// Also, if tokenCookie != null, then the page is good to continue loading
if (Validate.validateAdminSession(ses) && tokenCookie != null)
{
	//Logging Username
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString());
// Getting Session Variables
//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
Encoder encoder = ESAPI.encoder();
String csrfToken = encoder.encodeForHTMLAttribute(tokenCookie.getValue());
String userName = encoder.encodeForHTML(ses.getAttribute("userName").toString());
String userRole = encoder.encodeForHTML(ses.getAttribute("userRole").toString());
String userId = encoder.encodeForHTML(ses.getAttribute("userStamp").toString());
String ApplicationRoot = getServletContext().getRealPath("");
boolean showClasses = false;

ResultSet classList = Getter.getClassInfo(ApplicationRoot);
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
		<h1 class="title">Set Default Registration Class</h1>
		<div class="entry">
			<form id="theForm" action="javascript:;">
				<p>Any user that registers with this instance of Security Shepherd will be automatically assigned to the class group you choose in this form.</p>
				<div id="badData"></div>
				<input type="hidden" id="csrfToken" value="<%=csrfToken%>"/>
				<table align="center">
					<tr>
						<td>
							<p>Class:</p>
						</td>
						<td>
							<select id="classId">
								<option value="">Unassigned Players</option>
								<%
									if(showClasses)
									{
										try
										{
											do
											{
												String classId = encoder.encodeForHTML(classList.getString(1));
												String classYearName = encoder.encodeForHTML(classList.getString(3)) + " " + encoder.encodeForHTML(classList.getString(2));
												%>
												<option value="<%=classId%>"><%=classYearName%></option>
												<%
											}
											while(classList.next());
											classList.close();
										}
										catch(SQLException e)
										{
											ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "setDefaultClass: Error occured when manipulating classList: " + e.toString());
											showClasses = false;
										}
									}
									%>
							</select>
						</td>
					</tr>
					<tr><td colspan="2" align="center">
						<input type="submit" id="submitButton" value="Set to Default"/>
					</td></tr>
				</table>
			</form>
		</div>
	</div>
	<script>	
	$("#theForm").submit(function(){
		var theClass = $("#classId").val();
		var theCsrfToken = $('#csrfToken').val();
		//Validation
		if (theClass == null)
		{
			$('#badData').html("<p><strong><font color='red'>All fields are required</font></strong></p>");
		}
		else
		{
			//The Ajax Operation
			var ajaxCall = $.ajax({
				type: "POST",
				url: "setDefaultRegistrationClass",
				data: {
					classId: theClass,
					csrfToken: theCsrfToken
				},
				async: false
			});
			if(ajaxCall.status == 200)
			{
				$("#contentDiv").hide("fast", function(){
					$("#contentDiv").html(ajaxCall.responseText);
					$("#contentDiv").show("fast");
				});
			}
			else
			{
				$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
			}
		}
	});
	</script>
	<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
	<%
}
else
{
response.sendRedirect("login.jsp");
}
}
else
{
response.sendRedirect("login.jsp");
}
%>