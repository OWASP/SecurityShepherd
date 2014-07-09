<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.*" errorPage=""%>

<%
	//No Quotes In level Name
	String levelName = "What is Mobile Reverse Engineering";
	//Alphanumeric Only
	String levelHash = "19753b944b63232812b7af1a0e0adb59928158da5994a39f584cb799f25a95b9";
	//Level blurb can be written here in HTML OR go into the HTML body and write it there. Nobody will update this but you
	String levelBlurb = "";

	try {
		if (request.getSession() != null) {
			HttpSession ses = request.getSession();
			String userName = (String) ses
					.getAttribute("decyrptedUserName");
			ShepherdExposedLogManager.logEvent(request.getRemoteAddr(),
					request.getHeader("X-Forwarded-For"), levelName
							+ " has been accessed by " + userName);
		}
	} catch (Exception e) {
		ShepherdExposedLogManager.logEvent(request.getRemoteAddr(),
				request.getHeader("X-Forwarded-For"), levelName
						+ " has been accessed");
		ShepherdExposedLogManager.logEvent(request.getRemoteAddr(),
				request.getHeader("X-Forwarded-For"),
				"Could not recover username: " + e.toString());
	}

	/**
	 * <br/><br/>
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
	 * @author Sean Duggan
	 */
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>Security Shepherd - <%=levelName%></title>
<link href="../css/theCss.css" rel="stylesheet" type="text/css"
	media="screen" />

</script>
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
	<div id="contentDiv">
		<h2 class="title"><%=levelName%></h2>
		<p>
			<div id="lessonIntro">
				One of the biggest dangers to Mobile Applications is reverse
				engineering. This is easily done and can reveal source code, API
				keys, Encryption Keys, Hidden Admin Functionality or hard coded passwords. A developer may obfuscate their code in order to make it more difficult for an attacker to read. In some cases obfuscation is the only defence. 
				
				<br/>
				<a>ProGuard</a> is a tool which now comes with the Android SDK and can be used to optimize,
				shrink and <a>obfuscate code</a>. Obfuscation makes the code harder to
				read for anyone who reverse engineers the APK. Although ProGuard is
				an excellent tool, it will not secure your code. 
				
				ProGuard should generally be used all the time due to the other benefits it offers, but it will only slow down an attacker from gathering information and cannot prevent attackers from finding sensitive information.
				
				<br/>
				Other obfuscation techniques involve not only making the code harder to read but also making the code more confusing. Instead of a hard coded string comparison, an App could use a function to check to validity of a password.<br/>
				<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
			</div>
			<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
			<br/>
			<br/> 
			There are two tools you will need to reverse engineer an APK. <a>Dex2Jar</a> and <a>JD-GUI</a>. Once you have
			these tools and the target APK, use dex2jar to convert the APK to a	jar file then open the .jar file with JD-GUI. Carry this out against the app for this lesson and investigate the source code to retrieve the result key. <br />
				
				<br/>
				<%= Analytics.getMobileLevelBlurb("ReverseEngineer.apk") %>
				<script>
				
				$('#hideLesson').click(function(){
				$("#lessonIntro").hide("slow", function(){
					$("#showLesson").show("fast");
				});
			});
			
			$("#showLesson").click(function(){
				$('#showLesson').hide("fast", function(){
					$("#lessonIntro").show("slow");
				});
			});
		</script>
		<% if(Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
		</p>
	</div>
</body>
</html>
