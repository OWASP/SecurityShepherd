<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" import="utils.*" errorPage="" %>
<%@ page import="java.util.Locale, java.util.ResourceBundle"%>
<%
String levelName = "Failure To Restrict URL Access Lesson Target";
String levelResult = "f60d1337ac4d35cb67880a3adda79";
String levelHash = "oed23498d53ad1d965a589e257d8366d74eb52ef955e103c813b592dba0477e3";

//Translation Stuff
Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
ResourceBundle bundle = ResourceBundle.getBundle("i18n.lessons.failure_to_restrict_url_access." + levelHash, locale);

ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " Accessed");
if (request.getSession() != null)
{
	HttpSession ses = request.getSession();
	//Getting CSRF Token from client
	Cookie tokenCookie = null;
	try
	{
		tokenCookie = Validate.getToken(request.getCookies());
	}
	catch(Exception htmlE)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName +".jsp: tokenCookie Error:" + htmlE.toString());
	}
	// validateSession ensures a valid session, and valid role credentials
	// If tokenCookie == null, then the page is not going to continue loading
	if (Validate.validateSession(ses) && tokenCookie != null)
	{
		ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), levelName + " has been accessed by " + ses.getAttribute("userName").toString(), ses.getAttribute("userName"));

%>
<html><head></head><body>
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<br/>
<p><%= bundle.getString("challenge.resultKey") %>: <strong><%= Hash.generateUserSolutionKeyOnly(levelResult, (String)ses.getAttribute("userName")) %></strong></p>
</body></html>
<%
	}
	else
	{
		response.sendRedirect("../../loggedOutSheep.html");
	}
}
else
{
	response.sendRedirect("../../loggedOutSheep.html");
}
%>