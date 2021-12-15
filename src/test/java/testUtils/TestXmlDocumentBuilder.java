package testUtils;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.XmlDocumentBuilder;

import testUtils.TestProperties;

import javax.xml.parsers.DocumentBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;

public class TestXmlDocumentBuilder {
    private static org.apache.logging.log4j.Logger log = LogManager.getLogger(TestXmlDocumentBuilder.class);

    @BeforeAll
    public static void initAll()
    {
        TestProperties.setTestPropertiesFileDirectory(log);
    }

    @Test
    @DisplayName("Should Return Type DocumentBuilder")
    public void xmlDocBuilder_ShouldReturnTypeDocumentBuilder()
    {
        DocumentBuilder db = XmlDocumentBuilder.xmlDocBuilder(true, true, true, true, true, true);
        assertThat(db, instanceOf(DocumentBuilder.class));
        assertNotNull(db);
    }

}
