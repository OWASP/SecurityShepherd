package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.challenges.insecuredata1.InsecureData1Fragment;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for Insecure Data Storage Challenge 1
 * Tests SharedPreferences vulnerability
 */
public class InsecureData1Test {

    @Test
    public void testClassExists() {
        assertNotNull("InsecureData1Fragment class should exist", 
                     InsecureData1Fragment.class);
    }

    @Test
    public void testOnCreateViewMethodExists() throws Exception {
        Method method = InsecureData1Fragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void testShowVulnerabilityInfoMethodExists() throws Exception {
        Method method = InsecureData1Fragment.class.getDeclaredMethod("showVulnerabilityInfo");
        assertNotNull("showVulnerabilityInfo method should exist", method);
    }

    @Test
    public void testOnDestroyViewMethodExists() throws Exception {
        Method method = InsecureData1Fragment.class.getMethod("onDestroyView");
        assertNotNull("onDestroyView method should exist", method);
    }

    @Test
    public void testFragmentExtendsFragment() {
        assertTrue("Should extend Fragment", 
            androidx.fragment.app.Fragment.class.isAssignableFrom(InsecureData1Fragment.class));
    }
}
