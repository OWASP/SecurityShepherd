package servlets.admin.builder;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import utils.Vulnerabilities;
import utils.Vulnerabilities.Vulnerability;

/**
 * Responsable for generating control classes for the challenge builder function.
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
public class OperationStructure
{
	private static org.apache.log4j.Logger log = Logger.getLogger(ChallengeStructure.class);
	private static Encoder encoder = ESAPI.encoder();
	/**
	 * This method is called for generating the Custom JSP servlets for custom challenges
	 * @param challengeName The custom challenge name
	 * @param tableName The challenge's schemas's table name
	 * @param attrib Array of attributes in schema table
	 * @param attribAmount Amount of attributes in schema table
	 * @param lookUpAttrib The attribute to use for lookup functions
	 * @param vulnerability The vulnerability type to implement in generating the control class
	 * @param propertiesFile The name of the properties file to use for connecting to vulnerable database schema
	 * @param customFilterSet Boolean value depicting if a custom filter has been set
	 * @param filterFrom String to be filtered
	 * @param filterTo String to replace filtered text with
	 * @return The JSP content of the custom control structure.
	 * @throws Exception
	 */
	public static String createOperationStructure(String challengeName, String tableName, String[] attrib, 
			int attribAmount, int lookUpAttrib, int vulnerability, String propertiesFile, 
			boolean customFilterSet, String filterFrom, String filterTo)
	throws Exception
	{
		String thePage = new String();
		Vulnerabilities vulnerabilities = new Vulnerabilities();
		Vulnerability theVulnerability = null;
		//Select vulnerbility to use for generation
		switch(vulnerability)
		{
		case 1:
			log.debug("Generating SQL Injection Challenge");
			theVulnerability = vulnerabilities.sqlInjection;
			break;
		case 2:
			log.debug("Generating Blind SQL Injection Challenge");
			theVulnerability = vulnerabilities.blindSqlInjection;
			break;
		/* Cross Site Scriping Not finished, as the Framework does no currently support database INSERT's
		case 3:
			log.debug("Generating Cross Site Scripting Challenge");
			theVulnerability = vulnerabilities.crossSiteScripting;
			break;
		*/
		case 4: 
			log.debug("Generating Reflected Cross Site Scripting Challenge");
			theVulnerability = vulnerabilities.reflectedCrossSiteScripting;
			break;
		default:
			log.debug("Invalid vulneraiblity type chosen: " + vulnerability);
			throw new Exception("Invalid Vulnerbaility Exception: " + vulnerability + " is not a valid vulnerability type.");
		}
		//Based on the vulnerbailities boolean values, compile the servlet jsp page
		thePage = operateInitiation;
		if(customFilterSet)
		{
			log.debug("Adding Custom Filter: '" + filterFrom + "' to '" + filterTo + "'");
			String theFilter = "String filterFrom = new String(\"" + encoder.encodeForDN(filterFrom).replaceAll("\\\\;", ";") + "\");\n" +
					"String filterTo = new String(\"" + encoder.encodeForHTML(filterTo) + "\");\n";
			theFilter += "while(userInput.contains(filterFrom))\n" +
			"{\n" +
				"userInput = userInput.replaceAll(filterFrom, filterTo);\n" +
			"}\n" +
			"//END theFilter\n";
			thePage += theFilter;
		}
		else
		{
			log.debug("No filter detected");
		}
		log.debug("Creating Database Call");
		if(theVulnerability.secureDatabaseCall)
		{
			thePage += generateSecureCall(attrib, attribAmount, lookUpAttrib, tableName, propertiesFile);
		}
		else
		{
			thePage += generateInsecureCall(attrib, attribAmount, lookUpAttrib, tableName, propertiesFile);
		}
		log.debug("Creating Display Preperation");
		if(theVulnerability.secureDisplay)
		{
			thePage += generatePrepareDisplay(attrib, attribAmount);
		}
		else
		{
			thePage += generatePrepareDisplayWithXss(attrib, attribAmount);
		}
		log.debug("Creating Display");
		if(theVulnerability.secureDisplay)
		{
			thePage += generateDisplay(attribAmount);
		}
		else
		{
			thePage += generateInsecureDisplay(attrib, attribAmount);
		}
		log.debug("Creating Error Catch");
		if(theVulnerability.outputError)
		{
			if(theVulnerability.outputErrorSecure)
			{
				thePage += generateCatchErrorWithOutput(challengeName);
			}
			else
			{
				thePage += generateCatchErrorWithOutputInsecure(challengeName);
			}
		}
		else
		{
			thePage += generateCatchError(challengeName);
		}
		
		return operateBegin + thePage + operateEnd;
	}
	
	/**
	 * Method used to generate a secure call function in the new control section of the custom challenge
	 * @param attrib
	 * @param attribAmount
	 * @param lookUpAttrib
	 * @param tableName
	 * @param propertiesFile
	 * @return
	 */
	private static String generateSecureCall (String[] attrib, int attribAmount, int lookUpAttrib, String tableName, String propertiesFile)
	{
		String composedCall = new String();
		String toAdd = new String();
		composedCall = operateSecureCall + propertiesFile + operateSecureCallPart1;
		for(int i = 0; i < attribAmount; i++)
		{
			toAdd += attrib[i];
			if(i < attribAmount - 1)
				toAdd += ", ";
		}
		composedCall += toAdd + operateSecureCallPart2 + "tb_" + tableName + operateSecureCallPart3 + attrib[lookUpAttrib] + operateSecureCallPart4;
		
		log.debug("Returning: " + composedCall);
		return composedCall;
	}
	
	/**
	 * Method used to generate a SQL Injection vulnerable database call
	 * @param attrib
	 * @param attribAmount
	 * @param lookUpAttrib
	 * @param tableName
	 * @param propertiesFile
	 * @return
	 */
	private static String generateInsecureCall (String[] attrib, int attribAmount, int lookUpAttrib, String tableName, String propertiesFile)
	{
		String composedCall = new String();
		String toAdd = new String();
		composedCall = operateInsecureCall + propertiesFile + operateInsecureCallPart1;
		for(int i = 0; i < attribAmount; i++)
		{
			toAdd += attrib[i];
			if(i < attribAmount - 1)
				toAdd += ", ";
		}
		composedCall += toAdd + operateInsecureCallPart2 + "tb_" + tableName + operateInsecureCallPart3 + attrib[lookUpAttrib] + operateInsecureCallPart4;
		
		log.debug("Returning: " + composedCall);
		return composedCall;
	}
	
	/**
	 * Method to prepare the Prepare display section of the generated control class for the custom challenge
	 * @param attrib
	 * @param attribAmount
	 * @return
	 */
	private static String generatePrepareDisplay(String[] attrib, int attribAmount)
	{
		String toAdd = new String();
		for(int i = 0; i < attribAmount; i++)
		{
			toAdd += "<th>" + encoder.encodeForHTML(attrib[i]) + "</th>";
		}
		return operateGetResults + "htmlOutput += \"" + toAdd + "\";\n";
	}
	
	/**
	 * Method to prepare the Prepare display section of the generated control class for the custom challenge that is vulnerable to XSS
	 * @param attrib
	 * @param attribAmount
	 * @return
	 */
	private static String generatePrepareDisplayWithXss(String[] attrib, int attribAmount)
	{
		String toAdd = new String();
		for(int i = 0; i < attribAmount; i++)
		{
			toAdd += "<th>" + encoder.encodeForHTML(attrib[i]) + "</th>";
		}
		return operateGetResultsWithXss + "htmlOutput += \"" + toAdd + "\";\n";
	}
	
	/**
	 * Method used to compile the actual display of data in the new control class
	 * @param attribAmount
	 * @return
	 */
	private static String generateDisplay(int attribAmount)
	{
		String theDisplay = new String();
		String toAdd = new String();
		for(int i = 0; i < attribAmount; i++)
		{
			toAdd += "htmlOutput += \"<td>\" + encoder.encodeForHTML(resultSet.getString(" + (i + 1) + ")) + \"</td>\";\n";
		}
		theDisplay = operatePrepareDisplayResults + toAdd + operatePrepareDisplayResulsEnd;
		return theDisplay;
	}
	 /**
	  * Method used to compile the actual display of data in the new control class that is vulnerable to XSS
	  * @param attrib
	  * @param attribAmount
	  * @return
	  */
	private static String generateInsecureDisplay (String[] attrib, int attribAmount)
	{
		String theDisplay = new String();
		String toAdd = new String();
		for(int i = 0; i < attribAmount; i++)
		{
			toAdd += "htmlOutput += \"<td>\" + resultSet.getString(" + (i + 1) + ") + \"</td>\";\n";
		}
		theDisplay = operatePrepareDisplayResults + toAdd + operatePrepareDisplayResulsEnd;
		return theDisplay;
	}
	
	/**
	 * Generates the Exception handling section of the control class. 
	 * @param challengeName
	 * @return
	 */
	private static String generateCatchError(String challengeName)
	{
		return catchError + challengeName + catchErrorPart2;
	}
	
	/**
	 * Generates Exception handling that is output to the user for the generated control class
	 * @param challengeName
	 * @return
	 */
	private static String generateCatchErrorWithOutput(String challengeName)
	{
		return catchError + challengeName + catchErrorPart2WithOutput;
	}
	
	/**
	 * Generates Exception handling that is output to the user for the generated control class that is vulnerable to XSS
	 * @param challengeName
	 * @return
	 */
	private static String generateCatchErrorWithOutputInsecure(String challengeName)
	{
		return catchError + challengeName + catchErrorPart2WithOutputInsecure;
	}
	private static String operateBegin = "<%@ page contentType=\"text/html; charset=iso-8859-1\" language=\"java\" import=\"java.sql.*,java.io.*,java.net.*,org.owasp.esapi.ESAPI, org.owasp.esapi.Encoder, dbProcs.*, utils.*\" errorPage=\"\" %>\n<%\n";
	private static String operateEnd = "%>";
	private static String operateInitiation = "PrintWriter output = response.getWriter();\n" +
		"output.print(getServletInfo());\n" +
		"String htmlOutput = new String();\n" +
		"Encoder encoder = ESAPI.encoder();\n" +
		"String userInput = request.getParameter(\"customParam\");\n" +
		"String ApplicationRoot = getServletContext().getRealPath(\"\");\n" +
		"//End operateInitiation\n";
	private static String operateSecureCall = "try\n" +
		"{\n" +
			"Connection conn = Database.getChallengeConnection(ApplicationRoot, \"";
	private static String operateSecureCallPart1 = "\");\n" +
			"//IF NOT SQL Injection\n" +
				"PreparedStatement prepStat = conn.prepareStatement(\"SELECT";
				//Lookup Attribs
	private static String operateSecureCallPart2 = " FROM ";
				//Table Name
	private static String operateSecureCallPart3 = " WHERE ";
				//Lookup Attrib
	private static String operateSecureCallPart4 = " = ?\");\n" +
				"prepStat.setString(1, userInput);\n" +
				"//End operateSecureCall\n";
	
	private static String operateInsecureCall = "try\n" +
		"{\n" +
			"Connection conn = Database.getChallengeConnection(ApplicationRoot, \"";
	private static String operateInsecureCallPart1 = "\");\n" +
			"//IF SQL Injection\n" +
			"PreparedStatement prepStat = conn.prepareStatement(\"SELECT ";
			//Attrib Names
	private static String operateInsecureCallPart2 = " FROM ";
			//Table Name
	private static String operateInsecureCallPart3 = " WHERE ";
			//Lookup Attrib
	private static String operateInsecureCallPart4 = " = '\" + userInput + \"'\");\n" +
			"//End operateInsecureCall\n";
	
	private static String operateGetResults = "ResultSet resultSet = prepStat.executeQuery();\n" +
			"htmlOutput = \"<h2 class='title'>Lookup Results</h2>\";\n" +
			"htmlOutput = \"<p>Search Results for'<a>\" + encoder.encodeForHTML(userInput) + \"</a>'</p><table><tr>\";\n" +
			"//End operateGetResults\n";
		// Plus <th>Encoded Attribs 1 - n </th>
	
	private static String operateGetResultsWithXss = "ResultSet resultSet = prepStat.executeQuery();\n" +
			"htmlOutput = \"<h2 class='title'>Lookup Results</h2>\";\n" +
			"htmlOutput = \"<p>Search Results for'<a>\" + userInput + \"</a>'</p>\"" +
			"<table><tr>\";\n" +
			"//End operateGetResults\n";
		// Plus <th>Encoded Attribs 1 - n </th>
		
	private static String operatePrepareDisplayResults = "htmlOutput += \"</tr>\";\n" +
			"while(resultSet.next())\n" +
			"{\n" +
			"htmlOutput += \"<tr>\";\n";
		//Plus <tr>resultSet.getString(i)</tr> for 1 - n where i = 1 - n
	private static String operatePrepareDisplayResulsEnd = "\n" +
			"htmlOutput += \"</tr>\";\n" +
			"}\n" +
			"htmlOutput += \"</table>\";\n" +
			"output.write(htmlOutput);\n" +
			"}\n" +
			"//End operatePrepareDisplay\n";
			
	private static String catchError = "catch(SQLException e)\n" +
		"{\n" +
			"System.out.println(\"";
			//Challenge Name
	private static String catchErrorPart2 = "Error: \" + e.toString());\n" + 
		"}\n" +
		"//End Catch Error\n";
		
	private static String catchErrorPart2WithOutput = "Error: \" + e.toString());\n" + 
			"output.write(encoder.encodeForHTML(\"Error: \" + e.toString()));\n" +
		"}\n" +
		"//End CatchError with Output\n";
		
	private static String catchErrorPart2WithOutputInsecure = "Error: \" + e.toString());\n" + 
			"output.write(\"Error: \" + e.toString());\n" +
		"}\n" +
		"//End CatchError with Insecure Output\n";
}
