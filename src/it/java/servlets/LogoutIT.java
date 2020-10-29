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

import dbProcs.GetterTest;
import testUtils.TestProperties;

public class LogoutIT
{
	private static String lang = "en_GB";
	private static org.apache.log4j.Logger log = Logger.getLogger(LogoutIT.class);
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
    
	/**
	 * Method to simulate login servlet interaction. Can't seem to recyle the method in LoginTest with the MockRequests
	 * @param userName User to Sign in
	 * @param password User Password to use to Sign in
	 * @param theClass Class of the User
	 * @throws Exception If the process fails, an exception will be thrown
	 */
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
			}
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	
	public void logoutDoPost(String csrfToken) throws Exception
	{
		try
		{
			int expectedResponseCode = 302;
			
			log.debug("Creating Logout Servlet Instance");
			Logout servlet = new Logout();
			servlet.init(new MockServletConfig("Logout"));
			
			//Setup Servlet Parameters and Attributes
			log.debug("Setting Up Params and Atrributes");
			request.addParameter("csrfToken", csrfToken);
			try
			{
				log.debug("Running doGet");
				servlet.doGet(request, response);
			}
			catch(IllegalStateException e)
			{
				log.debug("Logout Function Always throws: " + e.toString());
			}
			if(response.getStatus() != expectedResponseCode)
				fail("Logout Servlet Returned " + response.getStatus() + " Code. 302 Expected");
			else
			{
				log.debug("302 OK Detected");
			}
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	
	/**
	 * This test logs the user in and then calls the logout function
	 */
	@Test
	public void testLogout()
	{
		String userName = "logoutUser1";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
			//Sign in as Normal User
			log.debug("Signing in as User Through LoginServlet");
			loginDoPost(userName, userName, null);
			log.debug("Login Servlet Complete, Getting CSRF Token");
			if(response.getCookie("token") == null)
				fail("No CSRF Token Was Returned from Login Servlet");
			String csrfToken = response.getCookie("token").getValue();
			if(csrfToken.isEmpty())
			{
				String message = new String("No CSRF token returned from Login Servlet");
				log.fatal(message);
				fail(message);
			}
			else
			{
				//Add Cookies from Response to outgoing request
				request.setCookies(response.getCookies());
				logoutDoPost(csrfToken);
				log.debug("Checking Session");
				HttpSession ses = request.getSession();
				if(ses.getAttribute("userStamp") != null)
					fail("User's Session is not Cleared After Logout");
				else
				{
					log.debug("User Logged Out! Pass!");
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testLogout: " + e.toString());
			fail("Could not Complete testLogout");
		}
	}
	
	/**
	 * This test logs the user in and then calls the logout function
	 */
	@Test
	public void testLogoutBadCsrf()
	{
		String userName = "logoutUser2";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
			//Sign in as Normal User
			log.debug("Signing in as User Through LoginServlet");
			loginDoPost(userName, userName, null);
			log.debug("Login Servlet Complete, Getting CSRF Token");
			if(response.getCookie("token") == null)
				fail("No CSRF Token Was Returned from Login Servlet");
			String csrfToken = response.getCookie("token").getValue();
			if(csrfToken.isEmpty())
			{
				String message = new String("No CSRF token returned from Login Servlet");
				log.fatal(message);
				fail(message);
			}
			else
			{
				//Add Cookies from Response to outgoing request
				request.setCookies(response.getCookies());
				logoutDoPost("wrongCsrfToken");
				HttpSession ses = request.getSession();
				if(ses.getAttribute("userStamp") == null)
					fail("User's Session is Cleared After CSRF'd Logout");
				else
				{
					log.debug("User Not Logged Out! Pass!");
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testLogoutBadCsrf: " + e.toString());
			fail("Could not Complete testLogoutBadCsrf");
		}
	}
}
