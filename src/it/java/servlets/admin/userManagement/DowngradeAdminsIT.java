package servlets.admin.userManagement;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import dbProcs.Getter;
import dbProcs.Setter;
import testUtils.TestProperties;

/**
 * This class just tests the DowngradeAdmin servlet code.
 */

public class DowngradeAdminsIT {
	private static org.apache.log4j.Logger log = Logger.getLogger(DowngradeAdminsIT.class);
	private static String applicationRoot = new String();
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private static String lang = "en_GB";

	@Before
	public void setUp() {
		TestProperties.setTestPropertiesFileDirectory(log);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	public String doThePost(String csrfToken, String testAdminId) throws Exception {

		int expectedResponseCode = 302;
		String moduleClassName = "DowngradeAdmin";
		log.debug("Creating " + moduleClassName + " Servlet Instance");
		DowngradeAdmin servlet = new DowngradeAdmin();
		servlet.init(new MockServletConfig(moduleClassName));

		// Adding Correct CSRF Token (Token Submitted)
		request.addParameter("csrfToken", csrfToken);
		request.addParameter("admin", testAdminId);

		log.debug("Running doPost");
		servlet.doPost(request, response);

		if (response.getStatus() != expectedResponseCode)
			fail(moduleClassName + " Servlet Returned " + response.getStatus() + " Code. " + expectedResponseCode
					+ " Expected");
		else {
			log.debug(expectedResponseCode + " Detected");
			log.debug("Servlet Successful, returning response retrieved: " + response.getContentAsString());
			return (response.getContentAsString());
		}

		return null;
	}

	@Test
	public void testWithUserAuth() throws Exception {
		String userName = "configUserTester";
		String password = userName;
		// Verify / Create user in DB

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
			// Add Cookies from Response to outgoing request
			request.setCookies(response.getCookies());
			String responseBody = doThePost(csrfToken, new String());
			if (responseBody.contains("Please try non administrator functions")) {
				log.debug("No Admin Access Result Received");
			} else {
				String message = "Did not get authorisation error for User accessing Admin Function";
				log.fatal(message);
				fail(message);
			}
		}

	}

	@Test
	public void testWithAdminAuth() throws Exception {
		String userName = "configAdminTester";
		String password = userName;
		// Verify / Create user in DB

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
			String responseBody = doThePost(csrfToken, new String());
			if (responseBody.contains("Admin(s) Not Found")) {
				log.debug("Admin(s) Not Found Message Received");
			} else {
				String message = "Admin user unable to downgrade to player";
				log.fatal(message);
				fail(message);
			}
		}

	}

	@Test
	public void testWithAdminAuthValidUser() throws Exception {
		String userName = "configAdminTester";
		String password = userName;
		// Verify / Create user in DB

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

			String testUsername = "testAdminDowngradeServlet";
			String testuserId = Getter.getUserIdFromName(applicationRoot, testUsername);

			if (testuserId == null || testuserId.isEmpty()) {
				assert (Setter.userCreate(applicationRoot, null, testUsername, testUsername, "admin",
						testUsername + "@test.com", false));
				testuserId = Getter.getUserIdFromName(applicationRoot, testUsername);
			} else {
				Setter.updateUserRole(applicationRoot, testuserId, "admin"); // make sure user is admin, so
																				// downgrading makes sense
			}
			assert (testuserId != null && !testuserId.isEmpty());

			String responseBody = doThePost(csrfToken, testuserId);
			if (responseBody.contains("downgraded successfully to player")) {
				log.debug("Admin user downgrade successful Message Received");
			} else {
				String message = "Failed to downgrade admin to player";
				log.fatal(message);
				fail(message);
			}
		}

	}

	@Test
	public void testCsrf() throws Exception {
		String userName = "configAdminTester";
		String password = userName;
		// Verify / Create user in DB

		TestProperties.verifyTestAdmin(log, applicationRoot, userName, password);
		// Sign in as Admin User
		log.debug("Signing in as Admin Through LoginServlet");
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
			String responseBody = doThePost("wrongToken", new String());
			if (responseBody.contains("CSRF Tokens Did Not Match")) {
				log.debug("CSRF Error Occurred");
			} else {
				String message = "CSRF Error Not Detected with Bad CSRF Token";
				log.fatal(message);
				fail(message);
			}
		}

	}
}