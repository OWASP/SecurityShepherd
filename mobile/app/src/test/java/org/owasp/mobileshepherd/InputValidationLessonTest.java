package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.InputValidationLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for InputValidationLessonFragment - testing deep link URL handling
 */
public class InputValidationLessonTest {

    @Test
    public void testAdminUrlConstantExists() throws Exception {
        Field urlField = InputValidationLessonFragment.class.getDeclaredField("ADMIN_URL");
        urlField.setAccessible(true);
        String adminUrl = (String) urlField.get(null);
        assertNotNull("ADMIN_URL constant should exist", adminUrl);
        assertFalse("ADMIN_URL should not be empty", adminUrl.isEmpty());
    }

    @Test
    public void testAdminUrlValue() throws Exception {
        Field urlField = InputValidationLessonFragment.class.getDeclaredField("ADMIN_URL");
        urlField.setAccessible(true);
        String adminUrl = (String) urlField.get(null);
        assertEquals("ADMIN_URL should match expected value",
                "https://admin.internal/dashboard", adminUrl);
    }

    @Test
    public void testAdminUrlIsHttps() throws Exception {
        Field urlField = InputValidationLessonFragment.class.getDeclaredField("ADMIN_URL");
        urlField.setAccessible(true);
        String adminUrl = (String) urlField.get(null);
        assertTrue("ADMIN_URL should be an HTTPS URL", adminUrl.startsWith("https://"));
    }

    @Test
    public void testProcessDeepLinkMethodExists() throws Exception {
        Method method = InputValidationLessonFragment.class.getDeclaredMethod("processDeepLink", String.class);
        assertNotNull("processDeepLink method should exist", method);
    }
}
