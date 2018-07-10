<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*, java.util.Calendar, java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>

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
// Token is now validated when accessing admin pages to stop attackers causing other users to tigger logs of access attempts
Object tokenParmeter = request.getParameter("csrfToken");
if(Validate.validateAdminSession(ses, tokenCookie, tokenParmeter))
{
	//Logging Username
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
// Getting Session Variables
//The org.owasp.encoder.Encode class should be used to encode any softcoded data. This should be performed everywhere for safety

String csrfToken = Encode.forHtmlAttribute(tokenCookie.getValue());
String userName = Encode.forHtml(ses.getAttribute("userName").toString());
String userRole = Encode.forHtml(ses.getAttribute("userRole").toString());
String userId = Encode.forHtml(ses.getAttribute("userStamp").toString());
String ApplicationRoot = getServletContext().getRealPath("");
%>
	<div id="formDiv" class="post">
		<h1 class="title">Create New Class</h1>
		<div id="createClassDiv" class="entry">
			<form id="theForm" action="javascript:;">
			<p>Please input the data you would like the new class to have. The class year format should be YYYY, such as 2010.</p>
			<input type="hidden" id="csrfToken" value="<%= csrfToken %>"/>
				<table align="center">
					<tr><td><p>Class Name:</p></td><td><input type="text" id="className" value=""/></td></tr>
					<tr><td><p>Class Year:</p></td><td><input type="text" id="classYear" value="<%= Calendar.getInstance().get(Calendar.YEAR) %>"/></td></tr>
					<tr><td colspan="2" align="center">
						<input type="submit" id="submitButton" value="Create New Class"/>
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
		else if(theClassYear.length != 4)
		{
			$('#classYear').val("");
			$('#classYear').css("background", "#E42217");
			$('#badData').html("<p><strong><font color='red'>Invalid class year. Please try again with the YYYY format (eg: 2010)</font></strong></p>");
		}
		else
		{
			//Hide&Show Stuff
			$("#loadingDiv").show("fast");
			$("#badData").hide("fast");
			$("#resultDiv").hide("fast");
			$("#createClassDiv").slideUp("fast", function(){
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
				$("#loadingDiv").hide("fast", function(){
					if(ajaxCall.status == 200)
					{
						//Now output Result Div and Show
						$("#resultDiv").html(ajaxCall.responseText);
						$("#resultDiv").show("fast");
					}
					else
					{
						$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
						$("#badData").show("slow");
					}
					$("#createClassDiv").slideDown("slow");
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