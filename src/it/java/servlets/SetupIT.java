package servlets;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.After;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;

import dbProcs.Constants;
import dbProcs.Getter;
import dbProcs.GetterTest;
import dbProcs.Setter;
import testUtils.TestProperties;
import utils.InstallationException;

public class SetupIT {
	private static String lang = "en_GB";
	private static org.apache.log4j.Logger log = Logger.getLogger(SetupIT.class);
	private static String applicationRoot = new String();
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private StringBuffer dbProp = new StringBuffer();

	/**
	 * Initialize directories
	 */

	@Before
	public void setup() {
		log.debug("Setting Up Blank Request and Response");
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	public void testNoCoreDatabase() {
		
		int expectedResponseCode = 302;
		
		log.debug("Creating Setup Servlet Instance");
		Setup servlet = new Setup();
		try {
			servlet.init(new MockServletConfig("Setup"));
		} catch (ServletException e) {
			log.fatal(e.toString());
			fail(e.toString());
		}
		
		try {
			TestProperties.createMysqlResource();
		} catch (IOException e) {
			String message = "Could not create mysql resource file: " + e.toString();
			log.fatal(message);
			fail(message);
		}
		
		assertFalse(Setup.isInstalled());

		String authData = "";
		try {
			authData = FileUtils.readFileToString(new File(Constants.SETUP_AUTH), StandardCharsets.UTF_8);
		} catch (IOException e) {
			String message = "Error when loading auth file " + Constants.SETUP_AUTH + ". Exception message was " + e.toString();
			log.fatal(message);
			fail(message);
		}

		if (authData == null) {
			String message = "Auth data loaded from " + Constants.SETUP_AUTH + " was null!";
			log.fatal(message);
			fail(message);
		}

		if (authData == "") {
			String message = "Auth data loaded from " + Constants.SETUP_AUTH + " was empty!";
			log.fatal(message);
			fail(message);
		}
		/*
		 * request.getSession().setAttribute("lang", lang);
		 * 
		 * request.addParameter("dbhost", "localhost"); request.addParameter("dbport",
		 * "3306"); request.addParameter("dbuser", "root");
		 * request.addParameter("dbpass", ""); request.addParameter("dbauth", authData);
		 * 
		 * log.debug("Running doPost"); try { servlet.doPost(request, response); } catch
		 * (ServletException | IOException e) { e.printStackTrace();
		 * log.fatal(e.toString()); fail(e.toString()); }
		 * log.debug("doPost successful, reading response");
		 * 
		 * if(response.getStatus() != expectedResponseCode) { String
		 * message="Login Servlet Returned " + response.getStatus() +
		 * " Code. 302 Expected"; log.fatal(message); fail(message); } String location =
		 * ""; try { location = response.getHeader("Location"); } catch
		 * (NullPointerException e) { String message =
		 * "Got invalid location from posting setup request: " + e.toString();
		 * log.fatal(message); fail(message); }
		 * 
		 * if (!location.endsWith("login.jsp")) {
		 * fail("Setup not Redirecting to login.jsp."); }
		 */

	}
	
	@Test
	public void testNoMysqlResource() {

		int expectedResponseCode = 302;

		log.debug("Creating Setup Servlet Instance");
		Setup servlet = new Setup();
		try {
			servlet.init(new MockServletConfig("Setup"));
		} catch (ServletException e) {
			log.fatal(e.toString());
			fail(e.toString());
		}
		
		TestProperties.deleteMysqlResource();
		assertTrue(Setup.isInstalled());

		String authData = "";
		try {
			authData = FileUtils.readFileToString(new File(Constants.SETUP_AUTH), StandardCharsets.UTF_8);
		} catch (IOException e) {
			String message = "Error when loading auth file " + Constants.SETUP_AUTH + ". Exception message was " + e.toString();
			log.fatal(message);
			fail(message);
		}

		if (authData == null) {
			String message = "Auth data loaded from " + Constants.SETUP_AUTH + " was null!";
			log.fatal(message);
			fail(message);
		}

		if (authData == "") {
			String message = "Auth data loaded from " + Constants.SETUP_AUTH + " was empty!";
			log.fatal(message);
			fail(message);
		}
		/*
		 * request.getSession().setAttribute("lang", lang);
		 * 
		 * request.addParameter("dbhost", "localhost"); request.addParameter("dbport",
		 * "3306"); request.addParameter("dbuser", "root");
		 * request.addParameter("dbpass", ""); request.addParameter("dbauth", authData);
		 * 
		 * log.debug("Running doPost"); try { servlet.doPost(request, response); } catch
		 * (ServletException | IOException e) { e.printStackTrace();
		 * log.fatal(e.toString()); fail(e.toString()); }
		 * log.debug("doPost successful, reading response");
		 * 
		 * if(response.getStatus() != expectedResponseCode) { String
		 * message="Login Servlet Returned " + response.getStatus() +
		 * " Code. 302 Expected"; log.fatal(message); fail(message); } String location =
		 * ""; try { location = response.getHeader("Location"); } catch
		 * (NullPointerException e) { String message =
		 * "Got invalid location from posting setup request: " + e.toString();
		 * log.fatal(message); fail(message); }
		 * 
		 * if (!location.endsWith("login.jsp")) {
		 * fail("Setup not Redirecting to login.jsp."); }
		 */

	}
}
