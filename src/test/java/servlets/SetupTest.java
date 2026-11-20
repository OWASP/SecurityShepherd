package servlets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dbProcs.Constants;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.junit.jupiter.api.Test;
import testUtils.TestProperties;

public class SetupTest {

  private static final Logger log = LogManager.getLogger(SetupTest.class);

  /** Collects the messages logged to the logger it is attached to. */
  private static final class CapturingAppender extends AbstractAppender {
    private final List<String> messages = new CopyOnWriteArrayList<>();

    CapturingAppender() {
      super("SetupTestCapture", null, null, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
      messages.add(event.getMessage().getFormattedMessage());
    }
  }

  @Test
  public void generateAuth_doesNotLogToken() throws IOException {
    TestProperties.setTestPropertiesFileDirectory(log);
    Path authFile = Paths.get(Constants.SETUP_AUTH);
    Files.createDirectories(authFile.getParent());
    byte[] existing = Files.exists(authFile) ? Files.readAllBytes(authFile) : null;
    Files.deleteIfExists(authFile);

    org.apache.logging.log4j.core.Logger setupLogger =
        (org.apache.logging.log4j.core.Logger) LogManager.getLogger(Setup.class);
    CapturingAppender appender = new CapturingAppender();
    appender.start();
    setupLogger.addAppender(appender);
    try {
      Setup.generateAuth();

      String token = new String(Files.readAllBytes(authFile), StandardCharsets.UTF_8).trim();
      assertFalse(token.isEmpty(), "generateAuth should write a token");
      assertFalse(appender.messages.isEmpty(), "Expected generateAuth to log something");
      for (String message : appender.messages) {
        assertFalse(message.contains(token), "Setup logged the auth token: " + message);
      }
    } finally {
      setupLogger.removeAppender(appender);
      appender.stop();
      Files.deleteIfExists(authFile);
      if (existing != null) {
        Files.write(authFile, existing);
      }
    }
  }

  @Test
  public void validateHostPort_bothEmpty_isValid() {
    assertNull(Setup.validateHostPort("", ""));
  }

  @Test
  public void validateHostPort_bothProvided_isValid() {
    assertNull(Setup.validateHostPort("localhost", "3306"));
  }

  @Test
  public void validateHostPort_onlyHostProvided_isInvalid() {
    assertNotNull(Setup.validateHostPort("localhost", ""));
  }

  @Test
  public void validateHostPort_onlyPortProvided_isInvalid() {
    assertNotNull(Setup.validateHostPort("", "3306"));
  }

  @Test
  public void validateHostPort_bothNull_isValid() {
    assertNull(Setup.validateHostPort(null, null));
  }

  @Test
  public void validateHostPort_hostNullPortProvided_isInvalid() {
    assertNotNull(Setup.validateHostPort(null, "3306"));
  }

  @Test
  public void validateHostPort_hostProvidedPortNull_isInvalid() {
    assertNotNull(Setup.validateHostPort("localhost", null));
  }
}
