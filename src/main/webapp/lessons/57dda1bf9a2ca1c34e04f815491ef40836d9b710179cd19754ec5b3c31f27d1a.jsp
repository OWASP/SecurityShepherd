<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java"
         import="utils.*, org.owasp.encoder.Encode" errorPage="" %>
<%@ page import="java.util.Locale, java.util.ResourceBundle" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="dbProcs.FileInputProperties" %>
<%
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
     * @author ismisepaul
     */

    final String LEVEL_NAME = "XXE Lesson";
    final String LEVEL_HASH = "57dda1bf9a2ca1c34e04f815491ef40836d9b710179cd19754ec5b3c31f27d1a";

    Locale locale = new Locale(Validate.validateLanguage(request.getSession()));
    //Logger log = Logger.getLogger(this.getClass());
    ResourceBundle bundle = ResourceBundle.getBundle("i18n.lessons.xxe." + LEVEL_HASH, locale);
    //Used more than once translations
    String i18nLevelName = bundle.getString("title.question.xxe");

    ResourceBundle generic = ResourceBundle.getBundle("i18n.text", locale);
    String owaspMoreInfo = 	generic.getString("module.generic.owasp.more.info");
    String owaspGuideTo = generic.getString("module.generic.owasp.guide.to");
    String owaspUrlAttack = FileInputProperties.readPropFileClassLoader("/uri.properties", "owasp.attack.xxe");

    ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), LEVEL_NAME + " Accessed");
    if (request.getSession() != null) {
        HttpSession ses = request.getSession();
        //Getting CSRF Token from client
        Cookie tokenCookie = null;
        try {
            tokenCookie = Validate.getToken(request.getCookies());
        } catch (Exception htmlE) {
            ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
                    LEVEL_NAME + ".jsp: tokenCookie Error:" + htmlE.toString());
        }
        // validateSession ensures a valid session, and valid role credentials
        // If tokenCookie == null, then the page is not going to continue loading
        if (Validate.validateSession(ses) && tokenCookie != null) {
            ShepherdLogManager.logEvent(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"),
                    LEVEL_NAME + " has been accessed by " + ses.getAttribute("userName").toString(),
                    ses.getAttribute("userName"));

            String csrfToken = Encode.forHtml(tokenCookie.getValue());

%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
    <title>Security Shepherd - <%=i18nLevelName%>
    </title>
    <link href="../css/lessonCss/theCss.css" rel="stylesheet" type="text/css" media="screen"/>
</head>
<body>
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/clipboard-js/clipboard.min.js"></script>
<script type="text/javascript" src="../js/clipboard-js/tooltips.js"></script>
<script type="text/javascript" src="../js/clipboard-js/clipboard-events.js"></script>
<div id="contentDiv">
    <h2 class="title"><%= i18nLevelName %>
    </h2>
    <p>
    <div id="lessonIntro">
        <%= bundle.getString("paragraph.info.1") %>
        <br/>
        <br/>
        <%= bundle.getString("paragraph.info.2")%>
        <br/>
        <br/>
        <%= bundle.getString("paragraph.info.3") %>
        <br/>
        <%= owaspMoreInfo %> <a href="<%= owaspUrlAttack %>" target="_blank"> <%= owaspGuideTo %> XXE </a>
        <br/>
        <br/>
        <%= bundle.getString("example.text") %> 1:
        <br>
        <code><%= bundle.getString("example.xxe.1") %></code>
        <br/><br/>
        <%= bundle.getString("example.text") %> 2:
        <br>
        <code><%= bundle.getString("example.xxe.2") %></code>
        <br/>
        <br/>
        <input type="button" value="<%= bundle.getString("button.hideIntro") %>" id="hideLesson"/>
    </div>
    <input type="button" value="<%= bundle.getString("button.showIntro") %>" id="showLesson" style="display: none;"/>
    <br/>
    <%= bundle.getString("paragraph.whattodo.xxe")
            + "<b>" + System.getProperty("user.dir") + "/"
            + FileInputProperties.readPropFileClassLoader("/fileSystemKeys.properties", "xxe.lesson.file") + "</b>" %>
    <br/>
    <br/>
    <form id="leForm" action="javascript:;" contentType="application/xml">
        <table>
            <tr><td>
                <%= bundle.getString("paragraph.info.emailAddr") %>
            </td></tr>
            <tr><td>
                <input style="width: 400px;" id="emailAddr" type="text" autocomplete="off"/>
            </td></tr>
            <tr><td>
                <div id="submitButton"><input type="submit" value="<%= bundle.getString("button.resetPassword") %>"/></div>
                <p style="display: none;" id="loadingSign"><%= bundle.getString("word.info.loading") %></p>
                <div style="display: none;" id="hintButton"><input type="button" value="<%= bundle.getString("sentence.question.wouldYouLikeHint") %>" id="theHintButton"/></div>
            </td></tr>
        </table>
    </form>

    <div id="resultsDiv"></div>
    </p>
</div>
<script>
    $("#leForm").submit(function () {
        $("#submitButton").hide("fast");
        $("#loadingSign").show("slow");
        var theEmailAddr = $("#emailAddr").val();
        $("#resultsDiv").hide("slow", function () {
            var ajaxCall = $.ajax({
                type: "POST",
                beforeSend: function(request) {
                    request.setRequestHeader("csrfToken", "<%= csrfToken %>");//don't forget the "" around the Java variable or JS will try convert
                },
                contentType: "application/xml",
                url: "<%= LEVEL_HASH %>",
                data: "<?xml version=\"1.0\"?><email>"+ theEmailAddr +"</email>",
                async: false
            });
            if (ajaxCall.status == 200) {
                $("#resultsDiv").html(ajaxCall.responseText);
            }
            else {
                $("#resultsDiv").html("<p> <%= bundle.getString("error.occurred") %>: "
                    + ajaxCall.status + " " + ajaxCall.statusText + "</p>");
            }
            $("#resultsDiv").show("slow", function () {
                $("#loadingSign").hide("fast", function () {
                    $("#submitButton").show("slow");
                });
            });
        });
    });

    $('#hideLesson').click(function () {
        $("#lessonIntro").hide("slow", function () {
            $("#showLesson").show("fast");
        });
    });

    $("#showLesson").click(function () {
        $('#showLesson').hide("fast", function () {
            $("#lessonIntro").show("slow");
        });
    });
</script>
<% if (Analytics.googleAnalyticsOn) { %><%= Analytics.googleAnalyticsScript %><% } %>
</body>
</html>
<%
        } else {
            response.sendRedirect("../loggedOutSheep.html");
        }
    } else {
        response.sendRedirect("../loggedOutSheep.html");
    }
%>
