package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.utils.FlagValidator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for FlagValidator — verifies that offline flag values match their stored hashes.
 *
 * Each test uses the static {@link FlagValidator#validateFlag(FlagValidator.Module, String)}
 * method so no Android context is required.
 */
public class FlagValidatorTest {

    @Test
    public void testReLessonOfflineFlag() {
        assertTrue(FlagValidator.validateFlag(
                FlagValidator.Module.RE_LESSON, "Frozen_Clock_Melts_By_Noon"));
    }

    @Test
    public void testClientSideInjectionLessonOfflineFlag() {
        assertTrue(FlagValidator.validateFlag(
                FlagValidator.Module.CLIENT_SIDE_INJECTION_LESSON, "Marble_Rolls_Past_The_Cat"));
    }

    @Test
    public void testInsecureAuthLessonOfflineFlag() {
        assertTrue(FlagValidator.validateFlag(
                FlagValidator.Module.INSECURE_AUTH_LESSON, "Iron_Gate_Opens_By_Silence"));
    }

    @Test
    public void testInsecureCommLessonOfflineFlag() {
        assertTrue(FlagValidator.validateFlag(
                FlagValidator.Module.INSECURE_COMM_LESSON, "Signal_Lost_In_The_Fog"));
    }

    @Test
    public void testPrivacyLessonOfflineFlag() {
        assertTrue(FlagValidator.validateFlag(
                FlagValidator.Module.PRIVACY_LESSON, "Photo_Leaks_GPS_Data"));
    }

    @Test
    public void testInsufficientCryptoLessonOfflineFlag() {
        assertTrue(FlagValidator.validateFlag(
                FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON, "Weak_Key_Fails_The_Lock"));
    }

    @Test
    public void testWrongFlagRejected() {
        assertFalse(FlagValidator.validateFlag(
                FlagValidator.Module.RE_LESSON, "wrong_flag"));
    }

    @Test
    public void testEmptyFlagRejected() {
        assertFalse(FlagValidator.validateFlag(
                FlagValidator.Module.RE_LESSON, ""));
    }

    @Test
    public void testNullFlagRejected() {
        assertFalse(FlagValidator.validateFlag(
                FlagValidator.Module.RE_LESSON, null));
    }

    @Test
    public void testModuleWithNoHashReturnsFalse() {
        // Modules that require server-side validation return false locally
        assertFalse(FlagValidator.validateFlag(
                FlagValidator.Module.POOR_AUTH_LESSON, "any_value"));
    }
}
