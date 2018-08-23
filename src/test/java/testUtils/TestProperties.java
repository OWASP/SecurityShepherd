package testUtils;

import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.io.FileUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import dbProcs.Database;
import dbProcs.Getter;
import dbProcs.Setter;
import servlets.Login;
import utils.InstallationException;

public class TestProperties
{
	public static void executeSql(org.apache.log4j.Logger log) throws InstallationException
	{
		try 
		{
			File file = new File(System.getProperty("user.dir")+"/src/main/resources/database/coreSchema.sql");
			String data = FileUtils.readFileToString(file, Charset.defaultCharset() );
			
			Connection databaseConnection = Database.getDatabaseConnection(null, true);
			Statement psProcToexecute = databaseConnection.createStatement();
			psProcToexecute.executeUpdate(data);
			
			file = new File(System.getProperty("user.dir")+"/src/main/resources/database/moduleSchemas.sql");
			data = FileUtils.readFileToString(file, Charset.defaultCharset() );
			psProcToexecute = databaseConnection.createStatement();
			psProcToexecute.executeUpdate(data);
	
		} 
		catch (Exception e) 
		{
			throw new InstallationException(e);
		}
	}
	
	/**
	 * Bit of a Hack to get JUnits to run inside of
	 * @param log
	 */
	public static void setTestPropertiesFileDirectory(org.apache.log4j.Logger log)
	{
		if(System.getProperty("catalina.base") == null)
		{
			String userDir = System.getProperty("user.dir");
			log.debug("catalina.base returns null. Creating it with base of user.dir; " + userDir+File.separator+"target"+File.separator+"test-classes");
			System.setProperty("catalina.base", userDir+File.separator+"target"+File.separator+"test-classes");
		}
	}

	/**
	 * Method to simulate login servlet interaction. Can't seem to recyle the method in LoginTest with the MockRequests
	 * @param userName User to Sign in
	 * @param password User Password to use to Sign in
	 * @param theClass Class of the User
	 * @throws Exception If the process fails, an exception will be thrown
	 */
	public static void loginDoPost(org.apache.log4j.Logger log, MockHttpServletRequest request, MockHttpServletResponse response, String userName, String password, String theClass, String lang) throws Exception
	{
		try
		{
			int expectedResponseCode = 302;

			log.debug("Creating Login Servlet Instance");
			Login servlet = new Login();
			servlet.init(new MockServletConfig("Login"));

			//Setup Servlet Parameters and Attributes
			log.debug("Setting Up Params and Atrributes");
			request.addParameter("login", userName);
			request.addParameter("pwd", password);
			request.getSession().setAttribute("lang", lang);

			log.debug("Running doPost");
			servlet.doPost(request, response);

			if(response.getStatus() != expectedResponseCode)
				throw new Exception("Login Servlet Returned " + response.getStatus() + " Code. 302 Expected");
			else
			{
				log.debug("302 OK Detected");
				String location = response.getHeader("Location");
				log.debug("302 pointing at: " + location);
				if(!location.endsWith("index.jsp"))
				{
					throw new Exception("Login not Redirecting to index.jsp. Login Proceedure Failed");
				}
			}
		}
		catch(Exception e)
		{
			throw e;
		}
	}

	/**
	 * This method will sign in as a User, or create the user and sign in as them. If this fails it will throw an Exception
	 * @param applicationRoot Context of running application
	 * @param userName The user name of the user you want to create or sign in as
	 * @param password The password of the user you want to create or sign in as
	 * @return Boolean value depicting if the user exists and can be authenticated
	 * @throws Exception If User Create function fails, an exception will be passed up
	 */
	public static boolean verifyTestUser(org.apache.log4j.Logger log, String applicationRoot, String userName, String password) throws Exception
	{
		boolean result = false;
		try
		{
			String user[] = Getter.authUser(applicationRoot, userName, userName);
			if(user == null || user[0].isEmpty())
			{
				log.debug("Test Failed. User not found in DB. Adding user to DB and Retesting before reporting failure");
				Setter.userCreate(applicationRoot, null, userName, userName, "player", userName+"@test.com", false);
				user = Getter.authUser(applicationRoot, userName, userName);
			}
			if(user != null && !user[0].isEmpty())
			{
				log.debug(userName + " could authenticate. returning true");
				result = true;
			}
			else
			{
				log.error("Couldnt verify that " + userName + " could authenticate at all. Throwing Exception");
				throw new Exception("Could not Verify User " + userName + " could authenticate at all.");
			}
		}
		catch(Exception e)
		{
			throw new Exception("Could not Create User " + userName + ": " + e.toString());
		}
		return result;
	}

	/**
	 * This method will sign in as a User, or create the user and sign in as them. If this fails it will throw an Exception
	 * @param applicationRoot Context of running application
	 * @param userName The user name of the user you want to create or sign in as
	 * @param password The password of the user you want to create or sign in as
	 * @param classId Class to create the user in
	 * @return Boolean value depicting if the user exists and can be authenticated
	 * @throws Exception If User Create function fails, an exception will be passed up
	 */
	public static boolean verifyTestUser(org.apache.log4j.Logger log, String applicationRoot, String userName, String password, String classId) throws Exception
	{
		boolean result = false;
		try
		{
			String user[] = Getter.authUser(applicationRoot, userName, userName);
			if(user == null || user[0].isEmpty())
			{
				log.debug("User not found in DB. Adding user to DB");
				Setter.userCreate(applicationRoot, classId, userName, userName, "player", userName+"@test.com", false);
				user = Getter.authUser(applicationRoot, userName, userName);
			}
			if(user != null && !user[0].isEmpty())
			{
				log.debug(userName + " could authenticate. returning true");
				result = true;
			}
			else
			{
				log.error("Couldnt verify that " + userName + " could authenticate at all. Throwing Exception");
				throw new Exception("Could not Verify User " + userName + " could authenticate at all.");
			}
		}
		catch(Exception e)
		{
			throw new Exception("Could not Create User " + userName + ": " + e.toString());
		}
		return result;
	}

	/**
	 * This method will sign in as a User, or create the user and sign in as them. If this fails it will throw an Exception
	 * @param applicationRoot Context of running application
	 * @param userName The user name of the user you want to create or sign in as
	 * @param password The password of the user you want to create or sign in as
	 * @param classId Class to create the user in
	 * @return Boolean value depicting if the user exists and can be authenticated
	 * @throws Exception If User Create function fails, an exception will be passed up
	 */
	public static boolean verifyTestAdmin(org.apache.log4j.Logger log, String applicationRoot, String userName, String password, String classId) throws Exception
	{
		boolean result = false;
		try
		{
			String user[] = Getter.authUser(applicationRoot, userName, userName);
			if(user == null || user[0].isEmpty())
			{
				log.debug("Test Failed. User not found in DB. Adding user to DB and Retesting before reporting failure");
				Setter.userCreate(applicationRoot, classId, userName, userName, "admin", userName+"@test.com", false);
				user = Getter.authUser(applicationRoot, userName, userName);
			}
			if(user != null && !user[0].isEmpty())
			{
				log.debug(userName + " could authenticate. returning true");
				result = true;
			}
			else
			{
				log.error("Couldnt verify that " + userName + " could authenticate at all. Throwing Exception");
				throw new Exception("Could not Verify User " + userName + " could authenticate at all.");
			}
		}
		catch(Exception e)
		{
			throw new Exception("Could not Create User " + userName + ": " + e.toString());
		}
		return result;
	}

	/**
	 * This method will sign in as an admin, or create the admin and sign in as them. If this fails it will throw an Exception
	 * @param applicationRoot Context of running application
	 * @param userName The user name of the user you want to create or sign in as
	 * @param password The password of the user you want to create or sign in as
	 * @return Boolean value depicting if the user exists and can be authenticated
	 * @throws Exception If User Create function fails, an exception will be passed up
	 */
	public static boolean verifyTestAdmin(org.apache.log4j.Logger log, String applicationRoot, String userName, String password) throws Exception
	{
		boolean result = false;
		try
		{
			String user[] = Getter.authUser(applicationRoot, userName, userName);
			if(user == null || user[0].isEmpty())
			{
				log.debug("Test Failed. User not found in DB. Adding user to DB and Retesting before reporting failure");
				Setter.userCreate(applicationRoot, null, userName, userName, "admin", userName+"@test.com", false);
				user = Getter.authUser(applicationRoot, userName, userName);
			}
			if(user != null && !user[0].isEmpty())
			{
				log.debug(userName + " could authenticate. returning true");
				result = true;
			}
			else
			{
				log.error("Couldnt verify that " + userName + " could authenticate at all. Throwing Exception");
				throw new Exception("Could not Verify User " + userName + " could authenticate at all.");
			}
		}
		catch(Exception e)
		{
			throw new Exception("Could not Create User " + userName + ": " + e.toString());
		}
		return result;
	}

	/**
	 * Searches for class based on class name. If nothing is found, the class is created and the new class Id is returned
	 * @param className Name of the class you wish to search / create
	 * @return The Identifier of the class owning the name submitted
	 * @throws Exception If the class cannot be created or found
	 */
	public static String findCreateClassId(org.apache.log4j.Logger log, String className, String applicationRoot) throws Exception
	{
		String classId = new String();
		ResultSet rs = Getter.getClassInfo(applicationRoot);
		while(rs.next())
		{
			if(rs.getString(2).compareTo(className) == 0)
			{
				classId = rs.getString(1);
				break;
			}
		}
		rs.close();
		if(classId.isEmpty())
		{
			log.debug("Could not find class. Creating it");
			if(Setter.classCreate(applicationRoot, className, "2015"))
			{
				log.debug("Class Created. Getting ID");
				classId = findCreateClassId(log, className, applicationRoot);
			}
			else
			{
				throw new Exception("Could not Create Class " + className);
			}
		}
		return classId;
	}

	/**
	 * This method will login/create a PLAYER, open all modules, Collect the Module Adddress and Mark the moduleId as complete
	 * @param log Logger
	 * @param userName Username to complete level with
	 * @param userPass Password to complete level with
	 * @param moduleId If of level to complete
	 * @param feedbackString Leave as null for default
	 * @param applicationRoot
	 */
	public static boolean completeModuleForUser(org.apache.log4j.Logger log, String userName, String userPass, String moduleId, String feedbackString, String applicationRoot)
	{
		boolean result = false;
		try
		{
			if(verifyTestUser(log, applicationRoot, userName, userPass))
			{
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				//Open all Modules First so that the Module Can Be Opened
				if(Setter.openAllModules(applicationRoot))
				{
					//Simulate user Opening Level
					if(!Getter.getModuleAddress(applicationRoot, moduleId, userId).isEmpty())
					{
						//Then, Mark the Challenge Complete for user (Insecure Data Storage Lesson)
						String feedbackSearchCode = "RwarUNiqueFeedbackCodeToSEARCHFor1182371723";
						String markLevelCompleteTest = Setter.updatePlayerResult(applicationRoot, moduleId, userId, feedbackSearchCode, 1, 1, 1);
						if (markLevelCompleteTest != null)
						{
							String checkPlayerResultTest = Getter.checkPlayerResult(applicationRoot, moduleId, userId);
							log.debug("checkPlayerResultTest" + checkPlayerResultTest);
							if(checkPlayerResultTest == null)
							{
								result = true;
							}
							else
							{
								fail("Function says user has not completed module"); //Even though this test just marked it as Completed
							}
						}
						else
							fail("Could not mark data storage lesson as complete for user");
					}
					else
						fail("Could not Mark Data Storage Lesson as Opened by Default admin");
				}
				else
					fail("Could not Open All Modules");
			}
			else
			{
				fail("Could not verify user (No Exception Failure)");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Verify User: " + e.toString());
			fail("Could not Verify User " + userName);
		}
		return result;
	}
}
