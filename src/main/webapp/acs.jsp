<%@page import="com.onelogin.saml2.Auth"%>
<%@page import="com.onelogin.saml2.servlet.ServletUtils"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="dbProcs.Getter"%>
<%@ page import="org.owasp.encoder.Encode, utils.ShepherdLogManager"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	language="java" import="utils.*" errorPage=""%>
<%@ include file="translation.jsp"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>OWASP Security Shepherd</title>
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
		</div>

		<div id="page">

			<%
				Auth auth = new Auth(request, response);
				auth.processResponse();

				if (!auth.isAuthenticated()) {
					out.println("<div class=\"alert alert-danger\" role=\"alert\">Not authenticated</div>");
				}

				List<String> errors = auth.getErrors();

				if (errors.isEmpty()) {

					Map<String, List<String>> attributes = auth.getAttributes();
					String nameId = auth.getNameId();
					String nameIdFormat = auth.getNameIdFormat();
					String sessionIndex = auth.getSessionIndex();
					String nameidNameQualifier = auth.getNameIdNameQualifier();
					String nameidSPNameQualifier = auth.getNameIdSPNameQualifier();

					session.setAttribute("attributes", attributes);
					session.setAttribute("nameId", nameId);
					session.setAttribute("nameIdFormat", nameIdFormat);
					session.setAttribute("sessionIndex", sessionIndex);
					session.setAttribute("nameidNameQualifier", nameidNameQualifier);
					session.setAttribute("nameidSPNameQualifier", nameidSPNameQualifier);

					String relayState = request.getParameter("RelayState");

					if (relayState != null && !relayState.isEmpty()
							&& !relayState.equals(ServletUtils.getSelfRoutedURLNoQuery(request))
							&& !relayState.contains("/dologin.jsp")) { // We don't want to be redirected to login.jsp neither
						response.sendRedirect(request.getParameter("RelayState"));
					} else {

						if (attributes.isEmpty()) {
			%>
			<div class="alert alert-danger" role="alert">You don't have any
				attributes</div>
			<%
				} else {

							// Get id and name from SAML data
							String userID = attributes.get("urn:oid:0.9.2342.19200300.100.1.3").get(0);
							String userName = attributes.get("urn:oid:2.16.840.1.113730.3.1.241").get(0);

							Getter.authUserSSO("", null, userName, userID, "player");

						}
			%>
			<a href="attrs.jsp" class="btn btn-primary">See user data stored
				at session</a> <a href="dologout.jsp" class="btn btn-primary">Logout</a>
			<%
				}
				}
			%>
		</div>
	</div>
</body>
</html>














