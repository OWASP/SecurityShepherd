package org.owasp.mobileshepherd.ui.challenges.poorauth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentPoorAuthChallengeBinding;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PoorAuthChallengeFragment extends Fragment {

    private FragmentPoorAuthChallengeBinding binding;
    private static String expectedResetToken;
    private static final String TAG = "PoorAuthChallenge";
    private static final String USERNAME = "Jack";
    // Hardcoded salt baked into the APK - visible to anyone who decompiles the app.
    private static final String RESET_TOKEN_SALT = "sh3ph3rd-reset-2024";
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPoorAuthChallengeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.POOR_AUTH_CHALLENGE));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showVulnerabilityInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }


        // Setup forgot password button
        binding.forgotPasswordButton.setOnClickListener(v -> openResetSection());

        // Setup login button
        binding.loginButton.setOnClickListener(v -> handleLogin());

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
        
        introText.setText(R.string.poor_auth_intro);
        // vulnerabilitiesText.setText(R.string.poor_auth_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Poor Authentication Challenge\nOWASP M3: Insecure Authentication/Authorization")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void openResetSection() {
        binding.loginSection.setVisibility(View.GONE);
        binding.resetSection.setVisibility(View.VISIBLE);

        binding.requestTokenButton.setOnClickListener(v -> handleRequestToken());
        binding.resetButton.setOnClickListener(v -> handleVerifyToken());
        binding.cancelButton.setOnClickListener(v -> resetSectionToInitialState());
    }

    private void resetSectionToInitialState() {
        binding.loginSection.setVisibility(View.VISIBLE);
        binding.resetSection.setVisibility(View.GONE);
        binding.requestTokenButton.setVisibility(View.VISIBLE);
        binding.resetStatusText.setVisibility(View.GONE);
        binding.tokenInputLayout.setVisibility(View.GONE);
        binding.resetButton.setVisibility(View.GONE);
        binding.tokenInput.setText("");
    }

    private void handleRequestToken() {
        String today = getTodayDateString();
        expectedResetToken = generateResetToken(USERNAME, today);

        // Looks like ordinary audit logging of a password-reset request.
        // VULNERABLE (CWE-341/CWE-330): the token itself is never logged, but it
        // is fully derived from public/observable inputs (username + today's date)
        // plus a value hardcoded in the app binary.
        Log.d(TAG, "Password reset requested for user=" + USERNAME + " date=" + today);

        binding.requestTokenButton.setVisibility(View.GONE);
        binding.resetStatusText.setText(getString(R.string.poor_auth_reset_confirmation));
        binding.resetStatusText.setVisibility(View.VISIBLE);
        binding.tokenInputLayout.setVisibility(View.VISIBLE);
        binding.resetButton.setVisibility(View.VISIBLE);

        Toast.makeText(getContext(), "A password reset token has been generated for this account.", Toast.LENGTH_LONG).show();
    }

    private void handleVerifyToken() {
        String enteredToken = binding.tokenInput.getText().toString().trim();
        if (enteredToken.isEmpty()) {
            Toast.makeText(getContext(), "Empty Fields Detected.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (expectedResetToken != null && enteredToken.equalsIgnoreCase(expectedResetToken)) {
            binding.loginSection.setVisibility(View.GONE);
            binding.resetSection.setVisibility(View.GONE);
            binding.successSection.setVisibility(View.VISIBLE);
            binding.flagText.setText("Fetching your flag...");

            Toast.makeText(getContext(), "Reset token verified! Access granted.", Toast.LENGTH_LONG).show();

            FlagProvider.getFlag(requireContext(), FlagValidator.Module.POOR_AUTH_CHALLENGE, flag -> {
                binding.flagText.setText("Congratulations! Here's your flag:\n\n" + flag);

                FlagValidator.validateFlag(requireContext(), FlagValidator.Module.POOR_AUTH_CHALLENGE, flag, isValid -> {
                    if (isValid) {
                        progressTracker.markCompleted(FlagValidator.Module.POOR_AUTH_CHALLENGE);
                        int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.POOR_AUTH_CHALLENGE);
                        String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";

                        new AlertDialog.Builder(requireContext())
                            .setTitle("🎉 Success!")
                            .setMessage("Congratulations! You exploited weak authentication.\n\nFlag: " + flag + completionText)
                            .setPositiveButton("OK", null)
                            .show();
                    }
                });
            });
        } else {
            Toast.makeText(getContext(), "Invalid reset token.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Invalid reset token attempt for user=" + USERNAME);
            binding.tokenInput.setText("");
        }
    }

    private void handleLogin() {
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Empty Fields Detected.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Login attempt - Username: " + username);
        Toast.makeText(getContext(), "Your account has been locked! Use password reset.", Toast.LENGTH_LONG).show();
    }

    private String generateResetToken(String username, String dateStr) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((username + ":" + dateStr + ":" + RESET_TOKEN_SALT).getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 6).toUpperCase(Locale.US);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTodayDateString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
        binding = null;
    }
}
