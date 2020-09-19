package servlets;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import dbProcs.Getter;
import dbProcs.GetterTest;
import dbProcs.Setter;
import testUtils.TestProperties;

public class LoginIT
{
	private static String lang = "en_GB";
	private static org.apache.log4j.Logger log = Logger.getLogger(LoginIT.class);
	private static String applicationRoot = new String();
	private MockHttpServletRequest request;
    private MockHttpServletResponse response;
	
    /**
	 * Creates DB or Restores DB to Factory Defaults before running tests
	 */
	@BeforeClass
	public static void resetDatabase() throws IOException, SQLException {
		TestProperties.setTestPropertiesFileDirectory(log);

		TestProperties.createMysqlResource();

		TestProperties.executeSql(log);

	}
    
	@Before
	public void setup() 
	{
		log.debug("Setting Up Blank Request and Response");
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
	}
	
	@Test
	public void testUserLogin()
	{
		String userName = "userLogin1";
		try
		{
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
			loginDoPost(userName, userName, null);
			HttpSession ses = request.getSession();
			if(!ses.getAttribute("userRole").toString().equalsIgnoreCase("player"))
			{
				String message = new String("User has admin role after login when they should be player");
				log.fatal(message);
				fail(message);
			}
			else
			{
				//Check CSRF Token Exists
				log.debug("Getting CSRF Token");
				String csrfToken = response.getCookie("token").getValue();
				if(csrfToken.isEmpty())
					log.debug("Csrf Token Value Empty: " + csrfToken);
				else
				{
					log.debug("Csrf Token Value: " + csrfToken);
					log.debug("Unit Test Passed");
				}
			}	
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testUserLogin: " + e.toString());
			fail("Could not Complete testUserLogin");
		}
	}
	
	@Test
	public void testUserLoginWithChangedPass()
	{
		String userName = "userTempPass1";
		String currentPass = new String();
		String newPass = new String();
		boolean loggedIn = false;
		try
		{
			try
			{
				currentPass = userName;
				newPass = userName+userName;
				log.debug("Logging in with default Pass");
				loggedIn = GetterTest.verifyTestUser(applicationRoot, userName, currentPass);
			}
			catch(Exception e)
			{
				newPass = userName;
				currentPass = userName+userName;
				log.debug("Could not log in with default pass: " + e.toString());
				log.debug("Logging in with alternative pass: " + currentPass);
				String[] auth = Getter.authUser(applicationRoot, userName, currentPass);
				loggedIn = auth != null;
			}
			if(!loggedIn)
			{
				log.debug("Could not sign in with any pass.");
				fail("Could not Verify User");
			}
			else
			{
				log.debug("Logged in! Updating Password now");
				if(!Setter.updatePassword(applicationRoot, userName, currentPass, newPass))
				{
					log.debug("Could not update password");
					fail("Could not update password");
				}
				else
				{
					log.debug("Password Updated. Authenticating with new pass: " + newPass);
					loginDoPost(userName, newPass, null);
					HttpSession ses = request.getSession();
					if(!ses.getAttribute("userRole").toString().equalsIgnoreCase("player"))
					{
						String message = new String("User has admin role after login when they should be player");
						log.fatal(message);
						fail(message);
					}
					else if(ses.getAttribute("ChangePassword") != null)
					{
						String message = "User Being Prompt To Change pass when they shouldn't be";
						log.debug(message);
						fail(message);
					}
					else
					{
						log.debug("User with Temp Pass Unit Test Pass");
					}
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testUserLogin: " + e.toString());
			fail("Could not Complete testUserLogin");
		}
	}
	
	@Test
	public void testUserLoginWithTempPass()
	{
		String userName = "userTempPass2";
		String currentPass = new String();
		String newPass = new String();
		boolean loggedIn = false;
		try
		{
			try
			{
				currentPass = userName;
				newPass = userName+userName;
				log.debug("Logging in with default Pass");
				loggedIn = GetterTest.verifyTestUser(applicationRoot, userName, currentPass);
			}
			catch(Exception e)
			{
				newPass = userName;
				currentPass = userName+userName;
				log.debug("Could not log in with default pass: " + e.toString());
				log.debug("Logging in with alternative pass: " + currentPass);
				String[] auth = Getter.authUser(applicationRoot, userName, currentPass);
				loggedIn = auth != null;
			}
			if(!loggedIn)
			{
				log.debug("Could not sign in with any pass.");
				fail("Could not Verify User");
			}
			else
			{
				log.debug("Logged in! Updating Password now");
				String userId = Getter.getUserIdFromName(applicationRoot, userName);
				if(!Setter.updatePasswordAdmin(applicationRoot, userId, newPass))
				{
					log.debug("Could not update password");
					fail("Could not update password");
				}
				else
				{
					log.debug("Password Updated. Authenticating with new pass: " + newPass);
					loginDoPost(userName, newPass, null);
					log.debug("Checking Session");
					HttpSession ses = request.getSession();
					if(!ses.getAttribute("userRole").toString().equalsIgnoreCase("player"))
					{
						String message = new String("User has admin role after login when they should be player");
						log.fatal(message);
						fail(message);
					}
					else if(ses.getAttribute("ChangePassword") == null)
					{
						String message = "No Password update Prompt Detected in Session";
						log.debug(message);
						fail(message);
					}
					else
					{
						log.debug("User with Temp Pass Unit Test Pass");
					}
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testUserLogin: " + e.toString());
			fail("Could not Complete testUserLogin");
		}
	}
	
	@Test
	public void testAdminLogin()
	{
		String userName = "adminLogin1";
		try
		{
			GetterTest.verifyTestAdmin(applicationRoot, userName, userName);
			loginDoPost(userName, userName, null);
			HttpSession ses = request.getSession();
			if(!ses.getAttribute("userRole").toString().equalsIgnoreCase("admin"))
			{
				String message = new String("User has player role after login when they should be admin");
				log.fatal(message);
				fail(message);
			}
			else
			{
				log.debug("Admin Logged in and has Admin Session. PASS");
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testUserLogin: " + e.toString());
			fail("Could not Complete testUserLogin");
		}
	}
	
	@Test
	public void testUserLoginWithBadPass()
	{
		String userName = "userLogin2";
		try
		{
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
			try
			{
				loginDoPost(userName, "wrongPassword", null);
				fail("LoginDoPost ran without exception with bad password");
			}
			catch(Exception e)
			{
				log.debug("Login Servlet Test Threw Exception: " + e.toString());
				if(e.toString().endsWith("Login Proceedure Failed"))
				{
					log.debug("User Could not sign in with bad password. PASS");
					return; // Pass
				}
				fail("Could not Complete testUserLoginWithBadPass");
			}

		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testUserLoginWithBadPass: " + e.toString());
			fail("Could not Complete testUserLoginWithBadPass");
		}
	}
	
	@Test
	public void testUserLoginWithNullUser()
	{
		String userName = "userLogin2";
		try
		{
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
			try
			{
				loginDoPost(null, userName, null);
				fail("LoginDoPost ran without exception with bad password");
			}
			catch(Exception e)
			{
				log.debug("Login Servlet Test Threw Exception: " + e.toString());
				if(e.toString().endsWith("Login Proceedure Failed"))
				{
					log.debug("User Could not sign in with null username. PASS");
					return; // Pass
				}
				fail("Could not Complete testUserLoginWithNullUser");
			}

		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testUserLoginWithNullUser: " + e.toString());
			fail("Could not Complete testUserLoginWithNullUser");
		}
	}
	
	@Test
	public void testUserLoginWithSqli()
	{
		String userName = "userLogin4";
		try
		{
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
			try
			{
				loginDoPost(userName, "'OR'1'='1", null);
				fail("LoginDoPost ran without exception with bad password");
			}
			catch(Exception e)
			{
				log.debug("Login Servlet Test Threw Exception: " + e.toString());
				if(e.toString().endsWith("Login Proceedure Failed"))
				{
					log.debug("User Could not sign in with null username. PASS");
					return; // Pass
				}
				fail("Could not Complete testUserLoginWithSqli");
			}

		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testUserLoginWithNullUser: " + e.toString());
			fail("Could not Complete testUserLoginWithNullUser");
		}
	}
	
	@Test
	public void testUserLoginWithSqliName()
	{
		String userName = "userLogin5";
		try
		{
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
			try
			{
				loginDoPost("'OR'1'='1'; -- ;", userName, null);
				fail("LoginDoPost ran without exception with bad password");
			}
			catch(Exception e)
			{
				log.debug("Login Servlet Test Threw Exception: " + e.toString());
				if(e.toString().endsWith("Login Proceedure Failed"))
				{
					log.debug("User Could not sign in with null username. PASS");
					return; // Pass
				}
				fail("Could not Complete testUserLoginWithSqliName");
			}

		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testUserLoginWithNullUser: " + e.toString());
			fail("Could not Complete testUserLoginWithNullUser");
		}
	}
	
	@Test
	public void testLoginDoGet()
	{
		try
		{
			loginDoGet();
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testLoginDoGet: " + e.toString());
			fail("Could not Complete testLoginDoGet");
		}
	}
	
	public void loginDoGet() throws Exception
	{
		try
		{
			int expectedResponseCode = 302;
			
			log.debug("Creating Login Servlet Instance");
			Login servlet = new Login();
			servlet.init(new MockServletConfig("Login"));
			
			log.debug("Running doPost");
			servlet.doGet(request, response);
			
			if(response.getStatus() != expectedResponseCode)
				fail("Login Servlet Returned " + response.getStatus() + " Code. 302 Expected");
			else
			{
				log.debug("302 OK Detected");
				String location = response.getHeader("Location");
				log.debug("302 pointing at: " + location);
				if(!location.endsWith("index.jsp"))
				{
					throw new Exception("Login doGet not Redirecting to index.jsp. Login Proceedure Failed");
				}
			}
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	
	public void loginDoPost(String userName, String password, String theClass) throws Exception
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
				fail("Login Servlet Returned " + response.getStatus() + " Code. 302 Expected");
			else
			{
				log.debug("302 OK Detected");
				String location = response.getHeader("Location");
				log.debug("302 pointing at: " + location);
				if(!location.endsWith("index.jsp"))
				{
					throw new Exception("Login not Redirecting to index.jsp. Login Proceedure Failed");
				}
				else
				{
					log.debug("Making sure all Session Attributes have been set");
					HttpSession ses  = request.getSession();
					try
					{
						String userStamp = ses.getAttribute("userStamp").toString();
						log.debug("userStamp =  " + userStamp);
						String sessionUserName = ses.getAttribute("userName").toString();
						log.debug("userName = " + sessionUserName);
						String userRole = ses.getAttribute("userRole").toString();
						log.debug("userRole = " + userRole);
						String userLang = ses.getAttribute("lang").toString();
						log.debug("lang = " + userLang);
						if(theClass != null) //Null Class wasn't Submitted, So Check ses has a class id in it
						{
							String userClass = ses.getAttribute("userClass").toString();
							log.debug("userClass = " + userClass);
						}
						else if(ses.getAttribute("userClass") != null)//Null was Submitted. Make sure the class is null
						{
							fail("Player is in a class, but should have class id of null");
						}
					}
					catch(Exception e)
					{
						log.fatal("Could not Retrieve All Session Attibutes: " + e.toString());
						fail("Could not Retreive All Expected Session Attributes");
					}
				}
			}
		}
		catch(Exception e)
		{
			throw e;
		}
	}
}
