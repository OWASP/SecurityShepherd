package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic corpus of XSS-like payloads used to check that {@link FindXSS#search(String)} keeps
 * the verdicts it gave before the jtidy nesting guard was added (issue #960).
 *
 * <p>The corpus is split into sections so that a changed verdict is easy to locate. Changing the
 * corpus (adding, removing or reordering payloads) invalidates the recorded verdicts in {@code
 * src/test/resources/utils/FindXSSVerdicts.txt}; regenerate them with {@link
 * FindXSSVerdictRecorder} against the code the comparison should be made with.
 */
final class FindXSSPayloadCorpus {

  private FindXSSPayloadCorpus() {}

  /** Tags known to jtidy, tags it rejects (HTML5, SVG, custom) and a couple of void elements. */
  static final String[] TAGS = {
    "a",
    "b",
    "body",
    "button",
    "details",
    "div",
    "embed",
    "form",
    "frameset",
    "h1",
    "iframe",
    "img",
    "input",
    "isindex",
    "keygen",
    "marquee",
    "object",
    "p",
    "select",
    "span",
    "style",
    "svg",
    "table",
    "td",
    "textarea",
    "video",
    "audio",
    "x",
    "custom-tag",
    "math"
  };

  static final String[] EVENT_HANDLERS = {
    "onclick",
    "onerror",
    "onload",
    "onmouseover",
    "onfocus",
    "onpointerover",
    "ontoggle",
    "onanimationstart",
    "onauxclick",
    "formaction",
    "onClick",
    "ONLOAD",
    "onnotanevent",
    "onblur"
  };

  static final String[] JS_FORMS = {
    "alert(1)",
    "alert`1`",
    "javascript:alert(1)",
    "window.alert(document.cookie)",
    "ALERT(1)",
    "confirm(1)",
    "prompt(1)",
    "alert&#40;1&#41;"
  };

  /** Opening and closing quote for an attribute value. The last one is unquoted. */
  static final String[][] QUOTES = {{"\"", "\""}, {"'", "'"}, {"`", "`"}, {"", ""}};

  static final String[] URI_ATTRIBUTES = {"href", "src", "action", "formaction", "data", "HREF"};

  static final String[] URI_SCHEMES = {
    "javascript", "JaVaScRiPt", "data", "vbscript", " javascript", "java\tscript"
  };

  static final String[] COLONS = {
    ":", "&#x3a", "&#x3a;", "&#58", "&#58;", "&colon;", "%3a", "&#0000058;"
  };

  static final String[] SCRIPT_PREFIXES = {
    "",
    "x",
    "<p>",
    "</title>",
    "\"><",
    "'>",
    "<!--",
    "-->",
    "<svg>",
    "<b>",
    "]]>",
    "<textarea>",
    "</textarea>",
    "<style>",
    "</style>",
    "<noscript>",
    "<![CDATA[",
    "<html><body>",
    "<x>",
    "<table>"
  };

  static final String[] SCRIPT_FORMS = {
    "<script>alert(1)</script>",
    "<SCRIPT>alert(1)</SCRIPT>",
    "<script>alert(1)",
    "<scr<script>ipt>alert(1)</script>",
    "<scr<script>ipt>alert(1)</scr</script>ipt>",
    "<script src=x>alert(1)</script>",
    "<script type=\"text/javascript\">alert(1)</script>",
    "<script >alert(1)</script >",
    "<script/x>alert(1)</script>",
    "<script\n>alert(1)</script>",
    "<script>alert`1`</script>",
    "<script>prompt(1)</script>",
    "<script>console.log(1)</script>",
    "< script>alert(1)</script>",
    "<script>/*alert*/</script>",
    "<script><!--\nalert(1)\n--></script>",
    "<script>document.write('<script>alert(1)</scr'+'ipt>')</script>",
    "&lt;script&gt;alert(1)&lt;/script&gt;",
    "<script>al\\u0065rt(1)</script>",
    "<script>alert(1)</script><script>alert(2)</script>"
  };

  static final String[] SCRIPT_SUFFIXES = {"", "x", "</p>", "<br>", "-->"};

  /** Payloads from FindXSSTest and XssLessonIT. */
  static final String[] KNOWN_PAYLOADS = {
    "<script>alert(1)</script>",
    "<script>console.log(1)</script>",
    "<p>Hello World</p>",
    "",
    "just some text",
    "<div onclick=\"alert(1)\">click</div>",
    "<img src=x onerror=\"alert(1)\">",
    "<span onmouseover=\"alert(1)\">hover</span>",
    "<div onpointerover=\"alert(1)\">MOVE HERE</div>",
    "<div onpointerenter=\"alert(1)\">hover</div>",
    "<div onanimationstart=\"alert(1)\">animation</div>",
    "<div ontransitionend=\"alert(1)\">transition</div>",
    "<div onauxclick=\"alert(1)\">click</div>",
    "<div onclick=\"doSomething()\">click</div>",
    "<a href=\"javascript:alert(1)\">link</a>",
    "<a href=\"data:text/html,<script>alert(1)</script>\">x</a>",
    "<a href=\"http://example.com\">link</a>",
    "<img src='#' onerror='alert(1)'>",
    "<iframe src='javascript:alert(1);//'></iframe>",
    "<a href='javascript:alert(1);//'>click me</a>",
    "<p>NotXSSAtAll</p>",
    "<script>alert(1)"
  };

  /** Returns the corpus, section name to payloads, in a fixed order. */
  static Map<String, List<String>> sections() {
    Map<String, List<String>> sections = new LinkedHashMap<>();

    List<String> events = new ArrayList<>();
    for (String tag : TAGS) {
      for (String handler : EVENT_HANDLERS) {
        for (String js : JS_FORMS) {
          for (String[] q : QUOTES) {
            events.add("<" + tag + " " + handler + "=" + q[0] + js + q[1] + ">x</" + tag + ">");
          }
        }
      }
    }
    sections.put("events", events);

    List<String> uris = new ArrayList<>();
    for (String tag : TAGS) {
      for (String attribute : URI_ATTRIBUTES) {
        for (String scheme : URI_SCHEMES) {
          for (String colon : COLONS) {
            for (String[] q : QUOTES) {
              uris.add(
                  "<"
                      + tag
                      + " "
                      + attribute
                      + "="
                      + q[0]
                      + scheme
                      + colon
                      + "alert(1)"
                      + q[1]
                      + ">x</"
                      + tag
                      + ">");
            }
          }
        }
      }
    }
    sections.put("uris", uris);

    List<String> scripts = new ArrayList<>();
    for (String prefix : SCRIPT_PREFIXES) {
      for (String form : SCRIPT_FORMS) {
        for (String suffix : SCRIPT_SUFFIXES) {
          scripts.add(prefix + form + suffix);
        }
      }
    }
    sections.put("scripts", scripts);

    sections.put("known", List.of(KNOWN_PAYLOADS));
    return sections;
  }
}
