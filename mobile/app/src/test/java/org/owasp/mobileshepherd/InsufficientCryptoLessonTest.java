package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.crypto.InsufficientCryptoLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

/**
 * Unit tests for InsufficientCryptoLessonFragment
 */
public class InsufficientCryptoLessonTest {

    @Test
    public void testWeakDesKeyConstantExists() throws Exception {
        Field keyField = InsufficientCryptoLessonFragment.class.getDeclaredField("WEAK_DES_KEY");
        keyField.setAccessible(true);
        String key = (String) keyField.get(null);
        assertNotNull("WEAK_DES_KEY constant should exist", key);
        assertFalse("WEAK_DES_KEY should not be empty", key.isEmpty());
    }

    @Test
    public void testWeakDesKeyValue() throws Exception {
        Field keyField = InsufficientCryptoLessonFragment.class.getDeclaredField("WEAK_DES_KEY");
        keyField.setAccessible(true);
        String key = (String) keyField.get(null);
        assertEquals("Hardcoded DES key should match the value visible in the decompiled APK",
                "SHEPHERD", key);
    }

    @Test
    public void testWeakDesKeyIsStaticFinal() throws Exception {
        Field keyField = InsufficientCryptoLessonFragment.class.getDeclaredField("WEAK_DES_KEY");
        assertTrue("WEAK_DES_KEY should be static", Modifier.isStatic(keyField.getModifiers()));
        assertTrue("WEAK_DES_KEY should be final", Modifier.isFinal(keyField.getModifiers()));
    }
}
