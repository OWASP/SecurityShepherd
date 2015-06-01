<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: changeLevelLayout.jsp *************************");

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
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(changeLevelLayout.jsp): tokenCookie Error:" + htmlE.toString());
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
		Encoder encoder = ESAPI.encoder();
		String csrfToken = encoder.encodeForHTMLAttribute(tokenCookie.getValue());
%>
			<div id="formDiv" class="post">
				<h1 class="title">Change Module Layout</h1>
				<div class="entry">
					<p>
						You can change the layout in which modules are presented to players. There are three options;
					</p>
					
					<!-- CTF Mode Section -->
					<div id="ctfDiv">
						<h2 class="title">CTF Mode</h2>
						<p>
							When Shepherd has been deployed in the CTF mode, a user can only access one uncompleted module at a time. The first module presented to the user is the easiest in Security Shepherd, which has not been marked as closed by the administrator. 
							The levels increase slowly in difficulty and jump from one topic to another. This layout is the recommended setting when using Security Shepherd for a competitive training scenario.
						</p>
						<a href="javascript:;" style="text-decoration: none;" id="enableCtfMode" title="Enable CTF Mode">
							<div class="menuButton">Enable CTF Mode</div>
						</a>
					</div>
					<div id="ctfLoadingDiv" style="display:none;" class="menuButton">Loading...</div>
					<div id="ctfResultDiv" style="display: none"></div>
					<div id="ctfBadData" style="display: none"></div>
					<br>
					
					<!-- Open Floor Mode Section -->
					<div id="openFloor">
						<h2 class="title">Open Floor Mode</h2>
						<p>
							When Shepherd has been deployed in the Open Floor mode, a user can access any level that is marked as open by the admin. Modules are sorted into their Security Risk Categories, and the lessons are presented first. This layout is ideal for users wishing to explore security risks.
						</p>
						<a href="javascript:;" style="text-decoration: none;" id="enableOpenFloorMode" title="Enable Open Floor Mode">
							<div class="menuButton">Enable Open Floor Mode</div>
						</a>
					</div>
					<div id="openFloorLoadingDiv" style="display:none;" class="menuButton">Loading...</div>
					<div id="openFloorResultDiv" style="display:none;"></div>
					<div id="openFloorBadData" style="display:none;"></div>
					<br>
					
					<!-- Tournament Mode Section -->
					<div id="tournament">
						<h2 class="title">Tournament Mode</h2>
						<p>
							When Shepherd has been deployed in the Tournament Mode, a user can access any level that is marked as open by the admin. Modules are sorted into difficulty bands, from least to most difficult. This layout is ideal when Shepherd is being utilised as an open application security competition.
						</p>	
						<a href="javascript:;" style="text-decoration: none;" id="enableTournamentMode" title="Enable Tournament Mode">
							<div class="menuButton">Enable Tournament Mode</div>
						</a>
					</div>
					<div id="tournamentLoadingDiv" style="display:none;" class="menuButton">Loading...</div>
					<div id="tournamentResultDiv" style="display:none;"></div>
					<div id="tournamentBadData" style="display:none;"></div>
					<br>
					<script>
					var theCsrfToken = "<%= csrfToken %>";
					
					$("#enableCtfMode").click(function(){
						$("#ctfLoadingDiv").show("fast");
						$("#ctfBadData").hide("fast");
						$("#ctfResultDiv").hide("fast");
						//The Ajax Operation
						$("#ctfDiv").slideUp("fast", function(){
							var ajaxCall = $.ajax({
								type: "POST",
								url: "setCtfMode",
								data: {
									csrfToken: theCsrfToken
								},
								async: false
							});
							$("#ctfLoadingDiv").hide("fast", function(){
								//$("#ctfDiv").show("slow"); //Commented out because I like the way The Option is gone after it has been clicked
								if(ajaxCall.status == 200)
								{
									$("#ctfResultDiv").html(ajaxCall.responseText);
									$("#ctfResultDiv").show("fast");
								}
								else
								{
									$("#ctfBadData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
									$("#ctfBadData").show("slow");
									$("#ctfDiv").slideDown("slow");
								}
							});
							
						});
					});
					
					$("#enableOpenFloorMode").click(function(){
						$("#openFloorLoadingDiv").show("fast");
						$("#openFloorBadData").hide("fast");
						$("#openFloorResultDiv").hide("fast");
						//The Ajax Operation
						$("#openFloor").slideUp("fast", function(){
							var ajaxCall = $.ajax({
								type: "POST",
								url: "openFloorModules",
								data: {
									csrfToken: theCsrfToken
								},
								async: false
							});
							$("#openFloorLoadingDiv").hide("fast", function(){
								if(ajaxCall.status == 200)
								{
									console.log("200 Ok. Revealing openFloorResultDiv");
									$("#openFloorResultDiv").html(ajaxCall.responseText);
									$("#openFloorResultDiv").show("fast");
								}
								else
								{
									$("#openFloorBadData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
									$("#openFloorBadData").show("slow");
									$("#openFlood").slideDown("slow");
								}										
							});
						});
					});
					
					$("#enableTournamentMode").click(function(){
						$("#tournamentLoadingDiv").show("fast");
						$("#tournamentBadData").hide("fast");
						$("#tournamentResultDiv").hide("fast");
						//The Ajax Operation
						$("#tournament").slideUp("fast", function(){
							var ajaxCall = $.ajax({
								type: "POST",
								url: "tournamentModeModules",
								data: {
									csrfToken: theCsrfToken
								},
								async: false
							});
							$("#tournamentLoadingDiv").hide("fast", function(){
								if(ajaxCall.status == 200)
								{
									$("#tournamentResultDiv").html(ajaxCall.responseText);
									$("#tournamentResultDiv").show("fast");
								}
								else
								{
									$("#tournamentBadData").html("<div id='errorAlert'><p> Sorry but there was an error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p></div>");
									$("#tournamentBadData").show("slow");
									$("#tournament").slideDown("slow");
								}								
							});
						});
					});
					</script>
					<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
				</div>
			</div>
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
