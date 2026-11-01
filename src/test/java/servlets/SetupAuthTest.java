package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dbProcs.Constants;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import testUtils.TestProperties;

/** Regression tests for GHSA-m4gj-wcx8-jjr7: the setup token check and the installed-state gate. */
public class SetupAuthTest {

  private static final Logger log = LogManager.getLogger(SetupAuthTest.class);

  private static final String TOKEN = "3f1c2a9e-6b0d-4c8e-9a51-7d2e4b6f8c10";

  private static final ResourceBundle bundle =
      ResourceBundle.getBundle("i18n.text", Locale.ENGLISH);

  @BeforeAll
  public static void initAll() {
    TestProperties.setTestPropertiesFileDirectory(log);
  }

  @BeforeEach
  public void setUp() throws IOException {
    Files.createDirectories(Paths.get(Constants.CATALINA_CONF));
    // Without database.properties, doPost takes the fresh-install path and connects to the
    // database given in the request: reaching that connection means the token check passed
    TestProperties.deleteMysqlResource();
    deleteAuthFile();
    Setup.resetInstalledCache();
  }

  @AfterEach
  public void tearDown() {
    deleteAuthFile();
    Setup.resetInstalledCache();
  }

  private static void deleteAuthFile() {
    FileUtils.deleteQuietly(new File(Constants.SETUP_AUTH));
  }

  private static void writeAuthFile(String content) throws IOException {
    Files.write(Paths.get(Constants.SETUP_AUTH), content.getBytes(StandardCharsets.UTF_8));
  }

  private static void markInstalled() throws ReflectiveOperationException {
    Field installed = Setup.class.getDeclaredField("installedCached");
    installed.setAccessible(true);
    installed.set(null, Boolean.TRUE);
  }

  /** A setup request against a closed local port, so any connection attempt fails quickly. */
  private static MockHttpServletRequest setupRequest(String dbAuth) {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/setup");
    request.addParameter("dbhost", "127.0.0.1");
    request.addParameter("dbport", "1");
    request.addParameter("dbuser", "root");
    request.addParameter("dbpass", "");
    request.addParameter("dboverride", "override");
    request.addParameter("mhost", "127.0.0.1");
    request.addParameter("mport", "27017");
    if (dbAuth != null) {
      request.addParameter("dbauth", dbAuth);
    }
    return request;
  }

  private static MockHttpServletResponse post(MockHttpServletRequest request) throws Exception {
    MockHttpServletResponse response = new MockHttpServletResponse();
    new Setup().doPost(request, response);
    return response;
  }

  private static void assertRejectedBeforeConnecting(MockHttpServletResponse response)
      throws IOException {
    String body = response.getContentAsString();
    assertTrue(
        body.contains(bundle.getString("generic.text.setup.authentication.failed")),
        "Expected an authentication failure, got: " + body);
    assertFalse(
        body.contains(bundle.getString("generic.text.setup.connection.failed")),
        "Setup tried to connect to the database without a valid token: " + body);
  }

  @Test
  public void isValidSetupAuth_noTokenFile_failsClosed() {
    assertFalse(Setup.isValidSetupAuth(""));
    assertFalse(Setup.isValidSetupAuth(null));
    assertFalse(Setup.isValidSetupAuth(TOKEN));
  }

  @Test
  public void isValidSetupAuth_emptyTokenFile_failsClosed() throws IOException {
    writeAuthFile("\n");
    assertFalse(Setup.isValidSetupAuth(""));
    assertFalse(Setup.isValidSetupAuth(" "));
    assertFalse(Setup.isValidSetupAuth(null));
  }

  @Test
  public void isValidSetupAuth_matchesOnlyTheToken() throws IOException {
    writeAuthFile(TOKEN + "\n");
    assertTrue(Setup.isValidSetupAuth(TOKEN));
    assertFalse(Setup.isValidSetupAuth(""));
    assertFalse(Setup.isValidSetupAuth(TOKEN.substring(1)));
    assertFalse(Setup.isValidSetupAuth(TOKEN + "0"));
  }

  @Test
  public void doPost_emptyAuthAndNoTokenFile_isRejectedBeforeConnecting() throws Exception {
    assertRejectedBeforeConnecting(post(setupRequest("")));
  }

  @Test
  public void doPost_missingAuthAndNoTokenFile_isRejectedBeforeConnecting() throws Exception {
    assertRejectedBeforeConnecting(post(setupRequest(null)));
  }

  @Test
  public void doPost_emptyAuthAndEmptyTokenFile_isRejectedBeforeConnecting() throws Exception {
    writeAuthFile("");
    assertRejectedBeforeConnecting(post(setupRequest("")));
  }

  @Test
  public void doPost_wrongAuth_isRejectedBeforeConnecting() throws Exception {
    writeAuthFile(TOKEN);
    assertRejectedBeforeConnecting(post(setupRequest("not-the-token")));
  }

  @Test
  public void doPost_correctAuth_triesToConnect() throws Exception {
    writeAuthFile(TOKEN);
    String body = post(setupRequest(TOKEN)).getContentAsString();
    assertTrue(
        body.contains(bundle.getString("generic.text.setup.connection.failed")),
        "Expected a connection attempt with the right token, got: " + body);
  }

  @Test
  public void doPost_installed_isRefusedEvenWithCorrectAuth() throws Exception {
    writeAuthFile(TOKEN);
    markInstalled();

    MockHttpServletResponse response = post(setupRequest(TOKEN));

    assertEquals(404, response.getStatus());
    assertFalse(
        response
            .getContentAsString()
            .contains(bundle.getString("generic.text.setup.connection.failed")));
  }

  @Test
  public void doPost_installedWithEmptyAuth_isRefused() throws Exception {
    markInstalled();
    assertEquals(404, post(setupRequest("")).getStatus());
  }
}
