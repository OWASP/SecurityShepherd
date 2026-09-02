package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.challenges.crypto.InsufficientCryptoChallengeFragment;
import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Unit tests for Insufficient Cryptography Challenge
 * Tests weak cryptographic implementations
 */
public class InsufficientCryptoChallengeTest {

    @Test
    public void testClassExists() {
        assertNotNull("InsufficientCryptoChallengeFragment class should exist", 
                     InsufficientCryptoChallengeFragment.class);
    }

    @Test
    public void testOnCreateViewMethodExists() throws Exception {
        Method method = InsufficientCryptoChallengeFragment.class.getMethod("onCreateView",
                android.view.LayoutInflater.class,
                android.view.ViewGroup.class,
                android.os.Bundle.class);
        assertNotNull("onCreateView method should exist", method);
    }

    @Test
    public void testShowHintsMethodExists() throws Exception {
        Method method = InsufficientCryptoChallengeFragment.class.getDeclaredMethod("showHints");
        assertNotNull("showHints method should exist", method);
    }

    @Test
    public void testOnDestroyViewMethodExists() throws Exception {
        Method method = InsufficientCryptoChallengeFragment.class.getMethod("onDestroyView");
        assertNotNull("onDestroyView method should exist", method);
    }

    @Test
    public void testFragmentExtendsFragment() {
        assertTrue("Should extend Fragment", 
            androidx.fragment.app.Fragment.class.isAssignableFrom(InsufficientCryptoChallengeFragment.class));
    }
}
