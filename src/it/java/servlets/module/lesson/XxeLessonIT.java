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
import utils.Validate;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class XxeLessonIT
{
	private static final String LANGUAGE_CODE = "en_GB";
	private static Logger log = Logger.getLogger(XxeLessonIT.class);
	private static String applicationRoot = new String();
	private MockHttpServletRequest request;
    private MockHttpServletResponse response;
	private static final String MODULE_CLASS_NAME = "XxeLesson";
	private static String TEST_USERNAME = "lessonTester";
	private static Locale enLocale;
	private static ResourceBundle errors;

	/**
	 * Creates DB or Restores DB to Factory Defaults before running tests
	 */
	@BeforeClass
	public static void before()
	{
		enLocale = Locale.forLanguageTag(LANGUAGE_CODE);
		errors = ResourceBundle.getBundle("i18n.servlets.errors", enLocale);
		TestProperties.setTestPropertiesFileDirectory(log);
		try 
		{
			TestProperties.executeSql(log);
			TestProperties.createFileSystemKey(log, "xxe.lesson.file", "xxe.lesson.solution");
			GetterTest.verifyTestUser(applicationRoot, TEST_USERNAME, TEST_USERNAME);
		} 
		catch (InstallationException e) 
		{
			String message = "Could not create DB: " + e.toString();
			log.fatal(message);
			fail(message);
		}
		catch (Exception e) { log.fatal("Could not initialise test"); fail(e.toString()); }
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

	public String doMockPost(byte[] xmlEmail, String csrfToken, int expectedResponseCode) throws Exception
	{
		log.debug("Creating " + MODULE_CLASS_NAME + " Servlet Instance");

		try
		{
			XxeLesson servlet = new XxeLesson();
			servlet.init(new MockServletConfig(MODULE_CLASS_NAME));

			request.setContentType("application/xml");
			log.debug("Setting Up Params and Atrributes");
			request.setContent(xmlEmail); //for an XML payload use setContent instead of addParameter
			//Adding Correct CSRF Token (Token Submitted)
			request.addHeader("csrfToken", csrfToken);

			log.debug("Running doPost");
			servlet.doPost(request, response);

			assertFalse(response.getStatus() != expectedResponseCode);

			return(response.getContentAsString());
		}
		catch(Exception e) { fail(e.toString()); throw e; }
	}

	@Test
	public void testLevelValidAnswerXxeInjection()
	{
		Setter.openAllModules(applicationRoot, true);
		try
		{
			//Sign in as Normal User
			log.debug("Signing in as " + TEST_USERNAME + " Through LoginServlet");
			TestProperties.loginDoPost(log, request, response, TEST_USERNAME, TEST_USERNAME, null, LANGUAGE_CODE);
			log.debug("Login Servlet Complete, Getting CSRF Token");

			String csrfToken = response.getCookie("token").getValue();

			assertFalse(csrfToken.isEmpty());

			request.setCookies(response.getCookies());
			String xxeString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
						+ "<!DOCTYPE foo ["
						+ "<!ELEMENT foo ANY >"
						+ "<!ENTITY xxe SYSTEM \"file://" + System.getProperty("user.dir")
						+ "/" + FileInputProperties.readfile(System.getProperty("user.dir")
						+ "/src/main/resources/fileSystemKeys.properties", "xxe.lesson.file") + "\""
						+ " >]><foo>&xxe;</foo>";

			//String xxeString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><email>test@test.com</email>";

			String servletResponse = doMockPost(xxeString.getBytes(), csrfToken, 302);

			log.debug("Servlet Response: " + servletResponse);
			Assert.assertTrue(servletResponse.contains("Marsellus Wallace&#39;s Key"));
			Assert.assertFalse(servletResponse.contains("You must be getting funky"));

		}
		catch(Exception e)
		{
			log.fatal("Could not Complete: " + e.toString());
			fail("Could not Complete: " + e.toString());
		}
	}

	@Test
	public void testLevelWhenUnsafeLevelsAreDisabled()
	{
		Setter.closeAllModules(applicationRoot);
		Setter.openAllModules(applicationRoot, false);

		try
		{
			//Sign in as Normal User
			log.debug("Signing in as " + TEST_USERNAME + " Through LoginServlet");
			TestProperties.loginDoPost(log, request, response, TEST_USERNAME, TEST_USERNAME, null, LANGUAGE_CODE);
			log.debug("Login Servlet Complete, Getting CSRF Token");

			String csrfToken = response.getCookie("token").getValue();

			assertFalse(csrfToken.isEmpty());

			request.setCookies(response.getCookies());
			String xxeString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
					+ "<!DOCTYPE foo ["
					+ "<!ELEMENT foo ANY >"
					+ "<!ENTITY xxe SYSTEM \"file://" + System.getProperty("user.dir")
					+ "/" + FileInputProperties.readfile(System.getProperty("user.dir")
					+ "/src/main/resources/fileSystemKeys.properties", "xxe.lesson.file") + "\""
					+ " >]><foo>&xxe;</foo>";

			//String xxeString = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><email>test@test.com</email>";

			String servletResponse = doMockPost(xxeString.getBytes(), csrfToken, 302);

			log.debug("Servlet Response: " + servletResponse);
			Assert.assertFalse(servletResponse.contains("Marsellus Wallace&#39;s Key"));
			Assert.assertTrue(servletResponse.contains(errors.getString("error.notOpen")));

		}
		catch(Exception e)
		{
			log.fatal("Could not Complete: " + e.toString());
			fail("Could not Complete: " + e.toString());
		}
	}



}
