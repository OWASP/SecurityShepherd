package org.owasp.mobileshepherd;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

public final class ThemeHelper {

    public static final String PREF_KEY = "theme_preference";

    private ThemeHelper() {}

    public static void apply(Context context) {
        String value = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_KEY, "system");
        apply(value);
    }

    public static void apply(String value) {
        AppCompatDelegate.setDefaultNightMode(toNightMode(value));
    }

    static int toNightMode(String value) {
        if ("light".equals(value)) {
            return AppCompatDelegate.MODE_NIGHT_NO;
        }
        if ("dark".equals(value)) {
            return AppCompatDelegate.MODE_NIGHT_YES;
        }
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }
}
