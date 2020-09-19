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

public class XssLessonIT
{
	private static String lang = "en_GB";
	private static org.apache.log4j.Logger log = Logger.getLogger(XssLessonIT.class);
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
        if(!Setter.openAllModules(applicationRoot, false) && !Setter.openAllModules(applicationRoot, true))
        	fail("Could not Mark All Modules As Open");
	}

	public String getModuleDoPost(String searchTerm, String csrfToken, int expectedResponseCode) throws Exception
	{
		try
		{
			String servletClassName = "XssLesson";
			log.debug("Creating " + servletClassName + " Servlet Instance");
			XssLesson servlet = new XssLesson();
			servlet.init(new MockServletConfig(servletClassName));

			//Setup Servlet Parameters and Attributes
			log.debug("Setting Up Params and Atrributes");
			request.addParameter("searchTerm", searchTerm);
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
	public void testLevelValidAnswerScriptTags()
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
				String xssString = "<script>alert(1)</script>";
				String servletResponse = getModuleDoPost(xssString, csrfToken, 302);
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
				if(!servletResponse.contains(xssString))
				{
					String message = new String("Xss String Not Reflected in Response");
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
	public void testLevelValidAnswerImgTags()
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
				String xssString = "<img src='#' onerror='alert(1)'>";
				String servletResponse = getModuleDoPost(xssString, csrfToken, 302);
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
				if(!servletResponse.contains(xssString))
				{
					String message = new String("Xss String Not Reflected in Response");
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
	public void testLevelValidAnsweriFrameTags()
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
				String xssString = "<iframe src='javascript:alert(1);//'></iframe>";
				String servletResponse = getModuleDoPost(xssString, csrfToken, 302);
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
				if(!servletResponse.contains(xssString))
				{
					String message = new String("Xss String Not Reflected in Response");
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
	public void testLevelValidAnswerAnchorTags()
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
				String xssString = "<a href='javascript:alert(1);//'>click me</a>";
				String servletResponse = getModuleDoPost(xssString, csrfToken, 302);
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
				if(!servletResponse.contains(xssString))
				{
					String message = new String("Xss String Not Reflected in Response");
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
	public void testLevelInvalidAnswer()
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
				String xssString = "<p>NotXSSAtAll</p>";
				String servletResponse = getModuleDoPost(xssString, csrfToken, 302);
				if(servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				if(servletResponse.contains("The result key for this lesson is"))
				{
					String message = new String("Invalid Solution Returnedd Result Key");
					log.fatal(message);
					fail(message);
				} 
				if(!servletResponse.contains(xssString))
				{
					String message = new String("Xss String Not Reflected in Response");
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
	public void testLevelInvalidAnswerIncompleteScript()
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
				String xssString = "<script>alert(1)";
				String servletResponse = getModuleDoPost(xssString, csrfToken, 302);
				if(servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				if(servletResponse.contains("The result key for this lesson is"))
				{
					String message = new String("Invalid Solution Returnedd Result Key");
					log.fatal(message);
					fail(message);
				} 
				if(!servletResponse.contains(xssString))
				{
					String message = new String("Xss String Not Reflected in Response");
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
	public void testLevelInvalidCsrfToken()
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
				String xssString = "<script>alert(1)";
				String servletResponse = getModuleDoPost(xssString, "wrongCsrfToken", 302);
				if(servletResponse.contains("You must be getting funky")) 
				{
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				if(servletResponse.contains("The result key for this lesson is"))
				{
					String message = new String("Invalid Solution Returnedd Result Key");
					log.fatal(message);
					fail(message);
				} 
				if(servletResponse.contains(xssString))
				{
					String message = new String("Xss String Reflected in Response with bad CSRF Token");
					log.fatal(message);
					fail(message);
				}
				if(!servletResponse.isEmpty())
				{
					String message = new String("CSRF Error should be a blank response. It wasn't");
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
			String servletResponse = getModuleDoPost("Anything", "Anything", 200); //Mock response is 200 for Unauthenticated response for some reason
			if(servletResponse.contains("You must be getting funky")) 
			{
				String message = new String("General 'Funky' Error Detected");
				log.fatal(message);
				fail(message);
			}
			else if(!servletResponse.contains("No session was detected with your request. Are you signed in?"))
			{
				String message = new String("Did not get 'Unauthenticated' Response");
				log.fatal(message);
				fail(message);
			} 
			else if(servletResponse.contains("Results")) //This string is in all possible responses except unauthenticated
			{
				String message = new String("Appears that an Unauthticated Response Worked");
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
