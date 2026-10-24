package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.poorauth.PoorAuthLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import static org.junit.Assert.*;

/**
 * Unit tests for PoorAuthLessonFragment
 * The fragment stores a hashed 6-digit PIN; the key itself is issued by the server.
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
    public void testHardcodedPinHashIsSha256OfExpectedPin() throws Exception {
        Field hashField = PoorAuthLessonFragment.class.getDeclaredField("HARDCODED_PIN_HASH");
        hashField.setAccessible(true);
        String hash = (String) hashField.get(null);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest("654321".getBytes(StandardCharsets.UTF_8));
        StringBuilder expected = new StringBuilder();
        for (byte b : digest) {
            expected.append(String.format("%02x", b));
        }
        assertEquals("Hardcoded hash should be the SHA-256 of the 6-digit PIN", expected.toString(), hash);
    }

    @Test
    public void testNoClientSideKeyFallback() {
        for (Field f : PoorAuthLessonFragment.class.getDeclaredFields()) {
            assertFalse("Key must come from the server, not an embedded fallback: " + f.getName(),
                    f.getName().equals("F") || f.getName().equals("K"));
        }
    }
}
