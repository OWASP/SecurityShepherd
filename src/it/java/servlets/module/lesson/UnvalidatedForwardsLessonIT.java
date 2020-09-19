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

import dbProcs.GetterTest;
import dbProcs.Setter;
import testUtils.TestProperties;

public class UnvalidatedForwardsLessonIT
{
	private static String lang = "en_GB";
	private static org.apache.log4j.Logger log = Logger.getLogger(UnvalidatedForwardsLessonIT.class);
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

	public String getModuleDoPost(String messageForAdmin, String csrfToken) throws Exception
	{
		try
		{
			int expectedResponseCode = 302;
			String servletClassName = "UnvalidatedRedirectLesson";
			log.debug("Creating " + servletClassName + " Servlet Instance");
			UnvalidatedForwardsLesson servlet = new UnvalidatedForwardsLesson();
			servlet.init(new MockServletConfig(servletClassName));

			//Setup Servlet Parameters and Attributes
			log.debug("Setting Up Params and Atrributes");
			request.addParameter("messageForAdmin", messageForAdmin);
			//Adding Correct CSRF Token (Token Submitted)
			request.addParameter("csrfToken", csrfToken);

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
				String falseIdTestValue = new String("1627312");
				String httpsBase = new String("https://localhost:8080/");
				String redriectUrlPath = new String("user/redirect?to=");
				String targetUrlPath = new String("root/grantComplete/unvalidatedredirectlesson?userId=");
				request.getSession().setAttribute("tempId", falseIdTestValue); //Test Value
				String messageForAdmin = new String(httpsBase+redriectUrlPath+httpsBase+targetUrlPath+falseIdTestValue);
				String servletResponse = getModuleDoPost(messageForAdmin, csrfToken);
				if(servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				if(!servletResponse.contains("The result key for this lesson is"))
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
	public void testLevelInvalidSecondUrlAnswer()
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
				String falseIdTestValue = new String("1627312");
				String httpsBase = new String("https://localhost:8080/");
				String redriectUrlPath = new String("user/redirect?to=");
				String targetUrlPath = new String("wrong?userId=");
				request.getSession().setAttribute("tempId", falseIdTestValue); //Test Value
				String messageForAdmin = new String(httpsBase+redriectUrlPath+httpsBase+targetUrlPath+falseIdTestValue);
				String servletResponse = getModuleDoPost(messageForAdmin, csrfToken);
				if(servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				if(servletResponse.contains("The result key for this lesson is"))
				{
					String message = new String("Invalid Solution returned Result Key");
					log.fatal(message);
					fail(message);
				}
				else if(!servletResponse.contains("Message Sent"))
				{
					String message = new String("Response did not contain expected hard coded message (Must have failed)");
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
	public void testLevelInvalidFirstUrlQueryAnswer()
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
				String falseIdTestValue = new String("1627312");
				String httpsBase = new String("https://localhost:8080/");
				String redriectUrlPath = new String("user/redirect?wrong=");
				String targetUrlPath = new String("root/grantComplete/unvalidatedredirectlesson?userId=");
				request.getSession().setAttribute("tempId", falseIdTestValue); //Test Value
				String messageForAdmin = new String(httpsBase+redriectUrlPath+httpsBase+targetUrlPath+falseIdTestValue);
				String servletResponse = getModuleDoPost(messageForAdmin, csrfToken);
				if(servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				if(servletResponse.contains("The result key for this lesson is"))
				{
					String message = new String("Invalid Solution returned Result Key");
					log.fatal(message);
					fail(message);
				}
				else if(!servletResponse.contains("Message Sent"))
				{
					String message = new String("Response did not contain expected hard coded message (Must have failed)");
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
	public void testLevelInvalidFirstUrlAnswer()
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
				String falseIdTestValue = new String("1627312");
				String httpsBase = new String("https://localhost:8080/");
				String redriectUrlPath = new String("user/redirectwrong?to=");
				String targetUrlPath = new String("root/grantComplete/unvalidatedredirectlesson?userId=");
				request.getSession().setAttribute("tempId", falseIdTestValue); //Test Value
				String messageForAdmin = new String(httpsBase+redriectUrlPath+httpsBase+targetUrlPath+falseIdTestValue);
				String servletResponse = getModuleDoPost(messageForAdmin, csrfToken);
				if(servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				if(servletResponse.contains("The result key for this lesson is"))
				{
					String message = new String("Invalid Solution returned Result Key");
					log.fatal(message);
					fail(message);
				}
				else if(!servletResponse.contains("Message Sent"))
				{
					String message = new String("Response did not contain expected hard coded message (Must have failed)");
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
	public void testLevelInvalidTempId()
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
				String falseIdTestValue = new String("1627312");
				String httpsBase = new String("https://localhost:8080/");
				String redriectUrlPath = new String("user/redirect?to=");
				String targetUrlPath = new String("root/grantComplete/unvalidatedredirectlesson?userId=");
				request.getSession().setAttribute("tempId", "differentValue"); //Test Value
				String messageForAdmin = new String(httpsBase+redriectUrlPath+httpsBase+targetUrlPath+falseIdTestValue);
				String servletResponse = getModuleDoPost(messageForAdmin, csrfToken);
				if(servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				if(servletResponse.contains("The result key for this lesson is"))
				{
					String message = new String("Invalid Solution returned Result Key");
					log.fatal(message);
					fail(message);
				}
				else if(!servletResponse.contains("Message Sent"))
				{
					String message = new String("Response did not contain expected hard coded message (Must have failed)");
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
	public void testLevelInvalidUrl()
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
				request.getSession().setAttribute("tempId", "anything"); //Test Value
				String messageForAdmin = new String("ftp:notavalidURLatall");
				String servletResponse = getModuleDoPost(messageForAdmin, csrfToken);
				if(!servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error NOT Detected");
					log.fatal(message);
					fail(message);
				}
				if(servletResponse.contains("The result key for this lesson is"))
				{
					String message = new String("Invalid Solution returned Result Key");
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
}
