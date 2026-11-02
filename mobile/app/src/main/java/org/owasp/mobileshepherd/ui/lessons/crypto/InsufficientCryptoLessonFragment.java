package org.owasp.mobileshepherd.ui.lessons.crypto;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentInsufficientCryptoLessonBinding;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class InsufficientCryptoLessonFragment extends Fragment {

    private FragmentInsufficientCryptoLessonBinding binding;
    private static final String TAG = "InsufficientCrypto";

    // Each secret is "encrypted" with its own short, guessable word. Reusing
    // a single key across secrets would itself be the vulnerability, so the
    // lesson instead shows why ANY weak, dictionary-guessable DES key is unsafe.
    private static final String[] SECRET_KEYS = {"sheep", "shepherd", "grass", "wool"};

    private String currentKey = "";
    private boolean completionSubmitted = false;
    private ProgressTracker progressTracker;
    private List<EncryptedSecret> encryptedSecrets;
    private int userSecretCount = 0;

    private static class EncryptedSecret {
        final String label;
        final String hint;
        final String encryptedValue;
        String decryptedValue;

        EncryptedSecret(String label, String hint, String encryptedValue) {
            this.label = label;
            this.hint = hint;
            this.encryptedValue = encryptedValue;
            this.decryptedValue = null;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInsufficientCryptoLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());
        encryptedSecrets = new ArrayList<>();

        KeyProvider.getKey(requireContext(), KeyValidator.Module.INSUFFICIENT_CRYPTO_LESSON,
                keyValue -> {
                    if (!isAdded()) return;
                    currentKey = keyValue;
                    initializeSecrets();
                    displaySecrets();
                });

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v ->
                    ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.INSUFFICIENT_CRYPTO_LESSON));
        }

        binding.encryptButton.setOnClickListener(v -> encryptText());
        binding.decryptButton.setOnClickListener(v -> decryptText());

        return root;
    }

    /**
     * Seed the encrypted secrets list. The first three are decoys; the last
     * ("wool") encrypts the real lesson key.
     */
    private void initializeSecrets() {
        encryptedSecrets = new ArrayList<>();
        encryptedSecrets.add(new EncryptedSecret(getString(R.string.crypto_secret_label, 1),
                getString(R.string.crypto_hint_animal),
                desEncrypt("Some sheep have gone missing", SECRET_KEYS[0])));
        encryptedSecrets.add(new EncryptedSecret(getString(R.string.crypto_secret_label, 2),
                getString(R.string.crypto_hint_role),
                desEncrypt("Reallocating budget for new staff", SECRET_KEYS[1])));
        encryptedSecrets.add(new EncryptedSecret(getString(R.string.crypto_secret_label, 3),
                getString(R.string.crypto_hint_not_human_food),
                desEncrypt("Grass is turning blue.", SECRET_KEYS[2])));
        encryptedSecrets.add(new EncryptedSecret(getString(R.string.crypto_secret_label, 4),
                getString(R.string.crypto_hint_warmth),
                desEncrypt(currentKey, SECRET_KEYS[3])));
    }

    private void displaySecrets() {
        if (binding == null) return;
        LinearLayout container = binding.secretsContainer;
        container.removeAllViews();
        for (EncryptedSecret secret : encryptedSecrets) {
            container.addView(createSecretView(secret));
        }
    }

    private View createSecretView(EncryptedSecret secret) {
        Context context = requireContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 12, 0, 12);
        layout.setClickable(true);
        layout.setFocusable(true);
        layout.setOnClickListener(v -> promptToDecryptSecret(secret));

        TextView labelView = new TextView(context);
        labelView.setText(secret.label);
        labelView.setTextSize(13f);
        labelView.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(labelView);

        TextView hintView = new TextView(context);
        hintView.setText(getString(R.string.crypto_hint_line, secret.hint));
        hintView.setTextSize(12f);
        hintView.setPadding(0, 4, 0, 4);
        layout.addView(hintView);

        TextView encryptedView = new TextView(context);
        encryptedView.setText(getString(R.string.crypto_encrypted_line, secret.encryptedValue));
        encryptedView.setTextSize(12f);
        encryptedView.setTypeface(android.graphics.Typeface.MONOSPACE);
        layout.addView(encryptedView);

        TextView decryptedView = new TextView(context);
        decryptedView.setTextSize(12f);
        decryptedView.setTypeface(android.graphics.Typeface.MONOSPACE);
        layout.addView(decryptedView);

        if (secret.decryptedValue != null) {
            decryptedView.setText(getString(R.string.crypto_decrypted_line, secret.decryptedValue));
            decryptedView.setTextColor(ContextCompat.getColor(context, R.color.success_green));
        } else {
            decryptedView.setText(R.string.crypto_decrypted_unknown);
            TextView tapHint = new TextView(context);
            tapHint.setText(R.string.crypto_tap_to_decrypt);
            tapHint.setTextSize(11f);
            tapHint.setTypeface(null, android.graphics.Typeface.ITALIC);
            layout.addView(tapHint);
        }
        return layout;
    }

    private void promptToDecryptSecret(EncryptedSecret secret) {
        if (secret.decryptedValue != null) {
            Toast.makeText(getContext(),
                    getString(R.string.crypto_already_decrypted, secret.decryptedValue),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        TextInputEditText input = new TextInputEditText(requireContext());
        input.setHint(R.string.crypto_key_input_hint);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 20);
        container.addView(input);

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.crypto_decrypt_dialog_title, secret.label))
                .setMessage(getString(R.string.crypto_decrypt_dialog_message, secret.hint))
                .setView(container)
                .setPositiveButton(R.string.decrypt, (dialog, which) ->
                        attemptDecryptSecret(secret, input.getText() != null ? input.getText().toString().trim() : ""))
                .setNegativeButton(R.string.crypto_cancel, null)
                .show();
    }

    private void attemptDecryptSecret(EncryptedSecret secret, String key) {
        if (key.isEmpty()) {
            Toast.makeText(getContext(), R.string.crypto_key_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String decrypted = desDecrypt(secret.encryptedValue, key);
        if (decrypted == null) {
            Toast.makeText(getContext(), R.string.crypto_wrong_key, Toast.LENGTH_SHORT).show();
            return;
        }

        secret.decryptedValue = decrypted;
        displaySecrets();
        Toast.makeText(getContext(), R.string.crypto_decrypted_ok, Toast.LENGTH_SHORT).show();
        checkForCompletion(decrypted);
    }

    private void encryptText() {
        String plaintext = binding.plaintextInput.getText() != null
                ? binding.plaintextInput.getText().toString().trim() : "";
        String key = binding.keyInput.getText() != null
                ? binding.keyInput.getText().toString().trim() : "";
        String hint = binding.hintInput.getText() != null
                ? binding.hintInput.getText().toString().trim() : "";

        if (plaintext.isEmpty() || key.isEmpty()) {
            Toast.makeText(getContext(), R.string.crypto_key_and_text_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (key.length() > 8) {
            Toast.makeText(getContext(), R.string.crypto_des_key_too_long, Toast.LENGTH_SHORT).show();
            return;
        }

        String encrypted = desEncrypt(plaintext, key);
        if (encrypted == null) {
            Toast.makeText(getContext(), R.string.crypto_decrypt_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        binding.cryptoOutput.setVisibility(View.VISIBLE);
        binding.cryptoOutput.setText(getString(R.string.crypto_encrypted_output, encrypted));

        userSecretCount++;
        String label = getString(R.string.crypto_user_secret_label, userSecretCount);
        String finalHint = hint.isEmpty() ? getString(R.string.crypto_no_hint) : hint;
        encryptedSecrets.add(new EncryptedSecret(label, finalHint, encrypted));
        displaySecrets();

        binding.plaintextInput.setText("");
        binding.hintInput.setText("");
        Toast.makeText(getContext(), R.string.crypto_added_to_secrets, Toast.LENGTH_SHORT).show();
    }

    private void decryptText() {
        String ciphertext = binding.plaintextInput.getText() != null
                ? binding.plaintextInput.getText().toString().trim() : "";
        String key = binding.keyInput.getText() != null
                ? binding.keyInput.getText().toString().trim() : "";

        if (ciphertext.isEmpty() || key.isEmpty()) {
            Toast.makeText(getContext(), R.string.crypto_key_and_text_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (key.length() > 8) {
            Toast.makeText(getContext(), R.string.crypto_des_key_too_long, Toast.LENGTH_SHORT).show();
            return;
        }

        String decrypted = desDecrypt(ciphertext, key);
        if (decrypted == null) {
            Toast.makeText(getContext(), R.string.crypto_decrypt_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.cryptoOutput.setVisibility(View.VISIBLE);
        binding.cryptoOutput.setText(getString(R.string.crypto_decrypted_output, decrypted));
        checkForCompletion(decrypted);
        tryDecryptSecretsWithKey(key);
    }

    private void checkForCompletion(String decryptedText) {
        if (currentKey.isEmpty() || !decryptedText.equals(currentKey) || completionSubmitted) {
            return;
        }
        completionSubmitted = true;
        KeyValidator.validateKey(requireContext(), KeyValidator.Module.INSUFFICIENT_CRYPTO_LESSON,
                currentKey, isValid -> {
            if (!isAdded()) return;
            if (isValid) {
                progressTracker.markCompleted(KeyValidator.Module.INSUFFICIENT_CRYPTO_LESSON);
                binding.headerCard.setVisibility(View.GONE);
                int completionCount = progressTracker.getCompletionCount(KeyValidator.Module.INSUFFICIENT_CRYPTO_LESSON);
                String completionText = completionCount > 1
                        ? getString(R.string.crypto_completed_times, completionCount) : "";
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.crypto_success_title)
                        .setMessage(getString(R.string.crypto_success_message, currentKey, completionText))
                        .setPositiveButton(R.string.csi_ok, null)
                        .show();
            } else {
                completionSubmitted = false;
                Toast.makeText(getContext(), R.string.crypto_key_validation_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void tryDecryptSecretsWithKey(String key) {
        boolean anyDecrypted = false;
        for (EncryptedSecret secret : encryptedSecrets) {
            if (secret.decryptedValue == null) {
                String decrypted = desDecrypt(secret.encryptedValue, key);
                if (decrypted != null) {
                    secret.decryptedValue = decrypted;
                    anyDecrypted = true;
                    checkForCompletion(decrypted);
                }
            }
        }
        if (anyDecrypted) {
            displaySecrets();
        }
    }

    // DES helpers

    private String desEncrypt(String plaintext, String key) {
        try {
            Cipher cipher = buildCipher(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "DES encrypt error: " + e.getMessage());
            return null;
        }
    }

    private String desDecrypt(String ciphertext, String key) {
        try {
            byte[] decoded = Base64.decode(ciphertext, Base64.NO_WRAP);
            Cipher cipher = buildCipher(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "DES decrypt error: " + e.getMessage());
            return null;
        }
    }

    private Cipher buildCipher(int mode, String keyStr) throws Exception {
        // Pad / truncate key to exactly 8 bytes
        byte[] keyBytes = new byte[8];
        byte[] src = keyStr.getBytes("UTF-8");
        System.arraycopy(src, 0, keyBytes, 0, Math.min(src.length, 8));
        DESKeySpec desKeySpec = new DESKeySpec(keyBytes);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = factory.generateSecret(desKeySpec);
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(mode, secretKey);
        return cipher;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
