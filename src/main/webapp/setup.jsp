<%@page import="dbProcs.Constants"%>
<%@page import="servlets.Setup"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java" import="utils.*, org.owasp.encoder.Encode" errorPage=""%>
<%@ include file="translation.jsp"%>

<%


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
 
HttpSession ses = request.getSession();
ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Sombody ("+ ses.getAttribute("lang") +") Conntected to login.jsp ...");
String error = "";

if (ses.getAttribute("dbConnectionFailed") != null) {
	error = ses.getAttribute("dbConnectionFailed").toString();
	ses.removeAttribute("dbConnectionFailed");
}

if (ses.getAttribute("dbAuthFailed") != null) {
	error = ses.getAttribute("dbAuthFailed").toString();
	ses.removeAttribute("dbAuthFailed");
}

if (ses.getAttribute("dbSetupFailed") != null) {
	error = ses.getAttribute("dbSetupFailed").toString();
	ses.removeAttribute("dbSetupFailed");
}

if (Setup.isInstalled()) {
	response.sendRedirect("login.jsp");
}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>OWASP Security Shepherd - Login</title>

<link href="css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
<link href="css/theResponsiveCss.css" rel="stylesheet" type="text/css" media="screen">
</head>
<body>
	<script type="text/javascript" src="js/jquery.js"></script>
	<script type="text/javascript" src="js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript" src="js/clipboard-js/clipboard-events.js"></script>
	<div id="wrapper">
		<jsp:include page="translation-select.jsp" />
		<!-- start header -->
		<div id="header">
			<h1>Security Shepherd</h1>
		</div>
		<!-- end header -->
		<!-- start page -->
		<div id="page">
			<!-- start content -->
			<div id="content">
				<div class="setupPage">
					<h1 class="title" id="login_title"><fmt:message key="generic.text.setup.title" /></h1>
					<p><fmt:message key="generic.text.setup.description" /></p>
					<h2 class="title" id="login_title"><fmt:message key="generic.text.setup.token.title" /></h2>
					<p><fmt:message key="generic.text.setup.token.description" /></p>
					<script>prepTooltips();prepClipboardEvents();</script>
					<div class='input-group' style="margin-bottom: 15px;">
						<textarea id='theKey' style="font-size: 12px; padding:0.1em; height: 30px; display: inline-block; float: left; padding-right: 1em; overflow: hidden; width:95%"><%=Constants.SETUP_AUTH%></textarea>
						<span class='input-group-button'>
							<button class='btn' type='button' data-clipboard-shepherd data-clipboard-target='#theKey' style='height: 30px;'>
								<img src='js/clipboard-js/clippy.svg' width='14' alt='<fmt:message key="generic.text.copy.to.clip"/>'>
							</button>
						</span>
					</div>
					<h2 class="title" id="login_title"><fmt:message key="generic.text.setup.form.title" /></h2>
					<form id="setupForm" action="javascript:;">
						<div class="row">
							<div class="col-25">
								<label for="dbhost"><fmt:message key="generic.text.setup.host" /></label>
							</div>
							<div class="col-75">
								<input type="text" id="dbhost" name="dbhost" placeholder="Database Hostname..." autofocus required>
							</div>
						</div>
						<div class="row">
							<div class="col-25">
								<label for="dbport"><fmt:message key="generic.text.setup.port" /></label>
							</div>
							<div class="col-75">
								<input type="text" id="dbport" name="dbport" placeholder="Database Port..." required>
							</div>
						</div>
						<div class="row">
							<div class="col-25">
								<label for="dbuser"><fmt:message key="generic.text.setup.user" /></label>
							</div>
							<div class="col-75">
								<input type="text" id="dbuser" name="dbuser" placeholder="Shepherd Database User.." required>
							</div>
						</div>
						<div class="row">
							<div class="col-25">
								<label for="dbpass"><fmt:message key="generic.text.setup.pwd" /></label>
							</div>
							<div class="col-75">
								<input type="password" id="dbpass" name="dbpass" placeholder="Database Password.." required>
							</div>
						</div>
						<div class="row">
							<div class="col-25">
								<label for="dboverride"><fmt:message key="generic.text.setup.overridedb" /></label> </div>
							<div class="col-75">
								<select id="dboverride" name="dboverride">
									<option value="true"><fmt:message key="generic.text.setup.wipe" /></option>
									<option value="false"><fmt:message key="generic.text.setup.dontwipe" /></option>
								</select>
							</div>
						</div>
						<div class="row">
							<div class="col-25">
								<label for="dbauth"><fmt:message key="generic.text.setup.authentication" /></label>
							</div>
							<div class="col-75">
								<input type="text" id="dbauth" name="dbauth" placeholder="Token from Server File System..." required>
							</div>
						</div>
						<div class="row">
							<input type="submit" id="submitButton" value="<fmt:message key="generic.text.submit" />">
						</div>
						<div align="center" id="submitLoading" style="display: none;"><fmt:message key="generic.text.loading" /></div>
					</form>
					<div id="resultResponse"></div>
				</div>
			</div>
			<!-- end content -->
		</div>
	</div>
	<!-- end page -->
	<script>
		jQuery.fn.center = function () 
		{
			this.css("position","absolute");
			this.css("left", (($(window).width() - this.outerWidth()) / 2) + $(window).scrollLeft() + "px");
			return this;
		}	
		
		$("#tools").click(function(){
			$("#toolsTable").show("slow");
		});
		
		$("#showAbout").click(function(){
			$("#aboutDiv").show("slow");
		});
		
		$("#setupForm").submit(function(){
			var thedbhost = $("#dbhost").val();
			var thedbport = $("#dbport").val();
			var thedbuser = $("#dbuser").val();
			var thedbpass = $("#dbpass").val();
			var thedboverride = $("#dboverride").val();
			var thedbauth = $("#dbauth").val();
			if(thedbauth != null)
			{
				$("#submitLoading").slideDown("fast");
				$("#resultResponse").slideUp("fast");
				//The Ajax Operation
				$("#submitButton").slideUp("fast", function(){
					var ajaxCall = $.ajax({
						type: "POST",
						url: "setup",
						data: {
							dbhost: thedbhost,
							dbport: thedbport,
							dbuser: thedbuser,
							dbpass: thedbpass,
							dboverride: thedboverride,
							dbauth: thedbauth
						},
						async: false
					});
					$("#submitLoading").slideUp("fast", function(){
						if(ajaxCall.status == 200)
						{
							console.log("Request OK. Showing Reponse");
							$('#resultResponse').html(ajaxCall.responseText);
						}
						else
						{
							$('#resultResponse').html("<br/><p> <fmt:message key="generic.text.sorryError" />: " + ajaxCall.status + " " + ajaxCall.statusText + "</p><br/>");
						}
						$("#resultResponse").show("slow");
						$("#submitButton").slideDown("slow");
					});
					if(ajaxCall.responseText.indexOf("<fmt:message key="generic.text.setup.response.success" />")!=-1)
					{
						window.location.replace("login.jsp");
					}
				});
			}
			else
			{
				console.log("No dbauth Submitted");
			}
		});
	</script>
	<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %>
	<% } %>
</body>
</html>
