package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.lessons.crypto.InsufficientCryptoLessonFragment;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import static org.junit.Assert.*;

/**
 * Unit tests for InsufficientCryptoLessonFragment
 */
public class InsufficientCryptoLessonTest {

    @Test
    public void testSecretKeysConstantHasFourThematicKeys() throws Exception {
        Field keysField = InsufficientCryptoLessonFragment.class.getDeclaredField("SECRET_KEYS");
        keysField.setAccessible(true);
        String[] keys = (String[]) keysField.get(null);
        assertArrayEquals("Each seeded secret should keep its own guessable word as its DES key",
                new String[] {"sheep", "shepherd", "grass", "wool"}, keys);
    }

    @Test
    public void testSecretKeysConstantIsStaticFinal() throws Exception {
        Field keysField = InsufficientCryptoLessonFragment.class.getDeclaredField("SECRET_KEYS");
        assertTrue("SECRET_KEYS should be static", Modifier.isStatic(keysField.getModifiers()));
        assertTrue("SECRET_KEYS should be final", Modifier.isFinal(keysField.getModifiers()));
    }

    @Test
    public void testDesRoundTripIsReversibleWithMatchingKey() throws Exception {
        // Exercise buildCipher directly rather than desEncrypt/desDecrypt: those
        // wrap android.util.Base64, which returns defaults (not real encode/decode)
        // under plain JVM unit tests.
        Method buildCipher = InsufficientCryptoLessonFragment.class
                .getDeclaredMethod("buildCipher", int.class, String.class);
        buildCipher.setAccessible(true);

        InsufficientCryptoLessonFragment fragment = new InsufficientCryptoLessonFragment();
        String plaintext = "KEY{round-trip-check}";

        Cipher encryptCipher = (Cipher) buildCipher.invoke(fragment, Cipher.ENCRYPT_MODE, "wool");
        byte[] encrypted = encryptCipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        Cipher decryptCipher = (Cipher) buildCipher.invoke(fragment, Cipher.DECRYPT_MODE, "wool");
        byte[] decrypted = decryptCipher.doFinal(encrypted);

        assertEquals("Encrypting then decrypting with the same weak key must recover the plaintext",
                plaintext, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    public void testInteractiveDecryptMethodsExist() throws Exception {
        assertNotNull(InsufficientCryptoLessonFragment.class
                .getDeclaredMethod("promptToDecryptSecret",
                        Class.forName(InsufficientCryptoLessonFragment.class.getName() + "$EncryptedSecret")));
        assertNotNull(InsufficientCryptoLessonFragment.class
                .getDeclaredMethod("tryDecryptSecretsWithKey", String.class));
    }
}
