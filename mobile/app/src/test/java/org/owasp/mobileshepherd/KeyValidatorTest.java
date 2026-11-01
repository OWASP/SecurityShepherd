package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.utils.KeyValidator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for KeyValidator — verifies that offline key values match their stored hashes.
 *
 * Each test uses the static {@link KeyValidator#validateKey(KeyValidator.Module, String)}
 * method so no Android context is required.
 */
public class KeyValidatorTest {

    @Test
    public void testReLessonOfflineKey() {
        assertTrue(KeyValidator.validateKey(
                KeyValidator.Module.RE_LESSON, "Frozen_Clock_Melts_By_Noon"));
    }

    @Test
    public void testClientSideInjectionLessonHasNoOfflineHash() {
        // No offline hash exists for this module — even the correct key is rejected locally.
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.CLIENT_SIDE_INJECTION_LESSON, "Marble_Rolls_Past_The_Cat"));
    }

    @Test
    public void testInsecureAuthLessonHasNoOfflineHash() {
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.INSECURE_AUTH_LESSON, "Iron_Gate_Opens_By_Silence"));
    }

    @Test
    public void testInsecureCommLessonHasNoOfflineHash() {
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.INSECURE_COMM_LESSON, "Signal_Lost_In_The_Fog"));
    }

    @Test
    public void testPrivacyLessonHasNoOfflineHash() {
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.PRIVACY_LESSON, "Photo_Leaks_GPS_Data"));
    }

    @Test
    public void testInsufficientCryptoLessonHasNoOfflineHash() {
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.INSUFFICIENT_CRYPTO_LESSON, "Weak_Key_Fails_The_Lock"));
    }

    @Test
    public void testWrongKeyRejected() {
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.RE_LESSON, "wrong_key"));
    }

    @Test
    public void testEmptyKeyRejected() {
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.RE_LESSON, ""));
    }

    @Test
    public void testNullKeyRejected() {
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.RE_LESSON, null));
    }

    @Test
    public void testModuleWithNoHashReturnsFalse() {
        // Modules that require server-side validation return false locally
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.POOR_AUTH_LESSON, "any_value"));
    }
}
