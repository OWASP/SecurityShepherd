<%@page import="dbProcs.Constants"%>
<%@page import="servlets.Setup"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java"
	import="utils.*, java.util.Properties, org.owasp.encoder.Encode"%>
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
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
			"Sombody (" + ses.getAttribute("lang") + ") Conntected to login.jsp ...");
	String error = "";
	boolean hasDBFile = false;
	Properties mysql_props;

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
	} else {
		mysql_props = Setup.getDBProps();

		if (mysql_props != null) {
			hasDBFile = true;

			String connectionURL = mysql_props.getProperty("databaseConnectionURL");
			if (connectionURL == null) {
				hasDBFile = false;
			}
			String databaseSchema = mysql_props.getProperty("databaseSchema");
			if (databaseSchema == null) {
				hasDBFile = false;

			}
			String dbOptions = mysql_props.getProperty("databaseOptions");
			if (dbOptions == null) {
				hasDBFile = false;

			}
			String driverType = mysql_props.getProperty("DriverType");
			if (driverType == null) {
				hasDBFile = false;

			}
			String username = mysql_props.getProperty("databaseUsername");
			if (username == null) {
				hasDBFile = false;

			}
			String password = mysql_props.getProperty("databasePassword");
			if (password == null) {
				hasDBFile = false;

			}

		}

	}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>OWASP Security Shepherd - Login</title>

<link href="css/theCss.css" rel="stylesheet" type="text/css"
	media="screen" />
<link href="css/theResponsiveCss.css" rel="stylesheet" type="text/css"
	media="screen">
</head>
<body>
	<script type="text/javascript" src="js/jquery.js"></script>
	<script type="text/javascript" src="js/clipboard-js/clipboard.min.js"></script>
	<script type="text/javascript" src="js/clipboard-js/tooltips.js"></script>
	<script type="text/javascript"
		src="js/clipboard-js/clipboard-events.js"></script>
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
					<h1 class="title" id="login_title">
						<fmt:message key="generic.text.setup.title" />
					</h1>
					<p>
						<fmt:message key="generic.text.setup.description" />
					</p>
					<h2 class="title" id="login_title">
						<fmt:message key="generic.text.setup.token.title" />
					</h2>
					<p>
						<fmt:message key="generic.text.setup.token.description" />
					</p>
					<script>
						prepTooltips();
						prepClipboardEvents();
					</script>
					<div class='input-group' style="margin-bottom: 15px;">
						<textarea id='theKey'
							style="font-size: 12px; padding: 0.1em; height: 30px; display: inline-block; float: left; padding-right: 1em; overflow: hidden; width: 95%"><%=Constants.SETUP_AUTH%></textarea>
						<span class='input-group-button'>
							<button class='btn' type='button' data-clipboard-shepherd
								data-clipboard-target='#theKey' style='height: 30px;'>
								<img src='js/clipboard-js/clippy.svg' width='14'
									alt='<fmt:message key="generic.text.copy.to.clip"/>'>
							</button>
						</span>
					</div>
					<h2 class="title" id="login_title">
						<fmt:message key="generic.text.setup.form.title" />
					</h2>
					<form id="setupForm" action="javascript:;">
						<div class="row">
							<div class="col-25">
								<label for="dbhost"><fmt:message
										key="generic.text.setup.host" /></label>
							</div>
							<div class="col-75">
								<%
									if (hasDBFile) {
								%>
								<input type="text" id="dbhost" name="dbhost"
									placeholder="(unchanged)">
								<%
									} else {
								%>
								<input type="text" id="dbhost" name="dbhost"
									placeholder="Database Hostname..." autofocus required>
								<%
									}
								%>
							</div>
						</div>
						<div class="row">
							<div class="col-25">
								<label for="dbport"><fmt:message
										key="generic.text.setup.port" /></label>
							</div>
							<div class="col-75">
								<%
									if (hasDBFile) {
								%>
								<input type="text" id="dbport" name="dbport"
									placeholder="(unchanged)">
								<%
									} else {
								%>
								<input type="text" id="dbport" name="dbport"
									placeholder="Database Port..." required>
								<%
									}
								%>
							</div>
						</div>
						<div class="row">
							<div class="col-25">
								<label for="dbuser"><fmt:message
										key="generic.text.setup.user" /></label>
							</div>
							<div class="col-75">

								<%
									if (hasDBFile) {
								%>
								<input type="text" id="dbuser" name="dbuser"
									placeholder="(unchanged)">
								<%
									} else {
								%>
								<input type="text" id="dbuser" name="dbuser"
									placeholder="Database Port..." required>
								<%
									}
								%>
							</div>
						</div>
						<div class="row">
							<div class="col-25">
								<label for="dbpass"><fmt:message
										key="generic.text.setup.pwd" /></label>
							</div>
							<div class="col-75">

								<%
									if (hasDBFile) {
								%>
								<input type="password" id="dbpass" name="dbpass"
									placeholder="(unchanged)">
								<%
									} else {
								%>
								<input type="password" id="dbpass" name="dbpass"
									placeholder="Database Port..." required>
								<%
									}
								%>
							</div>
						</div>
						<div class="row">
							<div class="col-25">
								<label for="dboverride"><fmt:message
										key="generic.text.setup.overridedb" /></label>
							</div>
							<div class="col-75">
								<select id="dboverride" name="dboverride">
									<option value="override"><fmt:message
											key="generic.text.setup.wipe" /></option>
									<option value="upgrade"><fmt:message
											key="generic.text.setup.upgrade" /></option>
									<option value="false"><fmt:message
											key="generic.text.setup.dontwipe" /></option>
								</select>
							</div>
						</div>
						<div class="row">
							<div class="col-25">
								<label for="dbauth"><fmt:message
										key="generic.text.setup.authentication" /></label>
							</div>
							<div class="col-75">
								<input type="text" id="dbauth" name="dbauth"
									placeholder="Token from Server File System..." required>
							</div>
						</div>
						<!-- MongoDb / NoSQL level -->
						<script>
							function uncheck() {
								$('#enableMongoChallenge')
										.removeAttr('checked');
							}

							$(document)
									.ready(
											function() {
												$('#enableMongoChallenge')
														.change(
																function() {
																	if (this.checked) {
																		$(
																				'#showHideDiv')
																				.fadeIn(
																						'slow');
																		$(
																				'#enableMongoChallenge')
																				.val(
																						"enable");
																		$(
																				"#mhost")
																				.prop(
																						'required',
																						true);
																		$(
																				"#mport")
																				.prop(
																						'required',
																						true);
																	} else {
																		$(
																				'#showHideDiv')
																				.fadeOut(
																						'slow');
																		$(
																				'#enableMongoChallenge')
																				.val(
																						"off");
																		$(
																				"#mhost")
																				.prop(
																						'required',
																						false);
																		$(
																				"#mport")
																				.prop(
																						'required',
																						false);
																	}
																});
											});
						</script>
						<div class="row">
							<div class="col-25">
								<span><fmt:message
										key="generic.text.setup.enable.mongodb" /></span>
							</div>
							<div class="col-75">
								<input type="checkbox" id="enableMongoChallenge"
									name="enableMongoChallenge" value="">
							</div>
						</div>
						<div id="showHideDiv" style="display: none">
							<div class="row">
								<div class="col-25">
									<label for="mhost">MongoDb <fmt:message
											key="generic.text.setup.host" /></label>
								</div>
								<div class="col-75">
									<input type="text" id="mhost" name="mhost"
										placeholder="Mongo Host...">
								</div>
							</div>
							<div class="row">
								<div class="col-25">
									<label for="mport">MongoDb <fmt:message
											key="generic.text.setup.port" /></label>
								</div>
								<div class="col-75">
									<input type="text" id="mport" name="mport"
										placeholder="Mongo Port...">
								</div>
							</div>
						</div>

						<script>
							function uncheckUnfafe() {
								$('#unsafeLevels').removeAttr('checked');
							}

							$(document).ready(function() {
								$('#unsafeLevels').change(function() {
									if (this.checked) {
										$('#showHideWarning').fadeIn('slow');
										$('#unsafeLevels').val("enable");
									} else {
										$('#showHideWarning').fadeOut('slow');
										$('#unsafeLevels').val("disable");
									}
								});
							});
						</script>

						<div class="row">
							<div class="col-25">
								<label for="mhost"><fmt:message
										key="generic.text.setup.enable.unsafe" /></label>
							</div>
							<div class="col-75">
								<input type="checkbox" id="unsafeLevels" name="unsafeLevels"
									value=""> <span id="showHideWarning"
									style="display: none"> <fmt:message
										key="generic.text.setup.enable.unsafe.warn" />
								</span>
							</div>
						</div>

						<div class="row">
							<input type="submit" id="submitButton"
								value="<fmt:message key="generic.text.submit" />">
						</div>
						<div align="center" id="submitLoading" style="display: none;">
							<fmt:message key="generic.text.loading" />
						</div>
					</form>
					<div id="resultResponse"></div>
				</div>
			</div>
			<!-- end content -->
		</div>
	</div>
	<!-- end page -->
	<script>
		jQuery.fn.center = function() {
			this.css("position", "absolute");
			this.css("left", (($(window).width() - this.outerWidth()) / 2)
					+ $(window).scrollLeft() + "px");
			return this;
		}

		$("#tools").click(function() {
			$("#toolsTable").show("slow");
		});

		$("#showAbout").click(function() {
			$("#aboutDiv").show("slow");
		});

		$("#setupForm")
				.submit(
						function() {
							var thedbhost = $("#dbhost").val();
							var thedbport = $("#dbport").val();
							var thedbuser = $("#dbuser").val();
							var thedbpass = $("#dbpass").val();
							var thedboverride = $("#dboverride").val();
							var thedbauth = $("#dbauth").val();
							var enableMongo = $("#enableMongoChallenge").val();
							var themhost = $("#mhost").val();
							var themport = $("#mport").val();
							var unsafeLevels = $("#unsafeLevels").val();
							if (thedbauth != null) {
								$("#submitLoading").slideDown("fast");
								$("#resultResponse").slideUp("fast");
								//The Ajax Operation
								$("#submitButton")
										.slideUp(
												"fast",
												function() {
													var ajaxCall = $
															.ajax({
																type : "POST",
																url : "setup",
																data : {
																	dbhost : thedbhost,
																	dbport : thedbport,
																	dbuser : thedbuser,
																	dbpass : thedbpass,
																	dboverride : thedboverride,
																	dbauth : thedbauth,
																	enableMongoChallenge : enableMongo,
																	mhost : themhost,
																	mport : themport,
																	unsafeLevels : unsafeLevels
																},
																async : false
															});
													$("#submitLoading")
															.slideUp(
																	"fast",
																	function() {
																		if (ajaxCall.status == 200) {
																			console
																					.log("Request OK. Showing Reponse");
																			$(
																					'#resultResponse')
																					.html(
																							ajaxCall.responseText);
																		} else {
																			$(
																					'#resultResponse')
																					.html(
																							"<br/><p> <fmt:message key="generic.text.sorryError" />: "
																									+ ajaxCall.status
																									+ " "
																									+ ajaxCall.statusText
																									+ "</p><br/>");
																		}
																		$(
																				"#resultResponse")
																				.show(
																						"slow");
																		$(
																				"#submitButton")
																				.slideDown(
																						"slow");
																	});
													if (ajaxCall.responseText
															.indexOf("<fmt:message key="generic.text.setup.response.success" />") != -1) {
														window.location
																.replace("login.jsp");
													}
												});
							} else {
								console.log("No dbauth Submitted");
							}
						});
	</script>
	<%
		if (Analytics.googleAnalyticsOn) {
	%><%=Analytics.googleAnalyticsScript%>
	<%
		}
	%>
</body>
</html>
