package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

/** Validate.validateLanguage keeps the language the player picked (#937) */
class ValidateLanguageTest {

  private static String languageFor(Object lang) {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute("lang", lang);
    return Validate.validateLanguage(session);
  }

  @Test
  public void pickerLanguages_AreKept() {
    for (String lang : new String[] {"en", "es", "fr", "pt", "ga", "zh", "hi", "ru"}) {
      assertEquals(lang, languageFor(lang), lang);
    }
  }

  @Test
  public void languageWithRegion_IsReducedToTheLanguage() {
    assertEquals("es", languageFor("es_ES"));
    assertEquals("pt", languageFor("pt-BR"));
    // translation.jsp stores the request's Locale when no language was picked
    assertEquals("fr", languageFor(Locale.FRANCE));
  }

  @Test
  public void unsupportedOrMalformed_FallsBackToDefault() {
    for (String lang : new String[] {"de", "xen-GB", "EN", "english", "../es", "es_ES_x", ""}) {
      assertEquals("en_GB", languageFor(lang), lang);
    }
  }

  @Test
  public void missingLanguage_FallsBackToDefault() {
    assertEquals("en_GB", Validate.validateLanguage(new MockHttpSession()));
    assertEquals("en_GB", Validate.validateLanguage(null));
  }
}
