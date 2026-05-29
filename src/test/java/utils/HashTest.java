package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HashTest {

  @Test
  void generateUserSolutionKeyOnly_returnsNonNull() {
    assertNotNull(Hash.generateUserSolutionKeyOnly("baseKey", "userSalt"));
  }

  @Test
  void generateUserSolutionKeyOnly_consistentForSameInputs() {
    String first = Hash.generateUserSolutionKeyOnly("key1", "salt1");
    String second = Hash.generateUserSolutionKeyOnly("key1", "salt1");
    assertEquals(first, second);
  }

  @Test
  void generateUserSolutionKeyOnly_differentForDifferentKeys() {
    String first = Hash.generateUserSolutionKeyOnly("key1", "salt1");
    String second = Hash.generateUserSolutionKeyOnly("key2", "salt1");
    assertNotEquals(first, second);
  }

  @Test
  void generateUserSolutionKeyOnly_differentForDifferentSalts() {
    String first = Hash.generateUserSolutionKeyOnly("key1", "salt1");
    String second = Hash.generateUserSolutionKeyOnly("key1", "salt2");
    assertNotEquals(first, second);
  }

  @Test
  void generateUserSolutionKeyOnly_producesHexOutput() {
    String result = Hash.generateUserSolutionKeyOnly("key", "salt");
    assertTrue(result.matches("[0-9A-F]+"));
  }

  @Test
  void generateUserSolution_containsHtmlMarkup() {
    String result = Hash.generateUserSolution("key", "salt");
    assertTrue(result.contains("<textarea"));
    assertTrue(result.contains("theKey"));
  }

  @Test
  void generateUserSolution_containsSolutionKey() {
    String key = Hash.generateUserSolutionKeyOnly("key", "salt");
    String html = Hash.generateUserSolution("key", "salt");
    assertTrue(html.contains(key));
  }

  @Test
  void randomKeyBytes_returns16Bytes() {
    byte[] bytes = Hash.randomKeyBytes();
    assertEquals(16, bytes.length);
  }

  @Test
  void randomKeyBytes_differentEachCall() {
    byte[] first = Hash.randomKeyBytes();
    byte[] second = Hash.randomKeyBytes();
    boolean allSame = true;
    for (int i = 0; i < first.length; i++) {
      if (first[i] != second[i]) {
        allSame = false;
        break;
      }
    }
    assertNotEquals(true, allSame);
  }

  @Test
  void randomString_returnsNonEmpty() {
    String result = Hash.randomString();
    assertNotNull(result);
    assertNotEquals("", result);
  }

  @Test
  void randomString_differentEachCall() {
    String first = Hash.randomString();
    String second = Hash.randomString();
    assertNotEquals(first, second);
  }

  @Test
  void getCurrentKey_returnsNonNull() {
    assertNotNull(Hash.getCurrentKey());
  }

  @Test
  void getCurrentKey_returns16Bytes() {
    assertEquals(16, Hash.getCurrentKey().length);
  }
}
