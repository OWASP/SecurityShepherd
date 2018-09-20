<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*, servlets.admin.userManagement.DowngradeAdmin" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: downgradeAdmins.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(downgradeAdmins.jsp): tokenCookie Error:" + htmlE.toString());
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
//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety

String csrfToken = Encode.forHtmlAttribute(tokenCookie.getValue());
String userName = Encode.forHtml(ses.getAttribute("userName").toString());
String userRole = Encode.forHtml(ses.getAttribute("userRole").toString());
String userId = Encode.forHtml(ses.getAttribute("userStamp").toString());
String ApplicationRoot = getServletContext().getRealPath("");
boolean showAdmins = false;

ResultSet classList = Getter.getAdmins(ApplicationRoot);
try
{
	showAdmins = classList.next();
}
catch(SQLException e)
{
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Could not open classList: " + e.toString());
	showAdmins = false;
}
%>
	<div id="formDiv" class="post">
		<h1 class="title">Downgrade Admin</h1>
		<div id="downgradeDiv" class="entry">
			<form id="theForm" action="javascript:;">
				<p>Please select the admin that you would like to downgrade to player</p>
				<div id="badData"></div>
				<input type="hidden" id="csrfToken" value="<%=csrfToken%>"/>
				<table align="center">
					<tr>
						<td>
							<p>Admins:</p>
						</td>
						<td>
							<select id="selectClass">
								<option value="">Select admin...</option>
								<%
									if(showAdmins)
														{
															try
															{
																do
																{
																	String adminId = Encode.forHtml(classList.getString(1));
																	String classYearName = Encode.forHtml(classList.getString(3)) + " " + Encode.forHtml(classList.getString(2));
								%>
												<option value="<%=adminId%>"><%=classYearName%></option>
											<%
												}
																			while(classList.next());
																			classList.first();
																		}
																		catch(SQLException e)
																		{
																			ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Error occured when manipulating classList: " + e.toString());
																			showAdmins = false;
																		}
																	}
											%>
							</select>
						</td>
					</tr>
					<tr><td colspan="2" align="center">
						<input type="submit" id="submitButton" value="Downgrade Admin"/>
					</td></tr>
				</table>
			</form>
		</div>
		<br>
		<div id="loadingDiv" style="display:none;" class="menuButton">Loading...</div>
		<div id="resultDiv" style="display:none;" class="informationBox"></div>
		<div id="badData"></div>
	</div>
	<script>
	$("#theForm").submit(function(){
		//Get Data
		var theCsrfToken = $('#csrfToken').val();
		var adminToDowngrade = $("#selectClass option:selected").val();
		//Validation
		if (adminToDowngrade == null)
		{
			$('#badData').html("<p><strong><font color='red'>Please select an admin to downgrade.</font></strong></p>");
		}
		else
		{
			//Hide&Show Stuff
			$("#loadingDiv").show("fast");
			$("#badData").hide("fast");
			$("#resultDiv").hide("fast");
			$("#downgradeDiv").slideUp("fast", function(){
				//The Ajax Operation
				var ajaxCall = $.ajax({
					type: "POST",
					url: "downgradeAdmin",
					data: {
						admin: adminToDowngrade, 
						csrfToken: theCsrfToken
					},
					async: false
				});
				$("#loadingDiv").hide("fast", function(){
					if(ajaxCall.status == 200)
					{
						//Now output Result Div and Show
						$("#resultDiv").html(ajaxCall.responseText);
						$("#resultDiv").show("fast");
						var success_mark = "successfully";
						
						if (ajaxCall.responseText.indexOf(success_mark) > 0){
							if ($("#selectClass").length == 1){
								var userSelect = $("#selectClass")[0];
								userSelect.remove(userSelect.selectedIndex);
							}
						}
					}
					else
					{
						$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
						$("#badData").show("slow");
					}
					$("#downgradeDiv").slideDown("slow");
				});
			});
		}
	});
	</script>
	<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
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