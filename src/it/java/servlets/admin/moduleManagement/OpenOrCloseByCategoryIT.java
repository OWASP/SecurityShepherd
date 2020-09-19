package servlets.admin.moduleManagement;

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

import dbProcs.Setter;
import testUtils.TestProperties;

public class OpenOrCloseByCategoryIT {
	private static org.apache.log4j.Logger log = Logger.getLogger(OpenOrCloseByCategoryIT.class);
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
		TestProperties.setTestPropertiesFileDirectory(log);
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		// Close All modules
		if (!Setter.closeAllModules(applicationRoot))
			fail("Could not Mark All Modules As Closed");
	}

	public String openOrCloseByCategoryDoPost(String openOrClose, String moduleCategory, String csrfToken) throws ServletException, IOException {

		int expectedResponseCode = 302;

		log.debug("Creating OpenOrCloseByCategory Servlet Instance");
		OpenOrCloseByCategory servlet = new OpenOrCloseByCategory();
		servlet.init(new MockServletConfig("OpenOrCloseByCategory"));

		// Setup Servlet Parameters and Attributes
		log.debug("Setting Up Params and Atrributes");
		request.addParameter("openOrClose", openOrClose);
		request.addParameter("toOpenOrClose[]", moduleCategory);

		// Adding Correct CSRF Token (Token Submitted)
		request.addParameter("csrfToken", csrfToken);

		log.debug("Running doPost");
		servlet.doPost(request, response);

		if (response.getStatus() != expectedResponseCode)
			fail("OpenOrCloseByCategory Servlet Returned " + response.getStatus() + " Code. " + expectedResponseCode
					+ " Expected");
		else {
			log.debug(expectedResponseCode + " Detected");
			log.debug("Servlet Successful, returning response retrieved: " + response.getContentAsString());
			return (response.getContentAsString());
		}

		return null;

	}

	/**
	 * 
	 */
	@Test
	public void testOpenByCategory() {
		String userName = "openAndCloseAdmin";
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
				String responseBody = openOrCloseByCategoryDoPost("open", "Injection", csrfToken);
				if (!responseBody.contains("Please try non administrator functions")) {
					log.debug("No Admin Access Result Recieved");
					String expectedResult = "The categories selected have been opened";
					if (responseBody.contains(expectedResult)) {
						log.debug("Received Expected Message for this test");
					} else {
						String message = "Did not find 'Categories Set to open' in response";
						log.fatal(message);
						fail(message);
					}
				} else {
					String message = "Admin user did gets 'Not an Admin' Error";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete testValidEnableScoreboardCall: " + e.toString());
			fail("Could not Complete testValidEnableScoreboardCall");
		}
	}

	@Test
	public void testOpenByCategoryUser() {
		String userName = "openAndCloseUser";
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
				String responseBody = openOrCloseByCategoryDoPost("open", "Injection", csrfToken);
				if (responseBody.contains("loggedOutSheep")) {
					log.debug("Admin Access Result Recieved");
				} else {
					String message = "User Does not get 'Admin' Error";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete testValidEnableScoreboardCall: " + e.toString());
			fail("Could not Complete testValidEnableScoreboardCall");
		}
	}

	@Test
	public void testOpenByCategoryXss() {
		String userName = "openAndCloseAdmin";
		String password = userName;
		// Verify / Create user in DB
		try {
			TestProperties.verifyTestAdmin(log, applicationRoot, userName, password);
			// Sign in as Normal User
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
				String responseBody = openOrCloseByCategoryDoPost("<script>alert(1)</script>", "Injection", csrfToken);
				if (!responseBody.contains("Please try non administrator functions")) {
					log.debug("No Admin Access Result Recieved");
					String expectedResult = "Invalid Request";
					if (responseBody.contains(expectedResult)) {
						log.debug("Received Expected Message for this test");
					} else {
						String message = "Did not find '" + expectedResult + "' in response";
						log.fatal(message);
						fail(message);
					}
				} else {
					String message = "Admin user did gets 'Not an Admin' Error";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete testValidEnableScoreboardCall: " + e.toString());
			fail("Could not Complete testValidEnableScoreboardCall");
		}
	}
}
