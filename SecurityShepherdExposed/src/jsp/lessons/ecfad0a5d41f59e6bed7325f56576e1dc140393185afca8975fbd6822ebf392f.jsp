<%
// Mobile Insecure Data Storage Lesson

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

System.out.println("Mobile Insecure Data Storage Lesson");
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="content-type" content="text/html; charset=utf-8" />
	<title>Security Shepherd - Mobile Insecure Data Storage Lesson</title>
	<link href="../css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
	<script type="text/javascript" src="../js/jquery.js"></script>
		<div id="contentDiv">
			<h2 class="title">What is Mobile Insecure Data Storage Lesson?</h2>
			<p> 
				<div id="lessonIntro">
					Stuff
					<br />
					<input type="button" value="Hide Lesson Introduction" id="hideLesson"/>
				</div>
				<input type="button" value="Show Lesson Introduction" id="showLesson"  style="display: none;"/>
				<br/>
				
				Start your Security Shepherd Android Virtual Device and open the <a>InsecureData</a> app.
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