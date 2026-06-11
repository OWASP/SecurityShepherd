package org.owasp.mobileshepherd.ui.challenges.crypto;

import androidx.lifecycle.ViewModel;

import org.owasp.mobileshepherd.utils.FlagValidator;

public class InsufficientCryptoChallengeModel extends ViewModel {

    public boolean validateFlag(String flag) {
        return FlagValidator.validateFlag(FlagValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE, flag);
    }
}
