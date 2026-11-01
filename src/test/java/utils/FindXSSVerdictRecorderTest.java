package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;
import java.util.BitSet;
import org.junit.jupiter.api.Test;

class FindXSSVerdictRecorderTest {

  @Test
  void outputPath_bareFileNameHasAParentDirectory() {
    // Files.createDirectories(out.getParent()) threw NullPointerException for a bare file name
    assertNotNull(
        FindXSSVerdictRecorder.outputPath(new String[] {"FindXSSVerdicts.txt"}).getParent());
  }

  @Test
  void outputPath_defaultsToTheCheckedInResource() {
    assertEquals(
        Paths.get("src/test/resources/utils/FindXSSVerdicts.txt").toAbsolutePath(),
        FindXSSVerdictRecorder.outputPath(new String[0]));
  }

  @Test
  void fromHex_readsWhatToHexWrote() {
    BitSet bits = new BitSet();
    bits.set(0);
    bits.set(9);
    bits.set(200);
    assertEquals(bits, FindXSSVerdictRecorder.fromHex(FindXSSVerdictRecorder.toHex(bits)));
  }

  @Test
  void fromHex_rejectsAnOddNumberOfDigits() {
    assertThrows(IllegalArgumentException.class, () -> FindXSSVerdictRecorder.fromHex("abc"));
  }

  @Test
  void fromHex_rejectsNonHexDigits() {
    assertThrows(IllegalArgumentException.class, () -> FindXSSVerdictRecorder.fromHex("zz"));
  }
}
