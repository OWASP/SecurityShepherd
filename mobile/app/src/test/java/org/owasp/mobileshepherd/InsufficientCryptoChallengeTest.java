package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.challenges.crypto.InsufficientCryptoChallengeFragment;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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

    @Test
    public void testXorKeyMatchesHintedValue() throws Exception {
        Field xorKeyField = InsufficientCryptoChallengeFragment.class.getDeclaredField("XOR_KEY");
        xorKeyField.setAccessible(true);
        byte xorKey = (byte) xorKeyField.get(null);
        assertEquals("XOR_KEY must stay 0x42 to match the hardcoded-key hint shown to students",
                (byte) 0x42, xorKey);
    }

    @Test
    public void testSharedPreferencesNamesMatchAdbHint() throws Exception {
        Field prefsNameField = InsufficientCryptoChallengeFragment.class.getDeclaredField("PREFS_NAME");
        prefsNameField.setAccessible(true);
        assertEquals("recovery_vault", prefsNameField.get(null));

        Field prefsKeyField = InsufficientCryptoChallengeFragment.class.getDeclaredField("PREFS_KEY");
        prefsKeyField.setAccessible(true);
        assertEquals("vault_data", prefsKeyField.get(null));
    }

    @Test
    public void testXorEncodingIsSelfReversing() throws Exception {
        Field xorKeyField = InsufficientCryptoChallengeFragment.class.getDeclaredField("XOR_KEY");
        xorKeyField.setAccessible(true);
        byte xorKey = (byte) xorKeyField.get(null);

        String plaintext = "{\"session_token\":\"KEY{test}\"}";
        byte[] original = plaintext.getBytes(StandardCharsets.UTF_8);

        byte[] encoded = new byte[original.length];
        for (int i = 0; i < original.length; i++) {
            encoded[i] = (byte) (original[i] ^ xorKey);
        }

        byte[] decoded = new byte[encoded.length];
        for (int i = 0; i < encoded.length; i++) {
            decoded[i] = (byte) (encoded[i] ^ xorKey);
        }

        assertEquals("XOR with the same single-byte key must be reversible — "
                + "this is the vulnerability the challenge teaches",
                plaintext, new String(decoded, StandardCharsets.UTF_8));
        assertFalse("Encoded bytes should not equal the plaintext bytes",
                plaintext.equals(new String(encoded, StandardCharsets.UTF_8)));
    }

    @Test
    public void testOfflineValidationHasNoLocalHashForThisModule() {
        // INSUFFICIENT_CRYPTO_CHALLENGE has no entry in KeyValidator's offline hash table,
        // so validation always requires a live server session. Guards against silently
        // reintroducing an offline bypass for this module.
        assertFalse(KeyValidator.validateKey(
                KeyValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE, "KEY{anything}"));
    }
}
