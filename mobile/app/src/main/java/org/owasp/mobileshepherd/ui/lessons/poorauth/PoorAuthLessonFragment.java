package org.owasp.mobileshepherd.ui.lessons.poorauth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentPoorAuthLessonBinding;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PoorAuthLessonFragment extends Fragment {

    private FragmentPoorAuthLessonBinding binding;
    private static final String TAG = "PoorAuthLesson";
    // SHA-256 hash of "654321"
    private static final String HARDCODED_PIN_HASH = "481f6cc0511143ccdd7e2d1b1b94faf0a700a8b49cd13922a70b5ae28acaa8c5";

    private int attemptCount = 0;
    private ProgressTracker progressTracker;
    private String currentKey = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPoorAuthLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());
        KeyProvider.getKey(
                requireContext(),
                KeyValidator.Module.POOR_AUTH_LESSON,
                keyValue -> currentKey = keyValue);

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.POOR_AUTH_LESSON));
        }

        // Log the hardcoded PIN hash (intentionally insecure for demonstration)
        Log.d(TAG, "Initializing authentication system...");
        Log.d(TAG, "PIN hash configured: " + HARDCODED_PIN_HASH);
        Log.d(TAG, "System ready. PIN verification enabled.");

        binding.verifyButton.setOnClickListener(v -> verifyPin());

        return root;
    }

    private void verifyPin() {
        String enteredPin = binding.pinInput.getText().toString().trim();
        attemptCount++;

        if (enteredPin.isEmpty()) {
            Toast.makeText(getContext(), R.string.poor_auth_enter_pin, Toast.LENGTH_SHORT).show();
            return;
        }

        String enteredPinHash = hashPin(enteredPin);

        Log.d(TAG, "PIN verification attempt #" + attemptCount);
        Log.d(TAG, "Entered PIN hash: " + enteredPinHash);
        Log.d(TAG, "Expected PIN hash: " + HARDCODED_PIN_HASH);

        if (enteredPinHash != null && enteredPinHash.equals(HARDCODED_PIN_HASH)) {
            if (currentKey == null || currentKey.isEmpty()) {
                Toast.makeText(getContext(), R.string.poor_auth_key_unavailable, Toast.LENGTH_LONG).show();
                return;
            }

            binding.headerCard.setVisibility(View.GONE);
            binding.verifyButton.setEnabled(false);
            binding.pinInput.setEnabled(false);

            KeyValidator.validateKey(requireContext(), KeyValidator.Module.POOR_AUTH_LESSON,
                    currentKey, correct -> {
                if (!isAdded()) return;
                if (correct) {
                    progressTracker.markCompleted(KeyValidator.Module.POOR_AUTH_LESSON);
                    int completionCount = progressTracker.getCompletionCount(KeyValidator.Module.POOR_AUTH_LESSON);
                    String completionText = completionCount > 1
                            ? getString(R.string.poor_auth_completed_times, completionCount) : "";
                    binding.demoCard.setCardBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.success_bg));
                    binding.keyText.setText(getString(R.string.poor_auth_key_result, currentKey, completionText));
                } else {
                    binding.demoCard.setCardBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.error_bg));
                    binding.keyText.setText(R.string.poor_auth_key_validation_failed);
                    binding.verifyButton.setEnabled(true);
                    binding.pinInput.setEnabled(true);
                }
                binding.keyText.setVisibility(View.VISIBLE);
            });
        } else {
            binding.demoCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_bg));
            binding.keyText.setVisibility(View.GONE);

            Toast.makeText(getContext(), R.string.poor_auth_incorrect_pin, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Authentication failed. Invalid PIN provided.");

            binding.pinInput.setText("");
        }
    }

    private String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(pin.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing PIN", e);
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
