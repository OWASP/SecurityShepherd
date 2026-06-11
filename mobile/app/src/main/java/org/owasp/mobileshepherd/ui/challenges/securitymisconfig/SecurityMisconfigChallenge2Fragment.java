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
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class SecurityMisconfigChallenge2Fragment extends Fragment {

    private static final String TAG = "BackupChallenge";
    private static final String PREFS_NAME = "BackupChallengePrefs";
    
    private String currentFlag = "";
    private TextInputEditText flagInput;
    private TextView resultText;
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_security_misconfig_challenge2, container, false);

        progressTracker = new ProgressTracker(requireContext());

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showVulnerabilityInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }


        flagInput = root.findViewById(R.id.flag_input);
        resultText = root.findViewById(R.id.result_text);
        
        Button validateButton = root.findViewById(R.id.validate_button);
        validateButton.setOnClickListener(v -> validateFlag());

        // Fetch server flag, then store it in SharedPreferences (will be backed up)
        FlagProvider.getFlag(requireContext(), FlagValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2, flagValue -> {
            currentFlag = flagValue;
            storeSecretData();
        });

        return root;
    }

    private void toggleFabExpansion(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2) {
        fabExpanded = !fabExpanded;
        if (fabExpanded) {
            if (fab1 != null) fab1.setVisibility(View.VISIBLE);
            if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            collapseFab(mainFab, fab1, fab2);
        }
    }

    private void collapseFab(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2) {
        fabExpanded = false;
        if (fab1 != null) fab1.setVisibility(View.GONE);
        if (fab2 != null) fab2.setVisibility(View.GONE);
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
                .setTitle("Security Misconfiguration - Backup\nOWASP M10: Extraneous Functionality")
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

        // Store the flag - this will be in the backup!
        editor.putString("secret_flag", currentFlag);
        editor.putString("flag_hint", "Extract me with adb backup!");

        editor.apply();

        Log.d(TAG, "Secret data stored in SharedPreferences");
        Log.d(TAG, "Backup is enabled for this app - data can be extracted!");
        Log.d(TAG, "Hint: adb backup -f backup.ab -noapk org.owasp.mobileshepherd");
    }

    private void validateFlag() {
        String userInput = flagInput.getText().toString().trim();

        if (userInput.isEmpty()) {
            resultText.setVisibility(View.VISIBLE);
            resultText.setText("Please enter a flag");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.card_warning_text));
            resultText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_warning_bg));
            return;
        }

        FlagValidator.validateFlag(requireContext(), FlagValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2,
                userInput, isValid -> {
            resultText.setVisibility(View.VISIBLE);
            if (isValid) {
                progressTracker.markCompleted(FlagValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2);
                int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2);
                String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";

                resultText.setText("\u2713 SUCCESS!\n\nYou successfully extracted the backup and found the flag in SharedPreferences!");
                resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_text));
                resultText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success_bg));

                new AlertDialog.Builder(requireContext())
                    .setTitle("\uD83C\uDF89 Success!")
                    .setMessage("Congratulations! You extracted data from the app backup.\n\nFlag: " + userInput + completionText)
                    .setPositiveButton("OK", null)
                    .show();
            } else {
                resultText.setText("\u2717 INCORRECT\n\nThat's not the right flag. Try extracting the app backup using ADB.");
                resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_text));
                resultText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_bg));
                Log.d(TAG, "Incorrect flag attempt: " + userInput);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
    }
}
