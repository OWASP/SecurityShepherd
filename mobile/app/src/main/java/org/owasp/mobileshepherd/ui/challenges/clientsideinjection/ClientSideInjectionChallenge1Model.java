package org.owasp.mobileshepherd.ui.challenges.clientsideinjection;

import androidx.lifecycle.ViewModel;

import org.owasp.mobileshepherd.utils.KeyValidator;

public class ClientSideInjectionChallenge1Model extends ViewModel {

    public boolean validateKey(String key) {
        return KeyValidator.validateKey(
            KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1,
            key
        );
    }
}
