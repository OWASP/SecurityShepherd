package servlets.admin.moduleManagement;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

/**
 * This class just tests the servlet code. The Setter code is better tested in
 * the SetterTest test's
 * 
 * @author Mark Denihan
 *
 */
public class GetFeedbackIT {
	private static org.apache.log4j.Logger log = Logger.getLogger(GetFeedbackIT.class);
	private static String applicationRoot = new String();
	private static String lang = "en_GB";
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
	public void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	public String doThePost(String csrfToken, String moduleId) throws ServletException {

		int expectedResponseCode = 302;
		String moduleClassName = "GetFeedback";
		log.debug("Creating " + moduleClassName + " Servlet Instance");
		GetFeedback servlet = new GetFeedback();
		servlet.init(new MockServletConfig(moduleClassName));
		request.addParameter("moduleId", moduleId);
		// Adding Correct CSRF Token (Token Submitted)
		request.addParameter("csrfToken", csrfToken);

		log.debug("Running doPost");
		try {
			servlet.doPost(request, response);
		} catch (IOException e) {
			TestProperties.failAndPrint("Could not post Servlet: " + e.toString());
		}

		if (response.getStatus() != expectedResponseCode)
			TestProperties.failAndPrint(moduleClassName + " Servlet Returned " + response.getStatus() + " Code. "
					+ expectedResponseCode + " Expected");
		else {
			log.debug("Received the expected response code " + expectedResponseCode);
			String responseText = "";

			try {
				responseText = response.getContentAsString();
			} catch (UnsupportedEncodingException e) {
				TestProperties.failAndPrint(
						"Encountered an unsupported encoding when interpreting response: " + e.toString());
			}

			log.debug("Servlet Successful, returning response retrieved: " + responseText);
			return (responseText);
		}

		return null;
	}

	/**
	 *
	 */
	@Test
	public void testWithUserAuth() {
		String userName = "configUserTester";
		String password = userName;
		// Verify / Create user in DB
		try {
			TestProperties.verifyTestUser(log, applicationRoot, userName, password);
			// Sign in as Normal User
			log.debug("Signing in as Normal User Through LoginServlet");
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
				String moduleId = "853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"; // CSRF 7
				// Add Cookies from Response to outgoing request
				request.setCookies(response.getCookies());
				String responseBody = doThePost(csrfToken, moduleId);
				if (responseBody.isEmpty()) {
					log.debug("No Admin Access Result Recieved");
				} else {
					String message = "Did not get authoristion error for User accessing Admin Function";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete: " + e.toString());
			fail("Could not Complete: " + e.toString());
		}
	}

	@Test
	public void testWithAdminAuth() throws SQLException {
		// Step One, Make sure there is feedback to be collected
		String feedbackUser = "testGetFeedbackServlet";
		String feedbackModule = "53a53a66cb3bf3e4c665c442425ca90e29536edd"; // directobref1
		String feedbackString = "19812d82CcustomFeedback192128";
		if (TestProperties.completeModuleForUser(log, feedbackUser, feedbackUser, feedbackModule, feedbackString,
				applicationRoot)) {
			String userName = "configAdminTester";
			String password = userName;
			// Verify / Create user in DB
			try {
				TestProperties.verifyTestAdmin(log, applicationRoot, userName, password);
				// Sign in as Normal User
				log.debug("Signing in as Admin User Through LoginServlet");
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
					// Add Cookies from Response to outgoing request
					request.setCookies(response.getCookies());
					String responseBody = doThePost(csrfToken, feedbackModule);
					if (responseBody.isEmpty() || responseBody.equalsIgnoreCase("Error Occurred!")) {
						String message = "Module Feedback collection Failed";
						log.fatal(message);
						fail(message);
					} else {
						log.debug("Got Feedback");
					}
				}
			} catch (Exception e) {
				log.fatal("Could not Complete: " + e.toString());
				fail("Could not Complete: " + e.toString());
			}
		}
		// else the method already threw a fail so no need to do anything here
	}

	@Test
	public void testWithAdminAuthNoResult() {
		String userName = "configAdminTester";
		String password = userName;
		// Verify / Create user in DB
		try {
			TestProperties.verifyTestAdmin(log, applicationRoot, userName, password);
			// Sign in as Normal User
			log.debug("Signing in as Admin User Through LoginServlet");
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
				// Add Cookies from Response to outgoing request
				request.setCookies(response.getCookies());
				String responseBody = doThePost(csrfToken, "wrongForNoResult");
				if (responseBody.isEmpty() || responseBody.equalsIgnoreCase("Error Occurred!")) {
					String message = "Module Feedback collection Failed";
					log.fatal(message);
					fail(message);
				} else {
					if (responseBody.equals("No Feedback Found!")) {
						log.debug("No Feedback Result Returned");
					} else {
						String message = "Did not get No Feedback Error";
						log.fatal(message);
						fail(message);
					}
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete: " + e.toString());
			fail("Could not Complete: " + e.toString());
		}
	}

	@Test
	public void testCSRF() throws SQLException, ServletException {
		String userName = "configAdminTester";
		String password = "adminTesterPassword";
		// Verify / Create user in DB

		TestProperties.verifyTestAdmin(log, applicationRoot, userName, password);
		// Sign in as Admin User
		log.debug("Signing in as Admin Through LoginServlet");
		TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
		log.debug("Login Servlet Complete, Getting CSRF Token");
		if (response.getCookie("token") == null)
			TestProperties.failAndPrint("No CSRF Token Was Returned from Login Servlet");
		String csrfToken = response.getCookie("token").getValue();
		if (csrfToken.isEmpty()) {
			TestProperties.failAndPrint("No CSRF token returned from Login Servlet");
		} else {
			String moduleId = "853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"; // CSRF 7
			// Add Cookies from Response to outgoing request
			request.setCookies(response.getCookies());
			String responseBody = doThePost("wrongToken", moduleId);
			if (responseBody.equalsIgnoreCase("Error Occurred!")) {
				log.debug("PASS: CSRF Error Occurred when incorrect CSRF token was supplied");
			} else {
				TestProperties.failAndPrint("CSRF Error Not Detected with Bad CSRF Token");
			}
		}

	}
}
