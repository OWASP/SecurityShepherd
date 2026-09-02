package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.insecureauthorization.InsecureAuthorizationLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

/**
 * Unit tests for InsecureAuthorizationLessonFragment - testing demo credentials and admin flag
 */
public class InsecureAuthorizationLessonTest {

    @Test
    public void testDemoUsernameExists() throws Exception {
        Field usernameField = InsecureAuthorizationLessonFragment.class.getDeclaredField("DEMO_USERNAME");
        usernameField.setAccessible(true);
        String username = (String) usernameField.get(null);
        
        assertNotNull("Demo username should exist", username);
        assertFalse("Username should not be empty", username.isEmpty());
        assertEquals("Username should be 'testuser'", "testuser", username);
    }

    @Test
    public void testDemoPasswordExists() throws Exception {
        Field passwordField = InsecureAuthorizationLessonFragment.class.getDeclaredField("DEMO_PASSWORD");
        passwordField.setAccessible(true);
        String password = (String) passwordField.get(null);
        
        assertNotNull("Demo password should exist", password);
        assertFalse("Password should not be empty", password.isEmpty());
        assertEquals("Password should be 'password123'", "password123", password);
    }

    @Test
    public void testPrefsNameConstant() throws Exception {
        Field prefsField = InsecureAuthorizationLessonFragment.class.getDeclaredField("PREFS_NAME");
        prefsField.setAccessible(true);
        String prefsName = (String) prefsField.get(null);
        
        assertEquals("SharedPreferences name should be 'UserSession'", "UserSession", prefsName);
    }

}
