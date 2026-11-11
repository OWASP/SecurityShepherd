package org.owasp.mobileshepherd;

import android.app.Application;

public class ShepherdApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.apply(this);
    }
}
