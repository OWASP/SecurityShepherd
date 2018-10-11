<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: createNewAdmin.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(createNewAdmin.jsp): tokenCookie Error:" + htmlE.toString());
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
String errorMessage = new String();
String newUserName = new String();
String userAddress = new String();

if(ses.getAttribute("errorMessage") != null)
{
	try
	{
	errorMessage = Encode.forHtml(ses.getAttribute("errorMessage").toString());
	newUserName = Encode.forHtmlAttribute(ses.getAttribute("userName").toString());
	userAddress = Encode.forHtmlAttribute(ses.getAttribute("userAddress").toString());
	ses.removeAttribute("userName");
	ses.removeAttribute("userAddress");
	ses.removeAttribute("errorMessage");
	}
	catch(Exception e)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "createNewAdmin.jsp error");
		response.sendRedirect("error.jsp");
	}
}
%>
	<div id="formDiv" class="post">
		<h1 class="title">Create New Admin</h1>
		<div id="createUserDiv" class="entry">
			<form id="theForm" action="javascript:;">
			<p>Please input what data you want the new administrator to have. Please note that the password will be temporary.</p>
			<input type="hidden" id="csrfToken" value="<%= csrfToken %>"/>
				<table align="center">
					<tr><td><p>Username<font color="red"><small>* </small></font> :</p></td><td><input type="text" id="userName" value="<%= newUserName %>"/></td></tr>
					<tr><td><p>Password<font color="red"><small>* </small></font> :</p></td><td><input type="password" id="passWord" /></td></tr>
					<tr><td><p>Confirm Password<font color="red"><small>* </small></font> :</p></td><td><input type="password" id="passWordConfirm" /></td></tr>
					<tr><td><p>Email Address:</p></td><td><input type="text" id="userAddress" value="<%= userAddress %>"/></td></tr>
					<tr><td><p>Confirm Address:</p></td><td><input type="text" id="userAddressCnf" /></td></tr>
					<tr><td colspan="2" align="center">
						<input type="submit" id="submitButton" value="Create New Admin"/>
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
		//Get data
		var theUserName = $("#userName").attr('value');
		var thePassWord = $('#passWord').attr('value');
		var thePassWordConfirm = $('#passWordConfirm').attr('value');
		var theUserAddress = $('#userAddress').attr('value');
		var theUserAddressCnf = $('#userAddressCnf').attr('value');
		var theCsrfToken = $('#csrfToken').attr('value');
		//Validation
		if (theUserName.length == 0 ||
			thePassWord.length == 0 ||
			thePassWordConfirm.length == 0)
		{
			$('#badData').html("<p><strong><font color='red'>All required fields must be populated </font></strong></p>");
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
		else
		{
			$("#loadingDiv").show("fast");
			$("#badData").hide("fast");
			$("#resultDiv").hide("fast");
			$("#createUserDiv").slideUp("fast", function(){
				//The Ajax Operation
				var ajaxCall = $.ajax({
					type: "POST",
					url: "createNewAdmin",
					data: {
						userName: theUserName, 
						passWord: thePassWord, 
						passWordConfirm: thePassWordConfirm,
						userAddress: theUserAddress,
						userAddressCnf: theUserAddressCnf,
						csrfToken: theCsrfToken
					},
					async: false
				});
				$("#loadingDiv").hide("fast", function(){
					if(ajaxCall.status == 200)
					{
						//Reset Form
						$("#userName").val('');
						$('#passWord').val('');
						$('#passWordConfirm').val('');
						$('#userAddress').val('');
						$('#userAddressCnf').val('');
						//Now output Result Div and Show
						$("#resultDiv").html(ajaxCall.responseText);
						$("#resultDiv").show("fast");
					}
					else
					{
						$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
						$("#badData").show("slow");
					}
					$("#createUserDiv").slideDown("slow");
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