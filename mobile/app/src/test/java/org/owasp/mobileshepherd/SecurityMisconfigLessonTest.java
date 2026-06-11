package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.securitymisconfig.SecurityMisconfigLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for SecurityMisconfigLessonFragment
 * Tests verify class structure and TAG constant
 */
public class SecurityMisconfigLessonTest {

    @Test
    public void testClassExists() {
        assertNotNull("SecurityMisconfigLessonFragment class should exist", 
                     SecurityMisconfigLessonFragment.class);
    }

    @Test
    public void testTagConstantExists() throws Exception {
        Field tagField = SecurityMisconfigLessonFragment.class.getDeclaredField("TAG");
        tagField.setAccessible(true);
        String tag = (String) tagField.get(null);
        
        assertNotNull("TAG should exist", tag);
        assertEquals("TAG should be 'SecurityMisconfig'", "SecurityMisconfig", tag);
    }

    @Test
    public void testCheckConfigurationMethodExists() throws Exception {
        Method method = SecurityMisconfigLessonFragment.class.getDeclaredMethod("checkConfiguration");
        assertNotNull("checkConfiguration method should exist", method);
    }

    @Test
    public void testShowDetailedInfoMethodExists() throws Exception {
        Method method = SecurityMisconfigLessonFragment.class.getDeclaredMethod("showDetailedInfo");
        assertNotNull("showDetailedInfo method should exist", method);
    }
    
    @Test
    public void testFragmentHasOnCreateViewMethod() throws Exception {
        Method method = SecurityMisconfigLessonFragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void testFragmentExtendsFragment() {
        assertTrue("SecurityMisconfigLessonFragment should extend Fragment",
                  androidx.fragment.app.Fragment.class.isAssignableFrom(SecurityMisconfigLessonFragment.class));
    }
}
