<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*,org.owasp.encoder.Encode" errorPage="" %>
<%@ include file="translation.jsp" %>
<%
	ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "DEBUG: register.jsp *************************");

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
if(url.contains("register.jsp"))
{
	url = url.substring(0, url.lastIndexOf("/") + 1);
}
else
{
	response.sendRedirect("register.jsp");
}
//The org.owasp.encoder.Encode class should be used to encode any softcoded data. This should be performed everywhere for safety

String csrfToken = Encode.forHtml(Hash.randomString());
ses.setAttribute("csrfToken", csrfToken);

String registrationSuccess = new String();
String errorMessage = new String();
String registerError = new String();
String userName = new String();
String userAddress = new String();
if(ses.getAttribute("errorMessage") != null)
{
	try
	{
		errorMessage = Encode.forHtml(ses.getAttribute("errorMessage").toString());
		userName = Encode.forHtmlAttribute(ses.getAttribute("userName").toString());
		userAddress = Encode.forHtmlAttribute(ses.getAttribute("userAddress").toString());
		ses.removeAttribute("userName");
		ses.removeAttribute("userAddress");
		ses.removeAttribute("errorMessage");
	}
	catch(Exception e)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), "Register.jsp error");
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
					<tr><td><p><fmt:message key="generic.text.username" /><font color="red"><small>* </small></font>:</p></td><td><input type="text" id="userName" value="<%=userName%>"/></td></tr>
					<tr><td><p><fmt:message key="generic.text.password" /><font color="red"><small>* </small></font>:</p></td><td><input type="password" id="passWord" autocomplete="OFF" /></td></tr>
					<tr><td><p><fmt:message key="generic.text.confirmPasswd" /><font color="red"><small>* </small></font>:</p></td><td><input type="password" id="passWordConfirm" autocomplete="OFF" /></td></tr>
					<tr><td><p><fmt:message key="generic.text.emailAddr" />:</p></td><td><input type="text" id="userAddress" value="<%=userAddress%>"/></td></tr>
					<tr><td><p><fmt:message key="generic.text.confirmEmailAddr" />:</p></td><td><input type="text" id="userAddressCnf" /></td></tr>
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
		var thePass = $("#passWord").val();
		var thePassAgain = $("#passWordConfirm").val();
		var theEmail = $("#userAddress").val();
		var theEmailAgain = $("#userAddressCnf").val();
		//Validation
		var theError = "";
		if (theName.length == 0 || thePass.length == 0 || thePassAgain.length == 0)
		{
			theError = "Please fill out all required fields marked with a '*'";
		}
		else if (theName.length < 5)
		{
			theError = "Your name must be longer than 5 characters";
		}
		else if (theName.length > 32)
		{
			theError = "Your name must be no longer than 32 characters";
		}
		else if (thePass != thePassAgain)
		{
			theError = "Passwords do not match";
		}
		else if (theEmail.length != 0 && theEmail != theEmailAgain)
		{
			theError = "Email addresses did not match";
		}
		else if (thePass.length < 8)
		{
			theError = "Your password must be at least 8 characters long";
		}
		else if (thePass.length > 512)
		{
			theError = "Your password must be no longer than 512 characters long";
		}
		else if (theEmail.length > 128)
		{
			theError = "Your email address must be no longer than 128 characters long";
		}
		
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
					url: "register",
					data: {
						userName: theName,
						passWord: thePass, 
						passWordConfirm: thePassAgain,
						userAddress: theEmail,
						userAddressCnf: theEmailAgain,
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
