package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.poorauth.PoorAuthLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

/**
 * Unit tests for PoorAuthLessonFragment
 * The fragment stores a hashed PIN and an obfuscated flag (XOR + Base64 encoded).
 */
public class PoorAuthLessonTest {

    @Test
    public void testHardcodedPinHashExists() throws Exception {
        Field hashField = PoorAuthLessonFragment.class.getDeclaredField("HARDCODED_PIN_HASH");
        hashField.setAccessible(true);
        String hash = (String) hashField.get(null);
        assertNotNull("HARDCODED_PIN_HASH should exist", hash);
        assertFalse("Hash should not be empty", hash.isEmpty());
    }

    @Test
    public void testHardcodedPinHashIsHex() throws Exception {
        Field hashField = PoorAuthLessonFragment.class.getDeclaredField("HARDCODED_PIN_HASH");
        hashField.setAccessible(true);
        String hash = (String) hashField.get(null);
        assertTrue("Hash should be a lowercase hex string", hash.matches("[0-9a-f]+"));
    }

    @Test
    public void testHardcodedPinHashIsSha256Length() throws Exception {
        Field hashField = PoorAuthLessonFragment.class.getDeclaredField("HARDCODED_PIN_HASH");
        hashField.setAccessible(true);
        String hash = (String) hashField.get(null);
        assertEquals("SHA-256 hash should be 64 hex characters", 64, hash.length());
    }

    @Test
    public void testObfuscatedFlagArrayExists() throws Exception {
        Field fField = PoorAuthLessonFragment.class.getDeclaredField("F");
        fField.setAccessible(true);
        Object value = fField.get(null);
        assertNotNull("Obfuscated flag array F should exist", value);
        assertTrue("F should be a String array", value instanceof String[]);
        assertTrue("F array should not be empty", ((String[]) value).length > 0);
    }

    @Test
    public void testXorKeyExists() throws Exception {
        Field kField = PoorAuthLessonFragment.class.getDeclaredField("K");
        assertEquals("K field should be of type byte", byte.class, kField.getType());
        assertTrue("K should be static", Modifier.isStatic(kField.getModifiers()));
        assertTrue("K should be final", Modifier.isFinal(kField.getModifiers()));
    }
}
