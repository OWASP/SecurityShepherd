package servlets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import dbProcs.Constants;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockServletConfig;
import testUtils.TestProperties;

public class SetupIT {

  private static final Logger log = LogManager.getLogger(SetupIT.class);

  /** Initialize directories */
  @After
  public void tearDown() throws IOException {
    log.debug("Cleaning up");

    removeDatabaseProps();
    Setup.isInstalled();
  }

  private void removeDatabaseProps() {
    FileUtils.deleteQuietly(new File(Constants.MYSQL_DB_PROP));
  }

  @Ignore
  @Test
  public void testNoCoreDatabase() {

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
      String message =
          "Error when loading auth file "
              + Constants.SETUP_AUTH
              + ". Exception message was "
              + e.toString();
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

  @Ignore
  @Test
  public void testNoMysqlResource() {

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
      String message =
          "Error when loading auth file "
              + Constants.SETUP_AUTH
              + ". Exception message was "
              + e.toString();
      log.fatal(message);
      fail(message);
    }

    if (authData == null) {
      String message = "Auth data loaded from " + Constants.SETUP_AUTH + " was null!";
      log.fatal(message);
      fail(message);
    }

    if (authData.equals("")) {
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
  @Ignore
  @Test
  public void testNoMongodbResource() {

    log.debug("Creating Setup Servlet Instance");
    Setup servlet = new Setup();
    try {
      servlet.init(new MockServletConfig("Setup"));
    } catch (ServletException e) {
      log.fatal(e.toString());
      fail(e.toString());
    }

    TestProperties.deleteMongoResource();
    assertTrue(Setup.isInstalled());

    String mongodbProp = "";
    try {
      mongodbProp = FileUtils.readFileToString(new File(Constants.MONGO_DB_PROP), StandardCharsets.UTF_8);
    } catch (IOException e) {
      String message =
              "Error when loading mongodb file "
                      + Constants.MONGO_DB_PROP
                      + ". Exception message was "
                      + e.toString();
      log.fatal(message);
      fail(message);
    }

    if (mongodbProp == null) {
      String message = "MongoDb data loaded from " + Constants.MONGO_DB_PROP + " was null!";
      log.fatal(message);
      fail(message);
    }

    if (mongodbProp.equals("")) {
      String message = "MongoDb data loaded from " + Constants.MONGO_DB_PROP + " was empty!";
      log.fatal(message);
      fail(message);
    }
  }
}
