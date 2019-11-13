package servlets.admin.config;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
import utils.CheatSheetStatus;

public class DisableCheatsIT {
	private static org.apache.log4j.Logger log = Logger.getLogger(DisableCheatsIT.class);
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
		// Enable Cheats before Each test
		CheatSheetStatus.enableForAll();
		if (!CheatSheetStatus.isEnabledAtAll()) {
			log.fatal("Unable to Enable Cheat Sheets");
			fail("Unable to Enable Cheat Sheets");
		}
	}

	/**
	 * Method to Simulate the interaction with the disableCheats servlet.
	 * 
	 * @param moduleId  The ID of the Module to Search For
	 * @param csrfToken The CSRF Token of the User
	 * @return The Content of the Response (Which is supposed to be the location of
	 *         the module)
	 * @throws ServletException
	 * 
	 */
	public String doThePost(String csrfToken) throws ServletException {
		String servletClassName = "DisableCheats";

		int expectedResponseCode = 302;

		log.debug("Creating " + servletClassName + " Servlet Instance");
		DisableCheats servlet = new DisableCheats();
		servlet.init(new MockServletConfig(servletClassName));

		// Adding Correct CSRF Token (Token Submitted)
		request.addParameter("csrfToken", csrfToken);

		log.debug("Running doPost");
		try {
			servlet.doPost(request, response);
		} catch (IOException e) {
			TestProperties.failAndPrint("Could not post Servlet: " + e.toString());
		}
		if (response.getStatus() != expectedResponseCode)
			TestProperties.failAndPrint(servletClassName + " Servlet Returned " + response.getStatus() + " Code. " + expectedResponseCode
					+ " Expected");
		else {
			log.debug("Received the expected response code " + expectedResponseCode);
			String responseText = "";

			try {
				responseText = response.getContentAsString();
			} catch (UnsupportedEncodingException e) {
				TestProperties.failAndPrint(
						"Encountered an unsupported encoding when interpreting response: " + e.toString());
			}

			assertNotEquals(responseText, "");

			log.debug("Servlet Successful, returning response retrieved: " + responseText);
			return (responseText);
		}

		return null;
	}

	/**
	 * This test checks that non admin users get access errors when disabling the
	 * cheats
	 */
	@Test
	public void testUserDisableCheatsCall() {
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
				if (responseBody.contains("loggedOutSheep")) {
					log.debug("No Admin Access Expected Result Recieved");
					if (!CheatSheetStatus.isEnabledAtAll()) {
						String message = "Cheat Sheets disabled in what should have been a failed request";
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
	 * This test checks that admin users can disable the cheats
	 */
	@Test
	public void testAdminCompleteDisableCheatsCall() {
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
				if (responseBody.contains("Cheat Sheets Disabled")) {
					log.debug("Cheat Sheets Were Disabled");
					if (CheatSheetStatus.isEnabledAtAll()) {
						String message = "Cheat Sheets were not actually Disabled";
						log.fatal(message);
						fail(message);
					}
				} else {
					String message = "Admin was unable to disable cheat sheets";
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
