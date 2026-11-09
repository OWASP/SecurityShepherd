package servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import dbProcs.Getter;
import dbProcs.Setter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import utils.CountdownHandler;
import utils.Hash;

class MobileFlagSubmitTest {

  private static final String MODULE = "client_side_injection_lesson";
  private static final String USER = "flagSubmitTestUser";

  private final StringWriter body = new StringWriter();
  private int status = HttpServletResponse.SC_OK;
  private String retryAfter;

  @BeforeEach
  @AfterEach
  void resetProgress() {
    MobileModuleProgress.clear();
  }

  private MobileFlagSubmit run(String flag) throws Exception {
    return run(MODULE, flag, true);
  }

  private MobileFlagSubmit run(String moduleId, String flag, boolean loggedIn) throws Exception {
    HttpSession ses = null;
    if (loggedIn) {
      ses = mock(HttpSession.class);
      when(ses.getAttribute("userRole")).thenReturn("player");
      when(ses.getAttribute("userName")).thenReturn(USER);
      when(ses.getAttribute("userStamp")).thenReturn("stamp-1");
    }

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("moduleId")).thenReturn(moduleId);
    when(request.getParameter("flag")).thenReturn(flag);
    when(request.getSession(false)).thenReturn(ses);
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");

    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getWriter()).thenReturn(new PrintWriter(body, true));
    org.mockito.Mockito.doAnswer(
            inv -> {
              status = inv.getArgument(0);
              return null;
            })
        .when(response)
        .setStatus(anyInt());
    org.mockito.Mockito.doAnswer(
            inv -> {
              retryAfter = inv.getArgument(1);
              return null;
            })
        .when(response)
        .setHeader(eq("Retry-After"), anyString());

    ServletContext ctx = mock(ServletContext.class);
    when(ctx.getRealPath("")).thenReturn("root");
    MobileFlagSubmit servlet = spy(new MobileFlagSubmit());
    doReturn(ctx).when(servlet).getServletContext();

    servlet.doPost(request, response);
    return servlet;
  }

  private static String correctFlag() {
    return Hash.generateUserSolutionKeyOnly(MobileModuleFlags.BASE_FLAGS.get(MODULE), USER);
  }

  private static void stubUpdatePlayerResult(MockedStatic<Setter> setter, String result) {
    setter
        .when(
            () ->
                Setter.updatePlayerResult(any(), any(), any(), any(), anyInt(), anyInt(), anyInt()))
        .thenReturn(result);
  }

  @Test
  void correctFlag_isScored_andReportedCorrect() throws Exception {
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<Setter> setter = mockStatic(Setter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(true);
      getter
          .when(() -> Getter.checkPlayerResult(anyString(), anyString(), anyString()))
          .thenReturn("module.name");
      stubUpdatePlayerResult(setter, "module.name");

      run(correctFlag());

      assertEquals(HttpServletResponse.SC_OK, status);
      assertTrue(new JSONObject(body.toString()).getBoolean("correct"));
    }
  }

  @Test
  void correctFlag_whenScoreNotRecorded_returns503NotCorrect() throws Exception {
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<Setter> setter = mockStatic(Setter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(true);
      getter
          .when(() -> Getter.checkPlayerResult(anyString(), anyString(), anyString()))
          .thenReturn("module.name");
      // updatePlayerResult returns null when the result could not be recorded.
      stubUpdatePlayerResult(setter, null);

      run(correctFlag());

      assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, status);
      assertFalse(new JSONObject(body.toString()).getBoolean("correct"));
    }
  }

  @Test
  void correctFlag_whenScoringThrows_returns503() throws Exception {
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(true);
      getter
          .when(() -> Getter.getModuleAddress(anyString(), anyString(), anyString()))
          .thenThrow(new RuntimeException("db down"));

      run(correctFlag());

      assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, status);
    }
  }

  @Test
  void alreadyCompletedModule_isStillReportedCorrect() throws Exception {
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<Setter> setter = mockStatic(Setter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(true);
      getter
          .when(() -> Getter.checkPlayerResult(anyString(), anyString(), anyString()))
          .thenReturn(null);

      run(correctFlag());

      assertEquals(HttpServletResponse.SC_OK, status);
      assertTrue(new JSONObject(body.toString()).getBoolean("correct"));
      setter.verifyNoInteractions();
    }
  }

  @Test
  void correctFlag_submittedTooSoonAfterStart_returns429AndIsNotScored() throws Exception {
    MobileModuleProgress.recordStartAt(
        "stamp-1", MODULE, System.currentTimeMillis() - MobileModuleProgress.MIN_SOLVE_MS / 2);
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<Setter> setter = mockStatic(Setter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(true);

      run(correctFlag());

      assertEquals(429, status);
      assertFalse(new JSONObject(body.toString()).getBoolean("correct"));
      assertTrue(retryAfter != null && Long.parseLong(retryAfter) > 0);
      setter.verifyNoInteractions();
    }
  }

  @Test
  void correctFlag_submittedAfterMinimum_isScored() throws Exception {
    MobileModuleProgress.recordStartAt(
        "stamp-1", MODULE, System.currentTimeMillis() - MobileModuleProgress.MIN_SOLVE_MS - 1_000);
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<Setter> setter = mockStatic(Setter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(true);
      getter
          .when(() -> Getter.checkPlayerResult(anyString(), anyString(), anyString()))
          .thenReturn("module.name");
      stubUpdatePlayerResult(setter, "module.name");

      run(correctFlag());

      assertEquals(HttpServletResponse.SC_OK, status);
      assertTrue(new JSONObject(body.toString()).getBoolean("correct"));
    }
  }

  @Test
  void incorrectFlag_shortlyAfterStart_isReportedIncorrectNotRateLimited() throws Exception {
    MobileModuleProgress.recordStart("stamp-1", MODULE);
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<Setter> setter = mockStatic(Setter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(true);

      run("not-the-flag");

      assertEquals(HttpServletResponse.SC_OK, status);
      assertFalse(new JSONObject(body.toString()).getBoolean("correct"));
    }
  }

  @Test
  void notLoggedIn_returns401AndIsNotScored() throws Exception {
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<Setter> setter = mockStatic(Setter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(true);

      run(MODULE, correctFlag(), false);

      assertEquals(HttpServletResponse.SC_UNAUTHORIZED, status);
      setter.verifyNoInteractions();
    }
  }

  @Test
  void missingParameters_returns400() throws Exception {
    run(MODULE, null, true);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, status);
  }

  @Test
  void unknownModuleId_returns404AndIsNotScored() throws Exception {
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<Setter> setter = mockStatic(Setter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(true);

      run("no_such_module", correctFlag(), true);

      assertEquals(HttpServletResponse.SC_NOT_FOUND, status);
      setter.verifyNoInteractions();
    }
  }

  @Test
  void closedModule_returns403AndIsNotScored() throws Exception {
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<Setter> setter = mockStatic(Setter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(false);

      run(correctFlag());

      assertEquals(HttpServletResponse.SC_FORBIDDEN, status);
      setter.verifyNoInteractions();
    }
  }

  @Test
  void correctFlag_withNoRecordedStart_isStillScored() throws Exception {
    assertEquals(-1, MobileModuleProgress.elapsedSinceStartMs("stamp-1", MODULE));
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<Setter> setter = mockStatic(Setter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(true);
      getter
          .when(() -> Getter.checkPlayerResult(anyString(), anyString(), anyString()))
          .thenReturn("module.name");
      stubUpdatePlayerResult(setter, "module.name");

      run(correctFlag());

      assertEquals(HttpServletResponse.SC_OK, status);
      assertTrue(new JSONObject(body.toString()).getBoolean("correct"));
    }
  }

  @Test
  void incorrectFlag_isNotScored() throws Exception {
    try (MockedStatic<Getter> getter = mockStatic(Getter.class);
        MockedStatic<Setter> setter = mockStatic(Setter.class);
        MockedStatic<CountdownHandler> countdown = mockStatic(CountdownHandler.class)) {
      countdown.when(CountdownHandler::isRunning).thenReturn(true);
      getter.when(() -> Getter.isModuleOpen(anyString(), anyString())).thenReturn(true);

      run("not-the-flag");

      assertEquals(HttpServletResponse.SC_OK, status);
      assertFalse(new JSONObject(body.toString()).getBoolean("correct"));
      setter.verifyNoInteractions();
    }
  }
}
