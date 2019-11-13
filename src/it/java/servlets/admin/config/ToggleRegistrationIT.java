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
import utils.OpenRegistration;

public class ToggleRegistrationIT {
	private static org.apache.log4j.Logger log = Logger.getLogger(ToggleRegistrationIT.class);
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
		// Set Registration to Closed
		OpenRegistration.disable();
		if (OpenRegistration.isEnabled()) {
			String message = "Unable to disable Registration";
			log.fatal(message);
			fail(message);
		}
	}

	/**
	 * Method to Simulate the interaction with the Toggle Registration
	 * 
	 * @param csrfToken The CSRF Token of the User
	 * @return The Content of the Response (Which is supposed to be the location of
	 *         the module)
	 * @throws IOException 
	 * @throws ServletException 
	 * @throws Exception
	 */
	public String doThePost(String csrfToken) throws ServletException, IOException {
		String servletClassName = "ToggleRegistration";

		int expectedResponseCode = 302;

		log.debug("Creating " + servletClassName + " Servlet Instance");
		ToggleRegistration servlet = new ToggleRegistration();
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

	/**
	 * This test checks that non admin users get access errors when changing
	 * Registration
	 */
	@Test
	public void testUserToggle() {
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
				if (responseBody.contains("try non administrator functions")) {
					log.debug("Expected No Admin Access Result Recieved");
					if (OpenRegistration.isEnabled()) {
						String message = "Registration opened after what should have been a failed request";
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

	/**
	 * This test checks that admin users can change the registration
	 */
	@Test
	public void testAdminToggle() {
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
				if (responseBody.contains("The application Registration has been enabled")) {
					log.debug("The application Registration has been enabled");
					if (!OpenRegistration.isEnabled()) {
						String message = "Registration was not actually Enabled";
						log.fatal(message);
						fail(message);
					} else if (OpenRegistration.isDisabled()) {
						String message = "OpenRegistration still returning possitive for isDisabled() check";
						log.fatal(message);
						fail(message);
					} else {
						// Do the Request again to disable registration again
						responseBody = doThePost(csrfToken);
						if (responseBody.contains("The application Registration has been disabled")) {
							log.debug("The application Registration has been disabled");
							if (!OpenRegistration.isDisabled()) {
								String message = "Registration was not actually disabled";
								log.fatal(message);
								fail(message);
							} else if (OpenRegistration.isEnabled()) {
								String message = "OpenRegistration still returning possitive for isEnabled() check";
								log.fatal(message);
								fail(message);
							}
						} else {
							String message = "Admin was unable to disable Registration";
							log.fatal(message);
							fail(message);
						}
					}
				} else {
					String message = "Admin was unable to enable Registration";
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
				if (responseBody.contains("Registration Availability Update Failure")) {
					log.debug("CSRF Error Occurred");
					if (OpenRegistration.isEnabled()) {
						String message = "Registration was enabled after what should have been a failed request";
						log.fatal(message);
						fail(message);
					}
				} else {
					String message = "CSRF Error Not Detectedd with Bad CSRF Token";
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
