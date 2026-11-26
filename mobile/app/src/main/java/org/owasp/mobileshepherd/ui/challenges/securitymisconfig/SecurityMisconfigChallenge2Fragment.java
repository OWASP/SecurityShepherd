package org.owasp.mobileshepherd.ui.challenges.securitymisconfig;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class SecurityMisconfigChallenge2Fragment extends Fragment {

    private static final String TAG = "BackupChallenge";
    private static final String PREFS_NAME = "BackupChallengePrefs";

    private String currentKey = "";
    private TextInputEditText keyInput;
    private TextView resultText;
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_security_misconfig_challenge2, container, false);

        progressTracker = new ProgressTracker(requireContext());

        // Setup expandable FAB with command reference
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showVulnerabilityInfo();
                collapseFab(fab, fabCommandRef);
            });
        }


        keyInput = root.findViewById(R.id.key_input);
        resultText = root.findViewById(R.id.result_text);

        Button validateButton = root.findViewById(R.id.validate_button);
        validateButton.setOnClickListener(v -> validateKey());

        // Fetch server key, then store it in SharedPreferences (will be backed up)
        KeyProvider.getKey(requireContext(), KeyValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2, keyValue -> {
            currentKey = keyValue;
            storeSecretData();
        });

        return root;
    }

    private void toggleFabExpansion(FloatingActionButton mainFab, FloatingActionButton fab1) {
        fabExpanded = !fabExpanded;
        if (fabExpanded) {
            if (fab1 != null) fab1.setVisibility(View.VISIBLE);
            if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            collapseFab(mainFab, fab1);
        }
    }

    private void collapseFab(FloatingActionButton mainFab, FloatingActionButton fab1) {
        fabExpanded = false;
        if (fab1 != null) fab1.setVisibility(View.GONE);
        if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_help);
    }


    private void showVulnerabilityInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.security_misconfig_intro);
        // vulnerabilitiesText.setText(R.string.security_misconfig_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Security Misconfiguration - Backup\nOWASP M8: Security Misconfiguration")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
    
    private void storeSecretData() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Store some normal preferences
        editor.putString("username", "admin");
        editor.putString("theme", "dark");
        editor.putString("language", "en");

        // Store the key - this will be in the backup!
        editor.putString("secret_key", currentKey);
        editor.putString("key_hint", "Extract me with adb backup!");

        editor.apply();

        Log.d(TAG, "Secret data stored in SharedPreferences");
        Log.d(TAG, "Backup is enabled for this app - data can be extracted!");
        Log.d(TAG, "Secret data stored in backup-enabled SharedPreferences.");
    }

    private void validateKey() {
        String userInput = keyInput.getText().toString().trim();

        if (userInput.isEmpty()) {
            resultText.setVisibility(View.VISIBLE);
            resultText.setText("Please enter a key");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.card_warning_text));
            resultText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_warning_bg));
            return;
        }

        KeyValidator.validateKey(requireContext(), KeyValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2,
                userInput, isValid -> {
            resultText.setVisibility(View.VISIBLE);
            if (isValid) {
                progressTracker.markCompleted(KeyValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2);
                resultText.setText("Correct!");
                resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_text));
                resultText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success_bg));

                android.widget.Toast.makeText(requireContext(), "Challenge complete!",
                        android.widget.Toast.LENGTH_LONG).show();
            } else {
                resultText.setText("Incorrect. Try extracting the app backup using ADB.");
                resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_text));
                resultText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_bg));
                Log.d(TAG, "Incorrect key attempt: " + userInput);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        collapseFab(fab, fabCommandRef);
    }
}
