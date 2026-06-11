package org.owasp.mobileshepherd.ui.challenges.insecuredata1;

import androidx.lifecycle.ViewModel;

import org.owasp.mobileshepherd.utils.FlagValidator;

public class InsecureData1Model extends ViewModel {

    public boolean validateFlag(String flag) {
        return FlagValidator.validateFlag(FlagValidator.Module.IDS_CHALLENGE_1, flag);
    }
}
