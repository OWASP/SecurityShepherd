<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="utils.ExposedServer,utils.ShepherdLogManager" %>

<%
	response.sendRedirect(ExposedServer.getUrl());

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
 
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Setting Up - Security Shepherd</title>

<link href="css/theCss.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
<script type="text/javascript" src="js/jquery.js"></script>
<div id="wrapper">
<!-- start header -->
<div id="header">
	<h1>Security Shepherd</h1>
</div>
<!-- end header -->
<!-- start page -->
<div id="page">
	<!-- start content -->
	<div id="content">
		<div class="post">
			<h1 class="title">Setting You Up</h1>
			<p>Setting up your browser certs!</p>
		</div>
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
	
	$("#tools").click(function(){
		$("#toolsTable").show("slow");
	});
</script>
</body>
</html>
