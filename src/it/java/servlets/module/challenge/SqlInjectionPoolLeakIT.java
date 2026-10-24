package servlets.module.challenge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import dbProcs.GetterIT;
import dbProcs.Setter;
import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.MissingResourceException;
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
import servlets.module.lesson.SqlInjectionLesson;
import testUtils.TestProperties;

/**
 * Each challenge schema has its own connection pool of at most three connections. A servlet that
 * does not return its connection when an injected query fails takes that challenge offline after
 * three bad submissions, until Tomcat restarts (#857, #931).
 *
 * <p>Each test sends more malformed submissions than the pool holds, then a well-formed one that
 * must still reach the database. Without the fix, the well-formed submission times out waiting for
 * a connection and the servlet shows its error message instead.
 */
public class SqlInjectionPoolLeakIT {

  private static final Logger log = LogManager.getLogger(SqlInjectionPoolLeakIT.class);
  private static final String lang = "en_GB";
  private static final String applicationRoot = "";
  private static final String userName = "lessonTester";

  /** More than the challenge pool's maximum size of three. */
  private static final int BAD_REQUESTS = 5;

  private static final String SQL_SYNTAX_ERROR = "SQL syntax";

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

  private static ResourceBundle bundle(String name) {
    return ResourceBundle.getBundle(name, new Locale(lang));
  }

  private String doPost(HttpServlet servlet, String... params) throws Exception {
    servlet.init(new MockServletConfig(servlet.getClass().getSimpleName()));
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/");
    request.setSession(session);
    for (int i = 0; i < params.length; i += 2) {
      request.addParameter(params[i], params[i + 1]);
    }
    MockHttpServletResponse response = new MockHttpServletResponse();
    servlet.service(request, response);
    String output = response.getContentAsString();
    log.debug(servlet.getClass().getSimpleName() + " returned: " + output);
    return output;
  }

  @Test
  public void sqlInjection5CouponCheckSurvivesMalformedCoupons() throws Exception {
    for (int i = 0; i < BAD_REQUESTS; i++) {
      try {
        doPost(new SqlInjection5CouponCheck(), "couponCode", "'");
      } catch (MissingResourceException e) {
        // The level's error handler looks up a message key that sqli5.properties does not have,
        // so a failed query currently ends in MissingResourceException. Only the pool matters
        // here.
        log.debug("Expected error from malformed coupon: " + e);
      }
    }
    String output = doPost(new SqlInjection5CouponCheck(), "couponCode", "noSuchCoupon");
    assertTrue(
        output.contains(
            bundle("i18n.servlets.challenges.sqli.sqli5").getString("response.noCoupon")),
        "Coupon check did not reach the database after malformed coupons: " + output);
  }

  @Test
  public void sqlInjection5VipCheckSurvivesMalformedCoupons() throws Exception {
    String checkFailed =
        bundle("i18n.servlets.challenges.sqli.sqli5").getString("response.checkFailed");
    for (int i = 0; i < BAD_REQUESTS; i++) {
      String output = doPost(new SqlInjection5VipCheck(), "couponCode", "'");
      assertTrue(output.contains(checkFailed), "Malformed VIP coupon did not fail: " + output);
    }
    String output = doPost(new SqlInjection5VipCheck(), "couponCode", "noSuchCoupon");
    assertTrue(
        output.contains(
            bundle("i18n.servlets.challenges.sqli.sqli5").getString("response.noCoupon")),
        "VIP coupon check did not reach the database after malformed coupons: " + output);
  }

  @Test
  public void sqlInjection6SurvivesMalformedPins() throws Exception {
    ResourceBundle sqli6 = bundle("i18n.servlets.challenges.sqli.sqli6");
    String badRequest = sqli6.getString("response.badRequest");
    for (int i = 0; i < BAD_REQUESTS; i++) {
      // \x27 is decoded to a single quote after the level strips quotes, breaking the query
      String output = doPost(new SqlInjection6(), "pinNumber", "\\x27");
      assertTrue(output.contains(badRequest), "Malformed pin did not fail: " + output);
    }
    String output = doPost(new SqlInjection6(), "pinNumber", "0");
    assertFalse(
        output.contains(badRequest),
        "SqlInjection6 did not reach the database after malformed pins: " + output);
    assertTrue(output.contains("<h3>"), "SqlInjection6 gave no login result: " + output);
  }

  @Test
  public void sqlInjection7SurvivesMalformedEmails() throws Exception {
    ResourceBundle sqli7 = bundle("i18n.servlets.challenges.sqli.sqli7");
    String incorrectCreds = sqli7.getString("response.incorrectCreds");
    String badRequest = sqli7.getString("response.badRequest");
    for (int i = 0; i < BAD_REQUESTS; i++) {
      String output =
          doPost(new SqlInjection7(), "subEmail", "a'b@example.com", "subPassword", "password");
      assertTrue(output.contains(incorrectCreds), "Malformed email did not fail: " + output);
    }
    String output =
        doPost(new SqlInjection7(), "subEmail", "nobody@example.com", "subPassword", "password");
    assertTrue(output.contains(incorrectCreds), "SqlInjection7 gave no login result: " + output);
    assertFalse(
        output.contains(badRequest),
        "SqlInjection7 did not reach the database after malformed emails: " + output);
  }

  @Test
  public void sqlInjectionEmailSurvivesMalformedEmails() throws Exception {
    for (int i = 0; i < BAD_REQUESTS; i++) {
      String output = doPost(new SqlInjectionEmail(), "userIdentity", "a'b@example.com");
      assertTrue(output.contains(SQL_SYNTAX_ERROR), "Malformed email did not fail: " + output);
    }
    String output = doPost(new SqlInjectionEmail(), "userIdentity", "nobody@example.com");
    assertTrue(
        output.contains(
            bundle("i18n.servlets.challenges.sqli.sqliEmail").getString("response.noResults")),
        "SqlInjectionEmail did not reach the database after malformed emails: " + output);
    assertFalse(output.contains(errors.getString("error.detected")), output);
  }

  @Test
  public void sqlInjectionEscapingSurvivesMalformedIds() throws Exception {
    for (int i = 0; i < BAD_REQUESTS; i++) {
      // A trailing backslash escapes the closing quote the level adds
      String output = doPost(new SqlInjectionEscaping(), "aUserId", "\\");
      assertTrue(output.contains(SQL_SYNTAX_ERROR), "Malformed id did not fail: " + output);
    }
    String output = doPost(new SqlInjectionEscaping(), "aUserId", "noSuchCustomer");
    assertTrue(
        output.contains(
            bundle("i18n.servlets.challenges.sqli.sqliEscaping").getString("response.noResults")),
        "SqlInjectionEscaping did not reach the database after malformed ids: " + output);
    assertFalse(output.contains(errors.getString("error.detected")), output);
  }

  @Test
  public void sqlInjectionStoredProcedureSurvivesMalformedIds() throws Exception {
    for (int i = 0; i < BAD_REQUESTS; i++) {
      String output = doPost(new SqlInjectionStoredProcedure(), "userIdentity", "'");
      assertTrue(output.contains(SQL_SYNTAX_ERROR), "Malformed id did not fail: " + output);
    }
    String output = doPost(new SqlInjectionStoredProcedure(), "userIdentity", "noSuchCustomer");
    assertTrue(
        output.contains(
            bundle("i18n.servlets.challenges.sqli.sqliStoreProcedure")
                .getString("response.noResults")),
        "SqlInjectionStoredProcedure did not reach the database after malformed ids: " + output);
    assertFalse(output.contains(errors.getString("error.detected")), output);
  }

  @Test
  public void sqlInjectionLessonSurvivesRepeatedSearches() throws Exception {
    // The lesson used to keep every connection it borrowed, not only after failed queries
    for (int i = 0; i < BAD_REQUESTS; i++) {
      String output = doPost(new SqlInjectionLesson(), "aUserName", "'");
      assertTrue(output.contains(SQL_SYNTAX_ERROR), "Malformed name did not fail: " + output);
    }
    for (int i = 0; i < BAD_REQUESTS; i++) {
      String output = doPost(new SqlInjectionLesson(), "aUserName", "noSuchUser");
      assertTrue(
          output.contains(
              bundle("i18n.servlets.lessons.sqlInjection").getString("response.noResults")),
          "SqlInjectionLesson did not reach the database on search " + (i + 1) + ": " + output);
    }
  }
}
