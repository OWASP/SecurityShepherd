<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: createNewClass.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(createNewClass.jsp): tokenCookie Error:" + htmlE.toString());
}
// validateAdminSession ensures a valid session, and valid administrator credentials
// Also, if tokenCookie != null, then the page is good to continue loading
if (Validate.validateAdminSession(ses) && tokenCookie != null)
{
// Getting Session Variables
//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
Encoder encoder = ESAPI.encoder();
String csrfToken = encoder.encodeForHTMLAttribute(tokenCookie.getValue());
String userName = encoder.encodeForHTML(ses.getAttribute("userName").toString());
String userRole = encoder.encodeForHTML(ses.getAttribute("userRole").toString());
String userId = encoder.encodeForHTML(ses.getAttribute("userStamp").toString());
String ApplicationRoot = getServletContext().getRealPath("");
	%>
	<div id="formDiv" class="post">
		<h1 class="title">Create New Class</h1>
		<div class="entry">
			<form id="theForm" action="javascript:;">
			<p>Please input the data you would like the new class to have. The class year format should be YY/YY, such as 11/12.</p>
			<div id="badData"></div>
			<input type="hidden" id="csrfToken" value="<%= csrfToken %>"/>
				<table align="center">
					<tr><td><p>Class Name:</p></td><td><input type="text" id="className" value=""/></td></tr>
					<tr><td><p>Class Year:</p></td><td><input type="text" id="classYear" /></td></tr>
					<tr><td colspan="2" align="center">
						<input type="submit" id="submitButton" value="Create New Class"/>
					</td></tr>
				</table>
			</form>
		</div>
	</div>
	<script>
	$("#theForm").submit(function(){
		var theClassName = $("#className").attr('value');
		var theClassYear = $('#classYear').attr('value');
		var theCsrfToken = $('#csrfToken').attr('value');
		//Validation
		if (theClassName.length == 0 || theClassYear.length == 0)
		{
			$('#badData').html("<p><strong><font color='red'>All fields are required</font></strong></p>");
		}
		else if(theClassName.length < 5 || theClassName.lenght > 32)
		{
			$('#className').val("");
			$('#className').css("background", "#E42217");
			$('#badData').html("<p><strong><font color='red'>Invalid Class Name. Please try Again.</font></strong></p>");
		}
		else if(theClassYear.length != 5)
		{
			$('#classYear').val("");
			$('#classYear').css("background", "#E42217");
			$('#badData').html("<p><strong><font color='red'>Invalid class year. Please try again with the YY/YY format (eg: 11/12)</font></strong></p>");
		}
		else
		{
			//The Ajax Operation
			var ajaxCall = $.ajax({
				type: "POST",
				url: "createNewClass",
				data: {
					className: theClassName, 
					classYear: theClassYear, 
					csrfToken: theCsrfToken
				},
				async: false
			});
			$("#contentDiv").hide("fast");
			if(ajaxCall.status == 200)
			{
				$("#contentDiv").html(ajaxCall.responseText);
			}
			else
			{
				$("#contentDiv").html("<p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
			}
			$("#contentDiv").show("fast");
		}
	});
	</script>
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