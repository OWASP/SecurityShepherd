package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.InputValidationLessonFragment;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for InputValidationLessonFragment - testing deep link URL handling.
 * The key is issued by the server, so only the validation structure is checked here.
 */
public class InputValidationLessonTest {

    @Test
    public void testProcessDeepLinkMethodExists() throws Exception {
        Method method = InputValidationLessonFragment.class.getDeclaredMethod("processDeepLink", String.class);
        assertNotNull("processDeepLink method should exist", method);
    }

    @Test
    public void testUrlAllowListCheckExists() throws Exception {
        Method method = InputValidationLessonFragment.class.getDeclaredMethod("isUrlAllowed", String.class);
        assertEquals("isUrlAllowed should return boolean", boolean.class, method.getReturnType());
    }

    private static boolean allowed(String url) throws Exception {
        Method method = InputValidationLessonFragment.class.getDeclaredMethod("isUrlAllowed", String.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(null, url);
    }

    @Test
    public void testTrustedUrlAllowed() throws Exception {
        assertTrue(allowed("https://toomanysheep.com/"));
    }

    @Test
    public void testOtherHostBlocked() throws Exception {
        assertFalse(allowed("https://attacker.test/"));
        assertFalse(allowed("http://toomanysheep.com/"));
    }

    @Test
    public void testPrefixCheckCanBeBypassed() throws Exception {
        assertTrue(allowed("https://toomanysheep.com@attacker.test/"));
        assertTrue(allowed("https://toomanysheep.com.attacker.test/"));
    }
    @Test
    public void testNoHardcodedKeyField() {
        for (java.lang.reflect.Field f : InputValidationLessonFragment.class.getDeclaredFields()) {
            assertFalse("Key must be server-issued, not a hardcoded field: " + f.getName(),
                    f.getName().equals("ADMIN_KEY") || f.getName().equals("SECRET_KEY"));
        }
    }
}
