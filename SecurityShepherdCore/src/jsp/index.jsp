<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>
<%@ include file="translation.jsp" %>
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
		<link href="css/theResponsiveCss.css" rel="stylesheet" type="text/css" media="screen">
		<link rel="shortcut icon" href="css/images/flavicon.jpg" type="image/jpeg" />

		</head>
		<body>
		<script type="text/javascript" src="js/jquery.js"></script>
		<script type="text/javascript" src="js/jqueryUI.js"></script>
		<div id="wrapper">
		<jsp:include page="translation-select.jsp" />
		<div id="header">
			<h1>Security Shepherd</h1>
			<div style="position: absolute;right: 100px;">
				<p>
					<strong><fmt:message key="index.label.welcome" /> <%= userName %></strong><br />
					<strong><a href="logout?csrfToken=<%= csrfToken %>"><fmt:message key="index.label.logout" /></a></strong>
				</p>
			</div>
		</div>
		
		<div id="page">
			<div id="submitResult">
				<form action="javascript:;" id="resultForm" >
				<input type="hidden" id="currentModule" value="">
					<table class="resultTable">
					<tr>
						<td> <input type="text" id="moduleResult" class="resultbox" placeholder="Submit Result Key Here..." autocomplete="OFF"/></td><td><div id="submitForm"><input type="submit" value="Submit"/></div>
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
			<div id="theSidebarWrapper" class="sidebarWrapper">
				<div class="menuIcon">
					&#9776;
				</div>
				<div id="sidebar">
					<ul>
						<% if (userRole.compareTo("admin") == 0){ %>
							<li>
								 <a id="adminList" href="javascript:;"><div class='menuButton'><fmt:message key="index.button.admin" /></div></a>
								<ul id="theAdminList" style="display: none;">
									<li>
										<a id="cheatSheetManagementList" href="javascript:;"><fmt:message key="index.link.admin.cheatSheet.manage" /></a>
										<ul id="theCheatSheetManagementList" style="display: none;">
											<li><a id="createCheatsLink" href="javascript:;"><fmt:message key="index.link.admin.cheatSheet.manage.create" /></a></li>
											<li><a id="disableCheatsLink" href="javascript:;"><fmt:message key="index.link.admin.cheatSheet.manage.disable" /></a></li>
											<li><a id="enableCheatsLink" href="javascript:;"><fmt:message key="index.link.admin.cheatSheet.manage.enable" /></a></li>										
										</ul>
									</li>
									<li>
										<a id="configurationList" href="javascript:;"><fmt:message key="index.link.admin.config" /></a>
										<ul id="theConfigurationList" style="display: none;">
											<li><a id="aboutShepherdLink" href="javascript:;"><fmt:message key="index.link.admin.config.about" /></a></li>
											<li><a id="levelLayoutLink" href="javascript:;"><fmt:message key="index.link.admin.config.change" /></a></li>
											<li><a id="configureFeedbackLink" href="javascript:;"><fmt:message key="index.link.admin.config.feedback" /></a></li>
											<li><a id="registrationLink" href="javascript:;"><fmt:message key="index.link.admin.config.openClose" /></a></li>
											<li><a id="scoreboardLink" href="javascript:;"><fmt:message key="index.link.admin.config.scoreboard" /></a></li>
											<li><a id="setCoreDatabaseLink" href="javascript:;"><fmt:message key="index.link.admin.config.coreDb" /></a></li>
										</ul>
									</li>
									<li>
										<a id="moduleManagementList" href="javascript:;"><fmt:message key="index.link.admin.moduleManage" /></a>
										<ul id="theModuleManagementList" style="display: none;">
											<li><a id="moduleBlockLink" href="javascript:;"><fmt:message key="index.link.admin.moduleManage.block" /></a></li>
											<li><a id="setModuleStatusLink" href="javascript:;"><fmt:message key="index.link.admin.moduleManage.openClose" /></a></li>
											<li><a id="openCloseByCategory" href="javascript:;"><fmt:message key="index.link.admin.moduleManage.openCloseCategory" /></a></li>
											<li><a id="feedbackLink" href="javascript:;"><fmt:message key="index.link.admin.moduleManage.feedback" /></a></li>
											<li><a id="progressLink" href="javascript:;"><fmt:message key="index.link.admin.moduleManage.progress" /></a></li>
										</ul>
									</li>
									<li>
										<a id="userManagementList" href="javascript:;"><fmt:message key="index.link.admin.userMange" /></a>
										<ul id="theUserManagementList" style="display: none;">
											<li><a id="addPlayersLink" href="javascript:;"><fmt:message key="index.link.admin.userMange.addPlayer" /></a></li>
											<li><a id="updatePlayerScoreLink" href="javascript:;"><fmt:message key="index.link.admin.userMange.addPoints" /></a></li>
											<li><a id="assignPlayersLink" href="javascript:;"><fmt:message key="index.link.admin.userMange.assignPlayer" /></a></li>
											<li><a id="createNewClassLink" href="javascript:;"><fmt:message key="index.link.admin.userMange.createClass" /></a></li>
											<li><a id="createNewAdminLink" href="javascript:;"><fmt:message key="index.link.admin.userMange.createAdmin" /></a></li>
											<li><a id="changePlayerPasswordLink" href="javascript:;"><fmt:message key="index.link.admin.userMange.resetPass" /></a></li>
											<li><a id="setDefaultClassForRegistrationLink" href="javascript:;"><fmt:message key="index.link.admin.userMange.setDefaultPlayerClass" /></a></li>
											<li><a id="suspendPlayerLink" href="javascript:;"><fmt:message key="index.link.admin.userMange.suspendPlayer" /></a></li>
											<li><a id="unSuspendPlayerLink" href="javascript:;"><fmt:message key="index.link.admin.userMange.unsuspendPlayer" /></a></li>
											<li><a id="upgradePlayersLink" href="javascript:;"><fmt:message key="index.link.admin.userMange.upgradeToAdmin" /></a></li>
										</ul>
									</li>
								</ul>
							</li>
						<% } %>
						<% if(canSeeScoreboard) { %>
						<div id="scoreboardButton">
							<a id="showScoreboard" href="scoreboard.jsp" target="bar"><div class="menuButton"><fmt:message key="index.button.scoreboard" /></div></a>
						</div>
						<% } %>
						<% if(showCheatSheet) { %>
						<div id="cheatSheetButton" style="display: none;">
							<a id="showSolution" href="javascript:;"><div class="menuButton"><fmt:message key="index.button.cheat" /></div></a>
						</div>
						<% } %>					
						<div id="levelListDiv">
							<% if(ModulePlan.isOpenFloor()) { %>
								<li>
									<a id="lessonList" href="javascript:;"><div class="menuButton"><fmt:message key="index.button.lessons" /></div></a>
									<ul id="theLessonList" style="display: none;">
										<%= Getter.getLessons(ApplicationRoot, (String)ses.getAttribute("userStamp")) %>
									</ul>
								</li>
								<li>
									<a id="challengeList" href="javascript:;"><div class="menuButton"><fmt:message key="index.button.challenges" /></div></a>
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
						</div>
						<div>
							<input id="searchModules" class="moduleSearchBox" type="search" placeholder="Search Modules...">
						</div> 
						<script>
							//Make list for module search box
							console.log("Making Search List...");
							var availableModules = [];
							$(".lesson").each(function(index){
								availableModules.push($(this).text());
							}); //Make array out of available modules listed
							console.log(availableModules.length + " modules added to search list");
							$("#searchModules").autocomplete({
								source: availableModules
							});
							
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
					</ul>
					<!-- You are currently looking at the core server. 
					Nothing related to the levels in Security Shepherd will be found in here. 
					You might be looking for the iframe embeded in the page.
					Try a tool like Firebug to make this stuff easier. -->
				</div> <!-- End of Sidebar -->
			</div> <!-- End of Sidebar Wrapper -->
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
						$("#contentDiv").load("admin/config/updateRegistration.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
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

			$("#moduleBlockLink").click(function(){
				$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/moduleManagement/moduleBlock.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
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

			$("#configureFeedbackLink").click(function(){
					$("#submitResult").slideUp("fast", function(){
					$("#contentDiv").hide("fast", function(){
						$("#contentDiv").load("admin/config/configFeedback.jsp?csrfToken=<%= csrfJsToken %>", function(response, status, xhr) {
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
					$('#contentDiv').html("<iframe frameborder='no' class='levelIframe' id='theChallenge' src='" + theActualFile + "'></iframe>");
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
					$('#contentDiv').html("<iframe frameborder='no' class='levelIframe' id='theLesson' src='" + theActualFile + "'></iframe>");
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
				$("#thePrivateList").hide("fast");
				$("#theLieutenantList").hide("fast");
			});  
			
			$("#privateList").click(function () {
				$("#thePrivateList").toggle("slow");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#theAdmiralList").hide("fast");
				$("#theFieldTrainingList").hide("fast");
				$("#theLieutenantList").hide("fast");
			}); 
			
			$("#corporalList").click(function () {
				$("#theCorporalList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#theAdmiralList").hide("fast");
				$("#thePrivateList").hide("fast");
				$("#theLieutenantList").hide("fast");
			});  
			
			$("#sergeantList").click(function () {
				$("#theSergeantList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theCorporalList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#theAdmiralList").hide("fast");
				$("#thePrivateList").hide("fast");
				$("#theLieutenantList").hide("fast");
			});  
			
			$("#lieutenantList").click(function () {
				$("#theLieutenantList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theAdmiralList").hide("fast");
				$("#thePrivateList").hide("fast");
				$("#theMajorList").hide("fast");
			}); 
			
			$("#majorList").click(function () {
				$("#theMajorList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theAdmiralList").hide("fast");
				$("#thePrivateList").hide("fast");
				$("#theLieutenantList").hide("fast");
			}); 
			
			$("#admiralList").click(function () {
				$("#theAdmiralList").toggle("slow");
				$("#theFieldTrainingList").hide("fast");
				$("#theCorporalList").hide("fast");
				$("#theSergeantList").hide("fast");
				$("#theMajorList").hide("fast");
				$("#thePrivateList").hide("fast");
				$("#theLieutenantList").hide("fast");
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
