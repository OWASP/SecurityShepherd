package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class XssFilterTest {

  @Test
  void levelOne_replacesScriptTag() {
    String result = XssFilter.levelOne("<script>alert(1)</script>");
    assertFalse(result.contains("script"));
    assertTrue(result.contains("scr.pt"));
  }

  @Test
  void levelOne_caseInsensitive() {
    String result = XssFilter.levelOne("<SCRIPT>alert(1)</SCRIPT>");
    assertFalse(result.toLowerCase().contains("script"));
  }

  @Test
  void levelOne_preservesNonScriptContent() {
    String result = XssFilter.levelOne("hello world");
    assertEquals("hello world", result);
  }

  @Test
  void levelTwo_replacesOnclick() {
    String result = XssFilter.levelTwo("<img onclick=\"alert(1)\">");
    assertFalse(result.contains("onclick"));
  }

  @Test
  void levelTwo_replacesOnmouseover() {
    String result = XssFilter.levelTwo("<div onmouseover=\"alert(1)\">");
    assertFalse(result.contains("onmouseover"));
  }

  @Test
  void levelTwo_replacesOnload() {
    String result = XssFilter.levelTwo("<body onload=\"alert(1)\">");
    assertFalse(result.contains("onload"));
  }

  @Test
  void levelTwo_replacesOnerror() {
    String result = XssFilter.levelTwo("<img onerror=\"alert(1)\">");
    assertFalse(result.contains("onerror"));
  }

  @Test
  void levelTwo_screwsHtmlEncodings() {
    String result = XssFilter.levelTwo("&#x6f;nclick");
    assertFalse(result.contains("&"));
    assertFalse(result.contains(":"));
  }

  @Test
  void levelThree_replacesScriptTag() {
    String result = XssFilter.levelThree("<script>alert(1)</script>");
    assertFalse(result.contains("script"));
  }

  @Test
  void levelThree_removesJavascriptTriggers() {
    String result = XssFilter.levelThree("<div onclick=\"alert(1)\">");
    assertFalse(result.contains("onclick"));
  }

  @Test
  void levelFour_replacesScriptRecursively() {
    String result = XssFilter.levelFour("<scrscriptipt>alert(1)</scrscriptipt>");
    assertFalse(result.contains("script"));
  }

  @Test
  void levelFour_removesJavascriptTriggers() {
    String result = XssFilter.levelFour("<img onerror=\"alert(1)\">");
    assertFalse(result.contains("onerror"));
  }

  @Test
  void encodeForHtml_encodesAngleBrackets() {
    String result = XssFilter.encodeForHtml("<script>");
    assertFalse(result.contains("<"));
    assertFalse(result.contains(">"));
  }

  @Test
  void encodeForHtml_restoresFirstQuote() {
    String result = XssFilter.encodeForHtml("\"test\"");
    assertTrue(result.startsWith("\""));
  }

  @Test
  void encodeForHtml_encodesOnHandler() {
    String result = XssFilter.encodeForHtml("onclick");
    assertFalse(result.contains("on"));
  }

  @Test
  void badUrlValidate_validHttpUrl() {
    String result = XssFilter.badUrlValidate("http://example.com");
    assertTrue(result.startsWith("http"));
  }

  @Test
  void badUrlValidate_nonHttpReturnsDefault() {
    String result = XssFilter.badUrlValidate("javascript:alert(1)");
    assertTrue(result.contains("google.com"));
  }

  @Test
  void badUrlValidate_replacesAngleBrackets() {
    String result = XssFilter.badUrlValidate("http://example.com/<script>");
    assertFalse(result.contains("<"));
    assertFalse(result.contains(">"));
  }

  @Test
  void anotherBadUrlValidate_validHttpUrl() {
    String result = XssFilter.anotherBadUrlValidate("http://example.com/path");
    assertTrue(result.startsWith("http"));
  }

  @Test
  void anotherBadUrlValidate_nonHttpReturnsDefault() {
    String result = XssFilter.anotherBadUrlValidate("ftp://files.com");
    assertTrue(result.contains("google.com"));
  }

  @Test
  void anotherBadUrlValidate_onlyReplacesFirstAngleBrackets() {
    String result = XssFilter.anotherBadUrlValidate("http://example.com/<a><b>");
    long count = result.chars().filter(c -> c == '<').count();
    assertTrue(count <= 1);
  }
}
