package org.owasp.mobileshepherd;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.owasp.mobileshepherd.utils.KeyValidator;

/** Insecure Data Storage keys are validated server-side only, so the offline validator must reject everything. */
public class InsecureDataKeyValidationTest {

    @Test
    public void lessonHasNoOfflineHash() {
        assertFalse(
                KeyValidator.validateKey(
                        KeyValidator.Module.IDS_LESSON, "Copper_Bridge_Stands_In_Rain"));
    }

    @Test
    public void challengeHasNoOfflineHash() {
        assertFalse(
                KeyValidator.validateKey(
                        KeyValidator.Module.IDS_CHALLENGE_1, "Tangled_Rope_Skips_A_Stone"));
    }

    @Test
    public void emptyAndNullKeysRejected() {
        assertFalse(KeyValidator.validateKey(KeyValidator.Module.IDS_LESSON, ""));
        assertFalse(KeyValidator.validateKey(KeyValidator.Module.IDS_CHALLENGE_1, null));
    }
}