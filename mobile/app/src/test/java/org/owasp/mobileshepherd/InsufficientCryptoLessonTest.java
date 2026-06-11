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
    public void testAlgorithmConstantExists() throws Exception {
        Field algoField = InsufficientCryptoLessonFragment.class.getDeclaredField("ALGORITHM");
        algoField.setAccessible(true);
        String algorithm = (String) algoField.get(null);
        assertNotNull("ALGORITHM constant should exist", algorithm);
        assertFalse("ALGORITHM should not be empty", algorithm.isEmpty());
    }

    @Test
    public void testAlgorithmIsDes() throws Exception {
        Field algoField = InsufficientCryptoLessonFragment.class.getDeclaredField("ALGORITHM");
        algoField.setAccessible(true);
        String algorithm = (String) algoField.get(null);
        assertEquals("Algorithm should be DES (weak 56-bit cipher)", "DES", algorithm);
    }

    @Test
    public void testAlgorithmIsStaticFinal() throws Exception {
        Field algoField = InsufficientCryptoLessonFragment.class.getDeclaredField("ALGORITHM");
        assertTrue("ALGORITHM should be static", Modifier.isStatic(algoField.getModifiers()));
        assertTrue("ALGORITHM should be final", Modifier.isFinal(algoField.getModifiers()));
    }
}
