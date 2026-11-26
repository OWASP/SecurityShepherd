package servlets.module.challenge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import dbProcs.Database;
import dbProcs.GetterIT;
import dbProcs.Setter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
 * Each challenge schema has its own connection pool of at most three connections.
 * UrlAccess3UserList never returned its connection, so the fourth request hung for the pool timeout
 * and then failed until Tomcat restarted (#857).
 *
 * <p>Each test calls a servlet more times than the pool holds and checks every well-formed request
 * still reaches the database.
 *
 * <p>Those tests run the success path, which these servlets already closed correctly before the
 * fix. The "WhenQueriesFail" tests cover the exception path: they hide the table a level queries so
 * the query fails after the connection is borrowed, repeat more times than the pool holds, restore
 * the table, and check a normal request still reaches the database. A servlet that returns its
 * connection only when no exception was thrown fails them.
 */
public class MiscChallengesPoolLeakIT {

  private static final Logger log = LogManager.getLogger(MiscChallengesPoolLeakIT.class);
  private static final String lang = "en_GB";
  private static final String applicationRoot = "";
  private static final String userName = "lessonTester";

  /** More than the challenge pool's maximum size of three. */
  private static final int REQUESTS = 5;

  private final ResourceBundle errors =
      ResourceBundle.getBundle("i18n.servlets.errors", new Locale(lang));

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

  private static String base64(String value) {
    return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  private String doPost(HttpServlet servlet, Cookie cookie, String... params) throws Exception {
    return send("POST", servlet, cookie, params);
  }

  private String doGet(HttpServlet servlet, Cookie cookie, String... params) throws Exception {
    return send("GET", servlet, cookie, params);
  }

  /** Sends a request through the servlet; params are name/value pairs */
  private String send(String method, HttpServlet servlet, Cookie cookie, String... params)
      throws Exception {
    if (params.length % 2 != 0) {
      throw new IllegalArgumentException("params must be name/value pairs");
    }
    servlet.init(new MockServletConfig(servlet.getClass().getSimpleName()));
    MockHttpServletRequest request = new MockHttpServletRequest(method, "/");
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

  /** Something to run while a challenge table is hidden. */
  private interface Action {
    void run() throws Exception;
  }

  /**
   * Hides a challenge table so every query against it fails after its servlet has borrowed a
   * connection, runs the action, and always restores the table.
   */
  private static void whileTableMissing(String schema, String table, Action action)
      throws Exception {
    String shown = "`" + schema + "`.`" + table + "`";
    String hidden = "`" + schema + "`.`" + table + "Hidden`";
    try (Connection conn = Database.getCoreConnection(applicationRoot);
        Statement stmt = conn.createStatement()) {
      stmt.executeUpdate("RENAME TABLE " + shown + " TO " + hidden);
      try {
        action.run();
      } finally {
        stmt.executeUpdate("RENAME TABLE " + hidden + " TO " + shown);
      }
    }
  }

  /** Runs the action more times than the pool holds, ignoring the failures that are expected. */
  private static void repeatBeyondPool(Action action) {
    for (int i = 1; i <= REQUESTS; i++) {
      try {
        action.run();
      } catch (Exception e) {
        log.debug("Expected failure while the table is missing: " + e);
      }
    }
  }

  private String order(String coupon) throws Exception {
    return doPost(
        new BrokenCrypto4(),
        null,
        "pineappleAmount",
        "0",
        "orangeAmount",
        "1",
        "appleAmount",
        "0",
        "bananaAmount",
        "0",
        "couponCode",
        coupon);
  }

  @Test
  public void urlAccess3UserListSurvivesMalformedCookies() throws Exception {
    String funky = errors.getString("error.funky");
    // The unbalanced double quote in the cookie breaks the level's user-list query
    Cookie malformed = new Cookie("currentPerson", base64("\""));
    for (int i = 1; i <= REQUESTS; i++) {
      String output = doPost(new UrlAccess3UserList(), malformed);
      assertTrue(output.contains(funky), "Malformed cookie " + i + " did not fail: " + output);
    }
    Cookie guest = new Cookie("currentPerson", base64("aGuest"));
    for (int i = 1; i <= REQUESTS; i++) {
      String output = doPost(new UrlAccess3UserList(), guest);
      assertFalse(output.contains(funky), "Request " + i + " failed: " + output);
      assertTrue(
          output.contains("aGuest<br>"), "Request " + i + " did not list the users: " + output);
    }
  }

  @Test
  public void brokenCrypto4ReturnsConnections() throws Exception {
    String orderComplete =
        ResourceBundle.getBundle(
                "i18n.servlets.challenges.insecureCryptoStorage.insecureCryptoStorage",
                new Locale(lang))
            .getString("insecureCryptoStorage.4.orderComplete");
    for (int i = 1; i <= REQUESTS; i++) {
      String output =
          doPost(
              new BrokenCrypto4(),
              null,
              "pineappleAmount",
              "1",
              "orangeAmount",
              "0",
              "appleAmount",
              "0",
              "bananaAmount",
              "0",
              "couponCode",
              "noSuchCoupon");
      assertTrue(output.contains(orderComplete), "Order " + i + " did not complete: " + output);
    }
  }

  /**
   * CsrfChallengeSixGetToken isn't covered: it is not mapped in web.xml, so players can't reach it
   */
  @Test
  public void csrfChallengeTokenListsReturnConnections() throws Exception {
    String noToken =
        ResourceBundle.getBundle("i18n.servlets.challenges.csrf.csrfGenerics", new Locale(lang))
            .getString("error.noToken");
    for (int i = 1; i <= REQUESTS; i++) {
      String seven = doGet(new CsrfChallengeSevenGetToken(), null, "userId", "noSuchPoolLeakUser");
      assertTrue(seven.contains("Your csrf Token for this Challenge is: "), seven);
      assertFalse(seven.contains(noToken), "CSRF 7 request " + i + " failed: " + seven);
    }
  }

  @Test
  public void securityMisconfigStealTokensReturnsConnections() throws Exception {
    String userId = "poolLeakTestUserId";
    for (int i = 1; i <= REQUESTS; i++) {
      String token = SecurityMisconfigStealTokens.getUserToken(userId, applicationRoot);
      assertFalse(token.isEmpty(), "Request " + i + " returned no token");
      assertFalse(
          SecurityMisconfigStealTokens.validToken(userId, token, applicationRoot),
          "A user's own token must not count as stolen");
    }
  }

  @Test
  public void brokenCrypto4ReturnsConnectionsWhenQueriesFail() throws Exception {
    String freeOranges =
        ResourceBundle.getBundle(
                "i18n.servlets.challenges.insecureCryptoStorage.insecureCryptoStorage",
                new Locale(lang))
            .getString("insecureCryptoStorage.4.freeOranges");
    whileTableMissing("CryptShop", "coupons", () -> repeatBeyondPool(() -> order("noSuchCoupon")));
    // The level's free-oranges coupon only takes effect if the coupon lookup reaches the database
    String output = order("e!c!3etZoumo@Stu4rU176");
    assertTrue(
        output.contains(freeOranges),
        "The coupon lookup did not reach the database after "
            + REQUESTS
            + " failed ones, so the servlet is keeping its connections: "
            + output);
  }

  @Test
  public void csrfChallengeSevenGetTokenReturnsConnectionsWhenQueriesFail() throws Exception {
    String noToken =
        ResourceBundle.getBundle("i18n.servlets.challenges.csrf.csrfGenerics", new Locale(lang))
            .getString("error.noToken");
    whileTableMissing(
        "csrfChallengeEnumTokens",
        "csrfTokens",
        () ->
            repeatBeyondPool(
                () ->
                    doGet(new CsrfChallengeSevenGetToken(), null, "userId", "noSuchPoolLeakUser")));
    String output = doGet(new CsrfChallengeSevenGetToken(), null, "userId", "noSuchPoolLeakUser");
    assertTrue(
        output.contains("Your csrf Token for this Challenge is: ") && !output.contains(noToken),
        "The token lookup did not reach the database after "
            + REQUESTS
            + " failed ones, so the servlet is keeping its connections: "
            + output);
  }

  @Test
  public void securityMisconfigGetUserTokenReturnsConnectionsWhenQueriesFail() throws Exception {
    String userId = "poolLeakTestUserId";
    whileTableMissing(
        "securityMisconfigStealToken",
        "tokens",
        () ->
            repeatBeyondPool(
                () -> SecurityMisconfigStealTokens.getUserToken(userId, applicationRoot)));
    assertFalse(
        SecurityMisconfigStealTokens.getUserToken(userId, applicationRoot).isEmpty(),
        "getUserToken did not reach the database after "
            + REQUESTS
            + " failed calls, so it is keeping its connections");
  }

  @Test
  public void securityMisconfigValidTokenReturnsConnectionsWhenQueriesFail() throws Exception {
    String userId = "poolLeakTestUserId";
    String token = SecurityMisconfigStealTokens.getUserToken(userId, applicationRoot);
    whileTableMissing(
        "securityMisconfigStealToken",
        "tokens",
        () ->
            repeatBeyondPool(
                () -> SecurityMisconfigStealTokens.validToken(userId, token, applicationRoot)));
    assertFalse(
        SecurityMisconfigStealTokens.validToken(userId, token, applicationRoot),
        "A user's own token must not count as stolen");
  }

  /**
   * CsrfChallengeTargetFour's token check is private and only fails on database errors, which a
   * player cannot trigger, so it is called directly.
   */
  @Test
  public void csrfChallengeFourTokenCheckReturnsConnectionsWhenQueriesFail() throws Exception {
    String token = "poolLeakCsrfFourToken";
    Setter.setCsrfChallengeFourCsrfToken("poolLeakCsrfFourUser", token, applicationRoot);
    Method validCsrfToken =
        CsrfChallengeTargetFour.class.getDeclaredMethod(
            "validCsrfToken", String.class, String.class);
    validCsrfToken.setAccessible(true);
    whileTableMissing(
        "csrfChallengeFour",
        "csrfTokens",
        () -> repeatBeyondPool(() -> validCsrfToken.invoke(null, applicationRoot, token)));
    assertTrue(
        (boolean) validCsrfToken.invoke(null, applicationRoot, token),
        "The token check did not reach the database after "
            + REQUESTS
            + " failed ones, so it is keeping its connections");
  }
}
