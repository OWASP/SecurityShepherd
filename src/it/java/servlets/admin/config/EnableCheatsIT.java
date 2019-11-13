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
import utils.CheatSheetStatus;

public class EnableCheatsIT {
	private static org.apache.log4j.Logger log = Logger.getLogger(EnableCheatsIT.class);
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
		CheatSheetStatus.disableForAll();
		if (CheatSheetStatus.isEnabledAtAll()) {
			log.fatal("Unable to Disable Cheat Sheets");
			fail("Unable to Disable Cheat Sheets");
		}
	}

	/**
	 * Method to Simulate the interaction with the enableCheats servlet.
	 * 
	 * @param moduleId  The ID of the Module to Search For
	 * @param csrfToken The CSRF Token of the User
	 * @return The Content of the Response (Which is supposed to be the location of
	 *         the module)
	 * @throws ServletException
	 * @throws IOException
	 * @throws Exception
	 */
	public String doThePost(String csrfToken, String enableForAll) throws ServletException, IOException {
		String servletClassName = "EnableCheats";

		int expectedResponseCode = 302;

		log.debug("Creating " + servletClassName + " Servlet Instance");
		EnableCheats servlet = new EnableCheats();
		servlet.init(new MockServletConfig(servletClassName));

		// Adding Params for Servlet
		request.addParameter("enableForAll", enableForAll);
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
	 * This test checks that non admin users get access errors when disabling the
	 * cheats
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	@Test
	public void testUserEnableCheatsCall() throws SQLException, ServletException, IOException {
		String userName = "configUserTester";
		String password = userName;
		// Verify / Create user in DB

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
			String enableForAll = "true";
			// Add Cookies from Response to outgoing request
			request.setCookies(response.getCookies());
			String responseBody = doThePost(csrfToken, enableForAll);
			if (responseBody.contains("loggedOutSheep")) {
				log.debug("No Admin Access Expected Result Recieved");
				if (CheatSheetStatus.isEnabledAtAll()) {
					String message = "Cheat Sheets enabled in what should have been a failed request";
					log.fatal(message);
					fail(message);
				}
			} else {
				String message = "Normal user did not get error when performing admin function";
				log.fatal(message);
				fail(message);
			}
		}

	}

	/**
	 * This test checks that admin can the cheats for all
	 * 
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	@Test
	public void testAdminCompleteEnableCheatsCall() throws SQLException, ServletException, IOException {
		String userName = "configAdminTester";
		String password = userName;
		// Verify / Create user in DB

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
			String enableForAll = "true";
			// Add Cookies from Response to outgoing request
			request.setCookies(response.getCookies());
			String responseBody = doThePost(csrfToken, enableForAll);
			if (responseBody.contains("Cheat Sheets have been enabled for all Security Shepherd Users")) {
				log.debug("Cheat Sheets Were Enabled for All");
				if (!CheatSheetStatus.isEnabledForPlayers()) {
					String message = "Cheat Sheets were not actually Enabled for players";
					log.fatal(message);
					fail(message);
				}
			} else {
				String message = "Admin was unable to enable cheat sheets for all";
				log.fatal(message);
				fail(message);
			}
		}

	}

	/**
	 * This test checks that admin users can enable restricted cheat sheets (Admins
	 * Only)
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws ServletException 
	 */
	@Test
	public void testAdminRestrictedEnableCheatsCall() throws SQLException, ServletException, IOException {
		String userName = "configAdminTester";
		String password = userName;
		// Verify / Create user in DB

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
			String enableForAll = "false";
			// Add Cookies from Response to outgoing request
			request.setCookies(response.getCookies());
			String responseBody = doThePost(csrfToken, enableForAll);
			if (responseBody.contains("Cheat Sheets have been enabled for Security Shepherd Administrators")) {
				log.debug("Cheat Sheets have been enabled for Security Shepherd Administrators");
				if (!CheatSheetStatus.isEnabledForAdminsOnly()) {
					String message = "Cheat Sheets were not actually Enabled for admins only";
					log.fatal(message);
					fail(message);
				} else if (CheatSheetStatus.isEnabledForPlayers()) {
					String message = "Cheat Sheets were still enabled for players";
					log.fatal(message);
					fail(message);
				}
			} else {
				String message = "Admin was unable to enable cheat sheets for administrators";
				log.fatal(message);
				fail(message);
			}
		}

	}
}
