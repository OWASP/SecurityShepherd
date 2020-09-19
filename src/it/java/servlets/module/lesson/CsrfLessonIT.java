package servlets.module.lesson;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;

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

public class CsrfLessonIT {
	private static String lang = "en_GB";
	private static org.apache.log4j.Logger log = Logger.getLogger(CsrfLessonIT.class);
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
	public void setup() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		// Open All modules
		if (!Setter.openAllModules(applicationRoot, false))
			fail("Could not Mark All Modules As Open");
	}

	public String getModuleDoPost(String messageForAdmin, String csrfToken) throws ServletException, IOException {

		int expectedResponseCode = 302;
		String servletClassName = "CsrfLesson";
		log.debug("Creating " + servletClassName + " Servlet Instance");
		CsrfLesson servlet = new CsrfLesson();
		servlet.init(new MockServletConfig(servletClassName));

		// Setup Servlet Parameters and Attributes
		log.debug("Setting Up Params and Atrributes");
		request.addParameter("messageForAdmin", messageForAdmin);
		// Adding Correct CSRF Token (Token Submitted)
		request.addParameter("csrfToken", csrfToken);

		log.debug("Running doPost");
		servlet.doPost(request, response);

		if (response.getStatus() != expectedResponseCode)
			fail(servletClassName + " Servlet Returned " + response.getStatus() + " Code. " + expectedResponseCode
					+ " Expected");
		else {
			log.debug("302 OK Detected");
			log.debug(servletClassName + " Successful, returning location retrieved: " + response.getContentAsString());
			return (response.getContentAsString());
		}

		return null;
	}

	@Test
	public void testLevelValidAnswer() throws SQLException, ServletException, IOException {
		String userName = "lessonTester";

		// Verify User Exists in DB
		GetterTest.verifyTestUser(applicationRoot, userName, userName);
		// Sign in as Normal User
		log.debug("Signing in as " + userName + " Through LoginServlet");
		TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
		log.debug("Login Servlet Complete, Getting CSRF Token");
		if (response.getCookie("token") == null)
			fail("No CSRF Token Was Returned from Login Servlet");
		String csrfToken = response.getCookie("token").getValue();
		if (csrfToken.isEmpty()) {
			String message = new String("No CSRF token returned from Login Servlet");
			log.fatal(message);
			fail(message);
		} else {
			request.setCookies(response.getCookies());
			String falseIdTestValue = new String("1627312");
			String httpsBase = new String("https://localhost:8080/");
			request.getSession().setAttribute("falseId", falseIdTestValue); // Test Value
			String messageForAdmin = new String(httpsBase + "root/grantComplete/csrflesson?userId=" + falseIdTestValue);
			String servletResponse = getModuleDoPost(messageForAdmin, csrfToken);
			if (servletResponse.contains("You must be getting funky")) {
				String message = new String("General 'Funky' Error Detected");
				log.fatal(message);
				fail(message);
			}
			if (!servletResponse.contains("The result key for this lesson is")) {
				String message = new String("Valid Solution did not yeild Result Key");
				log.fatal(message);
				fail(message);
			} else if (!servletResponse.contains("administrator@SecurityShepherd.com")) // This is hardcoded in
																						// response. if it fails
																						// then the level isn't
																						// working for some reason
			{
				String message = new String("Unexpected CSRF Lesson Response");
				log.fatal(message);
				fail(message);
			}
		}

	}

	@Test
	public void testLevelInvalidAnswer() {
		String userName = "lessonTester";
		try {
			// Verify User Exists in DB
			GetterTest.verifyTestUser(applicationRoot, userName, userName);
			// Sign in as Normal User
			log.debug("Signing in as " + userName + " Through LoginServlet");
			TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
			log.debug("Login Servlet Complete, Getting CSRF Token");
			if (response.getCookie("token") == null)
				fail("No CSRF Token Was Returned from Login Servlet");
			String csrfToken = response.getCookie("token").getValue();
			if (csrfToken.isEmpty()) {
				String message = new String("No CSRF token returned from Login Servlet");
				log.fatal(message);
				fail(message);
			} else {
				request.setCookies(response.getCookies());
				String falseIdTestValue = new String("1627312");
				String httpsBase = new String("https://localhost:8080/");
				request.getSession().setAttribute("falseId", falseIdTestValue); // Test Value
				String messageForAdmin = new String(
						httpsBase + "root/grantComplete/csrflesson?userId=" + falseIdTestValue + "wrong"); // +wrong
																											// makes the
																											// value
																											// incorrect
				String servletResponse = getModuleDoPost(messageForAdmin, csrfToken);
				if (servletResponse.contains("You must be getting funky")) {
					String message = new String("General 'Funky' Error Detected");
					log.fatal(message);
					fail(message);
				}
				if (servletResponse.contains("The result key for this lesson is")) {
					String message = new String("Result Key Returned for incorrect submission");
					log.fatal(message);
					fail(message);
				} else if (!servletResponse.contains("administrator@SecurityShepherd.com")) // This is hardcoded in
																							// response. if it fails
																							// then the level isn't
																							// working for some reason
				{
					String message = new String("Unexpected CSRF Lesson Response");
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete: " + e.toString());
			fail("Could not Complete: " + e.toString());
		}
	}
}
