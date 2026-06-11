package org.owasp.mobileshepherd.ui.challenges.reverseengineering;

import android.util.Base64;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.owasp.mobileshepherd.utils.FlagValidator;

public class ReverseEngineering1Model extends ViewModel {

    // Flag stored for static analysis discovery
    private static final String ENCODED_SECRET = "U2hhZG93X0tleV9IYW5nc19CeV9UaHJlYWQ=";

    private final MutableLiveData<String> mText;

    public ReverseEngineering1Model() {
        mText = new MutableLiveData<>();
        mText.setValue("Challenge 1");
    }

    public LiveData<String> getText() {
        return mText;
    }
    
    public boolean validateFlag(String input) {
        return FlagValidator.validateFlag(FlagValidator.Module.RE_CHALLENGE_1, input);
    }
}