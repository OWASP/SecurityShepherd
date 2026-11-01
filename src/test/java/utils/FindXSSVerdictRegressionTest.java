package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks that {@link FindXSS#search(String)} gives the same verdicts as it did on {@code dev}
 * before #960, over the ~50,000 payloads of {@link FindXSSPayloadCorpus}. FindXSS decides whether
 * an XSS lesson or challenge is solved, so a changed verdict changes how hard a level is.
 *
 * <p>If a change to FindXSS is meant to change verdicts, list the differences this test reports in
 * the PR, then re-record the verdicts with {@link FindXSSVerdictRecorder}.
 */
class FindXSSVerdictRegressionTest {

  private static Level findXssLevel;
  private static PrintStream stderr;
  private static PrintStream mutedStderr;

  @BeforeAll
  static void quietFindXss() {
    // FindXSS logs every payload at debug level, and jtidy prints its parse errors to System.err:
    // over the whole corpus that is hundreds of thousands of lines
    findXssLevel = LogManager.getLogger(FindXSS.class).getLevel();
    Configurator.setLevel(FindXSS.class.getName(), Level.INFO);
    stderr = System.err;
    mutedStderr = new PrintStream(OutputStream.nullOutputStream());
    System.setErr(mutedStderr);
  }

  @AfterAll
  static void restoreFindXssOutput() {
    System.setErr(stderr);
    mutedStderr.close();
    Configurator.setLevel(FindXSS.class.getName(), findXssLevel);
  }

  private static Map<String, String[]> recordedVerdicts() throws IOException {
    Map<String, String[]> recorded = new LinkedHashMap<>();
    InputStream in =
        FindXSSVerdictRegressionTest.class.getResourceAsStream(
            FindXSSVerdictRecorder.VERDICTS_RESOURCE);
    assertNotNull(in, FindXSSVerdictRecorder.VERDICTS_RESOURCE + " is missing");
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.isBlank()) {
          String[] fields = line.split("\t", -1);
          if (fields.length != 4) {
            throw new AssertionError("Corrupt verdicts file: " + line);
          }
          recorded.put(fields[0], fields);
        }
      }
    }
    return recorded;
  }

  private static int parseCount(String field) {
    try {
      return Integer.parseInt(field);
    } catch (NumberFormatException e) {
      throw new AssertionError("Corrupt verdicts file: " + field + " is not a count", e);
    }
  }

  @Test
  void search_verdictsMatchTheRecordedVerdicts() throws IOException {
    Map<String, List<String>> corpus = FindXSSPayloadCorpus.sections();
    Map<String, String[]> recorded = recordedVerdicts();
    assertEquals(
        recorded.keySet(), corpus.keySet(), "Corpus sections changed: re-record the verdicts");

    List<String> differences = new ArrayList<>();
    for (Map.Entry<String, List<String>> section : corpus.entrySet()) {
      List<String> payloads = section.getValue();
      String[] fields = recorded.get(section.getKey());
      assertEquals(
          parseCount(fields[1]),
          payloads.size(),
          "Section " + section.getKey() + " changed size: re-record the verdicts");
      BitSet expected;
      try {
        expected = FindXSSVerdictRecorder.fromHex(fields[3]);
      } catch (IllegalArgumentException e) {
        throw new AssertionError("Corrupt verdicts file: " + section.getKey(), e);
      }
      assertEquals(parseCount(fields[2]), expected.cardinality(), "Corrupt verdicts file");

      BitSet actual = FindXSSVerdictRecorder.verdicts(payloads);
      BitSet changed = (BitSet) actual.clone();
      changed.xor(expected);
      for (int i = changed.nextSetBit(0); i >= 0; i = changed.nextSetBit(i + 1)) {
        differences.add(
            section.getKey()
                + "["
                + i
                + "] "
                + expected.get(i)
                + " -> "
                + actual.get(i)
                + ": "
                + payloads.get(i));
      }
    }
    assertTrue(
        differences.isEmpty(),
        differences.size()
            + " verdicts changed, first 50:\n"
            + String.join("\n", differences.subList(0, Math.min(50, differences.size()))));
  }

  @Test
  void corpus_staysWithinTheTagLimit() {
    // Payloads over FindXSS.MAX_TAG_OPENERS are refused without being parsed (see FindXSSTest), so
    // a corpus payload over the limit would no longer test jtidy's verdict.
    for (List<String> payloads : FindXSSPayloadCorpus.sections().values()) {
      for (String payload : payloads) {
        long tags = payload.chars().filter(c -> c == '<').count();
        assertTrue(tags <= FindXSS.MAX_TAG_OPENERS, payload);
      }
    }
  }
}
