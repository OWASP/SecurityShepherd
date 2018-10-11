<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: configCheats.jsp *************************");

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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(configCheats.jsp): tokenCookie Error:" + htmlE.toString());
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
	
	String csrfToken = Encode.forHtml(tokenCookie.getValue());
	String ApplicationRoot = getServletContext().getRealPath("");
%>
	<h1 class="title">Configure Cheat Sheets</h1>
	<p>The Security Shepherd Application is capable of presenting users with &quot;Cheat Sheets&quot; that will instruct the reader on how to complete a specific module. These cheats are disabled by default, but can be enabled for administrators or all players. Once enabled, as you open Security Shepherd modules, a cheat button will appear in the left hand menu. Click this button to reveal the cheat sheet for the currently open module.</p>
	
	<!-- Enable Cheats Section -->
	<div id="enableCheats" <% if(CheatSheetStatus.isEnabledAtAll()) {%>style="display: none;"<% } %>>
		<h2 class="title">Enable Cheat Sheets</h2>
		<p>Enable cheat sheets for administrators or all users.</p>
		<a href="javascript:;" style="text-decoration: none;" id="enableCheatsAdmin" title="Enable Cheats for Administrators">
			<div class="menuButton">Enable Cheat Sheets for Administrators</div>
		</a>
		<a href="javascript:;" style="text-decoration: none;" id="enableCheatsAll" title="Enable Cheats for All Users">
			<div class="menuButton">Enable Cheat Sheets for All Users</div>
		</a>
		<br>
	</div>
	
	<!-- Disable Cheats Section -->
	<div id="disableCheats" <% if(!CheatSheetStatus.isEnabledAtAll()) {%>style="display: none;"<% } %>>
		<h2 class="title">Disable Cheat Sheets</h2>
		<p>Are you sure that you want to disable cheat sheets for all users?</p>
		<a href="javascript:;" style="text-decoration: none;" id="disableCheats" title="Disable Cheats">
			<div class="menuButton">Disable Cheat Sheets</div>
		</a>
		<br>
	</div>
	<div id="loadingDiv" style="display:none;" class="menuButton">Loading...</div>
	<div id="resultDiv" class="informationBox" style="display:none;"></div>
	<div id="badData" style="display:none;"></div>
	<script>
	var theCsrfToken = "<%= csrfToken %>";
	
	$("#enableCheatsAdmin").click(function(){
		$("#loadingDiv").show("fast");
		$("#badData").hide("fast");
		$("#resultDiv").hide("fast");
		//The Ajax Operation
		$("#enableCheats").slideUp("fast", function(){
			var ajaxCall = $.ajax({
				type: "POST",
				url: "enableCheats",
				data: {
					enableForAll: "nope",
					csrfToken: theCsrfToken
				},
				async: false
			});
			$("#loadingDiv").hide("fast", function(){
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
				//Show Disable Dialog
				$("#disableCheats").slideDown("slow");
			});
		});
	});
	
	$("#enableCheatsAll").click(function(){
		$("#loadingDiv").show("fast");
		$("#badData").hide("fast");
		$("#resultDiv").hide("fast");
		//The Ajax Operation
		$("#enableCheats").slideUp("fast", function(){
			var ajaxCall = $.ajax({
				type: "POST",
				url: "enableCheats",
				data: {
					enableForAll: "true",
					csrfToken: theCsrfToken
				},
				async: false
			});
			$("#loadingDiv").hide("fast", function(){
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
				//Show Disable Dialog
				$("#disableCheats").slideDown("slow");
			});
		});
	});
	
	$("#disableCheats").click(function(){
		$("#loadingDiv").show("fast");
		$("#badData").hide("fast");
		$("#resultDiv").hide("fast");
		//The Ajax Operation
		$("#disableCheats").slideUp("fast", function(){
			var ajaxCall = $.ajax({
				type: "POST",
				url: "disableCheats",
				data: {
					csrfToken: theCsrfToken
				},
				async: false
			});
			$("#loadingDiv").hide("fast", function(){
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
				//Show Enable Dialog
				$("#enableCheats").slideDown("slow");
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