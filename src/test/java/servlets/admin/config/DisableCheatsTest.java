package servlets.admin.config;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import testUtils.TestProperties;
import utils.CheatSheetStatus;
import utils.InstallationException;

public class DisableCheatsTest
{
	private static org.apache.log4j.Logger log = Logger.getLogger(DisableCheatsTest.class);
	private static String applicationRoot = new String();
	private static String lang = "en_GB";
	private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    
    /**
	 * Creates DB or Restores DB to Factory Defaults before running tests
	 */
	@BeforeClass
	public static void resetDatabase() 
	{
		TestProperties.setTestPropertiesFileDirectory(log);
		try 
		{
			TestProperties.executeSql(log);
		} 
		catch (InstallationException e) 
		{
			String message = new String("Could not create DB: " + e.toString());
			log.fatal(message);
			fail(message);
		}
	}
    
	@Before
	public void setUp()
	{
		request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        //Enable Cheats before Each test
        CheatSheetStatus.enableForAll();
        if(!CheatSheetStatus.isEnabledAtAll())
        {
        	log.fatal("Unable to Enable Cheat Sheets");
        	fail("Unable to Enable Cheat Sheets");
        }
	}

	/**
	 * Method to Simulate the interaction with the disableCheats servlet.
	 * @param moduleId The ID of the Module to Search For
	 * @param csrfToken The CSRF Token of the User
	 * @return The Content of the Response (Which is supposed to be the location of the module)
	 * @throws Exception
	 */
	public String doThePost(String csrfToken) throws Exception
	{
		String servletClassName = "DisableCheats";
		try
		{
			int expectedResponseCode = 302;

			log.debug("Creating " + servletClassName + " Servlet Instance");
			DisableCheats servlet = new DisableCheats();
			servlet.init(new MockServletConfig(servletClassName));

			//Adding Correct CSRF Token (Token Submitted)
			request.addParameter("csrfToken", csrfToken);

			log.debug("Running doPost");
			servlet.doPost(request, response);

			if(response.getStatus() != expectedResponseCode)
				fail(servletClassName + " Servlet Returned " + response.getStatus() + " Code. " + expectedResponseCode + " Expected");
			else
			{
				log.debug(expectedResponseCode + " Detected");
				log.debug("Servlet Successful, returning response retrieved: " + response.getContentAsString());
				return(response.getContentAsString());
			}
		}
		catch(Exception e)
		{
			throw e;
		}
		return null;
	}

	/**
	 * This test checks that non admin users get access errors when disabling the cheats
	 */
	@Test
	public void testUserDisableCheatsCall()
	{
		String userName = "configUserTester";
		String password = userName;
		//Verify / Create user in DB
		try
		{
			TestProperties.verifyTestUser(log, applicationRoot, userName, password);
			//Sign in as Normal User
			log.debug("Signing in as User Through LoginServlet");
			TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
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
				String responseBody = doThePost(csrfToken);
				if(responseBody.contains("loggedOutSheep"))
				{
					log.debug("No Admin Access Expected Result Recieved");
					if(!CheatSheetStatus.isEnabledAtAll())
					{
						String message = "Cheat Sheets disabled in what should have been a failed request";
						log.fatal(message);
						fail(message);
					}
				}
				else
				{
					String message = "Normal user did not get error when performing admin function";
					log.fatal(message);
					fail(message);
				}
			}
		}
		catch (Exception e)
		{
			log.fatal("Could not Complete: " + e.toString());
			fail("Exception Caught: " + e.toString());
		}
	}

	/**
	 * This test checks that admin users can disable the cheats
	 */
	@Test
	public void testAdminCompleteDisableCheatsCall()
	{
		String userName = "configAdminTester";
		String password = userName;
		//Verify / Create user in DB
		try
		{
			TestProperties.verifyTestAdmin(log, applicationRoot, userName, password);
			//Sign in as Admin User
			log.debug("Signing in as User Through LoginServlet");
			TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
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
				String responseBody = doThePost(csrfToken);
				if(responseBody.contains("Cheat Sheets Disabled"))
				{
					log.debug("Cheat Sheets Were Disabled");
					if(CheatSheetStatus.isEnabledAtAll())
					{
						String message = "Cheat Sheets were not actually Disabled";
						log.fatal(message);
						fail(message);
					}
				} else {
					String message = "Admin was unable to disable cheat sheets";
					log.fatal(message);
					fail(message);
				}
			}
		}
		catch (Exception e)
		{
			log.fatal("Could not Complete: " + e.toString());
			fail("Exception Caught: " + e.toString());
		}
	}
}
