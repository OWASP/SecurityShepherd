package servlets.module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import dbProcs.GetterIT;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import testUtils.LogCapture;
import testUtils.TestProperties;
import utils.Validate;

public class RefreshMenuIT {

  private static final String lang = "en_GB";
  private static final Logger log = LogManager.getLogger(RefreshMenuIT.class);
  private static final String applicationRoot = "";

  @BeforeAll
  public static void resetDatabase() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);
    TestProperties.createMysqlResource();
    TestProperties.ensureSchemaReady(log);
    TestProperties.reseedTestData();
  }

  /**
   * Refreshing the menu is something every player does, so it must not raise the unauthorised admin
   * access alert (#735)
   */
  @Test
  public void testRefreshMenuAsPlayerLogsNoAdminAlert()
      throws SQLException, ServletException, IOException {
    String userName = "refreshMenu1";
    GetterIT.verifyTestUser(applicationRoot, userName, userName);

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    TestProperties.loginDoPost(log, request, response, userName, userName, null, lang);
    if (response.getCookie("token") == null || response.getCookie("token").getValue().isEmpty()) {
      fail("No CSRF Token Was Returned from Login Servlet");
    }
    String csrfToken = response.getCookie("token").getValue();
    request.setCookies(response.getCookies());
    request.addParameter("csrfToken", csrfToken);

    RefreshMenu servlet = new RefreshMenu();
    servlet.init(new MockServletConfig("RefreshMenu"));
    MockHttpServletResponse menuResponse = new MockHttpServletResponse();
    try (LogCapture validateLogs = LogCapture.of(Validate.class)) {
      servlet.doPost(request, menuResponse);
      assertTrue(
          menuResponse.getContentAsString().contains("makeSearchList()"),
          "Expected the module menu, got: " + menuResponse.getContentAsString());
      assertEquals(List.of(), validateLogs.messagesAtOrAbove(Level.WARN));
    }
  }
}
