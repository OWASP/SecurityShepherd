<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(getStarted.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateSession ensures a valid session, and valid role credentials
// Also, if tokenCookie != null, then the page is good to continue loading
if (Validate.validateSession(ses) && tokenCookie != null)
{
// Getting Session Variables
//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
Encoder encoder = ESAPI.encoder();
String csrfToken = encoder.encodeForHTMLAttribute(tokenCookie.getValue());
String exposedServer = encoder.encodeForHTMLAttribute(ExposedServer.getUrl());
String userName = encoder.encodeForHTML(ses.getAttribute("userName").toString());
String userRole = encoder.encodeForHTML(ses.getAttribute("userRole").toString());
String userId = encoder.encodeForHTML(ses.getAttribute("userStamp").toString());
String threadId = (String) ses.getAttribute("ThreadSequenceId");
String ApplicationRoot = getServletContext().getRealPath("");
boolean changePassword = false;
if(ses.getAttribute("ChangePassword") != null)
{
	String tempPass = ses.getAttribute("ChangePassword").toString();
	changePassword = tempPass.equalsIgnoreCase("true");
}

int i = 0;

//IF Change Password is True, Stick up a form
if(!changePassword)
{
%>
	<div id="getStarted" style="display:none;">
	<div class="post">
		<h1 class="title">Lets Get Started</h1>
		<div class="entry">
			Now that you've signed in, lets get started with some Security Shepherd modules! To start a module, click on the list headers, <span><a>Lessons</a></span> or <span><a>Modules</a></span>, in the side menu to see what modules are currently available!
			<br/><br/>
			If you cannot see the message below this paragraph, please ensure that your browser can visit <a href="<%= exposedServer %>"> the exposed server</a>.
		</div>
		<br/>
	<div id="cantSee">
		
	</div>
	</div>
	</div>
	<script>
	$('#getStarted').slideDown("slow");
	$('#cantSee').html("<iframe style='width: 685px; height: 600px;' frameborder='no' id='theStart' src='<%= exposedServer %>test.jsp?ThreadSequenceId=<%=encoder.encodeForHTMLAttribute(encoder.encodeForURL(threadId))%>'></iframe>");
	$('#cantSee').html(function(){
		$("#theStart").load(function(){
			$("#contentDiv").slideDown("slow");
		});
	});
	</script>
	<%
}
else	//IF the  user doesnt need to change their pass, just post up the get Started message
{
	%>
	<div class="errorWrapper">
		Your password is a temperary password. This means that sombody else knows it! Lets keep things secure and change your password now!
		<br /><br />
		<div class="errorMessage">
			<form id="changePassword" method="POST" action="passwordChange">
			<table align="center">
				<tr><td>Current Password:</td><td><input type="password" name="currentPassword" /></td></tr>
				<tr><td>New Password:</td><td><input type="password" name="newPassword" /></td></tr>
				<tr><td>Password Confirmation:</td><td><input type="password" name="passwordConfirmation" /></td></tr>
				<tr><td colspan="2"><center><input type="submit" id="changePasswordSubmit" value = "Change Password"/></center></td></tr>
			</table>
			<input type="hidden" name="csrfToken" value="<%=csrfToken%>" />
			</form>
		</div>
	</div>
	<%
}
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