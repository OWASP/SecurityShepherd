<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*" errorPage="" %>

<%
// Insecure Direct Object References

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
 * @author Mark Denihan
 */

System.out.println("Insecure Direct Object References Lesson Accessed");
%>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Failure to Restrict URL Access Lesson</title>
	<link href="../css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">What is a Failure to Restrict URL Access?</h2>
			<p> 
				<div id="lessonIntro">
					An application that <a>fails to restrict URL access</a> is an application that does is not protecting it's "protected" pages sufficiently. This occurs when an application hides functionality from basic users. In an application that fails to restrict URL access, administration links are only put onto the page if the user is an administrator. If users discover the page's address, they can still access it via URL access.
					<br/>
					<br/>
					Preventing unauthorized URL access requires selecting an approach for requiring proper authentication and proper authorization for each page. The easier the authentication is to include in a page the more likely that all pages will be covered by the policy.
					<br/>
					<br/>
					<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
				<br/>
				The result key to this lesson is stored in a <a>web page</a> only administrators know about.
				<div id="hiddenDiv" style="display: none;">
					<!-- This is only displayed for Administrators -->
					<a href="adminOnly/resultKey.jsp">
						Administrator Result Page
					</a>
				</div>
			</p>
		</div>
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
</body>
</html>
