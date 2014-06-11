<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: addPlayers.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(addPlayers.jsp): tokenCookie Error:" + htmlE.toString());
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
		<h1 class="title">Add Players</h1>
		<div class="entry">
			<form id="theForm" action="javascript:;">
			<p>Please select the class you would like to add a player to and input the player information for the player you wish to create.</p>
			<div id="badData"></div>
			<input type="hidden" id="csrfToken" value="<%=csrfToken%>"/>
				<table align="center">
					<tr>
						<td>
							<p>Class :</p>
						</td>
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
																	String classId = encoder.encodeForHTMLAttribute(classList.getString(1));
																	String classYearName = encoder.encodeForHTML(classList.getString(3)) + " " + encoder.encodeForHTML(classList.getString(2));
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
					<tr><td><p>Username:</p></td><td><input type="text" id="userName" value=""/></td></tr>
					<tr><td><p>Password:</p></td><td><input type="password" id="passWord" /></td></tr>
					<tr><td><p>Confirm Password:</p></td><td><input type="password" id="passWordConfirm" /></td></tr>
					<tr><td><p>Email Address:</p></td><td><input type="text" id="userAddress" value=""/></td></tr>
					<tr><td><p>Confirm Address:</p></td><td><input type="text" id="userAddressCnf" /></td></tr>
					<tr><td colspan="2" align="center">
						<input type="submit" id="submitButton" value="Create New Player"/>
					</td></tr>
				</table>
			</form>
		</div>
	</div>
	<script>
	$("#theForm").submit(function(){
		var theClass = $("#classId").val();
		var theUserName = $("#userName").attr('value');
		var thePassWord = $('#passWord').attr('value');
		var thePassWordConfirm = $('#passWordConfirm').attr('value');
		var theUserAddress = $('#userAddress').attr('value');
		var theUserAddressCnf = $('#userAddressCnf').attr('value');
		var theCsrfToken = $('#csrfToken').attr('value');
		//Validation
		if (theUserName.length == 0 ||
			thePassWord.length == 0 ||
			thePassWordConfirm.length == 0 ||
			theUserAddress.length == 0 ||
			theUserAddressCnf.length == 0)
		{
			$('#badData').html("<p><strong><font color='red'>All fields are required</font></strong></p>");
		}
		else if(theUserName.length < 5 || theUserName.lenght > 32)
		{
			$('#userName').val("");
			$('#userName').css("background", "#E42217");
			$('#badData').html("<p><strong><font color='red'>Invalid Username. Please try Again.</font></strong></p>");
		}
		else if(thePassWord.lenght < 5 || thePassWord.lenght > 512 )
		{
			$('#passWord').val("");
			$('#passWordConfirm').val("");
			$('#passWord').css("background", "#E42217");
			$('#badData').html("<p><strong><font color='red'>Invalid password. Please try Again.</font></strong></p>");
		}
		else if(thePassWord != thePassWordConfirm)
		{
			$('#passWord').val("");
			$('#passWordConfirm').val("");
			$('#passWord').css("background", "#E42217");
			$('#passWordConfirm').css("background", "#E42217");
			$('#badData').html("<p><strong><font color='red'>Password fields did not match. Please try Again.</font></strong></p>");
		}
		else if(theUserAddress.lenght <= 4 || theUserAddress.lenght > 128)
		{
			$('#userAddress').val("");
			$('#userAddressCnf').val("");
			$('#userAddress').css("background", "#E42217");
			$('#badData').html("<p><strong><font color='red'>Invalid email address. Please try Again.</font></strong></p>");
		}
		else if(theUserAddress != theUserAddressCnf)
		{
			$('#userAddress').val("");
			$('#userAddressCnf').val("");
			$('#userAddress').css("background", "#E42217");
			$('#userAddressCnf').css("background", "#E42217");
			$('#badData').html("<p><strong><font color='red'>Email fields did not match. Please try Again.</font></strong></p>");
		}
		else
		{
			//The Ajax Operation
			var ajaxCall = $.ajax({
				type: "POST",
				url: "addPlayer",
				data: {
					classId: theClass,
					userName: theUserName, 
					passWord: thePassWord, 
					passWordConfirm: thePassWordConfirm,
					userAddress: theUserAddress,
					userAddressCnf: theUserAddressCnf,
					csrfToken: theCsrfToken
				},
				async: false
			});
			if(ajaxCall.status == 200)
			{
				$("#userName").val('');
				$('#passWord').val('');
				$('#passWordConfirm').val('');
				$('#userAddress').val('');
				$('#userAddressCnf').val('');
				$("#contentDiv").append(ajaxCall.responseText);
			}
			else
			{
				$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
			}
		}
	});
	</script>
	<% if(ExposedServer.googleAnalyticsOn) { %>
		<%= ExposedServer.googleAnalyticsScript %>
	<% } %>
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