package servlets.module.challenge;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import dbProcs.GetterIT;
import dbProcs.Setter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletConfig;
import testUtils.TestProperties;

/**
 * Each challenge schema has its own connection pool of at most three connections. The Session
 * Management 6 secret-question lookup is injectable, and a malformed email address left its
 * connection checked out, so three bad lookups took the level offline until Tomcat restarted
 * (#857).
 *
 * <p>Each test calls a servlet more times than the pool holds and checks every response came from
 * the database. The inputs name users that do not exist, so no challenge data is changed.
 */
public class SessionManagement6And7PoolLeakIT {

  private static final Logger log = LogManager.getLogger(SessionManagement6And7PoolLeakIT.class);
  private static final String lang = "en_GB";
  private static final String applicationRoot = "";
  private static final String userName = "lessonTester";
  private static final String noSuchUser = "noSuchPoolLeakUser";
  private static final String noSuchEmail = "nobody@example.com";

  /** More than the challenge pool's maximum size of three. */
  private static final int REQUESTS = 5;

  private static final String SQL_SYNTAX_ERROR = "SQL syntax";

  /** The untampered answer-control cookie these levels expect. */
  private static final Cookie answerControl =
      new Cookie(
          "ac",
          Base64.getEncoder()
              .encodeToString("doNotReturnAnswers".getBytes(StandardCharsets.UTF_8)));

  private MockHttpSession session;

  @BeforeAll
  public static void resetDatabase() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.ensureSchemaReady(log);
    TestProperties.reseedTestData();
  }

  @BeforeEach
  public void signIn() throws SQLException {
    if (!Setter.openAllModules(applicationRoot, false)) {
      fail("Could not Mark All Modules As Open");
    }
    GetterIT.verifyTestUser(applicationRoot, userName, userName);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
    session = (MockHttpSession) request.getSession();
    session.setAttribute("lang", lang);
  }

  private static String text(int level, String key) {
    return ResourceBundle.getBundle(
            "i18n.servlets.challenges.sessionManagement.sessionManagement" + level,
            new Locale(lang))
        .getString(key);
  }

  private String call(HttpServlet servlet, String method, String... params) throws Exception {
    if (params.length % 2 != 0) {
      throw new IllegalArgumentException("params must be name/value pairs");
    }
    servlet.init(new MockServletConfig(servlet.getClass().getSimpleName()));
    MockHttpServletRequest request = new MockHttpServletRequest(method, "/");
    request.setSession(session);
    request.setCookies(answerControl);
    for (int i = 0; i < params.length; i += 2) {
      request.addParameter(params[i], params[i + 1]);
    }
    MockHttpServletResponse response = new MockHttpServletResponse();
    servlet.service(request, response);
    String output = response.getContentAsString();
    log.debug(servlet.getClass().getSimpleName() + " " + method + " returned: " + output);
    return output;
  }

  private static void assertContains(String expected, String output, String what) {
    assertTrue(output.contains(expected), what + " (expected \"" + expected + "\"): " + output);
  }

  @Test
  public void sessionManagement6SecretQuestionSurvivesMalformedEmails() throws Exception {
    for (int i = 1; i <= REQUESTS; i++) {
      // The unbalanced double quote breaks the injectable secret-question query
      String output =
          call(new SessionManagement6SecretQuestion(), "GET", "subEmail", "a\"@example.com");
      assertContains(SQL_SYNTAX_ERROR, output, "Malformed email " + i + " did not fail");
    }
    String output = call(new SessionManagement6SecretQuestion(), "GET", "subEmail", noSuchEmail);
    assertContains(
        text(6, "question.noQuestion"),
        output,
        "Secret question lookup did not reach the database after malformed emails");
  }

  @Test
  public void sessionManagement6SecretAnswerReturnsConnections() throws Exception {
    for (int i = 1; i <= REQUESTS; i++) {
      String output =
          call(
              new SessionManagement6SecretQuestion(),
              "POST",
              "subEmail",
              noSuchEmail,
              "subAnswer",
              "notTheAnswer");
      assertContains(text(6, "question.badAnswer"), output, "Request " + i + " got no result");
    }
  }

  @Test
  public void sessionManagement6LoginReturnsConnections() throws Exception {
    for (int i = 1; i <= REQUESTS; i++) {
      String output =
          call(new SessionManagement6(), "POST", "subName", noSuchUser, "subPassword", "password");
      assertContains(text(6, "response.badUser"), output, "Request " + i + " got no result");
    }
  }

  @Test
  public void sessionManagement7LoginReturnsConnections() throws Exception {
    for (int i = 1; i <= REQUESTS; i++) {
      String output =
          call(new SessionManagement7(), "POST", "subName", noSuchUser, "subPassword", "password");
      assertContains(text(7, "response.badUser"), output, "Request " + i + " got no result");
    }
  }

  @Test
  public void sessionManagement7SecretAnswerReturnsConnections() throws Exception {
    for (int i = 1; i <= REQUESTS; i++) {
      // The answer must be on the level's flower list for the database to be queried. When the
      // query fails the level only logs it, so the response is then empty.
      String output =
          call(
              new SessionManagement7SecretQuestion(),
              "POST",
              "subEmail",
              noSuchEmail,
              "subAnswer",
              "Jade Vine");
      assertContains(text(7, "question.badAnswer"), output, "Request " + i + " got no result");
    }
  }
}
