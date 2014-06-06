<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: index.jsp *************************");

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
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG(index.jsp): tokenCookie Error:" + htmlE.toString());
	}
	// validateSession ensures a valid session, and valid role credentials
	// Also, if tokenCookie != null, then the page is good to continue loading
	if (Validate.validateSession(ses) && tokenCookie != null)
	{
		//Log User Name
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString());
		// Getting Session Variables
		//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
		Encoder encoder = ESAPI.encoder();
		String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
		String userName = encoder.encodeForHTML(ses.getAttribute("userName").toString());
		String userRole = encoder.encodeForHTML(ses.getAttribute("userRole").toString());
		String userId = encoder.encodeForJavaScript(ses.getAttribute("userStamp").toString());
		String csrfJsToken = encoder.encodeForJavaScript(tokenCookie.getValue());
		String ApplicationRoot = getServletContext().getRealPath("");
		int i = 0;

		String exposedServer = ExposedServer.getUrl();
%>
		<html xmlns="http://www.w3.org/1999/xhtml">
		<head>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<title>Security Shepherd</title>

		<!-- You are currently looking at the core server. 
			Nothing related to the levels in Security Shepherd will be found in here. 
			You might be looking for the iframe embeded in the page.
			Try a tool like Firebug to make this stuff easier. -->

		<link href="css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
		<link rel="shortcut icon" href="css/images/flavicon.jpg" type="image/jpeg" />

		</head>
		<body>
		<script type="text/javascript" src="js/jquery.js"></script>
		<div id="wrapper">
		<div id="header">
			<h1>Security Shepherd</h1>
			<div style="position: absolute;right: 100px;">
				<p>
					<strong>Welcome <%= userName %></strong><br />
					<strong><a href="logout?csrfToken=<%= csrfToken %>">Logout</a></strong>
				</p>
			</div>
		</div>
		
		<div id="page">
			<div id="submitResult" style="display: none; float: right;, width: 800px;">
				<form action="javascript:;" id="resultForm" >
				<input type="hidden" id="currentModule" value=""/>
					<table>
					<tr><td>
						Submit Result Key:</td><td> <input style="width: 470px;" type="text" id="moduleResult" value="" autocomplete="OFF"/></td><td><div id="submitForm"><input type="submit" value="Submit"/></div>
						<div id="submitLoading" style="display: none;">Loading... </div>
					</td></tr>
					</table>
					<div id="resultResponse"></div>
				</form>
				<% if(CheatSheetStatus.getStatus()) { %>
					<br/>
					<small><a id='showSolution' href="javascript:;">Show Module Cheat Sheet...</a></small><br/>
					<div id="solutionDiv" style="display:none;"></div>
					<br/>
					<br/>
				<% } %>
			</div>
			<!-- You are currently looking at the core server. 
			Nothing related to the levels in Security Shepherd will be found in here. 
			You might be looking for the iframe embeded in the page.
			Try a tool like Firebug to make this stuff easier. -->
			<% if(!ConfigurationHelper.alreadyConfigured() && userRole.compareTo("admin") == 0)
			{ 
			%>
				<div id="configurationWizardDiv" align="justify">
					<h1 class="title">First Time Configuration!</h1>
					<div align="justify">
						<p>Security Shepherd wants to help you through your first time setup. If you are going to use Security Shepherd locally and for yourself you can skip this by clicking the 
						<b><a>Skip</a></b> button! If you are going to use Security Shepherd as a service for people to connect to over the network, then you should check the values in the following form accurately depict the network location of your server.</p>
					</div>
						<form action="javascript:;" id="configurationWizard" style="border-color:#A878EF; border-style:dashed; background-color: #D4D4D4;padding-top:5px;padding-bottom:5px;padding-right:5px;padding-left:5px;">
							<table id="#configurationTable" align="center"><tbody>
							<tr>
								<td>Core Address:</td><td><input style="width: 470px;" id="coreHostAddress" autocomplete="ON" type="text" value="<%= encoder.encodeForHTMLAttribute(ExposedServer.getSecureUrl()) %>" /></td>
							</tr><tr>
								<td>Exposed Address:</td><td><input style="width: 470px;" id="exposedHostAddress" autocomplete="ON" type="text" value="<%= encoder.encodeForHTMLAttribute(ExposedServer.getUrl()) %>" /></td>
							</tr><tr id="doSomthingRow">
								<td><input value="Configure Server" type="submit"></td><td align="right"><input value="Skip" type="button" id="noSetupForMeThanks"></td>				
							</tr><tr id="configLoading" style="display: none;">
								<td colspan="2"> <div id="submitLoading" >Loading... </div></td>
							</tr>
							</tbody></table>
						</form>
					<div id="configResultResponse"></div>
				</div>
			<% 
				}
			%>
			<div id="contentDiv">
				<!-- Ajax Div -->
			</div>
			<div id="sidebar">
				<ul>
					<% if (userRole.compareTo("admin") == 0){ %>
						<li>
							<h2 id="adminList"><a href="javascript:;">Admin</a></h2>
							<ul id="theAdminList" style="display: none;">
								<li>
									<a id="cheatSheetManagementList" href="javascript:;">Cheat Sheet Management</a>
									<ul id="theCheatSheetManagementList" style="display: none;">
										<li><a id="enableCheatsLink" href="javascript:;">Enable Cheat Sheets</a></li>
										<li><a id="disableCheatsLink" href="javascript:;">Disable Cheat Sheets</a></li>
										<li><a id="createCheatsLink" href="javascript:;">Create New Cheat Sheet</a></li>
									</ul>
								</li>
								<li>
									<a id="moduleManagementList" href="javascript:;">Module Management</a>
									<ul id="theModuleManagementList" style="display: none;">
										<li><a id="openFloorModuleLink" href="javascript:;">Open Floor Modules</a></li>
										<li><a id="incrementalModulesLink" href="javascript:;">CTF Mode</a></li>
										<li><a id="stopHereLink" href="javascript:;">Enable Module Block</a></li>
										<li><a id="disableBlockLink" href="javascript:;">Disable Module Block</a></li>
										<li><a id="setModuleStatusLink" href="javascript:;">Set Module Status</a></li>
										<li><a id="feedbackLink" href="javascript:;">View Feedback</a></li>
										<li><a id="progressLink" href="javascript:;">View Progress</a></li>
										<li><a id="scoreboardLink" href="javascript:;">Scoreboard</a></li>
									</ul>
								</li>
								<li>
									<a id="userManagementList" href="javascript:;">User Management</a>
									<ul id="theUserManagementList" style="display: none;">
										<li><a id="createNewAdminLink" href="javascript:;">Create New Admin</a></li>
										<li><a id="upgradePlayersLink" href="javascript:;">Upgrade Player to Admin</a></li>
										<li><a id="addPlayersLink" href="javascript:;">Add Players</a></li>
										<li><a id="createNewClassLink" href="javascript:;">Create Class</a></li>
										<li><a id="assignPlayersLink" href="javascript:;">Assign Players to Class</a></li>
										<li><a id="registrationLink" href="javascript:;">Open/Close Registration</a></li>
									</ul>
								</li>
								<li>
									<a id="configurationList" href="javascript:;">Configuration</a>
									<ul id="theConfigurationList" style="display: none;">
										<li><a id="enableFeedbackLink" href="javascript:;">Enable Feedback</a></li>
										<li><a id="disableFeedbackLink" href="javascript:;">Disable Feedback</a></li>
										<li><a id="setCoreHostAddressLink" href="javascript:;">Set Core Host Address</a></li>
										<li><a id="setExposedHostAddressLink" href="javascript:;">Set Exposed Host Address</a></li>
										<li><a id="setCoreDatabaseLink" href="javascript:;">Set Core Database</a></li>
										<li><a id="setExposedDatabaseLink" href="javascript:;">Set Exposed Database</a></li>
										<li><a id="setVulnerableRootLink" href="javascript:;">Set Vulnerable Server App Root</a></li>
									</ul>
								</li>
							</ul>
						</li>
					<% } %>
					<% if(ModulePlan.isOpenFloor()) { %>
						<li>
							<h2 id="lessonList"><a href="javascript:;">Lessons</a></h2>
							<ul id="theLessonList" style="display: none;">
								<%= Getter.getLessons(ApplicationRoot, (String)ses.getAttribute("userStamp")) %>
							</ul>
						</li>
						<li>
							<h2 id="challengeList"><a href="javascript:;">Challenges</a></h2>
							<ul id="theChallengeList" style="display: none;">
								<%= Getter.getChallenges(ApplicationRoot, (String)ses.getAttribute("userStamp")) %>
							</ul>
						</li>
					<% } else {
						if(ModulePlan.isIncrementalFloor()){ %>
							<div id="sideMenuWrapper">
								<%= Getter.getIncrementalModules(ApplicationRoot, (String)ses.getAttribute("userStamp"), csrfToken) %>
							</div>
						<% } else {%>
						<li>
							<h2 id="challengeList"><a href="javascript:;">Challenges</a></h2>
							<ul id="theChallengeList" style="display: none;">
							<%= Getter.getTournamentModules(ApplicationRoot, (String)ses.getAttribute("userStamp")) %>
							</ul>
						</li>
						<% }
					} //End of Module List Output %>
				</ul>
				<!-- You are currently looking at the core server. 
				Nothing related to the levels in Security Shepherd will be found in here. 
				You might be looking for the iframe embeded in the page.
				Try a tool like Firebug to make this stuff easier. -->
			</div>
		</div>
		</div>
		<script src="js/toggle.js"></script>
		<script src="js/ajaxCalls.js"></script>
		
		<% //Hide UI Scripts from Users (Blocked at session level anyway, just stops spiders finding the links)
		if (userRole.compareTo("admin") == 0){ %>
			<script src="js/adminToggle.js"></script>
			<script src="js/adminAjaxCalls.js"></script>
		<% } %>
		
		<script>
		$(".challenge").click(function(){
			var whatFile = $(this).attr('id');
			$("#currentModule").val(whatFile);
			var theActualFile = "";
			$("#solutionDiv").hide("fast");
			$("#contentDiv").slideUp("slow", function(){
				var ajaxCall = $.ajax({
					type: "POST",
					url: "getModule",
					data: {
						moduleId: whatFile,
						csrfToken: "<%= csrfToken %>"
					},
					async: false
				});
				if(ajaxCall.status == 200)
				{
					theActualFile = ajaxCall.responseText;
					$('#contentDiv').html("<iframe frameborder='no' style='word-wrap: break-word; width: 685px; height: 2056px;' id='theChallenge' src='" + theActualFile + "'></iframe>");
					$("#theChallenge").load(function(){
						$("#submitResult").slideDown("fast", function(){
							$("#contentDiv").slideDown("slow");
						});
					}).appendTo('#contentDiv');
				}
				else
				{
					$('#contentDiv').html("<p> Sorry but there was a challenge error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					$("#contentDiv").slideDown("slow");
				}
			});
		});	

		$(".lesson").click(function(){
			var whatFile = $(this).attr('id');
			$("#currentModule").val(whatFile);
			var theActualFile = "";
			$("#solutionDiv").hide("fast");
			$("#contentDiv").slideUp("slow", function(){
				var ajaxCall = $.ajax({
					type: "POST",
					url: "getModule",
					data: {
						moduleId: whatFile,
						csrfToken: "<%= csrfToken %>"
					},
					async: false
				});
				if(ajaxCall.status == 200)
				{
					theActualFile = ajaxCall.responseText;
					$('#contentDiv').html("<iframe frameborder='no' style='word-wrap: break-word; width: 685px; height: 2056px;' id='theLesson' src='" + theActualFile + "'></iframe>");
					$("#theLesson").load(function(){
						$("#submitResult").slideDown("fast", function(){
							$("#contentDiv").slideDown("slow");
						});
					}).appendTo('#contentDiv');
				}
				else
				{
					$('#contentDiv').html("<p> Sorry but there was a lesson error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					$("#contentDiv").slideDown("slow");
				}
			});
		});

		$("#showSolution").click(function(){
			$("#solutionDiv").hide("fast", function(){
				var theModuleId = $("#currentModule").val();
				var ajaxCall = $.ajax({
					type: "POST",
					url: "getCheat",
					data: {
						moduleId: theModuleId,
						csrfToken: "<%= csrfToken %>"
					},
					async: false
				});
				if(ajaxCall.status == 200)
				{
					$('#solutionDiv').html(ajaxCall.responseText);
				}
				else
				{
					$('#solutionDiv').html("<p> Sorry but there was a show solution error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
				}
				$("#solutionDiv").show("fast");
			});
		});

		$("#resultForm").submit(function(){
			var theKey = $("#moduleResult").val();
			var theModuleId = $("#currentModule").val();
			if(theKey != null || theKey.length > 5)
			{
				$("#solutionDiv").hide("fast");
				$("#resultResponse").hide("fast");
				$("#submitForm").hide("fast");
				$("#submitLoading").show("slow");
				$("#contentDiv").slideUp("slow", function(){
					$("#moduleResult").val("");
					var ajaxCall = $.ajax({
						type: "POST",
						url: "solutionSubmit",
						data: {
							moduleId: theModuleId,
							solutionKey: theKey,
							csrfToken: "<%= csrfToken %>"
						},
						async: false
					});
					if(ajaxCall.status == 200)
					{
						$('#contentDiv').html(ajaxCall.responseText);
					}
					else
					{
						$('#resultResponse').html("<br/><p> Sorry but there was a result form error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p><br/>");
						$("#resultResponse").show("slow");
					}
					$("#contentDiv").slideDown("fast", function(){
						$("#submitLoading").hide("fast", function(){
							$("#submitForm").show("slow");
						});
					});
				});
			}
			else
			{
				alert('Invalid Key');
			}
		});
		
		<% if(!ConfigurationHelper.alreadyConfigured() && userRole.compareTo("admin") == 0)
		{ 
		%>
		$("#configurationWizard").submit(function(){
			var theCoreHostAddress = $("#coreHostAddress").val();
			var theExposedHostAddress = $("#exposedHostAddress").val();
			if((theExposedHostAddress.length > 8 && theCoreHostAddress.length > 8))
			{
				$("#badData").hide("fast");
				$("#configResultResponse").hide("fast");
				$("#doSomthingRow").hide("fast");
				$("#configLoading").show("slow", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "quickConfig",
						data: {
							coreHostAddress: theCoreHostAddress,
							exposedHostAddress: theExposedHostAddress,
							csrfToken: "<%= csrfToken %>"
						},
						async: false
					});
					if(ajaxCall.status == 200)
					{
						$('#configResultResponse').html(ajaxCall.responseText);
						if (ajaxCall.responseText.indexOf("Please try again") >= 0) //Error Message In Response (Don't Hide Form)
						{
							$("#configLoading").hide("fast", function(){
								$("#configResultResponse").show ("fast");
							});
						}
						else // No Error Message
						{
							$("#configLoading").hide("fast", function(){
								$("#configurationWizard").hide("slow", function(){
									$("#configResultResponse").show ("fast");
								});
							});
						}
					}
					else
					{
						$('#configResultResponse').html("<br/><p> Sorry but there was a result form error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p><br/>");
						$("#configResultResponse").show("slow");
					}
						$("#configLoading").hide("fast", function(){
							$("#doSomthingRow").show("slow");
					});
				});
			}
			else
			{
				$("#badData").html("<font color='red'>Invalid Url. Too Short.</font>");
				$("#badData").show("slow");
			}
		});
		
		$("#noSetupForMeThanks").click(function(){
			$("#configurationWizardDiv").hide("slow", function(){
				var ajaxCall = $.ajax({
					type: "POST",
					url: "cancelConfigPrompt",
					data: {
						csrfToken: "<%= csrfToken %>"
					},
					async: false
				});
				if(ajaxCall.status == 200)
				{
					//Grand
				}
				else
				{
					$('#resultResponse').html("<br/><p> Config Splash Cancel Failed!: " + ajaxCall.status + " " + ajaxCall.statusText + "</p><br/>");
					$("#resultResponse").show("slow");
				}
				$("#contentDiv").slideDown("fast", function(){
					$("#submitLoading").hide("fast", function(){
						$("#submitForm").show("slow");
					});
				});
				$("html, body").animate({ scrollTop: 0 }, "fast");
			});
		});
		<%
		}
		%>
		</script>
		<!-- You are currently looking at the core server. 
			Nothing related to the levels in Security Shepherd will be found in here. 
			You might be looking for the iframe embeded in the page.
			Try a tool like Firebug to make this stuff easier. -->
		</body>
		</html>
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
