package servlets.module.lesson;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import testUtils.TestProperties;
import dbProcs.GetterTest;
import dbProcs.Setter;

public class SecurityMisconfigLessonIT
{
	private static String lang = "en_GB";
	private static org.apache.log4j.Logger log = Logger.getLogger(SecurityMisconfigLessonIT.class);
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
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        //Open All modules
        if(!Setter.openAllModules(applicationRoot, false))
        	fail("Could not Mark All Modules As Open");
	}

	public String getModuleDoPost(String userName, String userPass, int expectedResponseCode) throws Exception
	{
		try
		{
			String servletClassName = "SecurityMisconfigLesson";
			log.debug("Creating " + servletClassName + " Servlet Instance");
			SecurityMisconfigLesson servlet = new SecurityMisconfigLesson();
			servlet.init(new MockServletConfig(servletClassName));

			//Setup Servlet Parameters and Attributes
			log.debug("Setting Up Params and Atrributes");
			request.addParameter("userName", userName);
			//Adding Correct CSRF Token (Token Submitted)
			request.addParameter("userPass", userPass);

			log.debug("Running doPost");
			servlet.doPost(request, response);

			if(response.getStatus() != expectedResponseCode)
				fail(servletClassName + " Servlet Returned " + response.getStatus() + " Code. " + expectedResponseCode + " Expected");
			else
			{
				log.debug("302 OK Detected");
				log.debug(servletClassName + " Successful, returning location retrieved: " + response.getContentAsString());
				return(response.getContentAsString());
			}
		}
		catch(Exception e)
		{
			throw e;
		}
		return null;
	}
	
	@Test
	public void testLevelValidAnswer()
	{
		String userName = "lessonTester";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
			//Sign in as Normal User
			log.debug("Signing in as " + userName + " Through LoginServlet");
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
				request.setCookies(response.getCookies());
				String submittedUserName = "admin";
				String submittedUserPass = "password";
				String servletResponse = getModuleDoPost(submittedUserName, submittedUserPass, 302);
				if(servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				else if(!servletResponse.contains("You have successfully signed in"))
				{
					String message = new String("Valid Solution did not yeild Result Key");
					log.fatal(message);
					fail(message);
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete: " + e.toString());
			fail("Could not Complete: " + e.toString());
		}
	}
	
	@Test
	public void testLevelInvalidPasswordAnswer()
	{
		String userName = "lessonTester";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
			//Sign in as Normal User
			log.debug("Signing in as " + userName + " Through LoginServlet");
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
				request.setCookies(response.getCookies());
				String submittedUserName = "admin";
				String submittedUserPass = "wrongpassword";
				String servletResponse = getModuleDoPost(submittedUserName, submittedUserPass, 302);
				if(servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				else if(!servletResponse.contains("Incorrect Password Submitted"))
				{
					String message = new String("Did not get Incorrect Password Response");
					log.fatal(message);
					fail(message);
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete: " + e.toString());
			fail("Could not Complete: " + e.toString());
		}
	}
	
	@Test
	public void testLevelInvalidUserAnswer()
	{
		String userName = "lessonTester";
		try
		{
			//Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
			//Sign in as Normal User
			log.debug("Signing in as " + userName + " Through LoginServlet");
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
				request.setCookies(response.getCookies());
				String submittedUserName = "notanadmin";
				String submittedUserPass = "password";
				String servletResponse = getModuleDoPost(submittedUserName, submittedUserPass, 302);
				if(servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				else if(!servletResponse.contains("No user records found for"))
				{
					String message = new String("Did not get No Records Found Response");
					log.fatal(message);
					fail(message);
				}
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete: " + e.toString());
			fail("Could not Complete: " + e.toString());
		}
	}
	
	
	@Test
	public void testLevelNoAuth()
	{
		try
		{
			request.getSession().setAttribute("lang", lang);
			String submittedUserName = "notanadmin";
			String submittedUserPass = "password";
			String servletResponse = getModuleDoPost(submittedUserName, submittedUserPass, 200); //Mock response is 200 for Unauthenticated response for some reason
			if(servletResponse.contains("You must be getting funky")) 
			{
				String message = new String("General 'Funky' Error Detected");
				log.fatal(message);
				fail(message);
			}
			else if(!servletResponse.contains("Are you signed in"))
			{
				String message = new String("Did not get 'Are you signed in' Response");
				log.fatal(message);
				fail(message);
			} 
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete: " + e.toString());
			fail("Could not Complete: " + e.toString());
		}
	}
}
