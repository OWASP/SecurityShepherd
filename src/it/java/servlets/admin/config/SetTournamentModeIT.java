package servlets.admin.config;

import static org.junit.Assert.*;

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
import utils.ModulePlan;

public class SetTournamentModeIT {
	private static org.apache.log4j.Logger log = Logger.getLogger(SetTournamentModeIT.class);
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
		// Set Module Plan to CTF Floor
		ModulePlan.setIncrementalFloor();
		if (!ModulePlan.isIncrementalFloor()) {
			String message = "Unable to Set floor plan to CTF";
			log.fatal(message);
			fail(message);
		}
	}

	/**
	 * Method to Simulate the interaction with the Set Tournament Mode Servlet
	 * 
	 * @param moduleId  The ID of the Module to Search For
	 * @param csrfToken The CSRF Token of the User
	 * @return The Content of the Response (Which is supposed to be the location of
	 *         the module)
	 * @throws ServletException 
	 * @throws IOException 
	 * @throws Exception
	 */
	public String doThePost(String csrfToken) throws ServletException, IOException {
		String servletClassName = "SetTournamentMode";

		int expectedResponseCode = 302;

		log.debug("Creating " + servletClassName + " Servlet Instance");
		SetTournamentMode servlet = new SetTournamentMode();
		servlet.init(new MockServletConfig(servletClassName));

		// Adding Correct CSRF Token (Token Submitted)
		request.addParameter("csrfToken", csrfToken);

		log.debug("Running doPost");
		servlet.doPost(request, response);

		if (response.getStatus() != expectedResponseCode)
			fail(servletClassName + " Servlet Returned " + response.getStatus() + " Code. " + expectedResponseCode
					+ " Expected");
		else {
			log.debug(expectedResponseCode + " Detected");
			log.debug("Servlet Successful, returning response retrieved: " + response.getContentAsString());
			return (response.getContentAsString());
		}
		return null;
	}

	@Test
	public void testUserEnableTournamentMode() {
		String userName = "configUserTester";
		String password = userName;
		// Verify / Create user in DB
		try {
			TestProperties.verifyTestUser(log, applicationRoot, userName, password);
			// Sign in as Normal User
			log.debug("Signing in as User Through LoginServlet");
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
				String responseBody = doThePost(csrfToken);
				if (responseBody.isEmpty()) {
					log.debug("Expected Empty Result Recieved");
					if (ModulePlan.isTournamentFloor()) {
						String message = "Floor Plan Changed to Tournament after what should have been a failed request";
						log.fatal(message);
						fail(message);
					}
				} else {
					String message = "Normal user did not get error when performing admin function";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete: " + e.toString());
			fail("Exception Caught: " + e.toString());
		}
	}

	@Test
	public void testAdminSetTournamentMode() {
		String userName = "configAdminTester";
		String password = userName;
		// Verify / Create user in DB
		try {
			TestProperties.verifyTestAdmin(log, applicationRoot, userName, password);
			// Sign in as Admin User
			log.debug("Signing in as User Through LoginServlet");
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
				String responseBody = doThePost(csrfToken);
				if (responseBody.contains("Tournament Floor Plan Enabled")) {
					log.debug("Tournament Mode enabled");
					if (!ModulePlan.isTournamentFloor()) {
						String message = "Tournament Mode was not actually Enabled";
						log.fatal(message);
						fail(message);
					} else if (ModulePlan.isIncrementalFloor()) {
						String message = "ModulePlan still returning possitive for CTF Mode Check";
						log.fatal(message);
						fail(message);
					}
				} else {
					String message = "Admin was unable to enable Tournament mode";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete: " + e.toString());
			fail("Exception Caught: " + e.toString());
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
			log.debug("Signing in as User Through LoginServlet");
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
				String responseBody = doThePost("wrongToken");
				if (responseBody.contains("Error Occurred")) {
					log.debug("CSRF Error Occurred");
					if (ModulePlan.isTournamentFloor()) {
						String message = "Tournament Mode was enabled after what should have been a failed request";
						log.fatal(message);
						fail(message);
					} else if (!ModulePlan.isIncrementalFloor()) {
						String message = "ModulePlan no longer returning possitive for CTF Floor Check after what should have been a failed request";
						log.fatal(message);
						fail(message);
					}
				} else {
					String message = "No CSRF Failure Occurred";
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
