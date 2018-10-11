<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="servlets.Register,java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: scoreboard Config.jsp *************************");

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
 
if (request.getSession() != null) //Session If
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
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(scoreboardConfig.jsp): tokenCookie Error:" + htmlE.toString());
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
			ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Could not open classList: " + e.toString(), ses.getAttribute("userName"));
			showClasses = false;
		}
%>
			<div id="formDiv" class="post">
				<h1 class="title">Configure Scoreboard</h1>
				<div class="entry">
					<br/>
					<div id="configureScoreboardDiv">
						<a href="javascript:;" style="text-decoration: none;" id="enableScoreboard" title="Enable scoreboard that lists all users regardless of class"><div class="menuButton">Enable Scoreboard</div></a>
						<a href="javascript:;" style="text-decoration: none;" id="enableAdminScoreboard" title="Disable the scoreboard"><div class="menuButton">Enable Scoreboard for Admins</div></a>
						<a href="javascript:;" style="text-decoration: none;" id="disableScoreboard" title="Disable the scoreboard"><div class="menuButton">Disable Scoreboard</div></a>
						<br/>
						<h2 class="title">Enable Class Specific Scoreboards</h2>
						<p>
							When users visit the scoreboard, they will only see a list of users from their class. Admins that visit the scoreboard will still see the scoreboard the default class.
						</p>
						<a href="javascript:;" style="text-decoration: none;" id="classSpecific" title="Enable Class Specific Scoreboard"><div class="menuButton">Enable Class Specific Scoreboard</div></a>
						<a href="javascript:;" style="text-decoration: none;" id="adminClassSpecific" title="Enable Class Specific Scoreboard For Admins"><div class="menuButton">Enable Class Specific Scoreboard for Admins</div></a>
					</div>
					<br>
					<div id="loadingSign" style="display:none;" class="menuButton">Loading...</div>
					<div id="badData" style="display:none;"></div>
					<div id="resultDiv" style="display:none;" class="informationBox"></div>
				</div>
			</div>
			<script>
			var theCsrfToken = "<%= csrfToken %>";
			var theClass;
			var theUsers = new Array();
			var topOfStack = 0;
			
			$("#classSpecific").click(function(){
				$("#loadingSign").show("fast");
				$("#badData").hide("fast");
				$("#resultDiv").hide("fast");
				//The Ajax Operation
				//$("#badData").hide("fast");
				$("#configureScoreboardDiv").slideUp("fast", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "EnableScoreboard",
						data: {
							classId: "classSpecific",
							csrfToken: theCsrfToken
						},
						async: false
					});
					$("#loadingSign").hide("fast", function(){
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
						$("#configureScoreboardDiv").slideDown("slow");
						$('html, body').animate({
					        scrollTop: $("#resultDiv").offset().top
					    }, 1000);
					});
				});
			});
			
			$("#adminClassSpecific").click(function(){
				$("#loadingSign").show("fast");
				$("#badData").hide("fast");
				$("#resultDiv").hide("fast");
				theClass = $("#classId").val();
				//The Ajax Operation
				//$("#badData").hide("fast");
				$("#configureScoreboardDiv").slideUp("fast", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "EnableScoreboard",
						data: {
							classId: "<%= Register.getDefaultClass() %>",
							restricted: "true",
							csrfToken: theCsrfToken
						},
						async: false
					});
					$("#loadingSign").hide("fast", function(){
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
						$("#configureScoreboardDiv").slideDown("slow");
						$('html, body').animate({
					        scrollTop: $("#resultDiv").offset().top
					    }, 1000);
					});
				});
			});
			
			$("#enableScoreboard").click(function(){
				$("#loadingSign").show("fast");
				$("#badData").hide("fast");
				$("#resultDiv").hide("fast");
				//The Ajax Operation
				//$("#badData").hide("fast");
				$("#configureScoreboardDiv").slideUp("fast", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "EnableScoreboard",
						data: {
							classId: "",
							csrfToken: theCsrfToken
						},
						async: false
					});
					$("#loadingSign").hide("fast", function(){
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
						$("#configureScoreboardDiv").slideDown("slow");
						$('html, body').animate({
					        scrollTop: $("#resultDiv").offset().top
					    }, 1000);
					});
				});
			});
			
			$("#enableAdminScoreboard").click(function(){
				$("#loadingSign").show("fast");
				$("#badData").hide("fast");
				$("#resultDiv").hide("fast");
				//The Ajax Operation
				//$("#badData").hide("fast");
				$("#configureScoreboardDiv").slideUp("fast", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "EnableScoreboard",
						data: {
							classId: "",
							restricted: "true",
							csrfToken: theCsrfToken
						},
						async: false
					});
					var htmlHeap = "";
					$("#loadingSign").hide("fast", function(){
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
						$("#configureScoreboardDiv").slideDown("slow");
						$('html, body').animate({
					        scrollTop: $("#resultDiv").offset().top
					    }, 1000);
					});
				});
			});
			
			$("#disableScoreboard").click(function(){
				$("#loadingSign").show("fast");
				$("#badData").hide("fast");
				$("#resultDiv").hide("fast");
				//The Ajax Operation
				$("#configureScoreboardDiv").slideUp("fast", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "DisableScoreboard",
						data: {
							classId: "",
							csrfToken: theCsrfToken
						},
						async: false
					});
					$("#loadingSign").hide("fast", function(){
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
						$("#configureScoreboardDiv").slideDown("slow");
						$('html, body').animate({
					        scrollTop: $("#resultDiv").offset().top
					    }, 1000);
					});
				});
			});
			</script>
			<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
	<% 
	} //Valid Session If
	else
	{
	response.sendRedirect("../../loggedOutSheep.html");
	}
} //Session If
else
{
response.sendRedirect("../../loggedOutSheep.html");
}
%>
