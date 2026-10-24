package org.owasp.mobileshepherd.ui.challenges.poorauth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentPoorAuthChallengeBinding;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPoorAuthChallengeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.POOR_AUTH_CHALLENGE));
        }

        binding.forgotPasswordButton.setOnClickListener(v -> openResetSection());
        binding.loginButton.setOnClickListener(v -> handleLogin());

        return root;
    }

    private void openResetSection() {
        binding.loginSection.setVisibility(View.GONE);
        binding.resetSection.setVisibility(View.VISIBLE);

        binding.requestTokenButton.setOnClickListener(v -> handleRequestToken());
        binding.resetButton.setOnClickListener(v -> handleVerifyToken());
        binding.cancelButton.setOnClickListener(v -> resetSectionToInitialState());
    }

    private void resetSectionToInitialState() {
        binding.headerCard.setVisibility(View.VISIBLE);
        binding.successSection.setVisibility(View.GONE);
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

        Toast.makeText(getContext(), R.string.poor_auth_token_generated, Toast.LENGTH_LONG).show();
    }

    private void handleVerifyToken() {
        String enteredToken = binding.tokenInput.getText().toString().trim();
        if (enteredToken.isEmpty()) {
            Toast.makeText(getContext(), R.string.poor_auth_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (expectedResetToken != null && enteredToken.equalsIgnoreCase(expectedResetToken)) {
            binding.headerCard.setVisibility(View.GONE);
            binding.loginSection.setVisibility(View.GONE);
            binding.resetSection.setVisibility(View.GONE);
            binding.successSection.setVisibility(View.VISIBLE);
            binding.keyText.setText(R.string.poor_auth_fetching_key);

            KeyProvider.getKey(requireContext(), KeyValidator.Module.POOR_AUTH_CHALLENGE, key -> {
                if (!isAdded()) return;
                if (key == null || key.isEmpty()) {
                    showKeyError(R.string.poor_auth_key_unavailable);
                    return;
                }

                KeyValidator.validateKey(requireContext(), KeyValidator.Module.POOR_AUTH_CHALLENGE, key, isValid -> {
                    if (!isAdded()) return;
                    if (isValid) {
                        progressTracker.markCompleted(KeyValidator.Module.POOR_AUTH_CHALLENGE);
                        int completionCount = progressTracker.getCompletionCount(KeyValidator.Module.POOR_AUTH_CHALLENGE);
                        String completionText = completionCount > 1
                                ? getString(R.string.poor_auth_completed_times, completionCount) : "";

                        binding.keyText.setText(getString(R.string.poor_auth_key_result, key, completionText));

                        new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.poor_auth_challenge_success_title)
                            .setMessage(getString(R.string.poor_auth_challenge_success_message, key, completionText))
                            .setPositiveButton(R.string.csi_ok, null)
                            .show();
                    } else {
                        showKeyError(R.string.poor_auth_key_validation_failed);
                    }
                });
            });
        } else {
            Toast.makeText(getContext(), R.string.poor_auth_invalid_token, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Invalid reset token attempt for user=" + USERNAME);
            binding.tokenInput.setText("");
        }
    }

    private void showKeyError(int messageRes) {
        resetSectionToInitialState();
        openResetSection();
        Toast.makeText(getContext(), messageRes, Toast.LENGTH_LONG).show();
    }

    private void handleLogin() {
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), R.string.poor_auth_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Login attempt - Username: " + username);
        Toast.makeText(getContext(), R.string.poor_auth_account_locked, Toast.LENGTH_LONG).show();
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
        binding = null;
    }
}
