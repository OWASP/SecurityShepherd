package servlets.admin.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;

import dbProcs.Database;
import dbProcs.Getter;
import dbProcs.Setter;

import utils.ExposedServer;
import utils.Hash;
import utils.Validate;
/**
 * Control class for the Challenge builder function
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
public class GenerateChallengeDb extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(GenerateChallengeDb.class);
	
	/** Initiated by wizard.jsp. This method will create the vulnerable database schema, populate it and create a properties file for accessing it.
	 *  This method will also create the custom challenges View and Control classes. If the operation failes, no updates are made.
	 * @param challengeName  Maxlength(64), 
	 * @param tableName Maxlength(20),
	 * @param attribNameN (Where N = 1-5) Maxlength(20), There are 5 attributeName parameters.
	 * @param lookUpAttib The attribute to use for generating challenge lookup funcitons
	 * @param filterFrom Maxlength(20), used by generated control class for custom challenge filter
	 * @param filterTo Maxlength(20), used by generated control class for custom challenge filter
	 * @param RowAttributeValues For each attribute, there are up to 5 values, these are all sent to this method aswell
	 * @param cheatSheet New challenges customised cheat sheet data
	 * @param challengeIntro New challenges customised introduction
	 * @param formType Specifies the type of view to generate
	 * @param vulnerabilityType Specifies the type of control class to generate
	 * @param csrfToken 
	 */
	private static Encoder encoder = ESAPI.encoder();
	public void doPost (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
	{
		log.debug("*** servlets.admin.builder.GenerateChallengeDb ***");
		
		PrintWriter out = response.getWriter();  
		out.print(getServletInfo());
		HttpSession ses = request.getSession(true);
		if(Validate.validateAdminSession(ses))
		{
			Cookie tokenCookie = Validate.getToken(request.getCookies());
			Object tokenParmeter = request.getParameter("csrfToken");
			if(Validate.validateTokens(tokenCookie, tokenParmeter))
			{
				//Many Many Many Parameters
				log.debug("Getting Parameters");
				String challengeName = Validate.validateParameter(request.getParameter("challengeName"), 64);
				String tableName = Validate.validateParameter(request.getParameter("tableName"), 20);
				String attrib1 = Validate.validateParameter(request.getParameter("attribName1"), 20);
				String attrib2 = Validate.validateParameter(request.getParameter("attribName2"), 20);
				String attrib3 = Validate.validateParameter(request.getParameter("attribName3"), 20);
				String attrib4 = Validate.validateParameter(request.getParameter("attribName4"), 20);
				String attrib5 = Validate.validateParameter(request.getParameter("attribName5"), 20);
				String filterFrom = Validate.validateParameter(request.getParameter("filterFrom"), 20);
				String filterTo = encoder.encodeForHTML(Validate.validateParameter(request.getParameter("filterTo"), 20));
				log.debug("challengeName: " + challengeName);
				log.debug("tableName: " + tableName);
				log.debug("attrib1: " + attrib1);
				log.debug("attrib2: " + attrib2);
				log.debug("attrib3: " + attrib3);
				log.debug("attrib4: " + attrib4);
				log.debug("attrib5: " + attrib5);
				log.debug("filterFrom: " + filterFrom);
				log.debug("filterTo: " + filterTo);
				
				//Params for Population
				String[][] population = new String[5][5];
				for(int i = 0; i < 5; i++)
					for(int j = 0; j < 5; j++)
						population[i][j] = new String();
				String[] currentRow = new String[5];
				boolean fullRow;
				boolean validPopulation = false;
				
				for(int i = 0; i < 5; i++)
				{
					fullRow = true;
					//Getting all values for a row of attributes
					for(int j = 0; j < 5; j++)
					{
						String theParam = Validate.validateParameter(request.getParameter("r" + (i + 1) + "a" + (j + 1)), 64);
						log.debug("r" + (i + 1) + "a" + (j + 1) + " = " + theParam);
						currentRow[j] = theParam;
						fullRow = fullRow && !currentRow[j].isEmpty();
					}
					if(fullRow)
					{
						validPopulation = true;
						for(int j = 0; j < 5; j++)
							population[i][j] = currentRow[j]; //Added to population!
					}
					else
					{
						log.debug("Not a Full row, So breaking at index " + i); //Hence, no more rows to add
						break;
					}
				}
				
				//Validating Filter Params
				boolean customFilterSet = true;
				if(filterFrom.isEmpty())
				{
					log.debug("Nothing to Filter From, so no custom filter will be included in the challenge.");
					customFilterSet = false;
					//If there is nothing specified to filter, the there is nothing to filter to
				}
				
				//Cheat Sheet and Introduction can be null
				String cheatSheet = Validate.validateParameter(request.getParameter("cheatSheet"), 5112);
				String challengeIntro = Validate.validateParameter(request.getParameter("challengeIntro"), 5112);
				log.debug("cheatSheet: " + cheatSheet);
				log.debug("challengeIntro: " + challengeIntro);
				
				//FormType and Lookup Attrib must be integers
				int formType = 1;
				int lookUpAttrib = 1;
				int theVulnerability = 0;
				try
				{
					String vulnerabilities = Validate.validateParameter(request.getParameter("vulnerabilityType"), 2);
					String tempFormType = Validate.validateParameter(request.getParameter("formType"), 1);
					String tempLookUp = Validate.validateParameter(request.getParameter("lookUpAttrib"), 1);
					formType = Integer.parseInt(tempFormType);
					lookUpAttrib = Integer.parseInt(tempLookUp);
					theVulnerability = Integer.parseInt(vulnerabilities);
					if(formType > 2)
						formType = 1;
					if(lookUpAttrib > 4)
						lookUpAttrib = 1;
				}
				catch (Exception e)
				{
					log.error("Casting Error: " + e.toString());
				}
				log.debug("formType: " + formType);
				log.debug("lookUpAttrib: " + lookUpAttrib);
				
				//Ensuring not empties
				log.debug("Validating Parmeters");
				String errorMessage = new String();
				boolean challengeNameEmpty = challengeName.isEmpty();
				boolean tableNameEmpty = tableName.isEmpty();
				boolean allAttribEmpty = attrib1.isEmpty() && attrib2.isEmpty() && attrib3.isEmpty() && attrib4.isEmpty() && attrib5.isEmpty();
				if(!challengeNameEmpty && !tableNameEmpty && !allAttribEmpty && validPopulation)
				{
					//Valid Data, Prepairing Attibute array
					String[] attrib = new String[5];
					int i = 0;
					if(!attrib1.isEmpty())
					{
						attrib[i] = attrib1.trim().toLowerCase().replaceAll(" ", "");
						log.debug("Putting '" + attrib[i] + "' into array at place: " + i);
						i++;
					}
					if(!attrib2.isEmpty())
					{
						attrib[i] = attrib2.trim().toLowerCase().replaceAll(" ", "");
						log.debug("Putting '" + attrib[i] + "' into array at place: " + i);
						i++;
					}
					if(!attrib3.isEmpty())
					{
						attrib[i] = attrib3.trim().toLowerCase().replaceAll(" ", "");
						log.debug("Putting '" + attrib[i] + "' into array at place: " + i);
						i++;
					}
					if(!attrib4.isEmpty())
					{
						attrib[i] = attrib4.trim().toLowerCase().replaceAll(" ", "");
						log.debug("Putting '" + attrib[i] + "' into array at place: " + i);
						i++;
					}
					if(!attrib5.isEmpty())
					{
						attrib[i] = attrib5.trim().toLowerCase().replaceAll(" ", "");
						log.debug("Putting '" + attrib[i] + "' into array at place: " + i);
						i++;
					}
					for(int j = 0; j < 5 && attrib[j] != null; j++)
						log.debug("attrib[" + j + "] = " + attrib[j]);
					// "i" is the amount of valid attrib names in the attrib array
					log.debug((i) + " attribs");
					
					//Generate Schema on Vulnerable DB
					boolean created = true;
					String ApplicationRoot = getServletContext().getRealPath("");
					Connection conn = Database.getVulnerableDbConnection(ApplicationRoot);
					String schemaName = new String();
					String userName = Hash.smallRandomString();
					String userPass = Hash.smallRandomString();
					try
					{
						//Create Schema and a user to access it
						schemaName = Setter.createVulnerableSchema(conn, challengeName, tableName, attrib, i);
						for(int j = 0; j < 5; j++)
						{
							if(population[j][0].isEmpty())
							{
								log.debug("No more rows to add");
								break;
							}
							String[] theRow = {population[j][0], population[j][1], population[j][2], population[j][3], population[j][4]};
							log.debug("Creating Row: " + population[j][0] + ". Row #" + j);
							Setter.populateVulnerableSchema(conn, schemaName, tableName, theRow, attrib, i);
						}
						Setter.addRestrictedUserToVulnerableDb(conn, userName, userPass, schemaName, tableName);
					}
					catch(SQLException e)
					{
						try 
						{
							created = false;
							log.debug("Performing ROLLBACK");
							PreparedStatement prepstat = conn.prepareStatement("ROLLBACK;");
							prepstat.execute();
						}
						catch (SQLException e1) 
						{
							log.error("Could not roll back: " + e1.toString());
						}
					}
					Database.closeConnection(conn);
					if(created)
					{
						try
						{
							//Create Properties File for Vulnerable App Server to connect to schema with.
							log.debug("Create Properties File for Vulnerable App Server Schema");
							String fileName = Validate.validateFileName(challengeName);
							log.debug("creatingFile: " + fileName);
							String filePath = ExposedServer.getApplicationRoot() + "/WEB-INF/challenges/" + fileName + ".properties";
							log.debug("At path: " + filePath);
							File schemaProperties = new File(filePath);	
							
							log.debug("Writing to file");
							FileWriter fileStream = new FileWriter(schemaProperties);
							BufferedWriter toFile = new BufferedWriter(fileStream);
							String fileContents = "databaseConnectionURL=jdbc:mysql://localhost:3308/" + schemaName + "\n" +
									"databaseUsername=" + userName + "\n" +
									"databasePassword=" + userPass + "\n" +
									"DriverType=org.gjt.mm.mysql.Driver";
							toFile.write(fileContents);
							log.debug("Closing file");
							toFile.close();
							fileStream.close();
							log.debug("Checking file was actually created");					
							if(!schemaProperties.isFile())
							{
								//File was not created, So it cannot be populated with access info...
								log.fatal("Could not create properties File");
								out.write("Could not create properties File. Run application server as administrator...");
							}
							else
							{
								log.debug("Storing module Information in secure DB");
								String moduleId = Setter.createModule(ApplicationRoot, challengeName, "challenge", "Custom", null);
								
								log.debug("Creating Cheat Sheet for module# " + moduleId);
								Setter.updateCheatSheet(ApplicationRoot, moduleId, encoder.encodeForHTML(cheatSheet));
								
								log.debug("Preparing JSP page");
								String moduleHash = Getter.getModuleHash(ApplicationRoot, moduleId);
								String theJspContent = ChallengeStructure.createChallengeStructure(challengeName, tableName, challengeIntro, formType, attrib, lookUpAttrib, moduleHash, fileName);
								String theJspPath = ExposedServer.getApplicationRoot() + "/challenges/";
								String theJspFileName = moduleHash + ".jsp";
								//Creating Actual Interaction File
								log.debug("Creating JSP page");
								File jspFile = new File(theJspPath + theJspFileName);
								log.debug("Writing to file");
								fileStream = new FileWriter(jspFile);
								toFile = new BufferedWriter(fileStream);
								toFile.write(theJspContent);
								log.debug("Closing File Steam");
								toFile.close();
								fileStream.close();
								if(!jspFile.isFile())
								{
									log.fatal("Could not create challenge JSP file!");
									out.write("Could not create Challenge JSP. Check your Vulnerable Application Root?");
								}
								else
								{
									log.debug("Interaction JSP Created: " + jspFile.getName());
			
									//Prepare Operation JSP (The JSP servlet that handles the user input.)
									String theOperation = OperationStructure.createOperationStructure(challengeName, tableName, attrib, i, lookUpAttrib, theVulnerability, fileName, customFilterSet, filterFrom, filterTo);
									String theOperateFileName = moduleHash + "operate.jsp";
									log.debug("Creating JSP Operate File");
									File operateFile = new File(theJspPath + theOperateFileName);
									log.debug("Writing to File");
									fileStream = new FileWriter(operateFile);
									toFile = new BufferedWriter(fileStream);
									toFile.write(theOperation);
									log.debug("Closing File Stream");
									toFile.close();
									fileStream.close();
									if(!operateFile.isFile())
									{
										log.fatal("Could not create Operate JSP file!");
										out.write("Could not create Challenge JSP. Check your Vulnerable Application Root?");
									}
									log.debug("Returning User with Step 2 - Challenge Schema Population");
								}
							}
						}
						catch(Exception e)
						{
							log.error("Could not Create File/Store challenge: " + e.toString());
							out.write("Error: Incorrect Vulnerable Application Root?");
						}
					}
					else
					{
						log.debug("Vulnerable Database was not created. Returning user to Step one");
						out.write("Could not create Database at this time.");
					}
				}
				else
				{
					log.debug("Invalid Data, returning error message");
					if(challengeNameEmpty)
						errorMessage = "Invalid challenge name";
					else if (tableNameEmpty)
						errorMessage = "Invalid table name";
					else if (!validPopulation)
						errorMessage = "No valid population for schema";
					else	
						errorMessage = "Invalid attribute name";
					log.debug("Returning '" + errorMessage + "' error message");
					out.write(errorMessage);
				}
			}
			else
			{
				log.debug("CSRF tokens did not match");
				out.print("<h2 class=\"title\">Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error occured! CSRF attack deteceted</font>" +
					"<p>");
			}
		}
		else
		{
			out.print("<h2 class=\"title\">Failure</h2><br>" +
					"<p>" +
					"<font color=\"red\">An error occured! Please login as an administator or try non administrator functions!</font>" +
					"<p>");
		}
		log.debug("*** GenerateChallengeDb END ***");
	}
}
