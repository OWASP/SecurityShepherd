package org.owasp.mobileshepherd.ui.challenges.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentInsufficientCryptoChallengeBinding;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.nio.charset.StandardCharsets;

public class InsufficientCryptoChallengeFragment extends Fragment {

    private FragmentInsufficientCryptoChallengeBinding binding;
    private String currentFlag = "";
    private static final String TAG = "RecoveryVault";
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;

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
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showHints();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }

        FlagProvider.getFlag(requireContext(), FlagValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE,
                flagValue -> currentFlag = flagValue);

        binding.saveButton.setOnClickListener(v -> saveVault());
        binding.validateButton.setOnClickListener(v -> validateFlag());

        return root;
    }

    private void saveVault() {
        String key1 = binding.key1Input.getText().toString().trim();
        String key2 = binding.key2Input.getText().toString().trim();

        if (key1.isEmpty() || key2.isEmpty()) {
            Toast.makeText(getContext(), "Enter both recovery keys before saving", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentFlag.isEmpty()) {
            Toast.makeText(getContext(), "Loading vault data, please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build JSON -- flag is hidden as "session_token" among legitimate-looking fields
        String json = "{"
                + "\"version\":1,"
                + "\"recovery_key_1\":\"" + key1 + "\","
                + "\"recovery_key_2\":\"" + key2 + "\","
                + "\"account_id\":\"usr_" + Integer.toHexString(key1.hashCode() & 0xFFFF) + "\","
                + "\"session_token\":\"" + currentFlag + "\","
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
        binding.savedConfirmation.setText(
                "\u2705 Recovery vault saved to SharedPreferences.\n\n"
                + "File: shared_prefs/" + PREFS_NAME + ".xml\n"
                + "Key:  " + PREFS_KEY);
        binding.key1Input.setEnabled(false);
        binding.key2Input.setEnabled(false);
        binding.saveButton.setEnabled(false);
    }

    private void validateFlag() {
        String enteredFlag = binding.flagInput.getText().toString().trim();

        if (enteredFlag.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a flag", Toast.LENGTH_SHORT).show();
            return;
        }

        FlagValidator.validateFlag(requireContext(), FlagValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE,
                enteredFlag, isValid -> {
            if (isValid) {
                progressTracker.markCompleted(FlagValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE);

                binding.resultCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.success_bg));
                binding.resultText.setText("\u2713 Correct!\n\nYou recovered the flag from insecurely stored data.");
                binding.resultText.setVisibility(View.VISIBLE);
                binding.validateButton.setEnabled(false);
                binding.flagInput.setEnabled(false);

                new AlertDialog.Builder(requireContext())
                    .setTitle("\uD83C\uDF89 Challenge Complete!")
                    .setMessage("You exploited insecure local storage:\n\n"
                            + "\u2022 Data was XOR-encoded with a hardcoded key (0x42)\n"
                            + "\u2022 The key is visible in the decompiled source\n"
                            + "\u2022 SharedPreferences are readable via ADB on debug builds\n\n"
                            + "Real-world fix: Use Android Keystore + AES/GCM with a per-install key.")
                    .setPositiveButton("OK", null)
                    .show();
            } else {
                binding.resultCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.error_bg));
                binding.resultText.setText("\u2717 Incorrect flag. Pull the vault data from SharedPreferences and decode it.");
                binding.resultText.setVisibility(View.VISIBLE);
                binding.flagInput.setText("");
            }
        });
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

    private void showHints() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);

        TextView introText = dialogView.findViewById(R.id.intro_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);

        introText.setText("The app stores your recovery vault locally using a weak encoding scheme. "
                + "Your goal is to recover the hidden session token from that stored data.");

        hintsSection.setVisibility(View.VISIBLE);
        hintsText.setText(
                "\u2022 Save the vault first, then inspect the device storage\n"
                + "\u2022 ADB: adb shell run-as org.owasp.mobileshepherd cat shared_prefs/recovery_vault.xml\n"
                + "\u2022 The stored value is Base64-encoded -- decode it first\n"
                + "\u2022 Then look at the source code to find how the bytes were scrambled\n"
                + "\u2022 Decompile with: jadx-gui app-debug.apk");
        additionalSection.setVisibility(View.GONE);

        new AlertDialog.Builder(requireContext())
                .setTitle("Hints -- OWASP M6: Insufficient Cryptography")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
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