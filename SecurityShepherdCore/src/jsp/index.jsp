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
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
		// Getting Session Variables
		//This encoder should escape all output to prevent XSS attacks. This should be performed everywhere for safety
		Encoder encoder = ESAPI.encoder();
		String csrfToken = encoder.encodeForHTML(tokenCookie.getValue());
		String userName = encoder.encodeForHTML(ses.getAttribute("userName").toString());
		String userRole = encoder.encodeForHTML(ses.getAttribute("userRole").toString());
		String userId = encoder.encodeForJavaScript(ses.getAttribute("userStamp").toString());
		boolean canSeeScoreboard = ScoreboardStatus.canSeeScoreboard((String)ses.getAttribute("userRole"));
		String csrfJsToken = encoder.encodeForJavaScript(tokenCookie.getValue());
		String ApplicationRoot = getServletContext().getRealPath("");
		boolean showCheatSheet = CheatSheetStatus.showCheat(userRole);
		int i = 0;
%>
		<html xmlns="http://www.w3.org/1999/xhtml">
		<head>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
		<title>OWASP Security Shepherd</title>

		<!-- You are currently looking at the core server. 
			Nothing related to the levels in Security Shepherd will be found in here. 
			You might be looking for the iframe embeded in the page.
			Try a tool like Firebug to make this stuff easier.
			
			Security Shepherd Version: 2.4 -->

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
				<input type="hidden" id="currentModule" value="">
					<table>
					<tr><td>
						Submit Result Key:</td><td> <input style="width: 470px;" type="text" id="moduleResult" value="" autocomplete="OFF"/></td><td><div id="submitForm"><input type="submit" value="Submit"/></div>
						<div id="submitLoading" style="display: none;">Loading... </div>
					</td></tr>
					</table>
					<div id="resultResponse"></div>
				</form>
				<% if(showCheatSheet) { %>
					<div id="solutionDiv" style="display:none;"></div>
				<% } %>
			</div>
			<!-- You are currently looking at the core server. 
			Nothing related to the levels in Security Shepherd will be found in here. 
			You might be looking for the iframe embeded in the page.
			Try a tool like Firebug to make this stuff easier. -->
			<div id="contentDiv">
				<!-- Ajax Div -->
			</div>
			<div id="sidebar">
				<ul>
					<% if (userRole.compareTo("admin") == 0){ %>
						<li>
							 <a id="adminList" href="javascript:;"><div class='menuButton'>Admin</div></a>
							<ul id="theAdminList" style="display: none;">
								<li>
									<a id="cheatSheetManagementList" href="javascript:;">Cheat Sheet Management</a>
									<ul id="theCheatSheetManagementList" style="display: none;">
										<li><a id="createCheatsLink" href="javascript:;">Create New Cheat Sheet</a></li>
										<li><a id="disableCheatsLink" href="javascript:;">Disable Cheat Sheets</a></li>
										<li><a id="enableCheatsLink" href="javascript:;">Enable Cheat Sheets</a></li>										
									</ul>
								</li>
								<li>
									<a id="configurationList" href="javascript:;">Configuration</a>
									<ul id="theConfigurationList" style="display: none;">
										<li><a id="aboutShepherdLink" href="javascript:;">About Security Shepherd</a></li>
										<li><a id="levelLayoutLink" href="javascript:;">Change Module Layout</a></li>
										<li><a id="scoreboardLink" href="javascript:;">Configure Scoreboard</a></li>
										<li><a id="disableFeedbackLink" href="javascript:;">Disable Feedback</a></li>
										<li><a id="enableFeedbackLink" href="javascript:;">Enable Feedback</a></li>
										<li><a id="setCoreDatabaseLink" href="javascript:;">Set Core Database</a></li>
									</ul>
								</li>
								<li>
									<a id="moduleManagementList" href="javascript:;">Module Management</a>
									<ul id="theModuleManagementList" style="display: none;">
										<li><a id="disableBlockLink" href="javascript:;">Disable Module Block</a></li>
										<li><a id="stopHereLink" href="javascript:;">Enable Module Block</a></li>
										<li><a id="setModuleStatusLink" href="javascript:;">Open and Close Modules</a></li>
										<li><a id="openCloseByCategory" href="javascript:;">Open or Close by Category</a></li>
										<li><a id="feedbackLink" href="javascript:;">View Feedback</a></li>
										<li><a id="progressLink" href="javascript:;">View Progress</a></li>
									</ul>
								</li>
								<li>
									<a id="userManagementList" href="javascript:;">User Management</a>
									<ul id="theUserManagementList" style="display: none;">
										<li><a id="addPlayersLink" href="javascript:;">Add Players</a></li>
										<li><a id="updatePlayerScoreLink" href="javascript:;">Add / Deduct Player Points</a></li>
										<li><a id="assignPlayersLink" href="javascript:;">Assign Players to Class</a></li>
										<li><a id="createNewClassLink" href="javascript:;">Create Class</a></li>
										<li><a id="createNewAdminLink" href="javascript:;">Create New Admin</a></li>
										<li><a id="registrationLink" href="javascript:;">Open/Close Registration</a></li>
										<li><a id="changePlayerPasswordLink" href="javascript:;">Reset Password</a></li>
										<li><a id="setDefaultClassForRegistrationLink" href="javascript:;">Set Default Player Class</a></li>
										<li><a id="suspendPlayerLink" href="javascript:;">Suspend Player</a></li>
										<li><a id="unSuspendPlayerLink" href="javascript:;">Unsuspend Player</a></li>
										<li><a id="upgradePlayersLink" href="javascript:;">Upgrade Player to Admin</a></li>
									</ul>
								</li>
							</ul>
						</li>
					<% } %>
					<% if(canSeeScoreboard) { %>
					<div id="scoreboardButton">
						<a id="showScoreboard" href="scoreboard.jsp" target="bar"><div class="menuButton">Scoreboard</div></a>
					</div>
					<% } %>
					<% if(showCheatSheet) { %>
					<div id="cheatSheetButton" style="display: none;">
						<a id="showSolution" href="javascript:;"><div class="menuButton">Cheat</div></a>
					</div>
					<% } %>
					<% if(ModulePlan.isOpenFloor()) { %>
						<li>
							<a id="lessonList" href="javascript:;"><div class="menuButton">Lessons</div></a>
							<ul id="theLessonList" style="display: none;">
								<%= Getter.getLessons(ApplicationRoot, (String)ses.getAttribute("userStamp")) %>
							</ul>
						</li>
						<li>
							<a id="challengeList" href="javascript:;"><div class="menuButton">Challenges</div></a>
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
							<%= Getter.getTournamentModules(ApplicationRoot, (String)ses.getAttribute("userStamp")) %>
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
			<script>

			$("#updatePlayerScoreLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/givePoints.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#suspendPlayerLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/suspendUser.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#unSuspendPlayerLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/unSuspendUser.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#changePlayerPasswordLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/changeUserPassword.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#createNewAdminLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/createNewAdmin.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#createNewClassLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/createNewClass.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#addPlayersLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/addPlayers.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#assignPlayersLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/assignPlayers.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#setDefaultClassForRegistrationLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/setDefaultClassForRegistration.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#upgradePlayersLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/upgradePlayers.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#enableCheatsLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/cheatManagement/enableCheats.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});
				});	
			});

			$("#disableCheatsLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/cheatManagement/disableCheats.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});
				});	
			});

			$("#createCheatsLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/cheatManagement/createCheat.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#feedbackLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/moduleManagement/feedback.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#setModuleStatusLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/moduleManagement/setStatus.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#openCloseByCategory").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/moduleManagement/openCloseByCategory.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});
				});	
			});

			$("#registrationLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/updateRegistration.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#progressLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/moduleManagement/classProgress.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#scoreboardLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/config/scoreboard.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#stopHereLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/moduleManagement/stopHere.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#disableBlockLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/moduleManagement/removeModuleBlock.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#setCoreDatabaseLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/config/setCoreDatabase.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#enableFeedbackLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/config/enableFeedback.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#disableFeedbackLink").click(function(){
					$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/config/disableFeedback.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});
				});	
			});
			
			$("#aboutShepherdLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/config/aboutShepherd.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});
				});	
			});
			
			$("#levelLayoutLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/config/changeLevelLayout.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "Sorry but there was an error: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});
				});	
			});
			</script>
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
						<% if(showCheatSheet) { %>
							$("#submitResult").slideDown("fast", function(){
								$("#cheatSheetButton").slideDown("fast", function(){
									$("#contentDiv").slideDown("slow");
								});
							});
						<% } else { %>
							$("#submitResult").slideDown("fast", function(){
								$("#contentDiv").slideDown("slow");
							});
						<% } %>
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
					<% if(showCheatSheet) { %>
						$("#submitResult").slideDown("fast", function(){
							$("#cheatSheetButton").slideDown("fast", function(){
								$("#contentDiv").slideDown("slow");
							});
						});
					<% } else { %>
						$("#submitResult").slideDown("fast", function(){
							$("#contentDiv").slideDown("slow");
						});
					<% } %>
					}).appendTo('#contentDiv');
				}
				else
				{
					$('#contentDiv').html("<p> Sorry but there was a lesson error: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
					$("#contentDiv").slideDown("slow");
				}
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
		
		<% if(showCheatSheet) { %>
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
		<% } %>
		
		<% if(ModulePlan.tornyFloor){%>
			$("#fieldTrainingList").click(function () {
				$("#theFieldTrainingList").toggle("slow");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#theAdmiralList").hide("fast");
			});  
			
			$("#corporalList").click(function () {
				$("#theCorporalList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#theAdmiralList").hide("fast");
			});  
			
			$("#sergeantList").click(function () {
				$("#theSergeantList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theCorporalList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#theAdmiralList").hide("fast");
			});  
			
			$("#majorList").click(function () {
				$("#theMajorList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theAdmiralList").hide("fast");
			}); 
			
			$("#admiralList").click(function () {
				$("#theAdmiralList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theMajorList").hide("fast");
			}); 
			
		<% } %>
		
		</script>
		<!-- You are currently looking at the core server. 
		Nothing related to the levels in Security Shepherd will be found in here. 
		You might be looking for the iframe embeded in the page.
		Try a tool like Firebug to make this stuff easier. -->
		<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
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
