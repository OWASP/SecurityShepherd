package org.owasp.mobileshepherd.ui.challenges.clientsideinjection;

import androidx.lifecycle.ViewModel;

import org.owasp.mobileshepherd.utils.FlagValidator;

public class ClientSideInjectionChallenge2Model extends ViewModel {
    
    public boolean validateFlag(String flag) {
        return FlagValidator.validateFlag(
            FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_2, 
            flag
        );
    }
}
