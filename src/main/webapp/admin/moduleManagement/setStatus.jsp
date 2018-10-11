<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: setStatus.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(setStatus.jsp): tokenCookie Error:" + htmlE.toString());
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
%>
	<div class="post">
		<h1 class="title">Open and Close Levels</h1>
		<div id="formDiv" class="entry">
			<div id="badData"></div>
				<form id="theForm" action="javascript:;">
					<p>Use this form to open and close levels by name. Levels that are closed will not appear in any level listings.</p>
					<div id="badData"></div>
					<input type="hidden" id="csrfToken" value="<%= csrfToken %>"/>
					<div  id="formDiv" align="center">
						<div>
							<table>
							<%= Getter.getModuleStatusMenu(ApplicationRoot) %>
							</table>
						</div>
						<div><input type="submit" value="Update Module Status"/></div>
					</div>
				</form>
			
		</div>
		<br />
	</div>
	<div id="loadingSign" style="display:none;" class="menuButton">Loading...</div>
	<div id="resultDiv" class="informationBox" style="display: none;"></div>
	<script>					
	$("#theForm").submit(function(){
		var toBeOpened = $("#toOpen").val();
		var toBeClosed = $("#toClose").val();
		var theCsrfToken = $('#csrfToken').val();
		var theModule = $("#theModule").val();
		//The Ajax Operation
		$("#loadingSign").show("fast");
		$("#badData").hide("fast");
		$("#resultDiv").hide("fast");
		$("#formDiv").slideUp("fast", function(){
			var ajaxCall = $.ajax({
				type: "POST",
				url: "setModuleStatus",
				data: {
					toClose: toBeClosed,
					toOpen: toBeOpened,
					csrfToken: theCsrfToken
				},
				async: false
			});
			$("#loadingSign").hide("fast", function(){
				if(ajaxCall.status == 200)
				{
					$("#resultDiv").html(ajaxCall.responseText);
					$("#resultDiv").show("fast");
					//Update toOpen and toClose lists before showing again
					$("#toClose option:selected").remove().appendTo('#toOpen').removeAttr("selected");
					$("#toOpen option:selected").remove().appendTo('#toClose').removeAttr("selected");
				}
				else
				{
					$("#badData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
					$("#badData").show("slow");
				}
				$("#formDiv").slideDown("slow");
				//Refresh the Side Menu
				refreshSideMenu(theCsrfToken, theRefreshError);
			});
		});
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