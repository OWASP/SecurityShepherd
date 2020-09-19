package servlets.module.challenge;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import dbProcs.GetterTest;
import dbProcs.Setter;
import testUtils.TestProperties;

public class NoSqlInjection1IT extends Mockito {

	private static String applicationRoot = new String();
	private static String USERNAME = "lessonTester";
	private static String LANG = "en_GB";
	private static String NOSQL_ATTACK = "';return(true);var a='a";

	private static org.apache.log4j.Logger log = Logger.getLogger(NoSqlInjection1IT.class);

	@Mock
	private MockHttpServletRequest request;

	@Mock
	private MockHttpServletResponse response;

	/**
	 * Creates DB or Restores DB to Factory Defaults before running tests
	 */
	@BeforeClass
	public static void resetDatabase() throws IOException, SQLException {
		TestProperties.setTestPropertiesFileDirectory(log);

		TestProperties.createMysqlResource();
		TestProperties.createMongoResource();

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

	public String moduleDoPost(String theSubmission, String csrfToken, int expectedResponseCode)
			throws ServletException, IOException {

		String servletClassName = "NoSqlInjection1";
		log.debug("Creating " + servletClassName + " Servlet Instance");
		NoSqlInjection1 servlet = new NoSqlInjection1();
		servlet.init(new MockServletConfig(servletClassName));

		// Setup Servlet Parameters and Attributes
		log.debug("Setting Up Params and Atrributes");
		request.addParameter("theGamerName", theSubmission);
		// Adding Correct CSRF Token (Token Submitted)
		request.addParameter("csrfToken", csrfToken);

		if (request == null) {
			log.debug("Request is null");
		}
		if (response == null) {
			log.debug("Request is null");
		}

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
	public void testLevelValidAnswer() throws Exception {

		GetterTest.verifyTestUser(applicationRoot, USERNAME, USERNAME);
		log.debug("Signing in as " + USERNAME + " Through LoginServlet");
		TestProperties.loginDoPost(log, request, response, USERNAME, USERNAME, null, LANG);
		log.debug("Login Servlet Complete, Getting CSRF Token");
		if (response.getCookie("token") == null)
			fail("No CSRF Tokena Was Returned from Login Servlet");
		String csrfToken = response.getCookie("token").getValue();
		if (csrfToken.isEmpty()) {
			String message = new String("No CSRF token returned from Login Servlet");
			log.fatal(message);
			fail(message);
		} else {
			request.setCookies(response.getCookies());
			String servletResponse = moduleDoPost(NOSQL_ATTACK, csrfToken, 302);
			if (servletResponse.contains("An error was detected")) {
				String message = new String("Valid Key Returned Funky Error");
				log.fatal(message);
				fail(message);
			} else if (!servletResponse.contains("Marlo</td><td>Baltimore</td></tr><tr><td>b6c02")) {
				String message = new String("Valid Solution did not return Result Key");
				log.fatal(message);
				fail(message);
			}
		}

	}

}