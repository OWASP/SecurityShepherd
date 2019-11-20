<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java"
	import="java.sql.*,java.io.*,java.net.*,org.owasp.encoder.Encode, dbProcs.*, utils.*"%>
<%@ page import="java.util.Locale"%>
<%@ include file="translation.jsp"%>
<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: index.jsp *************************");
	Locale lang = new Locale(Validate.validateLanguage(request.getSession()));
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
		//The org.owasp.encoder.Encode class should be used to encode any softcoded data. This should be performed everywhere for safety
		
		String csrfToken = Encode.forHtml(tokenCookie.getValue());
		String userName = Encode.forHtml(ses.getAttribute("userName").toString());
		String userRole = Encode.forHtml(ses.getAttribute("userRole").toString());
		String userId = Encode.forJavaScriptBlock(ses.getAttribute("userStamp").toString());
		boolean canSeeScoreboard = ScoreboardStatus.canSeeScoreboard((String)ses.getAttribute("userRole"));
		String csrfJsToken = Encode.forJavaScriptBlock(tokenCookie.getValue());
		String ApplicationRoot = getServletContext().getRealPath("");
		boolean showCheatSheet = CheatSheetStatus.showCheat(userRole);
		int i = 0;
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>OWASP Security Shepherd</title>

<!-- 
			<fmt:message key="generic.text.commentMessage.1" /> 
			<fmt:message key="generic.text.commentMessage.2" /> 
			<fmt:message key="generic.text.commentMessage.3" /> 
			<fmt:message key="generic.text.commentMessage.4" />
			
			<fmt:message key="generic.text.shepherdVersion" />
		-->

<link href="css/theCss.css" rel="stylesheet" type="text/css"
	media="screen" />
<link href="css/theResponsiveCss.css" rel="stylesheet" type="text/css"
	media="screen">
<link href="css/jquery.mCustomScrollbar.min.css" rel="stylesheet"
	type="text/css" media="screen">
<link rel="shortcut icon" href="css/images/flavicon.jpg"
	type="image/jpeg" />

</head>
<body>
	<script type="text/javascript" src="js/jquery.js"></script>
	<script type="text/javascript" src="js/jqueryUI.js"></script>
	<script type="text/javascript"
		src="js/jquery.mCustomScrollbar.concat.min.js"></script>
	<div id="wrapper">
		<jsp:include page="translation-select.jsp" />
		<div id="header">
			<h1>Security Shepherd</h1>
			<div style="position: absolute; top: 12px; right: 130px;">
				<p>
					<strong><%= userName %>&nbsp;&#x7c;&nbsp;<a
						href="logout?csrfToken=<%= csrfToken %>"><fmt:message
								key="generic.text.logout" /></a></strong>
				</p>
			</div>
		</div>

		<div id="page">
			<div id="submitResult">
				<form action="javascript:;" id="resultForm">
					<input type="hidden" id="currentModule" value="">
					<table class="resultTable">
						<tr>
							<td class="resultBoxCell"><input type="text"
								id="moduleResult" class="resultbox"
								placeholder="<fmt:message key="generic.text.submitResult" />..."
								autocomplete="OFF" /></td>
							<td class="submitResultCell"><div id="submitForm">
									<input type="submit"
										value="<fmt:message key="generic.text.submit" />" />
								</div>
								<div id="submitLoading" style="display: none;">
									<fmt:message key="generic.text.loading" />
								</div></td>
						</tr>
					</table>
					<div id="resultResponse"></div>
				</form>
				<% if(showCheatSheet) { %>
				<div id="solutionDiv" style="display: none;"></div>
				<% } %>
			</div>
			<!-- <fmt:message key="generic.text.commentMessage.1" /> 
			<fmt:message key="generic.text.commentMessage.2" /> 
			<fmt:message key="generic.text.commentMessage.3" /> 
			<fmt:message key="generic.text.commentMessage.4" /> -->
			<div id="contentDiv">
				<!-- Ajax Div -->
			</div>
			<div id="theSidebarWrapper" class="sidebarWrapper"
				onmouseover="resizeSidebar()">
				<div class="menuIcon">&#9776;</div>
				<div id="sidebar">
					<ul>
						<% if (userRole.compareTo("admin") == 0){ %>
						<li><a id="adminList" href="javascript:;"><div
									class='menuButton'>
									<fmt:message key="generic.text.admin" />
								</div></a>
							<ul id="theAdminList" style="display: none;">
								<li><a id="configurationList" href="javascript:;"><fmt:message
											key="generic.text.configuration" /></a>
									<ul id="theConfigurationList" style="display: none;">
										<li><a id="aboutShepherdLink" href="javascript:;"><fmt:message
													key="generic.text.aboutSecShep" /></a></li>
										<li><a id="cheatSheetLink" href="javascript:;"><fmt:message
													key="index.link.admin.cheatSheet.manage" /></a></li>
										<li><a id="configureFeedbackLink" href="javascript:;"><fmt:message
													key="index.link.admin.config.feedback" /></a></li>
										<li><a id="registrationLink" href="javascript:;"><fmt:message
													key="index.link.admin.config.openClose" /></a></li>
										<li><a id="scoreboardLink" href="javascript:;"><fmt:message
													key="index.link.admin.config.scoreboard" /></a></li>
										<li><a id="setCoreDatabaseLink" href="javascript:;"><fmt:message
													key="index.link.admin.config.coreDb" /></a></li>
									</ul></li>
								<li><a id="moduleManagementList" href="javascript:;"><fmt:message
											key="index.link.admin.moduleManage" /></a>
									<ul id="theModuleManagementList" style="display: none;">
										<li><a id="levelLayoutLink" href="javascript:;"><fmt:message
													key="index.link.admin.config.change" /></a></li>
										<li><a id="moduleBlockLink" href="javascript:;"><fmt:message
													key="index.link.admin.moduleManage.block" /></a></li>
										<li><a id="setModuleStatusLink" href="javascript:;"><fmt:message
													key="index.link.admin.moduleManage.openClose" /></a></li>
										<li><a id="openCloseByCategory" href="javascript:;"><fmt:message
													key="index.link.admin.moduleManage.openCloseCategory" /></a></li>
										<li><a id="feedbackLink" href="javascript:;"><fmt:message
													key="index.link.admin.moduleManage.feedback" /></a></li>
										<li><a id="progressLink" href="javascript:;"><fmt:message
													key="index.link.admin.moduleManage.progress" /></a></li>
									</ul></li>
								<li><a id="userManagementList" href="javascript:;"><fmt:message
											key="index.link.admin.userMange" /></a>
									<ul id="theUserManagementList" style="display: none;">
										<li><a id="addPlayersLink" href="javascript:;"><fmt:message
													key="index.link.admin.userMange.addPlayer" /></a></li>
										<li><a id="deletePlayersLink" href="javascript:;"><fmt:message
													key="index.link.admin.userMange.deletePlayer" /></a></li>
										<li><a id="updatePlayerScoreLink" href="javascript:;"><fmt:message
													key="index.link.admin.userMange.addPoints" /></a></li>
										<li><a id="assignPlayersLink" href="javascript:;"><fmt:message
													key="index.link.admin.userMange.assignPlayer" /></a></li>
										<li><a id="createNewClassLink" href="javascript:;"><fmt:message
													key="index.link.admin.userMange.createClass" /></a></li>
										<li><a id="createNewAdminLink" href="javascript:;"><fmt:message
													key="index.link.admin.userMange.createAdmin" /></a></li>
										<li><a id="changePlayerPasswordLink" href="javascript:;"><fmt:message
													key="index.link.admin.userMange.resetPass" /></a></li>
										<li><a id="setDefaultClassForRegistrationLink"
											href="javascript:;"><fmt:message
													key="index.link.admin.userMange.setDefaultPlayerClass" /></a></li>
										<li><a id="suspendPlayerLink" href="javascript:;"><fmt:message
													key="index.link.admin.userMange.suspendPlayer" /></a></li>
										<li><a id="unSuspendPlayerLink" href="javascript:;"><fmt:message
													key="index.link.admin.userMange.unsuspendPlayer" /></a></li>
										<li><a id="upgradePlayersLink" href="javascript:;"><fmt:message
													key="index.link.admin.userMange.upgradeToAdmin" /></a></li>
										<li><a id="downgradeAdminLink" href="javascript:;"><fmt:message
													key="index.link.admin.userMange.downgradeToPlayer" /></a></li>
									</ul></li>
							</ul></li>
						<% } %>
						<% if(canSeeScoreboard) { %>
						<div id="scoreboardButton">
							<a id="showScoreboard" href="scoreboard.jsp" target="bar"><div
									class="menuButton">
									<fmt:message key="generic.text.scoreboard" />
								</div></a>
						</div>
						<% } %>
						<% if(showCheatSheet) { %>
						<div id="cheatSheetButton" style="display: none;">
							<a id="showSolution" href="javascript:;"><div
									class="menuButton">
									<fmt:message key="generic.text.cheat" />
								</div></a>
						</div>
						<% } if (CountdownHandler.isRunning() || (userRole.compareTo("admin") == 0) ) { %>
						<div id="levelListDiv">
							<div id="sideMenuWrapper">
								<% if(ModulePlan.isOpenFloor()) { %>
								<li><a id="lessonList" href="javascript:;"><div
											class="menuButton">
											<fmt:message key="generic.text.lessons" />
										</div></a>
									<ul id="theLessonList" style="display: none;">
										<%= Getter.getLessons(ApplicationRoot, (String)ses.getAttribute("userStamp"), lang) %>
									</ul></li>
								<li><a id="challengeList" href="javascript:;"><div
											class="menuButton">
											<fmt:message key="generic.text.challenges" />
										</div></a>
									<ul id="theChallengeList" style="display: none;">
										<%= Getter.getChallenges(ApplicationRoot, (String)ses.getAttribute("userStamp"), lang) %>
									</ul></li>
								<% } else {
								if(ModulePlan.isIncrementalFloor()){ %>
								<%= Getter.getIncrementalModulesWithoutScript(ApplicationRoot, (String)ses.getAttribute("userStamp"), ses.getAttribute("lang").toString() ,csrfToken) %>
								<% } else {%>
								<li><%= Getter.getTournamentModules(ApplicationRoot, (String)ses.getAttribute("userStamp"), lang) %>
								</li>
								<% }
							} //End of Module List Output %>
							</div>
						</div>
						<% } %>
						<div id="menuRefreshLoadingDiv"></div>
						<div>
							<input id="searchModules" class="moduleSearchBox" type="search"
								placeholder="<fmt:message key="generic.text.searchModules" />...">
						</div>
						<div id="searchResults">
							<!-- Results from module search go here -->
						</div>
						<script>
							function makeSearchList() {
								//Make list for module search box
								console.log("Making Search List...");
								var availableModules = [];
								$(".lesson").each(function(index){
									availableModules.push($(this).text());
								}); //Make array out of available modules listed
								console.log(availableModules.length + " modules added to search list");
								$("#searchModules").autocomplete({
									source: availableModules,
									appendTo: "#searchResults"
								});
							}
							
							//Make Search List
							makeSearchList();
							
							$("#searchModules").on("autocompleteselect", function( event, ui ) {
								var toOpen = ui.item.value
								console.log("Opening: " + toOpen);
								$(".lesson").each(function(index){
									console.log($(this));
									if($(this).html().indexOf(toOpen) > -1){ //If the Level description exists in the lesson entry, then we have the right one
										console.log("Clicking lesson entry...");
										$(this).click();
										return false;
									}
								});
								console.log("Select Function Finish");
							});
							
							$("#searchModules").on("autocompleteopen", function( event, ui ) {
								console.log("Module Search Box Opened");
								$('#theSidebarWrapper').removeClass('sidebarWrapper').addClass("sidebarWrapperAlwaysOpen");
							});
							
							$("#searchModules").on("autocompleteclose", function( event, ui ) {
								console.log("Module Search Box Closed");
								$('#theSidebarWrapper').removeClass('sidebarWrapperAlwaysOpen').addClass("sidebarWrapper");
							});
						</script>
						<div id="startTimer">
							<!-- Countdown to start time -->
							<div id="startMessage"></div>
							<div id="startDays"></div>
							<div id="startHours"></div>
							<div id="startMinutes"></div>
							<div id="startSeconds"></div>
						</div>
						<div id="lockTimer">
							<!-- Countdown to lock time -->
							<div id="lockMessage"></div>
							<div id="lockDays"></div>
							<div id="lockHours"></div>
							<div id="lockMinutes"></div>
							<div id="lockSeconds"></div>
						</div>
						<div id="endTimer">
							<!-- Countdown to end time -->
							<div id="endMessage"></div>
							<div id="endDays"></div>
							<div id="endHours"></div>
							<div id="endMinutes"></div>
							<div id="endSeconds"></div>
						</div>
						<script>
						function makeTimer(prefix, endTime, message) {
					
							var endDateTime = new Date(endTime);			
							endDateTime = (Date.parse(endTime) / 1000);

							var now = new Date();
							now = (Date.parse(now) / 1000);

							var timeLeft = endDateTime - now;
							
							if(timeLeft < 0) {
								
								$("#" + prefix + "Message").html();
								$("#" + prefix + "Days").html();
								$("#" + prefix + "Hours").html();
								$("#" + prefix + "Minutes").html();
								$("#" + prefix + "Seconds").html();
								
								return;
							}

							$("#" + prefix + "Message").html(message);
							
							var days = Math.floor(timeLeft / 86400); 
							var hours = Math.floor((timeLeft - (days * 86400)) / 3600);
							var minutes = Math.floor((timeLeft - (days * 86400) - (hours * 3600 )) / 60);
							var seconds = Math.floor((timeLeft - (days * 86400) - (hours * 3600) - (minutes * 60)));
				  
							if (days > "0") {
								$("#" + prefix + "Days").html(days + "<span>Days</span>");
							} else {
								$("#" + prefix + "Days").html();								
							}
							
							if (hours > "0") {
								if (hours < "10") { 
									hours = "0" + hours; 
								}
								$("#" + prefix + "Hours").html(hours + "<span>Hours</span>");
							} else {
								$("#" + prefix + "Hours").html();
							}
							
							if (minutes > "0") {
								if (minutes < "10") { 
									minutes = "0" + minutes; 
								}
								$("#" + prefix + "Minutes").html(minutes + "<span>Minutes</span>");
							} else {
								$("#" + prefix + "Minutes").html();								
							}
							
							if (seconds > "0") {
								if (seconds < "10") { 
									seconds = "0" + seconds; 
								}
								$("#" + prefix + "Seconds").html(seconds + "<span>Seconds</span>");		
							} else {
								$("#" + prefix + "Seconds").html();		

							}
							}
							
							<% if (CountdownHandler.willStart()) { %>
							setInterval(function() { makeTimer("start", "<% out.print(CountdownHandler.getStartTime()); %>", "CTF will start in "); }, 1000);
							<% 
							}
							
							if (CountdownHandler.willLock()) { %>
							setInterval(function() { makeTimer("lock", "<% out.print(CountdownHandler.getLockTime()); %>", "Points available for "); }, 1000);
							<% 
							}
							
							if (CountdownHandler.willLock() || CountdownHandler.willEnd()) { %>
							setInterval(function() { makeTimer("end", "<% out.print(CountdownHandler.getEndTime()); %>", "CTF ends in "); }, 1000);
							<% 
							} %>
							
						</script>
					</ul>
					<!-- <fmt:message key="generic.text.commentMessage.1" /> 
					<fmt:message key="generic.text.commentMessage.2" /> 
					<fmt:message key="generic.text.commentMessage.3" /> 
					<fmt:message key="generic.text.commentMessage.4" /> -->
				</div>
				<!-- End of Sidebar -->
			</div>
			<!-- End of Sidebar Wrapper -->
		</div>
	</div>
	<script>
		$("#contentDiv").load("getStarted.jsp", function(response, status, xhr) {
			if (status == "error") {
			var msg = "Sorry but there was an error: ";
			$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
		  }
		});
		</script>

	<% //Hide UI Scripts from Users (Blocked at session level anyway, just stops spiders finding the links)
		if (userRole.compareTo("admin") == 0){ %>
	<script src="js/adminToggle.js"></script>
	<script>

			$("#updatePlayerScoreLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/givePoints.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});
			
			$("#deletePlayersLink").click(function(){
			   $("#submitResult").slideUp("fast", function(){
			       $("#contentDiv").hide("fast", function(){
			           $("#contentDiv").load("admin/userManagement/deletePlayers.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
			             if (status == "error") {
			                   var msg = "<fmt:message key="generic.text.sorryError" />: ";
			                   $("#contentDiv").html("<p" + msg + xhr.status + " " + xhr.statusText + "</p");
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});
			
			$("#downgradeAdminLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/userManagement/downgradeAdmins.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
						$("#contentDiv").load("admin/config/updateRegistration.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#moduleBlockLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/moduleManagement/moduleBlock.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});	
				});
			});

			$("#configureFeedbackLink").click(function(){
					$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/config/configFeedback.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});
				});	
			});
			
			$("#cheatSheetLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/config/configCheats.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
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
						$("#contentDiv").load("admin/moduleManagement/changeLevelLayout.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
						  if (status == "error") {
							var msg = "<fmt:message key="generic.text.sorryError" />: ";
							$("#contentDiv").html("<p>" + msg + xhr.status + " " + xhr.statusText + "</p>");
						  }
						  $("#contentDiv").show("fast");
						});
					});
				});	
			});
			</script>
	<%
		}
	%>

	<script>
			function applyMenuButtonActionsOpenOrTourney(theCsrfToken, theErrorMessage){
				console.log("Applying Menu Actions For Open/Tourney");
				
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
								csrfToken: theCsrfToken
							},
							async: false
						});
						if(ajaxCall.status == 200)
						{
							theActualFile = ajaxCall.responseText;
							$('#contentDiv').html("<iframe frameborder='no' class='levelIframe' id='theChallenge' src='" + theActualFile + "'></iframe>");
							$("#theChallenge").load(function(){
								<%if (showCheatSheet) {%>
									$("#submitResult").slideDown("fast", function(){
										$("#cheatSheetButton").slideDown("fast", function(){
											$("#contentDiv").slideDown("slow", function(){
												var scrollTo = $("#moduleResult").offset().top;
												scrollTo = scrollTo - 60;
												console.log("Scroll Up to: " + scrollTo);
												$('html, body').animate({
													scrollTop: scrollTo
												}, 1000);
											});
										});
									});
								<%} else {%>
									$("#submitResult").slideDown("fast", function(){
										$("#contentDiv").slideDown("slow", function(){
											var scrollTo = $("#moduleResult").offset().top;
											scrollTo = scrollTo - 60;
											console.log("Scroll Up to: " + scrollTo);
											$('html, body').animate({
												scrollTop: scrollTo
											}, 1000);
										});
									});
								<%}%>
							}).appendTo('#contentDiv');
							$("#theSidebarWrapper").height($("#contentDiv").height());
						}
						else
						{
							$('#contentDiv').html("<p> <fmt:message key="generic.text.sorryError" />: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
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
								csrfToken: theCsrfToken
							},
							async: false
						});
						if(ajaxCall.status == 200)
						{
							theActualFile = ajaxCall.responseText;
							$('#contentDiv').html("<iframe frameborder='no' class='levelIframe' id='theLesson' src='" + theActualFile + "'></iframe>");
							$("#theLesson").load(function(){
							<%if (showCheatSheet) {%>
								$("#submitResult").slideDown("fast", function(){
									$("#cheatSheetButton").slideDown("fast", function(){
										$("#contentDiv").slideDown("slow", function(){
											var scrollTo = $("#moduleResult").offset().top;
											scrollTo = scrollTo - 60;
											console.log("Scroll Up to: " + scrollTo);
											$('html, body').animate({
												scrollTop: scrollTo
											}, 1000);
										});
									});
								});
							<%} else {%>
								$("#submitResult").slideDown("fast", function(){
									$("#contentDiv").slideDown("slow", function(){
										var scrollTo = $("#moduleResult").offset().top;
										scrollTo = scrollTo - 60;
										console.log("Scroll Up to: " + scrollTo);
										$('html, body').animate({
											scrollTop: scrollTo
										}, 1000);
									});
								});
							<%}%>
							}).appendTo('#contentDiv');
							$("#theSidebarWrapper").height($("#contentDiv").height());
						}
						else
						{
							$('#contentDiv').html("<p> <fmt:message key="generic.text.sorryError" />: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
							$("#contentDiv").slideDown("slow");
						}
					});
				});
			}
			
			function applyMenuButtonActionsCtfMode(theCsrfToken, theErrorMessage){
				console.log("Applying JS Functions to Incremental Menu Buttons");
				
				$("#completedList").click(function () {
					$("#theCompletedList").toggle("slow");
					$("#theUncompletedList").hide("fast");
					$("#theAdminList").hide("fast");
				});
		
				$("#uncompletedList").click(function () {
					$("#theUncompletedList").toggle("slow");
					$("#theCompletedList").hide("fast");
					$("#theAdminList").hide("fast");
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
								csrfToken: theCsrfToken
							},
						async: false
						});
						if(ajaxCall.status == 200) {
							theActualFile = ajaxCall.responseText;
							$('#contentDiv').html("<iframe frameborder='no' class='levelIframe' id='theLesson' src='" + theActualFile + "'></iframe>");
							$("#theLesson").load(function(){
								<%if (showCheatSheet) {%>
									$("#submitResult").slideDown("fast", function(){
										$("#cheatSheetButton").slideDown("fast", function(){
											$("#contentDiv").slideDown("slow", function(){
												var scrollTo = $("#moduleResult").offset().top;
												scrollTo = scrollTo - 60;
												console.log("Scroll Up to: " + scrollTo);
												$('html, body').animate({
													scrollTop: scrollTo
												}, 1000);
											});
										});
									});
									<%} else {%>
									$("#submitResult").slideDown("fast", function(){
										$("#contentDiv").slideDown("slow", function(){
											var scrollTo = $("#moduleResult").offset().top;
											scrollTo = scrollTo - 60;
											console.log("Scroll Up to: " + scrollTo);
											$('html, body').animate({
												scrollTop: scrollTo
											}, 1000);
										});
									});
								<%}%>
							}).appendTo('#contentDiv');
						} else {
							$('#contentDiv').html("<p> " + theErrorMessage + ": " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
							$("#contentDiv").slideDown("slow");
						}
					});
				});
			}
		<%if (ModulePlan.isIncrementalFloor()) {%>
			applyMenuButtonActionsCtfMode('<%=Encode.forHtml(csrfToken)%>', "<fmt:message key="generic.text.sorryError"/>");
		<%} //End of if(CTF Mode Enabled)%>
		<%if (!ModulePlan.isIncrementalFloor()) {%>
			applyMenuButtonActionsOpenOrTourney('<%=Encode.forHtml(csrfToken)%>', "<fmt:message key="generic.text.sorryError"/>");
		<%} // End of Not CTF Mode If%>
		//RefreshModuleFormScript
		function refreshSideMenu(theCsrfToken, localErrorMessage){
			$("#menuRefreshLoadingDiv").show("fast");
			$("#sideMenuWrapper").slideUp("fast", function(){
				var ajaxCall = $.ajax({
					type: "POST",
					url: "refreshMenu",
					data: {
						csrfToken: theCsrfToken
					},
					async: false
				});
				$("#menuRefreshLoadingDiv").slideUp("fast", function(){
					if(ajaxCall.status == 200)
					{
						$("#sideMenuWrapper").html(ajaxCall.responseText);
					}
					else
					{
						$("#sideMenuWrapper").append("<br/><font color='red'>" + localErrorMessage + ": " + ajaxCall.status + "</font>");
					}
					$('#sideMenuWrapper').slideDown('slow');
				});
			});
		}

		$("#resultForm").submit(function(){
			var theKey = $("#moduleResult").val();
			var theModuleId = $("#currentModule").val();
			if(theKey != null)
			{
				$("#submitLoading").slideDown("fast");
				$("#solutionDiv").slideUp("fast");
				$("#resultResponse").slideUp("fast");
				$("#submitForm").slideUp("fast");
				//The Ajax Operation
				$("#contentDiv").slideUp("fast", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "solutionSubmit",
						data: {
							moduleId: theModuleId,
							solutionKey: theKey,
							csrfToken: "<%=csrfToken%>"
						},
						async: false
					});
					$("#submitLoading").slideUp("fast", function(){
						if(ajaxCall.status == 200)
						{
							console.log("Request OK. Showing Reponse");
							$('#contentDiv').html(ajaxCall.responseText);
						}
						else
						{
							$('#resultResponse').html("<br/><p> <fmt:message key="generic.text.sorryError" />: " + ajaxCall.status + " " + ajaxCall.statusText + "</p><br/>");
							$("#resultResponse").show("slow");
						}
						$("#moduleResult").val("");
						$("#submitForm").slideDown("slow");
						$("#contentDiv").slideDown("slow");
					});
				});
			}
			else
			{
				console.log("No Key Submitted");
			}
		});
		
		<%if (showCheatSheet) {%>
		$("#showSolution").click(function(){
			$("#solutionDiv").hide("fast", function(){
				var theModuleId = $("#currentModule").val();
				var ajaxCall = $.ajax({
					type: "POST",
					url: "getCheat",
					data: {
						moduleId: theModuleId,
						csrfToken: "<%=csrfToken%>"
									},
									async : false
								});
								if (ajaxCall.status == 200) {
									$('#solutionDiv').html(
											ajaxCall.responseText);
								} else {
									$('#solutionDiv').html(
											"<p> <fmt:message key="generic.text.sorryError" />: "
													+ ajaxCall.status + " "
													+ ajaxCall.statusText
													+ "</p>");
								}
								$("#solutionDiv").show("fast");
							});
				});
	<%}%>
		$(".successAlert").click(function() {
			alert("successAlert click");
			$(this).hide("slide", {
				direction : "left"
			}, 1000);
		});

		$(".errorAlert").click(function() {
			$(this).hide("slide", {
				direction : "left"
			}, 1000);
		});

		function openFloorToggleFunctions() {
			console.log("Enabling Open Floor Toggle Funtions");

			$("#lessonList").click(function() {
				$("#theLessonList").toggle("slow");
				$("#theChallengeList").hide("fast");
				$("#theAdminList").hide("fast");
			});

			$("#challengeList").click(function() {
				$("#theChallengeList").toggle("slow");
				$("#theLessonList").hide("fast");
				$("#theAdminList").hide("fast");
			});

			$(".challengeHeader").click(function() {
				$(".challengeList").hide("fast");
				$(this).parent().find(".challengeList").show("slow");
			});
		}
		openFloorToggleFunctions();

		function tournamentToggleFunctions() {
			console.log("Enablig Tournament Toggle Functions");

			$("#fieldTrainingList").click(function() {
				$("#theFieldTrainingList").toggle("slow");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#theAdmiralList").hide("fast");
				$("#thePrivateList").hide("fast");
				$("#theLieutenantList").hide("fast");
			});

			$("#privateList").click(function() {
				$("#thePrivateList").toggle("slow");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#theAdmiralList").hide("fast");
				$("#theFieldTrainingList").hide("fast");
				$("#theLieutenantList").hide("fast");
			});

			$("#corporalList").click(function() {
				$("#theCorporalList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#theAdmiralList").hide("fast");
				$("#thePrivateList").hide("fast");
				$("#theLieutenantList").hide("fast");
			});

			$("#sergeantList").click(function() {
				$("#theSergeantList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theCorporalList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#theAdmiralList").hide("fast");
				$("#thePrivateList").hide("fast");
				$("#theLieutenantList").hide("fast");
			});

			$("#lieutenantList").click(function() {
				$("#theLieutenantList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theAdmiralList").hide("fast");
				$("#thePrivateList").hide("fast");
				$("#theMajorList").hide("fast");
			});

			$("#majorList").click(function() {
				$("#theMajorList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theAdmiralList").hide("fast");
				$("#thePrivateList").hide("fast");
				$("#theLieutenantList").hide("fast");
			});

			$("#admiralList").click(function() {
				$("#theAdmiralList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#thePrivateList").hide("fast");
				$("#theLieutenantList").hide("fast");
			});
		}
	<%if (ModulePlan.isTournamentFloor()) {%>
		tournamentToggleFunctions();
	<%}%>
		function resizeSidebar() {
			//Make Sidebar as Long as Page
			if ($("#contentDiv").height() > 700) {
				console.log("Updating Sidebar Length to "
						+ $("#contentDiv").height());
				$("#theSidebarWrapper").height($("#contentDiv").height());
			} else {
				console.log("Setting Sidebar to 130% because  "
						+ $("#contentDiv").height() + "px is too short.");
				$("#theSidebarWrapper").height("130%");
			}
		}
	</script>
	<script>
		(function($) {
			$(window).load(function() {
				console.log("Initialising Custom Scrollbars (If Any)");
				$(".levelList").mCustomScrollbar({
					theme : "dark-thin",
					mouseWheel : {
						scrollAmount : 120
					}
				});
			});
		})(jQuery);

		function startScrollsBars() {
			console.log("Initialising Custom Scrollbars Again (If Any)");
			$(".levelList").mCustomScrollbar({
				theme : "dark-thin",
				mouseWheel : {
					scrollAmount : 120
				}
			});
		}
	</script>
	<!-- <fmt:message key="generic.text.commentMessage.1" /> 
		<fmt:message key="generic.text.commentMessage.2" /> 
		<fmt:message key="generic.text.commentMessage.3" /> 
		<fmt:message key="generic.text.commentMessage.4" /> -->
	<%
		if (Analytics.googleAnalyticsOn) {
	%><%=Analytics.googleAnalyticsScript%>
	<%
		}
	%>
</body>
</html>
<%
	} else {
			response.sendRedirect("login.jsp");
		}
	} else {
		response.sendRedirect("login.jsp");
	}
%>
