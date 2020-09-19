package servlets.admin.config;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import testUtils.TestProperties;
import utils.ScoreboardStatus;

public class EnableScoreboardIT {
	private static org.apache.log4j.Logger log = Logger.getLogger(EnableScoreboardIT.class);
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
		ScoreboardStatus.disableScoreboard();
		// Close Scoreboard
		if (ScoreboardStatus.isScoreboardEnabled()) {
			log.fatal("Unable to Disable Scoreboard");
			fail("Unable to Disable Scoreboard");
		}
	}

	/**
	 * Method to Simulate the interaction with the enableScoreboard servlet.
	 * 
	 * @param moduleId  The ID of the Module to Search For
	 * @param csrfToken The CSRF Token of the User
	 * @return The Content of the Response (Which is supposed to be the location of
	 *         the module)
	 * @throws Exception
	 */
	public String enableScoreboardDoPost(String classId, String csrfToken) throws Exception {
		return enableScoreboardDoPost(classId, csrfToken, "");
	}

	/**
	 * Method to Simulate the interaction with the enableScoreboard servlet.
	 * 
	 * @param moduleId  The ID of the Module to Search For
	 * @param csrfToken The CSRF Token of the User
	 * @return The Content of the Response (Which is supposed to be the location of
	 *         the module)
	 * @throws Exception
	 */
	public String enableScoreboardDoPost(String classId, String csrfToken, String restricted) throws Exception {
		if (classId == null)
			classId = new String();

		int expectedResponseCode = 302;

		log.debug("Creating EnableScoreboard Servlet Instance");
		EnableScoreboard servlet = new EnableScoreboard();
		servlet.init(new MockServletConfig("EnableScoreboard"));

		// Setup Servlet Parameters and Attributes
		log.debug("Setting Up Params and Atrributes");

		request.addParameter("classId", classId);
		request.addParameter("restricted", restricted);
		// Adding Correct CSRF Token (Token Submitted)
		request.addParameter("csrfToken", csrfToken);

		log.debug("Running doPost");
		servlet.doPost(request, response);

		if (response.getStatus() != expectedResponseCode)
			fail("EnableScoreboard Servlet Returned " + response.getStatus() + " Code. " + expectedResponseCode
					+ " Expected");
		else {
			log.debug(expectedResponseCode + " Detected");
			log.debug("Servlet Successful, returning response retrieved: " + response.getContentAsString());
			return (response.getContentAsString());
		}
		return null;
	}

	/**
	 * This test checks that non admin users get access errors when enabling the
	 * scoreboard
	 */
	@Test
	public void testNormalUserScoreboardCall() {
		String userName = "enableScore1";
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
				String responseBody = enableScoreboardDoPost(null, csrfToken);
				if (responseBody.contains("Please try non administrator functions")) {
					log.debug("No Admin Access Expected Result Recieved");
				} else {
					String message = "Normal user did not get error when performing admin function";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete testValidEnableScoreboardCall: " + e.toString());
			fail("Could not Complete testValidEnableScoreboardCall");
		}
	}

	/**
	 * This test checks that admin users can completely open the scoreboard (Null
	 * classID)
	 */
	@Test
	public void testUserScoreboardCompleteOpen() {
		String userName = "enableScore2";
		String password = userName;
		// Verify / Create user in DB
		try {
			TestProperties.verifyTestAdmin(log, applicationRoot, userName, password);
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
				String responseBody = enableScoreboardDoPost(null, csrfToken);
				String expectedResult = "Scoreboard is now enabled and lists all users regardless of their class";
				if (responseBody.contains(expectedResult)) {
					log.debug("Scoreboard was completely opened");
					if (ScoreboardStatus.canSeeScoreboard("player")) {
						log.debug("Users can see the scoreboard");
					} else {
						String message = "Players cannot view scoreboard and they should be able to after this test";
						log.fatal(message);
						fail(message);
					}
				} else {
					String message = "Did not get expected result message. Expected '" + expectedResult + "'";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete testUserScoreboardCompleteOpen: " + e.toString());
			fail("Could not Complete testUserScoreboardCompleteOpen");
		}
	}

	/**
	 * This test checks that admin users can open the scoreboard for class specific
	 * scoreboards
	 */
	@Test
	public void testUserScoreboardClassSpecificOpen() {
		String userName = "enableScore2";
		String password = userName;
		// Verify / Create user in DB
		try {
			TestProperties.verifyTestAdmin(log, applicationRoot, userName, password);
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
				String responseBody = enableScoreboardDoPost("classSpecific", csrfToken);
				String expectedResult = "Scoreboard has been enabled and only lists users from the viewer's class. Admin users will still see the scoreboard of the default class.";
				if (responseBody.contains(expectedResult)) {
					log.debug("Scoreboard was completely opened");
				} else {
					String message = "Did not get expected result message. Expected '" + expectedResult + "'";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete testUserScoreboardClassSpecificOpen: " + e.toString());
			fail("Could not Complete testUserScoreboardClassSpecificOpen");
		}
	}

	/**
	 * This test checks that admin users cannot open the scoreboard for an invalid
	 * class specific scoreboards
	 */
	@Test
	public void testUserScoreboardInvalidClass() {
		String userName = "enableScore2";
		String password = userName;
		// Verify / Create user in DB
		try {

			TestProperties.verifyTestAdmin(log, applicationRoot, userName, password);
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
				String responseBody = enableScoreboardDoPost("notAClassId", csrfToken);
				String expectedResult = "Invalid data was submitted. Please try again.";
				if (responseBody.contains(expectedResult)) {
					log.debug("Scoreboard was unchanged");
				} else {
					String message = "Did not get expected result message. Expected '" + expectedResult + "'";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete testUserScoreboardClassSpecificOpen: " + e.toString());
			fail("Could not Complete testUserScoreboardClassSpecificOpen");
		}
	}

	/**
	 * This test checks that admin users can configure the scoreboard to be open
	 * with a specific class
	 */
	@Test
	public void testUserScoreboardOpenValidClass() {
		String userName = "enableScore5";
		String password = userName;
		// Verify / Create user in DB
		try {
			String className1 = "scoreEnableClass";
			String classId = TestProperties.findCreateClassId(log, className1, applicationRoot);
			TestProperties.verifyTestAdmin(log, applicationRoot, userName, password, classId);
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
				String responseBody = enableScoreboardDoPost(classId, csrfToken);
				String expectedResult = "Scoreboard has been enabled and only lists users from ";
				if (responseBody.contains(expectedResult)) {
					log.debug("Scoreboard was changed");
					String scoreboardClass = ScoreboardStatus.getScoreboardClass();
					if (scoreboardClass.equalsIgnoreCase(classId)) {
						log.debug("Correct Class was set");
					} else {
						String message = "Did not update scoreboard setting to list specific class. Expected '"
								+ classId + "' but got '" + scoreboardClass + "'";
						log.fatal(message);
						fail(message);
					}
				} else {
					String message = "Did not get expected result message. Expected '" + expectedResult + "'";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete testUserScoreboardOpenValidClass: " + e.toString());
			fail("Could not Complete testUserScoreboardOpenValidClass");
		}
	}

	/**
	 * This test checks that admin users can configure the scoreboard to be open
	 * with a specific class for admins only
	 */
	@Test
	public void testUserScoreboardRestrictedValidClass() {
		String userName = "enableScore5";
		String password = userName;
		// Verify / Create user in DB
		try {
			String className1 = "scoreEnableClass";
			String classId = TestProperties.findCreateClassId(log, className1, applicationRoot);
			TestProperties.verifyTestAdmin(log, applicationRoot, userName, password, classId);
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
				String responseBody = enableScoreboardDoPost(classId, csrfToken, "true");
				String expectedResult = "Scoreboard has been enabled and only lists users from ";
				if (responseBody.contains(expectedResult)) {
					log.debug("Scoreboard was changed");
					String scoreboardClass = ScoreboardStatus.getScoreboardClass();
					if (scoreboardClass.equalsIgnoreCase(classId)) {
						log.debug("Correct Class was set");
						if (ScoreboardStatus.canSeeScoreboard("admin")) {
							log.debug("Admins can see scoreboard but players cannot");
							if (!ScoreboardStatus.canSeeScoreboard("player")) {
								log.debug("Users cannot see the scoreboard");
							} else {
								String message = "Players can view scoreboard and they should not be able to after this test";
								log.fatal(message);
								fail(message);
							}
						} else {
							String message = "Admins cannot view scoreboard and they should be able to after this test";
							log.fatal(message);
							fail(message);
						}
					} else {
						String message = "Did not update Scoreboard Setting to list specific class. Expected '"
								+ classId + "' but got '" + scoreboardClass + "'";
						log.fatal(message);
						fail(message);
					}
				} else {
					String message = "Did not get expected result message. Expected '" + expectedResult + "'";
					log.fatal(message);
					fail(message);
				}
			}
		} catch (Exception e) {
			log.fatal("Could not Complete testUserScoreboardRestrictedValidClass: " + e.toString());
			fail("Could not Complete testUserScoreboardRestrictedValidClass");
		}
	}
}
