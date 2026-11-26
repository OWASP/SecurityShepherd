package servlets.module.challenge;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import dbProcs.Database;
import dbProcs.GetterIT;
import dbProcs.Setter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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
 * Management 3 and 5 change-password servlets never returned their connection, so the fourth
 * password change hung for the pool timeout and then failed until Tomcat restarted (#857).
 *
 * <p>Each test calls a servlet more times than the pool holds and checks every response came from
 * the database. The inputs name users that do not exist, so no challenge data is changed.
 *
 * <p>Those tests only run the success path. The "OnFailure" tests cover the exception path: they
 * make the level's queries fail after the servlet has borrowed its connection, call the servlet
 * more times than the pool holds, then restore the table and check a normal request still reaches
 * the database. A servlet that returns its connection only when no exception was thrown fails them.
 */
public class SessionManagement2To5PoolLeakIT {

  private static final Logger log = LogManager.getLogger(SessionManagement2To5PoolLeakIT.class);
  private static final String lang = "en_GB";
  private static final String applicationRoot = "";
  private static final String userName = "lessonTester";
  private static final String noSuchUser = "noSuchPoolLeakUser";

  /** More than the challenge pool's maximum size of three. */
  private static final int REQUESTS = 5;

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

  private static String base64(String value) {
    return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  private String doPost(HttpServlet servlet, Cookie cookie, String... params) throws Exception {
    if (params.length % 2 != 0) {
      throw new IllegalArgumentException("params must be name/value pairs");
    }
    servlet.init(new MockServletConfig(servlet.getClass().getSimpleName()));
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
    request.setSession(session);
    if (cookie != null) {
      request.setCookies(cookie);
    }
    for (int i = 0; i < params.length; i += 2) {
      request.addParameter(params[i], params[i + 1]);
    }
    MockHttpServletResponse response = new MockHttpServletResponse();
    servlet.service(request, response);
    String output = response.getContentAsString();
    log.debug(servlet.getClass().getSimpleName() + " returned: " + output);
    return output;
  }

  private static void assertReachedDatabase(String expected, String output, int request) {
    assertTrue(
        output.contains(expected),
        "Request " + request + " did not get a database result (\"" + expected + "\"): " + output);
  }

  /** One request to a level servlet, returning what it wrote. */
  private interface Call {
    String send() throws Exception;
  }

  /**
   * Makes every query a level runs fail after its servlet has borrowed a connection, by hiding the
   * level's users table, sends more requests than the pool holds, restores the table, and checks
   * that a normal request still reaches the database.
   */
  private static void assertReturnsConnectionsWhenQueriesFail(
      String schema, Call call, Predicate<String> reachedDatabase) throws Exception {
    String users = "`" + schema + "`.users";
    String hidden = "`" + schema + "`.usersHidden";
    try (Connection conn = Database.getCoreConnection(applicationRoot);
        Statement stmt = conn.createStatement()) {
      stmt.executeUpdate("RENAME TABLE " + users + " TO " + hidden);
      try {
        for (int i = 1; i <= REQUESTS; i++) {
          try {
            call.send();
          } catch (Exception e) {
            log.debug("Expected failure while the table is missing: " + e);
          }
        }
      } finally {
        stmt.executeUpdate("RENAME TABLE " + hidden + " TO " + users);
      }
    }
    String output = call.send();
    assertTrue(
        reachedDatabase.test(output),
        "A normal request did not reach the database after "
            + REQUESTS
            + " failed ones, so the servlet is keeping its connections: "
            + output);
  }

  @Test
  public void sessionManagement2LoginReturnsConnections() throws Exception {
    for (int i = 1; i <= REQUESTS; i++) {
      String output =
          doPost(new SessionManagement2(), null, "subName", noSuchUser, "subPassword", "password");
      assertReachedDatabase(text(2, "response.badUser"), output, i);
    }
  }

  @Test
  public void sessionManagement2ChangePasswordReturnsConnections() throws Exception {
    // A failed update is only logged, so the new password is missing from the response
    Pattern changed = Pattern.compile(Pattern.quote(text(2, "response.changedTo")) + " \\S+");
    for (int i = 1; i <= REQUESTS; i++) {
      String output =
          doPost(new SessionManagement2ChangePassword(), null, "subEmail", "nobody@example.com");
      assertTrue(
          changed.matcher(output).find(), "Request " + i + " did not change a password: " + output);
    }
  }

  @Test
  public void sessionManagement3LoginReturnsConnections() throws Exception {
    for (int i = 1; i <= REQUESTS; i++) {
      String output =
          doPost(
              new SessionManagement3(),
              null,
              "subUserName",
              noSuchUser,
              "subUserPassword",
              "password");
      assertReachedDatabase(text(3, "response.badUser"), output, i);
    }
  }

  @Test
  public void sessionManagement3ChangePasswordReturnsConnections() throws Exception {
    Cookie current = new Cookie("current", base64(base64(noSuchUser)));
    for (int i = 1; i <= REQUESTS; i++) {
      String output =
          doPost(new SessionManagement3ChangePassword(), current, "newPassword", "newPassword123");
      assertReachedDatabase(text(3, "reset.password"), output, i);
    }
  }

  @Test
  public void sessionManagement5LoginReturnsConnections() throws Exception {
    for (int i = 1; i <= REQUESTS; i++) {
      String output =
          doPost(
              new SessionManagement5(),
              null,
              "subUserName",
              noSuchUser,
              "subUserPassword",
              "password");
      assertReachedDatabase(text(5, "response.badUser"), output, i);
    }
  }

  @Test
  public void sessionManagement5ChangePasswordReturnsConnections() throws Exception {
    // The level accepts a token made of the current time, in the format it parses
    String token = base64(new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy").format(new Date()));
    for (int i = 1; i <= REQUESTS; i++) {
      String output =
          doPost(
              new SessionManagement5ChangePassword(),
              null,
              "userName",
              noSuchUser,
              "newPassword",
              "aLongEnoughNewPassword",
              "resetPasswordToken",
              token);
      assertReachedDatabase(text(5, "changePass.success"), output, i);
    }
  }

  @Test
  public void sessionManagement5SetTokenReturnsConnections() throws Exception {
    for (int i = 1; i <= REQUESTS; i++) {
      String output = doPost(new SessionManagement5SetToken(), null, "subUserName", noSuchUser);
      assertReachedDatabase(text(5, "response.badUser"), output, i);
    }
  }

  @Test
  public void sessionManagement2LoginReturnsConnectionsOnFailure() throws Exception {
    String badUser = text(2, "response.badUser");
    assertReturnsConnectionsWhenQueriesFail(
        "BrokenAuthAndSessMangChalTwo",
        () ->
            doPost(
                new SessionManagement2(), null, "subName", noSuchUser, "subPassword", "password"),
        output -> output.contains(badUser));
  }

  @Test
  public void sessionManagement2ChangePasswordReturnsConnectionsOnFailure() throws Exception {
    Pattern changed = Pattern.compile(Pattern.quote(text(2, "response.changedTo")) + " \\S+");
    assertReturnsConnectionsWhenQueriesFail(
        "BrokenAuthAndSessMangChalTwo",
        () ->
            doPost(new SessionManagement2ChangePassword(), null, "subEmail", "nobody@example.com"),
        output -> changed.matcher(output).find());
  }

  @Test
  public void sessionManagement3LoginReturnsConnectionsOnFailure() throws Exception {
    String badUser = text(3, "response.badUser");
    assertReturnsConnectionsWhenQueriesFail(
        "BrokenAuthAndSessMangChalThree",
        () ->
            doPost(
                new SessionManagement3(),
                null,
                "subUserName",
                noSuchUser,
                "subUserPassword",
                "password"),
        output -> output.contains(badUser));
  }

  @Test
  public void sessionManagement3ChangePasswordReturnsConnectionsOnFailure() throws Exception {
    Cookie current = new Cookie("current", base64(base64(noSuchUser)));
    String changed = text(3, "reset.password");
    assertReturnsConnectionsWhenQueriesFail(
        "BrokenAuthAndSessMangChalThree",
        () ->
            doPost(
                new SessionManagement3ChangePassword(), current, "newPassword", "newPassword123"),
        output -> output.contains(changed));
  }

  @Test
  public void sessionManagement5LoginReturnsConnectionsOnFailure() throws Exception {
    String badUser = text(5, "response.badUser");
    assertReturnsConnectionsWhenQueriesFail(
        "BrokenAuthAndSessMangChalFive",
        () ->
            doPost(
                new SessionManagement5(),
                null,
                "subUserName",
                noSuchUser,
                "subUserPassword",
                "password"),
        output -> output.contains(badUser));
  }

  @Test
  public void sessionManagement5ChangePasswordReturnsConnectionsOnFailure() throws Exception {
    String token = base64(new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy").format(new Date()));
    String changed = text(5, "changePass.success");
    assertReturnsConnectionsWhenQueriesFail(
        "BrokenAuthAndSessMangChalFive",
        () ->
            doPost(
                new SessionManagement5ChangePassword(),
                null,
                "userName",
                noSuchUser,
                "newPassword",
                "aLongEnoughNewPassword",
                "resetPasswordToken",
                token),
        output -> output.contains(changed));
  }

  @Test
  public void sessionManagement5SetTokenReturnsConnectionsOnFailure() throws Exception {
    String badUser = text(5, "response.badUser");
    assertReturnsConnectionsWhenQueriesFail(
        "BrokenAuthAndSessMangChalFive",
        () -> doPost(new SessionManagement5SetToken(), null, "subUserName", noSuchUser),
        output -> output.contains(badUser));
  }
}
