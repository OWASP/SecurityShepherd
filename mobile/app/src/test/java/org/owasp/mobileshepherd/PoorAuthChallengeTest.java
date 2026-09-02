package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.challenges.poorauth.PoorAuthChallengeFragment;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for Poor Authentication Challenge
 * Tests weak authentication implementation
 */
public class PoorAuthChallengeTest {

    @Test
    public void testClassExists() {
        assertNotNull("PoorAuthChallengeFragment class should exist", 
                     PoorAuthChallengeFragment.class);
    }

    @Test
    public void testOnCreateViewMethodExists() throws Exception {
        Method method = PoorAuthChallengeFragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void testShowVulnerabilityInfoMethodExists() throws Exception {
        Method method = PoorAuthChallengeFragment.class.getDeclaredMethod("showVulnerabilityInfo");
        assertNotNull("showVulnerabilityInfo method should exist", method);
    }

    @Test
    public void testOnDestroyViewMethodExists() throws Exception {
        Method method = PoorAuthChallengeFragment.class.getMethod("onDestroyView");
        assertNotNull("onDestroyView method should exist", method);
    }

    @Test
    public void testFragmentExtendsFragment() {
        assertTrue("Should extend Fragment", 
            androidx.fragment.app.Fragment.class.isAssignableFrom(PoorAuthChallengeFragment.class));
    }
}
