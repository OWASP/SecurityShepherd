package org.owasp.mobileshepherd;

import org.owasp.mobileshepherd.ui.challenges.reverseengineering.ReverseEngineering1Model;
import org.junit.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

/**
 * Unit tests for ReverseEngineering1Model
 * The model stores an encoded secret (Base64) that can be discovered via reverse engineering.
 */
public class ReverseEngineeringChallenge1Test {

    @Test
    public void challenge1_EncodedSecretIsAccessibleViaReflection() throws Exception {
        Field secretField = ReverseEngineering1Model.class.getDeclaredField("ENCODED_SECRET");
        secretField.setAccessible(true);
        String encodedSecret = (String) secretField.get(null);
        assertNotNull("ENCODED_SECRET should exist", encodedSecret);
        assertFalse("ENCODED_SECRET should not be empty", encodedSecret.isEmpty());
    }

    @Test
    public void challenge1_ValidateFlagMethodExists() throws Exception {
        Method validateMethod = ReverseEngineering1Model.class.getDeclaredMethod("validateFlag", String.class);
        assertNotNull("validateFlag method should exist", validateMethod);
    }

    @Test
    public void challenge1_EncodedSecretIsStaticFinal() throws Exception {
        Field secretField = ReverseEngineering1Model.class.getDeclaredField("ENCODED_SECRET");
        assertTrue("ENCODED_SECRET should be static", Modifier.isStatic(secretField.getModifiers()));
        assertTrue("ENCODED_SECRET should be final", Modifier.isFinal(secretField.getModifiers()));
    }

    @Test
    public void challenge1_EncodedSecretIsValidBase64() throws Exception {
        Field secretField = ReverseEngineering1Model.class.getDeclaredField("ENCODED_SECRET");
        secretField.setAccessible(true);
        String encodedSecret = (String) secretField.get(null);
        // Valid Base64 characters: A-Z, a-z, 0-9, +, /, =
        assertTrue("ENCODED_SECRET should be valid Base64",
                encodedSecret.matches("^[A-Za-z0-9+/]+=*$"));
    }

    @Test
    public void challenge1_DiscoverEncodedSecretThroughFieldEnumeration() {
        try {
            Field[] fields = ReverseEngineering1Model.class.getDeclaredFields();
            boolean foundEncodedField = false;

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value instanceof String) {
                        String str = (String) value;
                        // Encoded secrets end with = padding or match Base64 pattern
                        if (str.endsWith("=") || str.matches("^[A-Za-z0-9+/]+=*$")) {
                            foundEncodedField = true;
                            break;
                        }
                    }
                }
            }

            assertTrue("Should discover an encoded field through field enumeration", foundEncodedField);
        } catch (Exception e) {
            fail("Should be able to enumerate fields: " + e.getMessage());
        }
    }
}
