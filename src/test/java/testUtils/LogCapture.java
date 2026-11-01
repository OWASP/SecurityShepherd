package testUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;

/**
 * Records what a class logs while it is open, so tests can assert on log output. Use it in a
 * try-with-resources block: the appender is removed again on close.
 */
public final class LogCapture extends AbstractAppender implements AutoCloseable {

  private final Logger logger;
  private final List<LogEvent> events = new CopyOnWriteArrayList<>();

  private LogCapture(Logger logger) {
    super("LogCapture-" + logger.getName(), null, null, true, Property.EMPTY_ARRAY);
    this.logger = logger;
  }

  /**
   * Starts recording the events logged by the logger of the given class.
   *
   * @param loggingClass The class whose logger to record
   * @return The open capture
   */
  public static LogCapture of(Class<?> loggingClass) {
    LogCapture capture = new LogCapture((Logger) LogManager.getLogger(loggingClass));
    capture.start();
    capture.logger.addAppender(capture);
    return capture;
  }

  @Override
  public void append(LogEvent event) {
    events.add(event.toImmutable());
  }

  /**
   * @param level The lowest level to include
   * @return The messages logged at the given level or a more severe one, each prefixed with its
   *     level
   */
  public List<String> messagesAtOrAbove(Level level) {
    return events.stream()
        .filter(event -> event.getLevel().isMoreSpecificThan(level))
        .map(event -> event.getLevel() + " " + event.getMessage().getFormattedMessage())
        .collect(Collectors.toList());
  }

  @Override
  public void close() {
    logger.removeAppender(this);
    stop();
  }
}
