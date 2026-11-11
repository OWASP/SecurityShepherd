package org.owasp.mobileshepherd;

import org.junit.Test;
import org.owasp.mobileshepherd.utils.KeyValidator;

import static org.junit.Assert.*;

/**
 * Client-Side Injection keys are validated server-side only, so the offline
 * validator must reject every submission, including the correct key.
 */
public class KeyValidatorTest {

    @Test
    public void testClientSideInjectionLessonHasNoOfflineHash() {
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.CLIENT_SIDE_INJECTION_LESSON, "Marble_Rolls_Past_The_Cat"));
    }

    @Test
    public void testClientSideInjectionChallengesHaveNoOfflineHash() {
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1, "any_value"));
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_2, "any_value"));
    }

    @Test
    public void testEmptyKeyRejected() {
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.CLIENT_SIDE_INJECTION_LESSON, ""));
    }

    @Test
    public void testNullKeyRejected() {
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.CLIENT_SIDE_INJECTION_LESSON, null));
    }
}