package testUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.xml.parsers.DocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.XmlDocumentBuilder;

public class TestXmlDocumentBuilder {

  private static final Logger log = LogManager.getLogger(TestXmlDocumentBuilder.class);

  @BeforeAll
  public static void initAll() {
    TestProperties.setTestPropertiesFileDirectory(log);
  }

  @Test
  @DisplayName("Should Return Type DocumentBuilder")
  void xmlDocBuilder_shouldReturnDocumentBuilder() {
    DocumentBuilder db = XmlDocumentBuilder.xmlDocBuilder(true, true, true, true, true, true);
    assertNotNull(db);
  }

  @Test
  @DisplayName("Secure config should disable XInclude")
  void xmlDocBuilder_secureConfig_xIncludeDisabled() {
    DocumentBuilder db = XmlDocumentBuilder.xmlDocBuilder(true, false, false, false, false, false);
    assertNotNull(db);
    assertFalse(db.isXIncludeAware());
  }

  @Test
  @DisplayName("Insecure config with XInclude enabled")
  void xmlDocBuilder_insecureConfig_xIncludeEnabled() {
    DocumentBuilder db = XmlDocumentBuilder.xmlDocBuilder(false, true, true, true, true, true);
    assertNotNull(db);
    assertTrue(db.isXIncludeAware());
  }

  @Test
  @DisplayName("Namespace awareness should be false by default")
  void xmlDocBuilder_namespaceAwareness() {
    DocumentBuilder db = XmlDocumentBuilder.xmlDocBuilder(true, false, false, false, false, false);
    assertNotNull(db);
    assertFalse(db.isNamespaceAware());
  }

  @Test
  @DisplayName("Secure config should not be validating")
  void xmlDocBuilder_secureConfig_notValidating() {
    DocumentBuilder db = XmlDocumentBuilder.xmlDocBuilder(true, false, false, false, false, false);
    assertNotNull(db);
    assertFalse(db.isValidating());
  }

  @Test
  @DisplayName("All false params should still return a builder")
  void xmlDocBuilder_allFalse_returnsBuilder() {
    DocumentBuilder db = XmlDocumentBuilder.xmlDocBuilder(false, false, false, false, false, false);
    assertNotNull(db);
  }
}
