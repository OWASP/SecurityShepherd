package org.owasp.mobileshepherd;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import org.owasp.mobileshepherd.utils.ProgressTracker;

/**
 * This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.<br>
 *
 * <p>The Security Shepherd project is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public License for more details.<br>
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Sean Duggan
 */
public class Preferences extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new PrefsFragment())
                .commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static class PrefsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Load the preferences from an XML resource
            setPreferencesFromResource(R.xml.preferences, rootKey);

            // Set up theme preference listener
            ListPreference themePreference = findPreference("theme_preference");
            if (themePreference != null) {
                themePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String themeValue = (String) newValue;
                        applyTheme(themeValue);
                        return true;
                    }
                });
            }

            // Set up submit issue preference listener
            Preference submitIssuePreference = findPreference("submit_issue_preference");
            if (submitIssuePreference != null) {
                submitIssuePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        String issuesUrl = "https://github.com/OWASP/SecurityShepherd/issues";
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(issuesUrl));
                        startActivity(browserIntent);
                        return true;
                    }
                });
            }

            // Set up reset progress preference listener
            Preference resetProgressPreference = findPreference("reset_progress_preference");
            if (resetProgressPreference != null) {
                resetProgressPreference.setOnPreferenceClickListener(preference -> {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Reset All Progress")
                            .setMessage("This will mark all lessons and challenges as incomplete. This cannot be undone.\n\nAre you sure?")
                            .setPositiveButton("Reset", (dialog, which) -> {
                                ProgressTracker tracker = new ProgressTracker(requireContext());
                                tracker.resetProgress();
                                ProgressTracker.CompletionChangeListener listener =
                                        ProgressTracker.getGlobalCompletionListener();
                                if (listener != null) {
                                    listener.onCompletionChanged();
                                }
                                android.widget.Toast.makeText(
                                        requireContext(),
                                        "All progress has been reset",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                    return true;
                });
            }
        }

        private void applyTheme(String themeValue) {
            switch (themeValue) {
                case "light":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "dark":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case "system":
                default:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
        }
    }
}