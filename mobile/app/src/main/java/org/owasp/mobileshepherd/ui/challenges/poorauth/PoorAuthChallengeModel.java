package org.owasp.mobileshepherd.ui.challenges.poorauth;

import androidx.lifecycle.ViewModel;

import org.owasp.mobileshepherd.utils.FlagValidator;

public class PoorAuthChallengeModel extends ViewModel {

    public boolean validateFlag(String enteredFlag) {
        return FlagValidator.validateFlag(FlagValidator.Module.POOR_AUTH_CHALLENGE, enteredFlag);
    }
}
