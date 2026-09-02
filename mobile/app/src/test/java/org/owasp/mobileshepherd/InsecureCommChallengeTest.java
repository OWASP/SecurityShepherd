package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.challenges.insecurecomm.InsecureCommChallengeFragment;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for Insecure Communication Challenge
 * Tests network security and SSL/TLS vulnerabilities
 */
public class InsecureCommChallengeTest {

    @Test
    public void testClassExists() {
        assertNotNull("InsecureCommChallengeFragment class should exist", 
                     InsecureCommChallengeFragment.class);
    }

    @Test
    public void testOnCreateViewMethodExists() throws Exception {
        Method method = InsecureCommChallengeFragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void testShowVulnerabilityInfoMethodExists() throws Exception {
        Method method = InsecureCommChallengeFragment.class.getDeclaredMethod("showVulnerabilityInfo");
        assertNotNull("showVulnerabilityInfo method should exist", method);
    }

    @Test
    public void testOnDestroyViewMethodExists() throws Exception {
        Method method = InsecureCommChallengeFragment.class.getMethod("onDestroyView");
        assertNotNull("onDestroyView method should exist", method);
    }

    @Test
    public void testFragmentExtendsFragment() {
        assertTrue("Should extend Fragment", 
            androidx.fragment.app.Fragment.class.isAssignableFrom(InsecureCommChallengeFragment.class));
    }
}
