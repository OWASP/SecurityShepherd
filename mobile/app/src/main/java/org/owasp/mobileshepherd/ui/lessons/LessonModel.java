package org.owasp.mobileshepherd.ui.lessons;

import android.os.Build;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LessonModel extends ViewModel {

    private final MutableLiveData<String> modelText;
    private final MutableLiveData<String> serialText;
    private final MutableLiveData<String> manufacturerText;
    private final MutableLiveData<String> brandText;
    private final MutableLiveData<String> sdkText;

    public LessonModel() {
        serialText = new MutableLiveData<>();
        modelText = new MutableLiveData<>();
        manufacturerText = new MutableLiveData<>();
        brandText = new MutableLiveData<>();
        sdkText = new MutableLiveData<>();

        // Set device info
        serialText.setValue(Build.SERIAL);
        modelText.setValue(Build.MODEL);
        manufacturerText.setValue(Build.MANUFACTURER);
        brandText.setValue(Build.BRAND);
        sdkText.setValue(String.valueOf(Build.VERSION.SDK_INT));
    }

    public LiveData<String> getSerialText() {
        return serialText;
    }

    public LiveData<String> getModelText() {
        return modelText;
    }

    public LiveData<String> getManufacturerText() {
        return manufacturerText;
    }

    public LiveData<String> getBrandText() {
        return brandText;
    }

    public LiveData<String> getSDKText() {
        return sdkText;
    }
}