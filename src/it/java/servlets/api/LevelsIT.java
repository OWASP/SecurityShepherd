package servlets.api;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import testUtils.TestProperties;
import utils.ModulePlan;
import dbProcs.Setter;

public class LevelsIT {
	private static String lang = "en_GB";
	private static org.apache.log4j.Logger log = Logger.getLogger(LevelsIT.class);
	private static String applicationRoot = new String();
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
	public void setup() {
		log.debug("Setting Up Blank Request and Response");
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		// Open All modules
		if (!Setter.openAllModules(applicationRoot, false))
			fail("Could not Mark All Modules As Open");
		// Default to CTF Mode
		ModulePlan.setIncrementalFloor();
		if (!ModulePlan.isIncrementalFloor()) {
			String message = "Could not enable CTF Mode";
			log.fatal(message);
			fail(message);
		}
	}

	/**
	 * Method to Simulate the interaction with the getModule servlet.
	 * 
	 * @param moduleId  The ID of the Module to Search For
	 * @param csrfToken The CSRF Token of the User
	 * @return The Content of the Response (Which is supposed to be the location of
	 *         the module)
	 * @throws ServletException
	 * @throws IOException
	 * @throws Exception
	 */
	public String getModuleDoGet(String csrfToken) throws ServletException, IOException {

		int expectedResponseCode = 302;

		String servletName = "Levels";
		log.debug("Creating " + servletName + " Servlet Instance");
		Levels servlet = new Levels();
		servlet.init(new MockServletConfig(servletName));

		// Adding Correct CSRF Token (Token Submitted)
		request.addParameter("csrfToken", csrfToken);

		log.debug("Running doGet");
		servlet.doGet(request, response);

		if (response.getStatus() != expectedResponseCode)
			fail("GetModule Servlet Returned " + response.getStatus() + " Code. " + expectedResponseCode + " Expected");
		else {
			log.debug("302 OK Detected");
			log.debug("Servlet Successful, returning location retrieved: " + response.getContentAsString());
			return (response.getContentAsString());
		}
		return null;
	}

	@Test
	public void testCtfModeNoLevelsComplete() throws SQLException, ServletException, IOException {
		String moduleId = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11"); // Insecure Direct Object References
																					// Module Id (First Challenge)
		String userName = "ctfModeNoLevels";

		// Verify User Exists in DB
		TestProperties.verifyTestUser(log, applicationRoot, userName, userName);
		// Sign in as Normal User
		log.debug("Signing in as " + userName + " Through LoginServlet");
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
			String jsonString = getModuleDoGet(csrfToken);
			JSONArray theResponse = new JSONArray(jsonString);
			log.debug("Response Returned, checking mode");
			if (!theResponse.getJSONObject(0).get("levelMode").toString().equalsIgnoreCase("ctf")) {
				String message = "CTF Mode not Detected in JSON Response";
				log.debug(message);
				fail(message);
			} else {
				log.debug("Extracting Modules from JSON response");
				// Is First Object Marked as Open, and is it the Module We Expect?
				JSONArray theModules = theResponse.getJSONObject(1).getJSONArray("modules");
				JSONObject module = theModules.getJSONObject(0);
				log.debug("Inspecting First Level: " + module);
				if (!module.get("moduleId").toString().equalsIgnoreCase(moduleId)) {
					String message = "Received " + module.get("moduleId") + " as first module instead of " + moduleId;
					log.debug(message);
					fail(message);
				} else if (!module.getBoolean("moduleOpen")) {
					String message = "Module " + module.get("moduleId")
							+ " returned false for moduleOpen Status when it should be true";
					log.debug(message);
					fail(message);
				} else if (module.getBoolean("moduleCompleted")) {
					String message = "Module " + module.get("moduleId")
							+ " is marked as completed when no module should be marked as complete for this user";
					log.debug(message);
					fail(message);
				} else {
					// First Level is Ok
					module = theModules.getJSONObject(1);
					log.debug("Inspecting Second Level: " + module);
					if (module.getBoolean("moduleOpen")) {
						String message = "Module " + module.get("moduleId")
								+ " returned true for moduleOpen Status when it should be false";
						log.debug(message);
						fail(message);
					} else if (module.getBoolean("moduleCompleted")) {
						String message = "Module " + module.get("moduleId")
								+ " is marked as completed when no module should be marked as complete for this user";
						log.debug(message);
						fail(message);
					} else {
						log.debug("Test Complete");
					}
				}
			}
		}
	}

	@Test
	public void testCtfModeLevelComplete() throws SQLException, ServletException, IOException {
		String moduleId = new String("0dbea4cb5811fff0527184f99bd5034ca9286f11"); // Insecure Direct Object References
																					// Module Id (First Challenge)
		String userName = "ctfModeWithLevel";

		// Verify User Exists in DB
		TestProperties.verifyTestUser(log, applicationRoot, userName, userName);
		TestProperties.completeModuleForUser(log, userName, userName, moduleId, "anything", "");
		// Sign in as Normal User
		log.debug("Signing in as " + userName + " Through LoginServlet");
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
			String jsonString = getModuleDoGet(csrfToken);
			JSONArray theResponse = new JSONArray(jsonString);
			log.debug("Response Returned, checking mode");
			if (!theResponse.getJSONObject(0).get("levelMode").toString().equalsIgnoreCase("ctf")) {
				String message = "CTF Mode not Detected in JSON Response";
				log.debug(message);
				fail(message);
			} else {
				log.debug("Extracting Modules from JSON response");
				// Is First Object Marked as Open, and is it the Module We Expect?
				JSONArray theModules = theResponse.getJSONObject(1).getJSONArray("modules");
				JSONObject module = theModules.getJSONObject(0);
				log.debug("Inspecting First Level: " + module);
				if (!module.get("moduleId").toString().equalsIgnoreCase(moduleId)) {
					String message = "Received " + module.get("moduleId") + " as first module instead of " + moduleId;
					log.debug(message);
					fail(message);
				} else if (!module.getBoolean("moduleOpen")) {
					String message = "Module " + module.get("moduleId")
							+ " returned false for moduleOpen Status when it should be true";
					log.debug(message);
					fail(message);
				} else if (!module.getBoolean("moduleCompleted")) {
					String message = "Module " + module.get("moduleId")
							+ " is marked as uncompleted when this module should be marked as complete for this user";
					log.debug(message);
					fail(message);
				} else if (!(module.getInt("scoredPoints") == 15)) // Max points for first to complete first
																	// challenge
				{
					String message = "Expected 15 Points to be earned from completing level. Got "
							+ module.getInt("scoredPoints");
					log.debug(message);
					fail(message);
				} else if (!module.getString("medalEarned").equalsIgnoreCase("gold")) {
					String message = "Expected gold medal to be earned from completing level. Got "
							+ module.getInt("medalEarned");
					log.debug(message);
					fail(message);
				} else {
					// First Level is Ok
					module = theModules.getJSONObject(1);
					log.debug("Inspecting Second Level: " + module);
					if (!module.getBoolean("moduleOpen")) {
						String message = "Module " + module.get("moduleId")
								+ " returned false for moduleOpen Status when it should be true";
						log.debug(message);
						fail(message);
					} else if (module.getBoolean("moduleCompleted")) {
						String message = "Module " + module.get("moduleId")
								+ " is marked as completed when it should not be marked as complete for this user";
						log.debug(message);
						fail(message);
					} else {
						log.debug("Test Complete");
					}
				}
			}
		}

	}
}
