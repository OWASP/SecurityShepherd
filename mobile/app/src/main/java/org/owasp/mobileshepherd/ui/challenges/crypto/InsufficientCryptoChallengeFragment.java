package org.owasp.mobileshepherd.ui.challenges.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentInsufficientCryptoChallengeBinding;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.nio.charset.StandardCharsets;

public class InsufficientCryptoChallengeFragment extends Fragment {

    private FragmentInsufficientCryptoChallengeBinding binding;
    private String currentKey = "";
    private static final String TAG = "RecoveryVault";
    private ProgressTracker progressTracker;

    // Vulnerability: hardcoded single-byte XOR key used to "protect" stored data
    private static final byte XOR_KEY = 0x42;
    private static final String PREFS_NAME = "recovery_vault";
    private static final String PREFS_KEY = "vault_data";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentInsufficientCryptoChallengeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE));
        }

        KeyProvider.getKey(requireContext(), KeyValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE,
                keyValue -> {
                    if (!isAdded()) return;
                    currentKey = keyValue;
                });

        binding.saveButton.setOnClickListener(v -> saveVault());
        binding.validateButton.setOnClickListener(v -> validateKey());

        return root;
    }

    private void saveVault() {
        String key1 = binding.key1Input.getText().toString().trim();
        String key2 = binding.key2Input.getText().toString().trim();

        if (key1.isEmpty() || key2.isEmpty()) {
            Toast.makeText(getContext(), R.string.crypto_vault_keys_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentKey.isEmpty()) {
            Toast.makeText(getContext(), R.string.crypto_vault_loading, Toast.LENGTH_SHORT).show();
            return;
        }

        // Build JSON -- key is hidden as "session_token" among legitimate-looking fields
        String json = "{"
                + "\"version\":1,"
                + "\"recovery_key_1\":\"" + key1 + "\","
                + "\"recovery_key_2\":\"" + key2 + "\","
                + "\"account_id\":\"usr_" + Integer.toHexString(key1.hashCode() & 0xFFFF) + "\","
                + "\"session_token\":\"" + currentKey + "\","
                + "\"backup_timestamp\":" + System.currentTimeMillis()
                + "}";

        // XOR encode and Base64-wrap before storing
        byte[] plainBytes = json.getBytes(StandardCharsets.UTF_8);
        byte[] encoded = new byte[plainBytes.length];
        for (int i = 0; i < plainBytes.length; i++) {
            encoded[i] = (byte) (plainBytes[i] ^ XOR_KEY);
        }
        String stored = Base64.encodeToString(encoded, Base64.NO_WRAP);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREFS_KEY, stored).apply();

        Log.d(TAG, "Vault saved. Keys: " + key1 + ", " + key2);

        binding.savedConfirmation.setVisibility(View.VISIBLE);
        binding.savedConfirmation.setText(getString(R.string.crypto_vault_saved, PREFS_NAME, PREFS_KEY));
        binding.key1Input.setEnabled(false);
        binding.key2Input.setEnabled(false);
        binding.saveButton.setEnabled(false);
    }

    private void validateKey() {
        String enteredKey = binding.keyInput.getText().toString().trim();

        if (enteredKey.isEmpty()) {
            Toast.makeText(getContext(), R.string.crypto_key_required, Toast.LENGTH_SHORT).show();
            return;
        }

        KeyValidator.validateKey(requireContext(), KeyValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE,
                enteredKey, isValid -> {
            if (!isAdded()) return;
            if (isValid) {
                progressTracker.markCompleted(KeyValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE);
                int completionCount = progressTracker.getCompletionCount(KeyValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE);
                String completionText = completionCount > 1
                        ? getString(R.string.crypto_completed_times, completionCount) : "";

                binding.headerCard.setVisibility(View.GONE);
                binding.resultCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.success_bg));
                binding.validateButton.setEnabled(false);
                binding.keyInput.setEnabled(false);

                new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.crypto_success_title)
                    .setMessage(getString(R.string.crypto_success_message, enteredKey, completionText))
                    .setPositiveButton(R.string.csi_ok, null)
                    .show();
            } else {
                binding.resultCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.error_bg));
                binding.resultText.setText(R.string.crypto_incorrect_key);
                binding.resultText.setVisibility(View.VISIBLE);
                binding.keyInput.setText("");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
