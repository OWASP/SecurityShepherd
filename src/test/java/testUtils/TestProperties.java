package testUtils;

import java.sql.ResultSet;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import dbProcs.Getter;
import dbProcs.Setter;
import servlets.Login;

public class TestProperties 
{
	public static String propertiesFileDirectory = new String("/target/test-classes");
	
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
	public static boolean verifyTestAdmin(org.apache.log4j.Logger log, String applicationRoot, String userName, String password, String classId) throws Exception
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
}
