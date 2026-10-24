package org.owasp.mobileshepherd.ui.challenges.poorauth;

import androidx.lifecycle.ViewModel;

import org.owasp.mobileshepherd.utils.KeyValidator;

public class PoorAuthChallengeModel extends ViewModel {

    public boolean validateKey(String enteredKey) {
        return KeyValidator.validateKey(KeyValidator.Module.POOR_AUTH_CHALLENGE, enteredKey);
    }
}
