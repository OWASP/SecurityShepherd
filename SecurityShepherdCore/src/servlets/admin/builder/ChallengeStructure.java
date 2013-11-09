package servlets.admin.builder;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.owasp.esapi.codecs.MySQLCodec;

/**
 * This class is responsible for producing custom challenge View structures
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
 * @author Mark Denihan
 *
 */
public class ChallengeStructure 
{
	private static org.apache.log4j.Logger log = Logger.getLogger(ChallengeStructure.class);
	private static Encoder encoder = ESAPI.encoder();
	/**
	 * This method prepares the user specified options for how the challenge will look. 
	 * @param challengeName Name of the challenge
	 * @param tableName Name of the vulnerable schemas table
	 * @param challengeIntro Introduction to custom challenge
	 * @param formType The type of form to generate for this View
	 * @param attrib Array of attribute names
	 * @param lookUpAttrib The attribute to use for lookup functions
	 * @param moduleHash The module hash for accessing the modules control class.
	 * @param propFileName The filename of the challenge schemas property file.
	 * @return The View's JSP content
	 */
	@SuppressWarnings("deprecation")
	public static String createChallengeStructure(String challengeName, String tableName, String challengeIntro, int formType, String[] attrib, int lookUpAttrib, String moduleHash, String propFileName)
	{
		String thePage = new String();
		//HTML Escape the user specified areas, to avoid unwanted cross site scripting
		challengeName = encoder.encodeForHTML(challengeName);
		challengeIntro = encoder.encodeForHTML(challengeIntro);
		String formContent = new String();
		
		log.debug("Preparing Form Content");
		switch(formType)
		{
		case 1:
			log.debug("Input Box to Lookup database content");
			formContent = "<table>\n" +
						"<tr><td>\n" +
							"Lookup:\n" +
						"</td><td>\n" +
							"<input type='text' style='width: 300px' id='customParam'/>\n" +
						"</td><td>\n" +
							"<input id='submitButton' type='submit' value='Lookup'/>\n" +
							"<div id='loadingSign' style='display: none;'><p>Loading...</p></div>\n" +
						"</td></tr>\n" +
					"</table>\n";
			break;
		case 2:
			//generating jsp content that will generate 
			log.debug("Combo Box Lookup");
			formContent = "<table>\n" +
					"<tr><td>\n" +
					"<select id='customParam'>\n" +
						"<%\n" +
						"try\n" +
						"{\n" +
							"Encoder encoder = ESAPI.encoder();\n" +
							"Connection connect = Database.getChallengeConnection(getServletContext().getRealPath(\"\"), \"" + propFileName + "\");\n" +
							"PreparedStatement prepStat = connect.prepareStatement(\"SELECT id, " +
								encoder.encodeForSQL(new MySQLCodec(MySQLCodec.MYSQL_MODE), attrib[lookUpAttrib]).replaceAll("\\\\_", "_") + " FROM tb_" +
								encoder.encodeForSQL(new MySQLCodec(MySQLCodec.MYSQL_MODE), tableName) +
								"\");\n" +
							"ResultSet rs = prepStat.executeQuery();\n" +
							"while(rs.next())\n" +
							"{\n" +
								"String id = rs.getString(1);\n" +
								"String display = rs.getString(2);\n" +
								"%>\n" +
								"<option value=\"<%= encoder.encodeForHTMLAttribute(id) %>\"><%= encoder.encodeForHTML(display) %></option>\n" +
							"<%}\n" +
							"connect.close();\n" +
						"}\n" +
						"catch(SQLException e1)\n" +
						"{\n" +
						"System.out.println(\"Error:\" + e1.toString());\n" +
						"}\n" +
						"%>\n" +
					"</select>\n" +
					"</td><td>\n" +
					"<input id='submitButton' type='submit' value='Lookup'/>\n" +
					"<div id='loadingSign' style='display: none;'><p>Loading...</p></div>\n" +
					"</td></tr>\n";
			break;
		default:
			log.error("Invalid Form Type Choice at this time");
		}
		String theForm = challengeFormStart + formContent + challengeFormEnd;
		
		log.debug("Prepairing Script");
		String theScript = challengeScriptStart + challengeScriptBeforeAjax + 
			"$.ajax({type: 'POST', url: '" + moduleHash + "operate.jsp', data: {customParam: theParam}, async: false});\n" +
			challengeScriptAfterAjax;
		
		thePage = challengeHeaderStart + "Custom Challenge '" + challengeName + "' Acessed" + challengeHeaderMiddle + challengeName + challengeHeaderEnd + challengeBodyStart + challengeName + challengeBodyMiddle + 
			challengeIntro + theForm + theScript + challengeBodyEnd;
		return thePage;
	}
	//HTML Static Content Variables
	
	private static String challengeHeaderStart = "<%@ page contentType=\"text/html; charset=iso-8859-1\" language=\"java\" import=\"java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*\" errorPage=\"\" %>\n" +
			"<% System.out.println(\"";
	private static String challengeHeaderMiddle = "\"); %>\n" +
			"<html xmlns='http://www.w3.org/1999/xhtml'>\n" +
			"<head>\n" +
			"	<meta http-equiv='content-type' content='text/html; charset=utf-8' />\n" +
			"	<title>Security Shepherd - ";
	private static String challengeHeaderEnd = "</title>\n" +
			"	<link href='../css/theCss.css' rel='stylesheet' type='text/css' media='screen' />\n" +
			"</head>";
	private static String challengeBodyStart = "<body>\n" +
			"	<script type='text/javascript' src='../js/jquery.js'></script>\n" +
			"	<div id='contentDiv'>\n" +
			"	<h2 class='title'>\n";
	private static String challengeBodyMiddle = "</h2><p>\n";
	private static String challengeFormStart = "<br/>\n" +
			"	<form id='leForm' action='javascript:;'>\n";
	private static String challengeFormEnd = "</form>\n" +
			"	<div id='resultsDiv'></div></p></div>\n";
	private static String challengeScriptStart = "<script>\n" +
			"$('#leForm').submit(function(){\n" +
			"	var theParam = $('#customParam').val();\n" +
			"	$('#submitButton').hide('fast');\n" +
			"	$('#loadingSign').show('slow');\n";
	private static String challengeScriptBeforeAjax = "\n" +
			"	$('#resultsDiv').hide('slow', function(){\n" +
			"		var ajaxCall = "; //following by jquery ajax
	private static String challengeScriptAfterAjax = "\n" +
			"		if(ajaxCall.status == 200)\n" +
			"		{\n" +
			"			$('#resultsDiv').html(ajaxCall.responseText);\n" +
			"		}\n" +
			"		else\n" +
			"		{\n" +
			"			$('#resultsDiv').html('<p> An Error Occured: ' + ajaxCall.status + ' ' + ajaxCall.statusText + '</p>');\n" +
			"		}\n" +
			"		$('#resultsDiv').show('slow', function(){\n" +
			"			$('#loadingSign').hide('fast', function(){\n" +
			"				$('#submitButton').show('slow');\n" +
			"			});\n" +
			"		});\n" +
			"	});\n" +
			"});</script>\n";
	private static String challengeBodyEnd = "</body>\n" +
			"</html>\n";
}
