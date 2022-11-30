package dbProcs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import testUtils.TestProperties;
import utils.ScoreboardStatus;

public class SetterTest {

  private static final Logger log = LogManager.getLogger(SetterTest.class);
  private static String applicationRoot = new String();

  /** Creates DB or Restores DB to Factory Defaults before running tests */
  @BeforeClass
  public static void resetDatabase() throws IOException, SQLException {
    TestProperties.setTestPropertiesFileDirectory(log);

    TestProperties.createMysqlResource();

    TestProperties.executeSql(log);
  }

  /**
   * Test to ensure class's can be created with this method. Other Unit Tests use this method, but
   * not nessisarily every time, as a class may already exist. This Method creates a random class
   * name so it can run every time without failure
   *
   * @throws SQLException
   */
  @Test
  public void testClassCreate() throws SQLException {
    Random rand = new Random();
    String className = "newC" + rand.nextInt(50) + rand.nextInt(50) + rand.nextInt(50);
    if (!Setter.classCreate(applicationRoot, className, "2015")) {
      TestProperties.failAndPrint("Could not Create Class");
    } else {

      boolean pass = false;
      ResultSet rs = Getter.getClassInfo(applicationRoot);
      while (rs.next()) {
        if (rs.getString(2).equalsIgnoreCase(className)) {
          pass = true;
          break;
        }
      }
      if (!pass) {
        TestProperties.failAndPrint("Could not find class in DB");
      } else {
        return; // PASS
      }
    }
  }

  @Test
  public void testIncrementBadSubmission() throws SQLException {
    String moduleId = "853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"; // CSRF7
    String userName = new String("BadSubUser");

    if (GetterTest.verifyTestUser(applicationRoot, userName, userName)) {
      String userId = Getter.getUserIdFromName(applicationRoot, userName);
      if (!Setter.openAllModules(applicationRoot, false)
          && !Setter.openAllModules(applicationRoot, true)) {
        TestProperties.failAndPrint("Could not mark all modules as open");
      } else {
        // Simulate user Opening Level
        if (Getter.getModuleAddress(applicationRoot, moduleId, userId).isEmpty()) {
          TestProperties.failAndPrint("Could not Simulate Opening First Level for User");
        } else {
          String markLevelCompleteTest =
              Setter.updatePlayerResult(
                  applicationRoot, moduleId, userId, "Feedback is Disabled", 1, 1, 1);
          if (markLevelCompleteTest != null) {
            // Giving the User a Score Bump in case they have already completed CSRF7 and
            // this is the 20th time the unit test has run
            if (!Setter.updateUserPoints(applicationRoot, userId, 20)) {
              TestProperties.failAndPrint("Could not give user extra points");
            }

            int scoreBefore = 0;
            ScoreboardStatus.setScoreboardOpen();
            String scoreboardData = Getter.getJsonScore(applicationRoot, "");
            if (scoreboardData.isEmpty()) {
              TestProperties.failAndPrint(
                  "Could not detect user in scoreboard before bad submission test");
            } else {
              JSONArray scoreboardJson = new JSONArray(scoreboardData);
              // Loop through array to find Our user
              for (int i = 0; i < scoreboardJson.length(); i++) {
                log.debug("Looping through Array " + i);
                JSONObject scoreRowJson = (JSONObject) scoreboardJson.get(i);
                if (scoreRowJson.get("username").toString().compareTo(userName) == 0) {
                  log.debug("Found user with score: " + scoreRowJson.get("score"));
                  scoreBefore = Integer.parseInt(scoreRowJson.get("score").toString());
                  break;
                }
              }
              if (scoreBefore == 0) {
                log.fatal("Could not find user " + userName + " with score > 0: " + scoreboardData);
                TestProperties.failAndPrint("User has score of 0 before BadSubmission Emulation");
              }

              // Resetting resetBadSubmission count back to 0
              if (!Setter.resetBadSubmission(applicationRoot, userId)) {
                TestProperties.failAndPrint("Could not Reset bad submission count");
              }
              // Simulating 41 bad submissions
              for (int i = 0; i <= 40; i++) {
                Setter.incrementBadSubmission(applicationRoot, userId);
              }

              // Check Score again
              int scoreAfter = 0;
              scoreboardData = Getter.getJsonScore(applicationRoot, "");
              scoreboardJson = new JSONArray(scoreboardData);
              // Loop through array to find Our user
              for (int i = 0; i < scoreboardJson.length(); i++) {
                log.debug("Looping through Array " + i);
                JSONObject scoreRowJson = (JSONObject) scoreboardJson.get(i);
                if (scoreRowJson.get("username").toString().compareTo(userName) == 0) {
                  log.debug("Found user with score: " + scoreRowJson.get("score"));
                  scoreAfter = Integer.parseInt(scoreRowJson.get("score").toString());
                  break;
                }
              }

              int expectedAfter = scoreBefore - (scoreBefore / 10);
              log.debug("expected score: " + expectedAfter);
              if (scoreAfter
                  != expectedAfter) // Checking exact number should be equal to and number
              // below as well incase rounded d
              {
                log.debug("score before: " + scoreBefore);
                log.debug("score after : " + scoreAfter);
                log.debug("Expected After: " + expectedAfter);
                int roundedUp = scoreAfter + 1;
                if (roundedUp != expectedAfter) {
                  TestProperties.failAndPrint("Invalid Score Deduction Detected");
                } else {
                  return; // PASS
                }
              } else {
                return; // Pass
              }
            }
          } else {
            TestProperties.failAndPrint("Could not Mark First level as complete");
          }
        }
      }
    } else {
      TestProperties.failAndPrint("Could not Create/Verify User");
    }
  }

  @Test
  public void testOpenOnlyMobileCategories() {
    if (!Setter.openOnlyMobileCategories(applicationRoot)) {
      TestProperties.failAndPrint("Could not Open Only Mobile Categories");
    }
  }

  @Test
  public void testOpenOnlyWebCategories() {
    if (!Setter.openOnlyWebCategories(applicationRoot, 0)) {
      TestProperties.failAndPrint("Could not Open Only Web Categories");
    }
  }

  @Test
  public void testResetBadSubmission() throws SQLException {
    String moduleId = "853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"; // CSRF7
    String userName = new String("BadSubResetUser");

    if (GetterTest.verifyTestUser(applicationRoot, userName, userName)) {
      String userId = Getter.getUserIdFromName(applicationRoot, userName);
      if (!Setter.openAllModules(applicationRoot, false)
          && !Setter.openAllModules(applicationRoot, true)) {
        TestProperties.failAndPrint("Could not mark all modules as open");
      } else {
        // Simulate user Opening Level
        if (Getter.getModuleAddress(applicationRoot, moduleId, userId).isEmpty()) {
          TestProperties.failAndPrint("Could not Simulate Opening First Level for User");
        } else {
          String markLevelCompleteTest =
              Setter.updatePlayerResult(
                  applicationRoot, moduleId, userId, "Feedback is Disabled", 1, 1, 1);
          if (markLevelCompleteTest != null) {
            int scoreBefore = 0;
            ScoreboardStatus.setScoreboardOpen();
            String scoreboardData = Getter.getJsonScore(applicationRoot, "");
            if (scoreboardData.isEmpty()) {
              fail("Could not detect user in scoreboard before bad submission test");
            } else {
              JSONArray scoreboardJson = new JSONArray(scoreboardData);
              // Loop through array to find Our user
              for (int i = 0; i < scoreboardJson.length(); i++) {
                JSONObject scoreRowJson = (JSONObject) scoreboardJson.get(i);
                if (scoreRowJson.get("username").toString().compareTo(userName) == 0) {
                  log.debug("Found user with score: " + scoreRowJson.get("score"));
                  scoreBefore = Integer.parseInt(scoreRowJson.get("score").toString());
                  break;
                }
              }
              if (scoreBefore == 0) {
                log.fatal("Could not find user " + userName + " with score > 0: " + scoreboardData);
                TestProperties.failAndPrint("User has score of 0 before BadSubmission Emulation");
              }

              // Resetting resetBadSubmission count back to 0
              if (!Setter.resetBadSubmission(applicationRoot, userId)) {
                TestProperties.failAndPrint("Could not Reset bad submission count");
              }
              // Simulating 40 bad submissions
              for (int i = 0; i < 40; i++) {
                if (!Setter.incrementBadSubmission(applicationRoot, userId)) {
                  TestProperties.failAndPrint("Could not Increment Bad Submission Counter");
                }
              }
              // Resetting Bad Submission Count back to 0 again
              if (!Setter.resetBadSubmission(applicationRoot, userId)) {
                TestProperties.failAndPrint("Could not Reset bad submission count");
              }
              // Incrementing one more time (Should set user bad submission counter to 1)
              if (!Setter.incrementBadSubmission(applicationRoot, userId)) {
                TestProperties.failAndPrint("Could not Increment Bad Submission Counter");
              }

              // Check Score again
              int scoreAfter = 0;
              scoreboardData = Getter.getJsonScore(applicationRoot, "");
              scoreboardJson = new JSONArray(scoreboardData);
              // Loop through array to find Our user
              for (int i = 0; i < scoreboardJson.length(); i++) {
                log.debug("Looping through Array " + i);
                JSONObject scoreRowJson = (JSONObject) scoreboardJson.get(i);
                if (scoreRowJson.get("username").toString().compareTo(userName) == 0) {
                  log.debug("Found user with score: " + scoreRowJson.get("score"));
                  scoreAfter = Integer.parseInt(scoreRowJson.get("score").toString());
                  break;
                }
              }

              if (scoreAfter != scoreBefore) // Checking exact number should be equal to and number
              // below as well incase rounded d
              {
                log.debug("score before: " + scoreBefore);
                log.debug("score after : " + scoreAfter);
                TestProperties.failAndPrint("Invalid Score Deduction Detected");
              } else {
                return; // Pass
              }
            }
          } else {
            TestProperties.failAndPrint("Could not Mark First level as complete");
          }
        }
      }
    } else {
      TestProperties.failAndPrint("Could not Create/Verify User");
    }
  }

  @Test
  public void testSetCsrfChallengeFourCsrfToken() throws SQLException {
    String userName = new String("csrfFourUser");

    if (GetterTest.verifyTestUser(applicationRoot, userName, userName)) {
      String userId = Getter.getUserIdFromName(applicationRoot, userName);
      String csrfTokenValue = new String("CsrfTokenTest");
      String csrfToken =
          Setter.setCsrfChallengeFourCsrfToken(userId, csrfTokenValue, applicationRoot);
      if (csrfToken.compareTo(csrfTokenValue) != 0) {
        fail("Retrieved CSRF token did not Match the Set Value");
      }
    } else {
      fail("Could not Verify User");
    }
  }

  @Test
  public void testSetCsrfChallengeSevenCsrfToken() throws SQLException {
    String userName = new String("csrfSevenUser");

    if (GetterTest.verifyTestUser(applicationRoot, userName, userName)) {
      String userId = Getter.getUserIdFromName(applicationRoot, userName);
      String csrfToken = new String("CsrfTokenTest");
      if (!Setter.setCsrfChallengeSevenCsrfToken(userId, csrfToken, applicationRoot)) {
        fail("Could not Set CSRF Challenge 7 Token");
      }
    } else {
      fail("Could not Verify User");
    }
  }

  @Test
  public void testSetModuleCategoryStatusOpen() throws SQLException {
    String moduleCategory = new String("Injection");
    if (!Setter.closeAllModules(applicationRoot)) {
      fail("Could not Mark all modules as closed");
    } else if (!Setter.setModuleCategoryStatusOpen(applicationRoot, moduleCategory, "open")) {
      fail("Could not Open module Category");
    } else {
      Connection conn = Database.getCoreConnection(applicationRoot);

      log.debug("Getting Number of Mobile Levels From DB");
      PreparedStatement prepStatement =
          conn.prepareStatement(
              "SELECT DISTINCT moduleCategory FROM modules WHERE moduleStatus = 'open';");
      ResultSet rs = prepStatement.executeQuery();
      while (rs.next()) {
        if (rs.getString(1).compareTo(moduleCategory) != 0) {
          log.debug("Found Category that wa snot injection: " + rs.getString(1));
          fail("Detected Category that was not Injection Open");
        }
      }
    }
  }

  @Test
  public void testSetModuleCategoryStatusClosed() throws SQLException {
    String moduleCategory = new String("Injection");
    if (!Setter.openAllModules(applicationRoot, false)
        && !Setter.openAllModules(applicationRoot, true)) {
      fail("Could not Mark all modules as open");
    } else if (!Setter.setModuleCategoryStatusOpen(applicationRoot, moduleCategory, "closed")) {
      fail("Could not close module Category");
    } else {
      Connection conn = Database.getCoreConnection(applicationRoot);

      log.debug("Getting Number of Mobile Levels From DB");
      PreparedStatement prepStatement =
          conn.prepareStatement(
              "SELECT DISTINCT moduleCategory FROM modules WHERE moduleStatus = 'closed';");
      ResultSet rs = prepStatement.executeQuery();
      while (rs.next()) {
        if (rs.getString(1).compareTo(moduleCategory) != 0) {
          log.debug("Found Category that wa snot injection: " + rs.getString(1));
          fail("Detected Category that was not Injection Closed");
        }
      }
    }
  }

  @Test
  public void testSetModuleStatusClosed() throws SQLException {
    String moduleId = new String("853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"); // CSRF 7
    if (!Setter.openAllModules(applicationRoot, false)) {
      fail("Could not Mark all modules as open");
    } else if (!Setter.setModuleStatusClosed(applicationRoot, moduleId)) {
      fail("Could not close CSRF 7 Module");
    } else {
      Connection conn = Database.getCoreConnection(applicationRoot);

      log.debug("Getting Number of Mobile Levels From DB");
      PreparedStatement prepStatement =
          conn.prepareStatement("SELECT moduleStatus FROM modules WHERE moduleId = ?");
      prepStatement.setString(1, moduleId);
      ResultSet rs = prepStatement.executeQuery();
      if (rs.next()) {
        if (rs.getString(1).compareTo("closed") != 0) {
          log.debug("Module was not closed by method");
          fail("Module was not closed by method");
        }
      }
    }
  }

  @Test
  public void testSetModuleStatusOpen() throws SQLException {
    String moduleId = new String("853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"); // CSRF 7
    if (!Setter.closeAllModules(applicationRoot)) {
      fail("Could not Mark all modules as closed");
    } else if (!Setter.setModuleStatusOpen(applicationRoot, moduleId)) {
      fail("Could not close CSRF 7 Module");
    } else {
      Connection conn = Database.getCoreConnection(applicationRoot);

      log.debug("Getting Number of Mobile Levels From DB");
      PreparedStatement prepStatement =
          conn.prepareStatement("SELECT moduleStatus FROM modules WHERE moduleId = ?");
      prepStatement.setString(1, moduleId);
      ResultSet rs = prepStatement.executeQuery();
      if (rs.next()) {
        if (rs.getString(1).compareTo("open") != 0) {
          log.debug("Module was not opened by method");
          fail("Module was not opened by method");
        }
      }
    }
  }

  @Test
  public void testSetStoredMessage() throws SQLException {
    log.debug("Testing Set Stored message");
    String userName = new String("storedMessageUser");
    String className = new String("sMessageClass");
    String moduleId = new String("853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"); // CSRF 7
    String message = new String("TestStoredMessage");

    log.debug("Getting class id");
    String classId = GetterTest.findCreateClassId(className, applicationRoot);
    log.debug("Checking User Name in DB");
    if (GetterTest.verifyTestUser(applicationRoot, userName, userName, classId)) {
      // Open all Modules First so that the Module Can Be Opened
      if (!Setter.openAllModules(applicationRoot, false)) {
        fail("Could not open all modules");
      }
      String userId = Getter.getUserIdFromName(applicationRoot, userName);
      // Simulate user Opening Level
      if (Getter.getModuleAddress(applicationRoot, moduleId, userId).isEmpty()) {
        fail("Could not Simulate Opening First Level for User");
      } else {
        Setter.setStoredMessage(applicationRoot, message, userId, moduleId);
        Connection conn = Database.getCoreConnection(applicationRoot);

        CallableStatement callstmt = conn.prepareCall("call resultMessageByClass(?, ?)");
        log.debug("Gathering resultMessageByClass ResultSet");
        callstmt.setString(1, classId);
        callstmt.setString(2, moduleId);
        ResultSet resultSet = callstmt.executeQuery();
        log.debug("resultMessageByClass executed");
        while (resultSet.next()) {
          if (resultSet.getString(1).compareTo(userName) == 0) {
            if (resultSet.getString(2).compareTo(message) != 0) {
              fail("Stored Message does not equal the one set");
            } else {
              return; // Pass
            }
          }
        }
        fail("Could not find user stored message");
      }
    } else {
      fail("Could not verify test User");
    }
  }

  @Test
  public void testSuspendUser() throws SQLException {
    String userName = new String("suspendedUser");

    log.debug("Checking User Name in DB");
    boolean loggedIn = false;
    try {
      log.debug("Trying to Verify User");
      loggedIn = GetterTest.verifyTestUser(applicationRoot, userName, userName);
    } catch (SQLException e) {
      log.debug("Could not verify. May be suspended. Unsuspending");
      // Might need to unsuspend player
      Setter.unSuspendUser(applicationRoot, Getter.getUserIdFromName(applicationRoot, userName));
      // Gotta Sleep for a sec otherwise the time setting for suspension will fail
      // test. Must be 1 sec after unsuspend function ran
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e1) {
        // Ignore if we're interrupted
        log.debug("Sleep was interrupted, continuing anyway...");
      }
      loggedIn = GetterTest.verifyTestUser(applicationRoot, userName, userName);
    }
    if (!loggedIn) {
      fail("Could not Verify User");
    } else {
      String userId = Getter.getUserIdFromName(applicationRoot, userName);
      if (!Setter.suspendUser(applicationRoot, userId, 10)) {
        fail("Could not suspend User");
      } else {
        String user[] = Getter.authUser(applicationRoot, userName, userName);
        if (user == null || user[0].isEmpty()) {
          return; // PASS: User Could not Authenticate after suspension
        } else {
          TestProperties.failAndPrint("Fail: could still authenticate as user after suspension");
        }
      }
    }
  }

  @Test
  public void testUnSuspendUser() throws SQLException {
    String userName = new String("UnsuspendedUser");

    log.debug("Checking User Name in DB");
    if (!GetterTest.verifyTestUser(applicationRoot, userName, userName)) {
      fail("Could not Verify User");
    } else {
      String userId = Getter.getUserIdFromName(applicationRoot, userName);
      if (!Setter.suspendUser(applicationRoot, userId, 10)) {
        fail("Could not suspend User");
      } else {
        if (!Setter.unSuspendUser(applicationRoot, userId)) {
          fail("Could not unsusepend user");
        } else {
          // Gotta Sleep for a sec, otherwise the time compair will round down and user
          // auth will fail. User is unsuspended 1 second after unsuspend funciton
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            // Ignore if we're interrupted
            log.debug("Sleep was interrupted, continuing anyway...");
          }
          String user[] = Getter.authUser(applicationRoot, userName, userName);
          if (user == null || user[0].isEmpty()) {
            fail("Could not Authenticate after unsuspension");
          } else {
            return; // PASS: User Could not Authenticate after unsuspension
          }
        }
      }
    }
  }

  @Test
  public void testUpdateUsername() {
    log.debug("Testing update Password");
    String userName = new String("updateUsernameTest");
    String newUsername = new String("newUpdatedUsernameTest");

    String password = new String("justaTestingPassword");

    boolean loggedIn = false;

    try {
      log.debug("Logging in as test user " + userName);
      loggedIn = GetterTest.verifyTestUser(applicationRoot, userName, password);
    } catch (SQLException e) {
      loggedIn = false;
      TestProperties.failAndPrint("Could not log in with default pass: " + e.toString());
    }
    if (!loggedIn) {
      TestProperties.failAndPrint("Could not sign in as the test user.");
    } else {
      log.debug("Logged in! Updating Username now");
      if (!Setter.updateUsername(applicationRoot, userName, newUsername)) {
        TestProperties.failAndPrint("Could not update username.");
      } else {
        log.debug("Username Updated: " + newUsername + ", testing auth as new name");
        String user[];

        log.debug("Logging in with new username");
        user = Getter.authUser(applicationRoot, newUsername, password);

        if (user != null && !user[0].isEmpty()) {
          log.debug("Pass: Could log in with new username");

        } else {
          TestProperties.failAndPrint("Could not sign in as the test user.");
        }
      }
    }
  }

  @Test
  public void testUpdatePassword() {
    log.debug("Testing update Password");
    String userName = new String("updatePassword");
    String currentPass = new String();
    String newPass = new String();
    boolean loggedIn = false;

    try {
      currentPass = userName;
      newPass = userName + userName;
      log.debug("Logging in with default Pass");
      loggedIn = GetterTest.verifyTestUser(applicationRoot, userName, currentPass);
    } catch (SQLException e) {
      newPass = userName;
      currentPass = userName + userName;
      log.debug("Could not log in with default pass: " + e.toString());
      log.debug("Logging in with alternative pass: " + currentPass);
      String[] auth = Getter.authUser(applicationRoot, userName, currentPass);
      loggedIn = auth != null;
    }
    if (!loggedIn) {
      log.debug("Could not sign in with any pass.");
      fail("Could not Verify User");
    } else {
      log.debug("Logged in! Updating Password now");
      if (!Setter.updatePassword(applicationRoot, userName, currentPass, newPass)) {
        log.debug("Could not update password");
        fail("Could not update password");
      } else {
        log.debug("Password Updated. Authenticating with new pass: " + newPass);
        String[] auth = Getter.authUser(applicationRoot, userName, newPass);
        if (auth == null) {
          fail("Could Not Auth With New Pass");
        }

        log.debug("Also attempting auth with old pass: " + currentPass);
        auth = Getter.authUser(applicationRoot, userName, currentPass);
        if (auth != null) {
          fail("Could auth with old password!");
        }
      }
    }
  }

  @Test
  public void testUpdatePasswordAdmin() {
    log.debug("Testing update Password");
    String userName = new String("adminPassUp");
    String currentPass = new String();
    String newPass = new String();
    boolean loggedIn = false;

    try {
      currentPass = userName;
      newPass = userName + userName;
      log.debug("Logging in with default Pass");
      loggedIn = GetterTest.verifyTestUser(applicationRoot, userName, currentPass);
    } catch (SQLException e) {
      newPass = userName;
      currentPass = userName + userName;
      log.debug("Could not log in with default pass: " + e.toString());
      log.debug("Logging in with alternative pass: " + currentPass);
      String[] auth = Getter.authUser(applicationRoot, userName, currentPass);
      loggedIn = auth != null;
    }
    if (!loggedIn) {
      log.debug("Could not sign in with any pass.");
      fail("Could not Verify User");
    } else {
      log.debug("Logged in! Updating Password now");
      if (!Setter.updatePasswordAdmin(
          applicationRoot, Getter.getUserIdFromName(applicationRoot, userName), newPass)) {
        log.debug("Could not update password");
        fail("Could not update password");
      } else {
        log.debug("Password Updated. Authenticating with new pass: " + newPass);
        String[] auth = Getter.authUser(applicationRoot, userName, newPass);
        if (auth == null) {
          fail("Could Not Auth With New Pass");
        } else {
          return; // PASS: Authenticated With New Pass
        }
      }
    }
  }

  @Test
  public void testUpdatePlayerClass() throws SQLException {
    String userName = new String("UpdateClassUser");
    String className = new String("Old Class");
    String otherClassName = new String("Other Class");
    String classId = new String();
    String otherClassId = new String();
    String newClass = new String();

    log.debug("Getting class ids");
    classId = GetterTest.findCreateClassId(className, applicationRoot);
    otherClassId = GetterTest.findCreateClassId(otherClassName, applicationRoot);
    log.debug("Verifying User");
    if (!GetterTest.verifyTestUser(applicationRoot, userName, userName, classId)) {
      fail("Could not verify user");
    } else {
      String userId = Getter.getUserIdFromName(applicationRoot, userName);
      String currentClass = Getter.getUserClassFromName(applicationRoot, userName);
      newClass = otherClassId;

      log.debug("Current Class: " + currentClass);
      log.debug("New Class: " + newClass);
      if (!Setter.updatePlayerClass(applicationRoot, newClass, userId).equalsIgnoreCase(userName)) {
        fail("Could not update player class");
      } else {
        String latestClass = Getter.getUserClassFromName(applicationRoot, userName);
        if (latestClass.compareTo(newClass) != 0) {
          log.debug("Latest Class: " + latestClass);
          log.debug("New Class: " + newClass);
          fail("Retrieved Class is not the Set Class");
        } else {
          return; // PASS
        }
      }
    }
  }

  @Test
  public void testUpdatePlayerClassToNull() throws SQLException {
    String userName = new String("UpdateClassUserFromNull");
    String className = new String("WutClass");
    String classId = new String();

    log.debug("Getting class ids");
    try {
      classId = GetterTest.findCreateClassId(className, applicationRoot);
    } catch (SQLException e) {
      TestProperties.failAndPrint(
          "Could not find or create class ID from name " + className + ": " + e.toString());
    }
    if (!GetterTest.verifyTestUser(applicationRoot, userName, userName, classId)) {
      fail("Could not verify user");
    } else {
      String userId = Getter.getUserIdFromName(applicationRoot, userName);
      String currentClass = Getter.getUserClassFromName(applicationRoot, userName);
      log.debug("Current Class: " + currentClass);
      if (!Setter.updatePlayerClassToNull(applicationRoot, userId).equalsIgnoreCase(userName)) {
        fail("Could not update player class to null");
      } else {
        String latestClass = Getter.getUserClassFromName(applicationRoot, userName);
        if (latestClass == null || latestClass.isEmpty()) {
          return; // PASS
        } else {
          log.debug("Latest Class: " + latestClass);
          fail("Retrieved Class is not null");
        }
      }
    }
  }

  @Test
  public void testUpdateUserRole() throws SQLException {
    String userName = new String("WasUserNowAdmin");
    String currentRole = new String();
    String newRole = new String();
    boolean testUserVerified = false;

    try {
      testUserVerified = GetterTest.verifyTestUser(applicationRoot, userName, userName);
    } catch (SQLException e) {
      TestProperties.failAndPrint("Could not create test user " + userName + ": " + e.toString());
    }

    assertTrue(testUserVerified);

    Connection conn = Database.getCoreConnection(applicationRoot);
    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement("SELECT userRole FROM users WHERE userName = ?");
    } catch (SQLException e) {
      TestProperties.failAndPrint("Could prepare DB statement : " + e.toString());
    }

    assertNotEquals(ps, null);

    try {
      ps.setString(1, userName);
    } catch (SQLException e) {
      TestProperties.failAndPrint("Could set statement username " + userName + ": " + e.toString());
    }

    ResultSet rs = null;
    try {
      rs = ps.executeQuery();
    } catch (SQLException e) {
      TestProperties.failAndPrint("Could execute DB Query : " + e.toString());
    }

    assertNotEquals(rs, null);

    boolean couldAdvance = false;

    try {
      couldAdvance = rs.next();
    } catch (SQLException e) {
      TestProperties.failAndPrint("Could not advance in result set : " + e.toString());
    }

    assertTrue(couldAdvance);

    if (couldAdvance) {
      try {
        currentRole = rs.getString(1);
      } catch (SQLException e) {
        TestProperties.failAndPrint("Could not get currentRole from result set: " + e.toString());
      }
      if (currentRole.equalsIgnoreCase("admin")) {
        log.debug("User is currently an admin. Changing to player");
        newRole = new String("player");
      } else {
        log.debug("User is currently a player. Changing to admin");
        newRole = new String("admin");
      }
    } else {
      fail("User not found in DB after it was created");
    }
    try {
      rs.close();
    } catch (SQLException e) {
      TestProperties.failAndPrint("Could not close result set: " + e.toString());
    }
    try {
      conn.close();
    } catch (SQLException e) {
      TestProperties.failAndPrint("Could not close db connection: " + e.toString());
    }
    String userId = Getter.getUserIdFromName(applicationRoot, userName);
    if (!Setter.updateUserRole(applicationRoot, userId, newRole).equalsIgnoreCase(userName)) {
      fail("Could not update user role from " + currentRole + " to " + newRole);
    } else {
      log.debug("Checking if change occurred");
      conn = Database.getCoreConnection(applicationRoot);
      try {
        ps = conn.prepareStatement("SELECT userRole FROM users WHERE userName = ?");
      } catch (SQLException e) {
        TestProperties.failAndPrint("Could not prepare DB statement: " + e.toString());
      }
      try {
        ps.setString(1, userName);
      } catch (SQLException e) {
        TestProperties.failAndPrint("Could not set string in DB statement: " + e.toString());
      }
      try {
        rs = ps.executeQuery();
      } catch (SQLException e) {
        TestProperties.failAndPrint("Could not execute DB query: " + e.toString());
      }

      couldAdvance = false;

      try {
        couldAdvance = rs.next();
      } catch (SQLException e) {
        TestProperties.failAndPrint("Could not advance in result set: " + e.toString());
      }

      assertTrue(couldAdvance);
      String returnedRole = "";

      try {
        returnedRole = rs.getString(1);
      } catch (SQLException e) {
        TestProperties.failAndPrint(
            "Could not get returned string from db result: " + e.toString());
      }

      assertNotEquals(returnedRole, "");

      if (!newRole.equalsIgnoreCase(returnedRole)) {
        fail("User Role was not updated in DB");
      }

      try {
        rs.close();
        conn.close();
      } catch (SQLException e) {
        TestProperties.failAndPrint("Could not close DB connection: " + e.toString());
      }
    }
  }

  @Test
  public void testMutipleClassMedals() {
    String moduleId = "853c98bd070fe0d31f1ec8b4f2ada9d7fd1784c5"; // CSRF7
    String userName = new String("classUserOne");
    String otherUserName = new String("difClassUserTwo");

    String classOne = "";
    try {
      classOne = TestProperties.findCreateClassId(log, "classA2737", applicationRoot);
    } catch (SQLException e) {
      TestProperties.failAndPrint("Could not create class classA2737: " + e.toString());
    }

    assertNotEquals(classOne, "");

    String classTwo = "";
    try {
      classTwo = TestProperties.findCreateClassId(log, "classB2737", applicationRoot);
    } catch (SQLException e) {
      TestProperties.failAndPrint("Could not create class classB2737: " + e.toString());
    }
    assertNotEquals(classTwo, "");

    log.debug("classOne: " + classOne);
    log.debug("classTwo: " + classTwo);

    boolean firstTestUserVerified = false;
    try {
      firstTestUserVerified =
          TestProperties.verifyTestUser(log, applicationRoot, userName, userName, classOne);
    } catch (SQLException e) {
      TestProperties.failAndPrint(
          "Unhandled exception when verifying test user " + userName + ": " + e.toString());
    }

    assertTrue(firstTestUserVerified);

    boolean secondTestUserVerified = false;
    try {
      secondTestUserVerified =
          TestProperties.verifyTestUser(
              log, applicationRoot, otherUserName, otherUserName, classTwo);
    } catch (SQLException e) {
      TestProperties.failAndPrint(
          "Unhandled exception when verifying test user " + otherUserName + ": " + e.toString());
    }

    assertTrue(secondTestUserVerified);

    String userId = Getter.getUserIdFromName(applicationRoot, userName);
    String otherUserId = Getter.getUserIdFromName(applicationRoot, otherUserName);

    boolean modulesOpened = Setter.openAllModules(applicationRoot, false);

    if (!modulesOpened) {
      TestProperties.failAndPrint("Could not mark all modules as open");
    }

    // Simulate user Opening Level
    if (Getter.getModuleAddress(applicationRoot, moduleId, userId).isEmpty()
        || Getter.getModuleAddress(applicationRoot, moduleId, otherUserId).isEmpty()) {
      fail("Could not Simulate Opening Level for Users");
    } else {
      String markLevelCompleteTest =
          Setter.updatePlayerResult(
              applicationRoot, moduleId, userId, "Feedback is Disabled", 1, 1, 1);
      if (markLevelCompleteTest != null) {
        String markLevelCompleteTestOtherUser =
            Setter.updatePlayerResult(
                applicationRoot, moduleId, otherUserId, "Feedback is Disabled", 1, 1, 1);
        // Do both Users have a gold medal?
        if (markLevelCompleteTestOtherUser != null) {
          ScoreboardStatus.setScoreboardOpen();
          String scoreboardData = Getter.getJsonScore(applicationRoot, "");
          if (scoreboardData.isEmpty()) {
            fail("Could not detect user in scoreboard before bad submission test");
          } else {
            JSONArray scoreboardJson = new JSONArray(scoreboardData);
            // Loop through array to find Our first user
            boolean goldMedal = false;
            for (int i = 0; i < scoreboardJson.length(); i++) {
              // log.debug("Looping through Array " + i);
              JSONObject scoreRowJson = (JSONObject) scoreboardJson.get(i);
              if (scoreRowJson.get("username").toString().compareTo(userName) == 0) {
                log.debug("Found user with goldMedalCount: " + scoreRowJson.get("goldMedalCount"));
                goldMedal = Integer.parseInt(scoreRowJson.get("goldMedalCount").toString()) > 0;
                break;
              }
            }
            if (!goldMedal) {
              TestProperties.failAndPrint(
                  "User "
                      + userName
                      + " should have a gold medal and does not. They were first in their class to"
                      + " complete module "
                      + moduleId);
            } else {
              // Search for the other user
              goldMedal = false;
              for (int i = 0; i < scoreboardJson.length(); i++) {
                // log.debug("Looping through Array " + i);
                JSONObject scoreRowJson = (JSONObject) scoreboardJson.get(i);
                if (scoreRowJson.get("username").toString().compareTo(otherUserName) == 0) {
                  log.debug(
                      "Found user with goldMedalCount: " + scoreRowJson.get("goldMedalCount"));
                  goldMedal = Integer.parseInt(scoreRowJson.get("goldMedalCount").toString()) > 0;
                  break;
                }
              }
              if (!goldMedal) {
                TestProperties.failAndPrint(
                    "User "
                        + otherUserName
                        + " should have a gold medal and does not. They were first in their class"
                        + " to complete challenge "
                        + moduleId);
              }
            }
          }
        } else {
          fail("Could not Mark First level as complete for Second User");
        }
      } else {
        fail("Could not Mark First level as complete");
      }
    }
  }

  @Test
  public void testUserDelete() {
    String testUsername = "testuserdelete";
    String testPassword = "testuserpassword";

    String testuserId = Getter.getUserIdFromName(applicationRoot, testUsername);

    if (testuserId == null || testuserId.isEmpty()) {
      boolean userCreated = false;
      try {
        userCreated =
            Setter.userCreate(
                applicationRoot,
                null,
                testUsername,
                testUsername,
                "player",
                testUsername + "@test.com",
                false);
      } catch (SQLException e) {
        TestProperties.failAndPrint(
            "Could not create test user "
                + testUsername
                + " with password "
                + testPassword
                + ": "
                + e.toString());
      }
      assert (userCreated);
    }

    testuserId = Getter.getUserIdFromName(applicationRoot, testUsername);
    assert (testuserId != null && !testuserId.isEmpty());

    boolean userDeleted = false;
    try {
      userDeleted = Setter.userDelete(applicationRoot, testuserId);
    } catch (SQLException e) {
      TestProperties.failAndPrint(
          "Could not delete test user " + testUsername + ": " + e.toString());
    }
    assert (userDeleted);

    testuserId = Getter.getUserIdFromName(applicationRoot, testUsername);
    assert (testuserId == null || testuserId.isEmpty());
  }

  @Test
  public void testSSOUserDelete() {
    String testUsername = "testSSOuserdelete";
    String testSSOName = "testSSOuserdelete@example.com";

    String testuserId;

    String user[];

    user = Getter.authUserSSO(applicationRoot, null, testUsername, testSSOName, "player");

    if (user == null || user[0].isEmpty()) {
      TestProperties.failAndPrint("Could not authenticate as newly created SSO user");
    } else {
      log.debug("PASS: User Could Authenticate after being created");
    }

    boolean userDeleted = false;
    try {
      userDeleted = Setter.userDelete(applicationRoot, user[0]);
    } catch (SQLException e) {
      TestProperties.failAndPrint(
          "Could not delete test user " + testUsername + ": " + e.toString());
    }
    assert (userDeleted);

    testuserId = Getter.getUserIdFromName(applicationRoot, testUsername);
    assert (testuserId == null || testuserId.isEmpty());
  }

  @Test
  public void testCreateDuplicateUser() {
    String userName = new String("duplicateUser");

    String user[] = Getter.authUser(applicationRoot, userName, userName);
    if (user == null || user[0].isEmpty()) {
      log.debug("User not found in DB. Adding user to DB and Retesting before giving up");
      try {
        Setter.userCreate(
            applicationRoot, null, userName, userName, "player", userName + "@test.com", false);
      } catch (SQLException e) {
        TestProperties.failAndPrint(
            "SQL error when creating user " + userName + ": " + e.toString());
      }
      user = Getter.authUser(applicationRoot, userName, userName);
    }
    if (user != null && !user[0].isEmpty()) {
      log.debug("User " + userName + " exists. Checking what happens if duplicate user is added");
      try {

        // Should fail here
        Setter.userCreate(
            applicationRoot, null, userName, userName, "player", userName + "@test.com", false);

        // If we're still here
        TestProperties.failAndPrint("No error when creating duplicate user " + userName);
      } catch (SQLException e) {
        log.debug("PASS: Could not add duplicate user " + userName);
      }

    } else {
      TestProperties.failAndPrint("Couldn't verify " + userName + " could authenticate at all");
    }
  }

  @Test
  public void testDisableAdminCheatSheetSetting() throws SQLException {

    Setter.setAdminCheatStatus(applicationRoot, false);
    assertFalse(Getter.getAdminCheatStatus(applicationRoot));
  }

  @Test
  public void testDisablePlayerCheatSheetSetting() throws SQLException {

    Setter.setPlayerCheatStatus(applicationRoot, false);
    assertFalse(Getter.getPlayerCheatStatus(applicationRoot));
  }

  @Test
  public void testEnableAdminCheatSheetSetting() throws SQLException {

    Setter.setAdminCheatStatus(applicationRoot, true);
    assertTrue(Getter.getAdminCheatStatus(applicationRoot));
  }

  @Test
  public void testEnablePlayerCheatSheetSetting() throws SQLException {

    Setter.setPlayerCheatStatus(applicationRoot, true);
    assertTrue(Getter.getPlayerCheatStatus(applicationRoot));
  }

  @Test
  public void testSetOpenFloorLayout() throws SQLException {

    Setter.setModuleLayout(applicationRoot, "tournament");
    Setter.setModuleLayout(applicationRoot, "open");

    assertEquals(Getter.getModuleLayout(applicationRoot), "open");
  }

  @Test
  public void testSetCTFLayout() throws SQLException {

    Setter.setModuleLayout(applicationRoot, "open");
    Setter.setModuleLayout(applicationRoot, "ctf");
    assertEquals(Getter.getModuleLayout(applicationRoot), "ctf");
  }

  @Test
  public void testSetTournamentLayout() throws SQLException {

    Setter.setModuleLayout(applicationRoot, "ctf");
    Setter.setModuleLayout(applicationRoot, "tournament");

    assertEquals(Getter.getModuleLayout(applicationRoot), "tournament");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyModuleLayouts() throws SQLException {

    Setter.setModuleLayout(applicationRoot, "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidModuleLayouts() throws SQLException {

    Setter.setModuleLayout(applicationRoot, "strangeLayout");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCaseCTFModuleLayouts() throws SQLException {

    Setter.setModuleLayout(applicationRoot, "CTF");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCaseOpenModuleLayouts() throws SQLException {

    Setter.setModuleLayout(applicationRoot, "Open");
  }

  @Test
  public void testEnableFeedbackStatus() throws SQLException {

    Setter.setFeedbackStatus(applicationRoot, false);
    Setter.setFeedbackStatus(applicationRoot, true);

    assertTrue(Getter.getFeedbackStatus(applicationRoot));
  }

  @Test
  public void testDisableFeedbackStatus() throws SQLException {

    Setter.setFeedbackStatus(applicationRoot, true);
    Setter.setFeedbackStatus(applicationRoot, false);

    assertFalse(Getter.getFeedbackStatus(applicationRoot));
  }

  @Test
  public void testEnableRegistrationStatus() throws SQLException {

    Setter.setRegistrationStatus(applicationRoot, false);
    Setter.setRegistrationStatus(applicationRoot, true);

    assertTrue(Getter.getRegistrationStatus(applicationRoot));
  }

  @Test
  public void testDisableRegistrationStatus() throws SQLException {

    Setter.setRegistrationStatus(applicationRoot, true);
    Setter.setRegistrationStatus(applicationRoot, false);

    assertFalse(Getter.getRegistrationStatus(applicationRoot));
  }

  @Test
  public void testSetClosedScoreboard() throws SQLException {

    Setter.setScoreboardStatus(applicationRoot, "closed");

    assertEquals(Getter.getScoreboardStatus(applicationRoot), "closed");
  }

  @Test
  public void testSetAdminOnlyScoreboard() throws SQLException {

    Setter.setScoreboardStatus(applicationRoot, "adminOnly");

    assertEquals(Getter.getScoreboardStatus(applicationRoot), "adminOnly");
  }

  @Test
  public void testSetClassSpecificScoreboard() throws SQLException {

    Setter.setScoreboardStatus(applicationRoot, "classSpecific");

    assertEquals(Getter.getScoreboardStatus(applicationRoot), "classSpecific");
  }

  @Test
  public void testSetOpenScoreboard() throws SQLException {

    Setter.setScoreboardStatus(applicationRoot, "open");

    assertEquals(Getter.getScoreboardStatus(applicationRoot), "open");
  }

  @Test
  public void testSetPublicScoreboard() throws SQLException {

    Setter.setScoreboardStatus(applicationRoot, "public");

    assertEquals(Getter.getScoreboardStatus(applicationRoot), "public");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyScoreboardStatus() throws SQLException {

    Setter.setScoreboardStatus(applicationRoot, "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidScoreboardStatus() throws SQLException {

    Setter.setScoreboardStatus(applicationRoot, "invalidStatus");
  }

  @Test
  public void testSetScoreboardClass() throws SQLException {

    Setter.setScoreboardClass(applicationRoot, "");

    assertEquals(Getter.getScoreboardClass(applicationRoot), "");

    Setter.setScoreboardClass(applicationRoot, "class1");

    assertEquals(Getter.getScoreboardClass(applicationRoot), "class1");

    Setter.setScoreboardClass(applicationRoot, "class2");

    assertEquals(Getter.getScoreboardClass(applicationRoot), "class2");

    Setter.setScoreboardClass(applicationRoot, "class3");

    assertEquals(Getter.getScoreboardClass(applicationRoot), "class3");
  }

  @Test
  public void testSetStartTimeStatus() throws SQLException {

    Setter.setStartTimeStatus(applicationRoot, false);
    Setter.setStartTimeStatus(applicationRoot, true);

    assertTrue(Getter.getStartTimeStatus(applicationRoot));

    Setter.setStartTimeStatus(applicationRoot, true);
    Setter.setStartTimeStatus(applicationRoot, false);

    assertFalse(Getter.getStartTimeStatus(applicationRoot));
  }

  @Test
  public void testSetStartTime() throws SQLException {

    Setter.setStartTime(applicationRoot, LocalDateTime.parse("2018-11-03T12:45:30"));
    assertEquals(Getter.getStartTime(applicationRoot), LocalDateTime.parse("2018-11-03T12:45:30"));

    Setter.setStartTime(applicationRoot, LocalDateTime.parse("2118-11-03T12:45:30"));
    assertEquals(Getter.getStartTime(applicationRoot), LocalDateTime.parse("2118-11-03T12:45:30"));
  }

  @Test
  public void testSetLockTimeStatus() throws SQLException {

    Setter.setLockTimeStatus(applicationRoot, false);
    Setter.setLockTimeStatus(applicationRoot, true);

    assertTrue(Getter.getLockTimeStatus(applicationRoot));

    Setter.setLockTimeStatus(applicationRoot, true);
    Setter.setLockTimeStatus(applicationRoot, false);

    assertFalse(Getter.getLockTimeStatus(applicationRoot));
  }

  @Test
  public void testSetLockTime() throws SQLException {

    Setter.setLockTime(applicationRoot, LocalDateTime.parse("2018-11-03T12:45:30"));
    assertEquals(Getter.getLockTime(applicationRoot), LocalDateTime.parse("2018-11-03T12:45:30"));

    Setter.setLockTime(applicationRoot, LocalDateTime.parse("2118-11-03T12:45:30"));
    assertEquals(Getter.getLockTime(applicationRoot), LocalDateTime.parse("2118-11-03T12:45:30"));
  }

  @Test
  public void testSetEndTimeStatus() throws SQLException {

    Setter.setEndTimeStatus(applicationRoot, false);
    Setter.setEndTimeStatus(applicationRoot, true);

    assertTrue(Getter.getEndTimeStatus(applicationRoot));

    Setter.setEndTimeStatus(applicationRoot, true);
    Setter.setEndTimeStatus(applicationRoot, false);

    assertFalse(Getter.getEndTimeStatus(applicationRoot));
  }

  @Test
  public void testSetEndTime() throws SQLException {

    Setter.setEndTime(applicationRoot, LocalDateTime.parse("2018-11-03T12:45:30"));

    assertEquals(Getter.getEndTime(applicationRoot), LocalDateTime.parse("2018-11-03T12:45:30"));

    Setter.setEndTime(applicationRoot, LocalDateTime.parse("2118-11-03T12:45:30"));

    assertEquals(Getter.getEndTime(applicationRoot), LocalDateTime.parse("2118-11-03T12:45:30"));
  }
}
