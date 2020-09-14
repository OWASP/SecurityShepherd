package servlets.admin.moduleManagement;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import testUtils.TestProperties;

import utils.InstallationException;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class OpenAllModulesIT {

	private static org.apache.log4j.Logger log = Logger.getLogger(OpenAllModulesIT.class);
	private static final String LANGUAGE_CODE = "en_GB";
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private static String[] testUsers = { "configAdminTester", "configUserTester", "unauthenticatedUser" };
	private static final String MODULE_CLASS_NAME = "OpenAllModules";

	/**
	 * Sets up DB with levels and users to test with
	 * @throws InstallationException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	@BeforeClass
	public static void readyDb() throws SQLException, IOException {
		testUtils.TestProperties.setTestPropertiesFileDirectory(log);

		testUtils.TestProperties.executeSql(log);
		log.debug("Creating - user: " + testUsers[0] + " password: " + testUsers[0]);
		TestProperties.verifyTestAdmin(log, null, testUsers[0], testUsers[0]);
		log.debug("Creating - user: " + testUsers[1] + " password: " + testUsers[1]);
		TestProperties.verifyTestUser(log, null, testUsers[1], testUsers[0]);

	}

	@Before
	public void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();

	}

	private String doMockPost(String csrfToken, String unsafe) {
		int expectedResponseCode = 302;

		log.debug("Creating " + MODULE_CLASS_NAME + " Servlet Instance");

		OpenAllModules servlet = new OpenAllModules();
		try {
			servlet.init(new MockServletConfig(MODULE_CLASS_NAME));
		} catch (ServletException e) {
			TestProperties.failAndPrint(
					"Could not initialize Servlet Instance for module " + MODULE_CLASS_NAME + ": " + e.toString());

		}

		// Adding Correct CSRF Token (Token Submitted)
		request.addParameter("csrfToken", csrfToken);
		request.addParameter("unsafeLevels", unsafe);

		log.debug("Running doPost");
		try {
			servlet.doPost(request, response);
		} catch (ServletException | IOException e) {
			TestProperties.failAndPrint("Could not post Servlet: " + e.toString());

		}

		assertFalse(response.getStatus() != expectedResponseCode);

		String result = "";
		try {
			result = response.getContentAsString();
		} catch (UnsupportedEncodingException e) {
			TestProperties.failAndPrint("Could not get Servlet response: " + e.toString());

		}

		return (result);

	}

	@Test
	public void testWithUserAuth() {

		log.debug("Signing in as Normal User Through LoginServlet");
		testUtils.TestProperties.loginDoPost(log, request, response, testUsers[1], testUsers[1], null, LANGUAGE_CODE);
		log.debug("Login Servlet Complete, Getting CSRF Token");

		String csrfToken = response.getCookie("token").getValue();
		assertFalse(csrfToken.isEmpty());

		// Add Cookies from Response to outgoing request
		request.setCookies(response.getCookies());
		String responseBody = doMockPost(csrfToken, "enable");

		assertTrue(responseBody.contains("loggedOutSheep"));

	}

	@Test
	public void testWithAdminAuthUnsafeLevelsClosed() {
		String unsafe = "disable";

		log.debug("Signing in as Admin User Through LoginServlet");
		TestProperties.loginDoPost(log, request, response, testUsers[0], testUsers[0], null, LANGUAGE_CODE);
		log.debug("Login Servlet Complete, Getting CSRF Token");

		String csrfToken = response.getCookie("token").getValue();
		assertFalse(csrfToken.isEmpty());

		// Add Cookies from Response to outgoing request
		request.setCookies(response.getCookies());
		String responseBody = doMockPost(csrfToken, unsafe);

		log.debug("Response Body: " + responseBody);

		assertTrue(responseBody.contains("All Modules are Now Open"));

	}

	@Test
	public void testWithAdminAuthUnsafeLevelsOpen() {
		String unsafe = "enable";

		log.debug("Signing in as Admin User Through LoginServlet");
		TestProperties.loginDoPost(log, request, response, testUsers[0], testUsers[0], null, LANGUAGE_CODE);
		log.debug("Login Servlet Complete, Getting CSRF Token");

		String csrfToken = response.getCookie("token").getValue();
		assertFalse(csrfToken.isEmpty());

		// Add Cookies from Response to outgoing request
		request.setCookies(response.getCookies());
		String responseBody = doMockPost(csrfToken, unsafe);

		assertTrue(responseBody.contains("[WARNING] Server is vulnerable. Unsafe levels open!"));
		assertTrue(responseBody.contains("All Modules are Now Open"));

	}

	@Test
	public void testWithUnauthenticatedUser() {
		String unsafe = "enable";

		log.debug("Signing in as Admin User Through LoginServlet");
		TestProperties.loginDoPost(log, request, response, testUsers[2], testUsers[2], null, LANGUAGE_CODE);
		log.debug("Login Servlet Complete, Getting CSRF Token");

		// Add Cookies from Response to outgoing request
		request.setCookies(response.getCookies());
		String responseBody = doMockPost("test", unsafe);
		assertTrue(responseBody.contains("loggedOutSheep"));

	}

}