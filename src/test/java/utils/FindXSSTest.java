package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
  void search_detectsOnpointeroverAlert() {
    assertTrue(FindXSS.search("<div onpointerover=\"alert(1)\">MOVE HERE</div>"));
  }

  @Test
  void search_detectsOnpointerenterAlert() {
    assertTrue(FindXSS.search("<div onpointerenter=\"alert(1)\">hover</div>"));
  }

  @Test
  void search_detectsOnanimationstartAlert() {
    assertTrue(FindXSS.search("<div onanimationstart=\"alert(1)\">animation</div>"));
  }

  @Test
  void search_detectsOntransitionendAlert() {
    assertTrue(FindXSS.search("<div ontransitionend=\"alert(1)\">transition</div>"));
  }

  @Test
  void search_detectsOnauxclickAlert() {
    assertTrue(FindXSS.search("<div onauxclick=\"alert(1)\">click</div>"));
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

  // search — deeply nested input (#960: jtidy r938 overflows the stack, CVE-2023-34623)

  /** Tomcat request threads run on the JVM's default thread stack size, 1 MB on 64-bit JVMs. */
  private static final long REQUEST_THREAD_STACK = 1024 * 1024;

  /**
   * Runs {@link FindXSS#search(String)} on a thread with the given stack size, so the result does
   * not depend on how large the test runner's own stack is, and rethrows anything it throws.
   */
  private static boolean searchWithStack(String input, long stackSize) throws Exception {
    AtomicReference<Throwable> thrown = new AtomicReference<>();
    AtomicBoolean result = new AtomicBoolean();
    Thread thread =
        new Thread(
            null,
            () -> {
              try {
                result.set(FindXSS.search(input));
              } catch (Throwable t) {
                thrown.set(t);
              }
            },
            "findxss-test",
            stackSize);
    thread.start();
    thread.join();
    if (thrown.get() != null) {
      throw new AssertionError("search threw " + thrown.get(), thrown.get());
    }
    return result.get();
  }

  @Test
  void search_deeplyNestedBlocksDoNotOverflowTheStack() throws Exception {
    assertFalse(
        searchWithStack(
            "<div>".repeat(10_000) + "<script>alert(1)</script>", REQUEST_THREAD_STACK));
    assertFalse(
        searchWithStack(
            "<div>".repeat(50_000) + "<script>alert(1)</script>", REQUEST_THREAD_STACK));
  }

  @Test
  void search_reopenedInlineElementsDoNotOverflowTheStack() throws Exception {
    // jtidy re-opens every <font> on its inline stack inside each new <map>, so the tree it builds
    // is about n²/4 deep for n tags: on dev, about 160 '<' characters overflow a 1 MB stack.
    assertFalse(
        searchWithStack(
            "<font><map>".repeat(100) + "<script>alert(1)</script>", REQUEST_THREAD_STACK));
  }

  @Test
  void search_inputAtTheTagLimitIsStillChecked() {
    // 30 + 2 '<' characters
    String payload = "<b>".repeat(FindXSS.MAX_TAG_OPENERS - 2) + "<script>alert(1)</script>";
    assertTrue(FindXSS.search(payload));
  }

  @Test
  void search_inputOverTheTagLimitIsNotDetected() {
    String payload = "<b>".repeat(FindXSS.MAX_TAG_OPENERS - 1) + "<script>alert(1)</script>";
    assertFalse(FindXSS.search(payload));
  }

  private static int countTags(String input) {
    return (int) input.chars().filter(c -> c == '<').count();
  }

  /** Repeats the tags of {@code unit} after {@code prefix} up to MAX_TAG_OPENERS '<'s in all. */
  private static String fillToTagLimit(String prefix, String unit) {
    String[] tags = unit.split("(?=<)");
    StringBuilder input = new StringBuilder(prefix);
    for (int i = 0, count = countTags(prefix); count < FindXSS.MAX_TAG_OPENERS; i++, count++) {
      input.append(tags[i % tags.length]);
    }
    return input.toString();
  }

  @Test
  void search_deepestShapesAtTheTagLimitFitInA256KbStack() throws Exception {
    // The shapes that build the deepest jtidy trees per '<' found while fixing #960: inline
    // elements re-opened in every <map>, and <font>s pushed again and again. If this fails,
    // MAX_TAG_OPENERS has been raised too far.
    String inlines = "<b><i><u><s><strike><em><strong><small><big><sub>";
    String[] shapes = {
      fillToTagLimit("", "<font><map>"),
      fillToTagLimit(inlines, "<map>"),
      fillToTagLimit(inlines, "<font><map>"),
      fillToTagLimit(inlines + "<font>".repeat(8), "<map>"),
      fillToTagLimit("<font>".repeat(16), "<map>"),
      fillToTagLimit("", "<li><div>"),
      fillToTagLimit("", "<ul>")
    };
    for (String shape : shapes) {
      assertEquals(FindXSS.MAX_TAG_OPENERS, countTags(shape), shape);
      assertFalse(searchWithStack(shape, 256 * 1024), shape);
    }
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
