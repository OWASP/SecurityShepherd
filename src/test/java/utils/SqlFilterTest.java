package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SqlFilterTest {

  @Test
  void levelOne_removesFirstSingleQuote() {
    assertEquals(" OR 1=1--", SqlFilter.levelOne("' OR 1=1--"));
  }

  @Test
  void levelOne_onlyRemovesFirstQuote() {
    assertEquals(" OR '1'='1", SqlFilter.levelOne("' OR '1'='1"));
  }

  @Test
  void levelOne_noQuoteUnchanged() {
    assertEquals("hello", SqlFilter.levelOne("hello"));
  }

  @Test
  void levelOne_emptyString() {
    assertEquals("", SqlFilter.levelOne(""));
  }

  @Test
  void levelTwo_removesOrKeyword() {
    String result = SqlFilter.levelTwo("' OR 1=1");
    assertFalse(result.contains("OR"));
    assertFalse(result.contains("or"));
  }

  @Test
  void levelTwo_removesAmpersand() {
    String result = SqlFilter.levelTwo("a&b");
    assertFalse(result.contains("&"));
  }

  @Test
  void levelTwo_pipeNotRemovedDueToRegex() {
    // replaceAll("|","") uses regex where | is alternation, so pipe is not actually removed
    String result = SqlFilter.levelTwo("a|b");
    assertTrue(result.contains("|"));
  }

  @Test
  void levelTwo_removesTrue() {
    String result = SqlFilter.levelTwo("true OR TRUE");
    assertFalse(result.contains("true"));
    assertFalse(result.contains("TRUE"));
  }

  @Test
  void levelTwo_preservesMixedCaseOr() {
    String result = SqlFilter.levelTwo("Or");
    assertEquals("Or", result);
  }

  @Test
  void levelThree_lowercasesInput() {
    String result = SqlFilter.levelThree("HELLO");
    assertEquals("hello", result);
  }

  @Test
  void levelThree_removesOrRecursively() {
    String result = SqlFilter.levelThree("oorr");
    assertFalse(result.contains("or"));
  }

  @Test
  void levelThree_removesOperators() {
    String result = SqlFilter.levelThree("a;b-c!d&e");
    assertFalse(result.contains(";"));
    assertFalse(result.contains("-"));
    assertFalse(result.contains("!"));
    assertFalse(result.contains("&"));
  }

  @Test
  void levelThree_removesTrueAndFalse() {
    String result = SqlFilter.levelThree("true false");
    assertFalse(result.contains("true"));
    assertFalse(result.contains("false"));
  }

  @Test
  void levelThree_removesIsKeyword() {
    String result = SqlFilter.levelThree("x is null");
    assertFalse(result.contains("is"));
  }

  @Test
  void levelFour_lowercasesInput() {
    assertTrue(SqlFilter.levelFour("HELLO").equals("hello"));
  }

  @Test
  void levelFour_removesAllSingleQuotes() {
    String result = SqlFilter.levelFour("''' OR '1'='1'");
    assertFalse(result.contains("'"));
  }

  @Test
  void levelFour_emptyString() {
    assertEquals("", SqlFilter.levelFour(""));
  }

  @Test
  void levelFour_noQuotesUnchanged() {
    assertEquals("hello world", SqlFilter.levelFour("HELLO WORLD"));
  }
}
