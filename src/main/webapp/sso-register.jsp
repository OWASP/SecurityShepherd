<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*,org.owasp.encoder.Encode" errorPage="" %>
<%@ include file="translation.jsp" %>
<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: sso-register.jsp *************************");

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
if(OpenRegistration.isEnabled())
{
HttpSession ses = request.getSession();
if(request.getSession() != null)
{
	if(ses.getAttribute("errorMessage") == null) 
	{
		ses.invalidate();
		ses = request.getSession(true);
		String language = request.getParameter("lang");
		if(language != null){
			ses.setAttribute("lang", language);
		}
	}
	if(ses.getAttribute("userName") != null)
	{
		//Logging Username
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Accessed by: " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));
	}
}
String url = (request.getRequestURL()).toString();
if(url.contains("sso-register.jsp"))
{
	url = url.substring(0, url.lastIndexOf("/") + 1);
}
else
{
	response.sendRedirect("sso-register.jsp");
}
//The org.owasp.encoder.Encode class should be used to encode any softcoded data. This should be performed everywhere for safety

String csrfToken = Encode.forHtml(Hash.randomString());
ses.setAttribute("csrfToken", csrfToken);

String registrationSuccess = new String();
String errorMessage = new String();
String registerError = new String();
String userName = new String();
String userID = new String();
if(ses.getAttribute("errorMessage") != null)
{
	try
	{
		errorMessage = Encode.forHtml(ses.getAttribute("errorMessage").toString());
		userName = Encode.forHtmlAttribute(ses.getAttribute("userName").toString());
		userID = Encode.forHtmlAttribute(ses.getAttribute("userID").toString());
		ses.removeAttribute("userName");
		ses.removeAttribute("userID");
		ses.removeAttribute("errorMessage");
	}
	catch(Exception e)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "sso-register.jsp error");
	}
}
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><fmt:message key="generic.text.ssRegister" /></title>

<link href="css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
<script type="text/javascript" src="js/jquery.js"></script>
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
		<div class="post" id="registerDiv">
			<h1 class="title"><fmt:message key="generic.text.register" /></h1>
			<%
				if(!errorMessage.isEmpty()) {
			%>
				<p><strong><font color="red"><%=errorMessage%></font></strong></p>
			<%
				}
			%>
			<div id="badData"></div>
			<form id="leForm" action="javascript:;">
				<div align="center">
				<br/>
				<table>
					<tr><td><p>Display name:</p></td><td><input type="text" id="userName" value="<%=userName%>" minlength="3" maxlength="32" required/></td></tr>
					<tr><td><p>Username:</p></td><td><input type="text" id="userID" minlength="3" disabled="disabled" value="<%=request.getParameter("userID") %>" required/></td></tr>
					
				</table>
				
				<br/>
				<div style="width: 400px; border-color:#A878EF; border-style:dashed; background-color: #D4D4D4;padding-top:5px;padding-bottom:5px;padding-right:5px;padding-left:5px;" align="justify">
					<center><big style="color:#A878EF;">SHEPHERD DISCLAIMER</big></center>
					<br/>
					<br/>
					The Security Shepherd project is for educational purposes only. 
					Do not attempt to use these techniques without authorization. 
					If you are caught engaging in unauthorized hacking, 
					most companies will take legal action. 
					Claiming that you were doing security research 
					will not protect you. 
					<br/><br/>
					Security Shepherd is a safe playground 
					for you to improve your web application security skills
					and only encourages white hat or ethical hacking behaviour. 
					<br/>
				</div>
				<br/>
				<center>
					<input style="width:350px; height: 50px;" type="submit" name="submit" value="Sign me up!" />
				</center>
				
				</div>
			</form>
		</div>
		<div id="resultDiv"></div>
		<div id="loadingSign" style="display: none"><p><h2 class="title">Enrolling Now</h2><p>Please wait as you are enrolled for your Security Shepherd account.</p></p></div>
	</div>
	<!-- end content -->
	<!-- start sidebar -->
	<!-- end sidebar -->
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

	$("#content").center();
	
	$(window).resize(function() 
	{
		$("#content").center();
	});
	
	$("#leForm").submit(function(){
		$("#badData").hide("fast");
		//Get data
		var theName = $("#userName").val();
		var theID = $("#userID").val();
		//Validation
		var theError = "";
		
		//If Valid, Sent info to servlet, if not, spit out the expected error
		if(theError == "")
		{
			//alert('no error');
			$("#registerDiv").hide("fast");
			$("#loadingSign").show("slow");
			$("#badData").hide("slow", function(){
				//alert('Before Ajax');
				var ajaxCall = $.ajax({
					type: "POST",
					url: "sso-register",
					data: {
						userName: theName,
						userID: theID,
						csrfToken: "<%=csrfToken%>"
					},
					async: false
				});
				if(ajaxCall.status == 200)
				{
					window.location = "login.jsp";
				}
				else
				{
					$("#badData").html("<p> An Error Occurred: " + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
				}
				$("#badData").show("slow", function(){
					$("#loadingSign").hide("fast", function(){
						$("#submitButton").show("slow");
					});
				});
			});
		}
		else
		{
			$("#badData").html("<p><font color='red'><strong>" + theError + "</strong></font></p>");
			$("#loadingSign").hide("fast", function(){
				$("#badData").show("slow");
				$("#registerDiv").show("slow");
			});
		}
	});
</script>
<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
</body>
</html>
<%
	}
else
{
ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Sombody is trying to register (Registration is Closed)");
%>
Registration is not available currently
<%
}
%>
