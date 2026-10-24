package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

/**
 * Records the verdicts of {@link FindXSS#search(String)} over {@link FindXSSPayloadCorpus} in
 * {@code src/test/resources/utils/FindXSSVerdicts.txt}, which {@link FindXSSVerdictRegressionTest}
 * compares against.
 *
 * <p>Not a test. Run it from the repository root against the code the comparison should be made
 * with (the verdicts checked in were recorded on {@code dev} before the #960 guard was added):
 *
 * <pre>
 * mvn -B test-compile dependency:build-classpath -Dmdep.outputFile=target/cp.txt
 * java -cp "target/classes:target/test-classes:$(cat target/cp.txt)" utils.FindXSSVerdictRecorder
 * </pre>
 *
 * <p>Each line of the file is {@code section <TAB> payloads <TAB> detected <TAB> verdict bits}, the
 * bits being a hex-encoded {@link BitSet} where bit {@code i} is the verdict for payload {@code i}
 * of the section.
 */
final class FindXSSVerdictRecorder {

  static final String VERDICTS_RESOURCE = "/utils/FindXSSVerdicts.txt";

  private FindXSSVerdictRecorder() {}

  static BitSet verdicts(List<String> payloads) {
    BitSet bits = new BitSet(payloads.size());
    for (int i = 0; i < payloads.size(); i++) {
      if (FindXSS.search(payloads.get(i))) {
        bits.set(i);
      }
    }
    return bits;
  }

  static String toHex(BitSet bits) {
    StringBuilder hex = new StringBuilder();
    for (byte b : bits.toByteArray()) {
      hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }

  static BitSet fromHex(String hex) {
    byte[] bytes = new byte[hex.length() / 2];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
    }
    return BitSet.valueOf(bytes);
  }

  public static void main(String[] args) throws IOException {
    Path out = Paths.get(args.length > 0 ? args[0] : "src/test/resources" + VERDICTS_RESOURCE);
    List<String> lines = new ArrayList<>();
    for (Map.Entry<String, List<String>> section : FindXSSPayloadCorpus.sections().entrySet()) {
      BitSet bits = verdicts(section.getValue());
      lines.add(
          section.getKey()
              + "\t"
              + section.getValue().size()
              + "\t"
              + bits.cardinality()
              + "\t"
              + toHex(bits));
      System.out.println(
          section.getKey() + ": " + bits.cardinality() + " of " + section.getValue().size());
    }
    Files.createDirectories(out.getParent());
    Files.write(out, lines, StandardCharsets.UTF_8);
  }
}
