package servlets.module;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import testUtils.TestProperties;
import utils.InstallationException;
import utils.ModuleBlock;
import dbProcs.GetterTest;
import dbProcs.Setter;

public class GetModuleTest
{
	private static String lang = "en_GB";
	private static org.apache.log4j.Logger log = Logger.getLogger(GetModuleTest.class);
	private static String applicationRoot = new String();
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
	public void setup()
	{
		log.debug("Setting Up Blank Request and Response");
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        //Open All modules
        if(!Setter.openAllModules(applicationRoot))
        	fail("Could not Mark All Modules As Open");
        //Ensure no Module block enabled
        ModuleBlock.reset();
        if(ModuleBlock.blockerEnabled)
        {
        	String message = "Unable to disable module block";
        	log.fatal(message);
        	fail(message);
        }
	}

	/**
	 * Method to Simulate the interaction with the getModule servlet.
	 * @param moduleId The ID of the Module to Search For
	 * @param csrfToken The CSRF Token of the User
	 * @return The Content of the Response (Which is supposed to be the location of the module)
	 * @throws Exception
	 */
	public String getModuleDoPost(String moduleId, String csrfToken) throws Exception
	{
		try
		{
			int expectedResponseCode = 302;

			log.debug("Creating GetModule Servlet Instance");
			GetModule servlet = new GetModule();
			servlet.init(new MockServletConfig("GetModule"));

			//Setup Servlet Parameters and Attributes
			log.debug("Setting Up Params and Atrributes");
			request.addParameter("moduleId", moduleId);
			//Adding Correct CSRF Token (Token Submitted)
			request.addParameter("csrfToken", csrfToken);

			log.debug("Running doPost");
			servlet.doPost(request, response);

			if(response.getStatus() != expectedResponseCode)
				fail("GetModule Servlet Returned " + response.getStatus() + " Code. " + expectedResponseCode + " Expected");
			else
			{
				log.debug("302 OK Detected");
				log.debug("Servlet Successful, returning location retrieved: " + response.getContentAsString());
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
	 * This test checks the module address returned when the requested module is currently blocked
	 */
	@Test
	public void testGetBlockedModule()
	{
		String moduleId = new String("20e755179a5840be5503d42bb3711716235005ea"); //CSRF 1
		String userName = "getModule5";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
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
				//Blocking Module
				ModuleBlock.blockerId = moduleId;
				ModuleBlock.blockerEnabled = true;
				//Add Cookies from Response to outgoing request
				request.setCookies(response.getCookies());
				String moduleAddress = getModuleDoPost(moduleId, csrfToken);
				log.debug(moduleAddress);
				//Resetting Module Block
				ModuleBlock.reset();
				if(moduleAddress.equalsIgnoreCase("../blockedMessage.jsp"))
					log.debug("Blocked Module Address Returned: PASS");
				else
				{
					String message = "Module Address Returned was not the Blocked Message Page";
					log.fatal(message + ". Should be ../blockedMessage.jsp");
					log.debug("Returned: " + moduleAddress);
					fail(message);				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testGetBlockedModule: " + e.toString());
			fail("Could not Complete testGetBlockedModule");
		}
	}

	/**
	 * This test retreives the location of a challenge module
	 */
	@Test
	public void testGetChallenge()
	{
		String moduleId = new String("20e755179a5840be5503d42bb3711716235005ea"); //CSRF 1
		String levelHash = new String("s74a796e84e25b854906d88f622170c1c06817e72b526b3d1e9a6085f429cf52");
		String userName = "getModule2";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
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
				String moduleAddress = getModuleDoPost(moduleId, csrfToken);
				if(moduleAddress.endsWith("challenges/" + levelHash + ".jsp"))
				{
					log.debug("Correct Location Returned");
				}
				else
				{
					String message = "The Incorrect Location was Returned for the CSRF 1 Challenge.";
					log.fatal(message);
					log.debug("location returned: " + moduleAddress);
					log.debug("Should be        : challenges&#x2f;" + levelHash + ".jsp");
					fail(message);
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testGetChallenge: " + e.toString());
			fail("Could not Complete testGetChallenge");
		}
	}

	@Test
	public void testGetModule()
	{
		String moduleId = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11"); //Insecure Direct Object References Module Id
		String levelHash = new String("fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100");
		String userName = "getModule1";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
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
				String moduleAddress = getModuleDoPost(moduleId, csrfToken);
				if(moduleAddress.endsWith("lessons/" + levelHash + ".jsp"))
				{
					log.debug("Correct Location Returned");
				}
				else
				{
					String message = "The Incorrect Location was Returned for the Insecure Director Object Reference Lesson.";
					log.fatal(message);
					log.debug("location returned: " + moduleAddress);
					fail(message);
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testGetModule: " + e.toString());
			fail("Could not Complete testGetModule");
		}
	}

	/**
	 * This test submits a valid Module Id but with an invalid CSRF Token Pair
	 */
	@Test
	public void testGetModuleBadCsrfToken()
	{
		String moduleId = new String("20e755179a5840be5503d42bb3711716235005ea"); //CSRF 1
		String userName = "getModule5";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
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
				String moduleAddress = getModuleDoPost(moduleId, "TheWrongCsrfToken");
				if(moduleAddress.isEmpty())
					log.debug("No Module Address Returned: PASS");
				else
				{
					String message = "Module Address Returned When Bad ID Was Submitted";
					log.fatal(message + ". Nothing should be returned");
					log.debug("Returned: " + moduleAddress);
					fail(message);
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testGetModuleBadCsrfToken: " + e.toString());
			fail("Could not Complete testGetModuleBadCsrfToken");
		}
	}

	/**
	 * This test attempts to retrieve a module with a non existant identifier
	 */
	@Test
	public void testGetModuleBadId()
	{
		String moduleId = new String("ThisModuleDoesNotExist");
		String userName = "getModule3";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
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
				String moduleAddress = getModuleDoPost(moduleId, csrfToken);
				if(moduleAddress.isEmpty())
					log.debug("No Module Address Returned: PASS");
				else
				{
					String message = "Module Address Returned When Bad ID Was Submitted";
					log.fatal(message + ". Nothing should be returned");
					log.debug("Returned: " + moduleAddress);
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testGetModuleBadId: " + e.toString());
			fail("Could not Complete testGetModuleBadId");
		}
	}

	/**
	 * This test submits a null value to the getModule Servlet
	 */
	@Test
	public void testGetModuleNullId()
	{
		String moduleId = null;
		String userName = "getModule4";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
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
				String moduleAddress = getModuleDoPost(moduleId, csrfToken);
				if(moduleAddress.isEmpty())
					log.debug("No Module Address Returned: PASS");
				else
				{
					String message = "Module Address Returned When Bad ID Was Submitted";
					log.fatal(message + ". Nothing should be returned");
					log.debug("Returned: " + moduleAddress);
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testGetModuleNullId: " + e.toString());
			fail("Could not Complete testGetModuleNullId");
		}
	}

	@Test
	public void testGetModuleWhenClosed()
	{
		String moduleId = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11"); //Insecure Direct Object References Module Id
		String levelHash = new String("fdb94122d0f032821019c7edf09dc62ea21e25ca619ed9107bcc50e4a8dbc100");
		String userName = "getModule1";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
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
				if(!Setter.closeAllModules(applicationRoot))
					fail("Could not Mark All Modules As Closed");
				//Add Cookies from Response to outgoing request
				request.setCookies(response.getCookies());
				String moduleAddress = getModuleDoPost(moduleId, csrfToken);
				if(moduleAddress.endsWith("lessons&#x2f;" + levelHash + ".jsp"))
				{
					String message = "Insecure Director Object Reference Lesson Address Returned when module was closed";
					log.fatal(message);
					log.debug("location returned: " + moduleAddress);
					fail(message);
				}
				else
				{
					log.debug("Address not returned! Pass!");
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete testGetModuleWhenClosed: " + e.toString());
			fail("Could not Complete testGetModuleWhenClosed");
		}
	}
}
