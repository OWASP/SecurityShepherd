package utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FindXSSTest {

  // search — script tag detection

  @Test
  void search_detectsScriptTagWithAlert() {
    assertTrue(FindXSS.search("<script>alert(1)</script>"));
  }

  @Test
  void search_scriptTagWithoutAlertNotDetected() {
    assertFalse(FindXSS.search("<script>console.log(1)</script>"));
  }

  @Test
  void search_cleanHtmlNotDetected() {
    assertFalse(FindXSS.search("<p>Hello World</p>"));
  }

  @Test
  void search_emptyStringNotDetected() {
    assertFalse(FindXSS.search(""));
  }

  @Test
  void search_plainTextNotDetected() {
    assertFalse(FindXSS.search("just some text"));
  }

  // search — event handler detection

  @Test
  void search_detectsOnclickAlert() {
    assertTrue(FindXSS.search("<div onclick=\"alert(1)\">click</div>"));
  }

  @Test
  void search_detectsOnerrorAlert() {
    assertTrue(FindXSS.search("<img src=x onerror=\"alert(1)\">"));
  }

  @Test
  void search_detectsOnmouseoverAlert() {
    assertTrue(FindXSS.search("<span onmouseover=\"alert(1)\">hover</span>"));
  }

  @Test
  void search_eventWithoutAlertNotDetected() {
    assertFalse(FindXSS.search("<div onclick=\"doSomething()\">click</div>"));
  }

  // search — URI-based detection

  @Test
  void search_detectsJavascriptUri() {
    assertTrue(FindXSS.search("<a href=\"javascript:alert(1)\">link</a>"));
  }

  @Test
  void search_detectsDataUri() {
    assertTrue(FindXSS.search("<a href=\"data:text/html,<script>alert(1)</script>\">x</a>"));
  }

  @Test
  void search_normalHrefNotDetected() {
    assertFalse(FindXSS.search("<a href=\"http://example.com\">link</a>"));
  }

  // findCsrfAttackUrl — path-only validation

  @Test
  void findCsrfAttackUrl_validPath() {
    assertTrue(FindXSS.findCsrfAttackUrl("http://localhost/admin/config", "/admin/config"));
  }

  @Test
  void findCsrfAttackUrl_invalidPath() {
    assertFalse(FindXSS.findCsrfAttackUrl("http://localhost/other/path", "/admin/config"));
  }

  @Test
  void findCsrfAttackUrl_malformedUrl() {
    assertFalse(FindXSS.findCsrfAttackUrl("not-a-url", "/admin/config"));
  }

  // findCsrfAttackUrl — path + query validation

  @Test
  void findCsrfAttackUrl_validPathAndQuery() {
    assertTrue(
        FindXSS.findCsrfAttackUrl(
            "http://localhost/admin/config?userId=abc123", "/admin/config", "userId", "abc123"));
  }

  @Test
  void findCsrfAttackUrl_wrongQueryValue() {
    assertFalse(
        FindXSS.findCsrfAttackUrl(
            "http://localhost/admin/config?userId=wrong", "/admin/config", "userId", "abc123"));
  }

  @Test
  void findCsrfAttackUrl_missingQuery() {
    assertFalse(
        FindXSS.findCsrfAttackUrl(
            "http://localhost/admin/config", "/admin/config", "userId", "abc123"));
  }

  // findCsrf — img tag CSRF detection

  @Test
  void findCsrf_validImgCsrf() {
    String msg = "<img src=\"http://localhost/root/grantComplete/csrflesson?userId=fakeId123\" />";
    assertTrue(FindXSS.findCsrf(msg, "fakeId123"));
  }

  @Test
  void findCsrf_noImgTag() {
    assertFalse(FindXSS.findCsrf("<p>hello</p>", "fakeId"));
  }

  @Test
  void findCsrf_imgWithWrongPath() {
    String msg = "<img src=\"http://localhost/wrong/path?userId=fakeId\" />";
    assertFalse(FindXSS.findCsrf(msg, "fakeId"));
  }

  @Test
  void findCsrf_imgWithSpacesInTags() {
    String msg =
        "< img src=\"http://localhost/root/grantComplete/csrflesson?userId=fakeId123\" / >";
    assertTrue(FindXSS.findCsrf(msg, "fakeId123"));
  }
}
