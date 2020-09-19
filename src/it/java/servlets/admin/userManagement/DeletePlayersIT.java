package servlets.admin.userManagement;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;

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
 * This class just tests the servlet code. The Setter code is better tested in
 * the SetterTest test's
 * 
 * @author Cosmin Craciun credit to Mark Denihan
 */
public class DeletePlayersIT {
	private static org.apache.log4j.Logger log = Logger.getLogger(DeletePlayersIT.class);
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

	public String doThePost(String csrfToken, String testuserId) throws ServletException, IOException {

		int expectedResponseCode = 302;
		String moduleClassName = "DeletePlayers";
		log.debug("Creating " + moduleClassName + " Servlet Instance");
		DeletePlayers servlet = new DeletePlayers();
		servlet.init(new MockServletConfig(moduleClassName));

		// Adding Correct CSRF Token (Token Submitted)
		request.addParameter("csrfToken", csrfToken);
		request.addParameter("player", testuserId);

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
	public void testWithUserAuth() throws SQLException, ServletException, IOException {
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
				log.debug("No Admin Access Result Recieved");
			} else {
				String message = "Did not get authoristion error for User accessing Admin Function";
				log.fatal(message);
				fail(message);
			}
		}
	}

	@Test
	public void testWithAdminAuth() throws SQLException, ServletException, IOException {
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
			if (responseBody.contains("Player(s) Not Found")) {
				log.debug("Player(s) Not Found Message Recieved");
			} else {
				String message = "Admin unable to use delete player";
				log.fatal(message);
				fail(message);
			}
		}

	}

	@Test
	public void testWithAdminAuthValidUser() throws SQLException, ServletException, IOException {
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

			String testUsername = "testuserdeleteservlet";
			String testuserId = Getter.getUserIdFromName(applicationRoot, testUsername);

			if (testuserId == null || testuserId.isEmpty()) {
				assert (Setter.userCreate(applicationRoot, null, testUsername, testUsername, "player",
						testUsername + "@test.com", false));
				testuserId = Getter.getUserIdFromName(applicationRoot, testUsername);
			}
			assert (testuserId != null && !testuserId.isEmpty());

			String responseBody = doThePost(csrfToken, testuserId);
			if (responseBody.contains("User deleted successfully")) {
				log.debug("User deleted successfully Message Recieved");
			} else {
				String message = "Admin unable to use delete player";
				log.fatal(message);
				fail(message);
			}
		}
	}

	@Test
	public void testCsrf() {
		String userName = "configAdminTester";
		String password = userName;
		// Verify / Create user in DB
		try {
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
		} catch (Exception e) {
			log.fatal("Could not Complete: " + e.toString());
			fail("Exception Caught: " + e.toString());
		}
	}
}