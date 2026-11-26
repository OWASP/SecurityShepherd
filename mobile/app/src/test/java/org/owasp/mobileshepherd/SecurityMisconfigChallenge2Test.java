package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.challenges.securitymisconfig.SecurityMisconfigChallenge2Fragment;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for Security Misconfiguration Challenge 2
 * Tests security configuration vulnerabilities
 */
public class SecurityMisconfigChallenge2Test {

    @Test
    public void testClassExists() {
        assertNotNull("SecurityMisconfigChallenge2Fragment class should exist", 
                     SecurityMisconfigChallenge2Fragment.class);
    }

    @Test
    public void testOnCreateViewMethodExists() throws Exception {
        Method method = SecurityMisconfigChallenge2Fragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void testShowVulnerabilityInfoMethodExists() throws Exception {
        Method method = SecurityMisconfigChallenge2Fragment.class.getDeclaredMethod("showVulnerabilityInfo");
        assertNotNull("showVulnerabilityInfo method should exist", method);
    }

    @Test
    public void testOnDestroyViewMethodExists() throws Exception {
        Method method = SecurityMisconfigChallenge2Fragment.class.getMethod("onDestroyView");
        assertNotNull("onDestroyView method should exist", method);
    }

    @Test
    public void testFragmentExtendsFragment() {
        assertTrue("Should extend Fragment", 
            androidx.fragment.app.Fragment.class.isAssignableFrom(SecurityMisconfigChallenge2Fragment.class));
    }
}
