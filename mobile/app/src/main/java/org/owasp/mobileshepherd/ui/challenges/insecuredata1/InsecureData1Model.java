package org.owasp.mobileshepherd.ui.challenges.insecuredata1;

import androidx.lifecycle.ViewModel;

import org.owasp.mobileshepherd.utils.KeyValidator;

public class InsecureData1Model extends ViewModel {

    public boolean validateKey(String key) {
        return KeyValidator.validateKey(KeyValidator.Module.IDS_CHALLENGE_1, key);
    }
}
