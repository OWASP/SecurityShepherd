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

    // Each secret is "encrypted" with its own short, guessable word — reusing
    // a single key across secrets would itself be the vulnerability, so the
    // lesson instead shows why ANY weak, dictionary-guessable DES key is unsafe.
    private static final String[] SECRET_KEYS = {"sheep", "shepherd", "grass", "wool"};

    private String currentKey = "";
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
        encryptedSecrets.add(new EncryptedSecret("Secret 1", "Animal",
                desEncrypt("Some sheep have gone missing", SECRET_KEYS[0])));
        encryptedSecrets.add(new EncryptedSecret("Secret 2", "Role",
                desEncrypt("Reallocating budget for new staff", SECRET_KEYS[1])));
        encryptedSecrets.add(new EncryptedSecret("Secret 3", "Not Human Food",
                desEncrypt("Grass is turning blue.", SECRET_KEYS[2])));
        encryptedSecrets.add(new EncryptedSecret("Secret 4", "Material for warmth",
                desEncrypt(currentKey, SECRET_KEYS[3])));
    }

    private void displaySecrets() {
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
        hintView.setText("Hint: " + secret.hint);
        hintView.setTextSize(12f);
        hintView.setPadding(0, 4, 0, 4);
        layout.addView(hintView);

        TextView encryptedView = new TextView(context);
        encryptedView.setText("Encrypted: " + secret.encryptedValue);
        encryptedView.setTextSize(12f);
        encryptedView.setTypeface(android.graphics.Typeface.MONOSPACE);
        layout.addView(encryptedView);

        TextView decryptedView = new TextView(context);
        decryptedView.setTextSize(12f);
        decryptedView.setTypeface(android.graphics.Typeface.MONOSPACE);
        if (secret.decryptedValue != null) {
            decryptedView.setText("Decrypted: " + secret.decryptedValue);
            decryptedView.setTextColor(ContextCompat.getColor(context, R.color.success_green));
        } else {
            decryptedView.setText("Decrypted: ???");
            TextView tapHint = new TextView(context);
            tapHint.setText("→ Tap to decrypt");
            tapHint.setTextSize(11f);
            tapHint.setTypeface(null, android.graphics.Typeface.ITALIC);
            layout.addView(decryptedView);
            layout.addView(tapHint);
            return layout;
        }
        layout.addView(decryptedView);
        return layout;
    }

    private void promptToDecryptSecret(EncryptedSecret secret) {
        if (secret.decryptedValue != null) {
            Toast.makeText(getContext(), "Already decrypted: " + secret.decryptedValue, Toast.LENGTH_SHORT).show();
            return;
        }

        TextInputEditText input = new TextInputEditText(requireContext());
        input.setHint("Enter key (up to 8 characters)");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 20);
        container.addView(input);

        new AlertDialog.Builder(requireContext())
                .setTitle("Decrypt " + secret.label)
                .setMessage("Hint: " + secret.hint + "\n\nEnter the decryption key:")
                .setView(container)
                .setPositiveButton("Decrypt", (dialog, which) ->
                        attemptDecryptSecret(secret, input.getText() != null ? input.getText().toString().trim() : ""))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void attemptDecryptSecret(EncryptedSecret secret, String key) {
        if (key.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a key", Toast.LENGTH_SHORT).show();
            return;
        }

        String decrypted = desDecrypt(secret.encryptedValue, key);
        if (decrypted.startsWith("[")) {
            Toast.makeText(getContext(), "Incorrect key. Try again!", Toast.LENGTH_SHORT).show();
            return;
        }

        secret.decryptedValue = decrypted;
        displaySecrets();
        Toast.makeText(getContext(), "Successfully decrypted!", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getContext(), "Enter both a key and text", Toast.LENGTH_SHORT).show();
            return;
        }
        if (key.length() > 8) {
            Toast.makeText(getContext(), "DES key must be 8 characters or less", Toast.LENGTH_SHORT).show();
            return;
        }

        String encrypted = desEncrypt(plaintext, key);
        binding.cryptoOutput.setVisibility(View.VISIBLE);
        binding.cryptoOutput.setText("Encrypted:\n" + encrypted);

        userSecretCount++;
        String label = "User Secret " + userSecretCount;
        String finalHint = hint.isEmpty() ? "No hint provided" : hint;
        encryptedSecrets.add(new EncryptedSecret(label, finalHint, encrypted));
        displaySecrets();

        binding.plaintextInput.setText("");
        binding.hintInput.setText("");
        Toast.makeText(getContext(), "Added to encrypted secrets list!", Toast.LENGTH_SHORT).show();
    }

    private void decryptText() {
        String ciphertext = binding.plaintextInput.getText() != null
                ? binding.plaintextInput.getText().toString().trim() : "";
        String key = binding.keyInput.getText() != null
                ? binding.keyInput.getText().toString().trim() : "";

        if (ciphertext.isEmpty() || key.isEmpty()) {
            Toast.makeText(getContext(), "Enter both a key and text", Toast.LENGTH_SHORT).show();
            return;
        }
        if (key.length() > 8) {
            Toast.makeText(getContext(), "DES key must be 8 characters or less", Toast.LENGTH_SHORT).show();
            return;
        }

        String decrypted = desDecrypt(ciphertext, key);
        binding.cryptoOutput.setVisibility(View.VISIBLE);
        binding.cryptoOutput.setText("Decrypted:\n" + decrypted);

        if (!decrypted.startsWith("[")) {
            checkForCompletion(decrypted);
            tryDecryptSecretsWithKey(key);
        } else {
            Toast.makeText(getContext(), "Decryption failed: invalid key or ciphertext", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkForCompletion(String decryptedText) {
        if (!currentKey.isEmpty() && decryptedText.equals(currentKey)) {
            progressTracker.markCompleted(KeyValidator.Module.INSUFFICIENT_CRYPTO_LESSON);
            KeyValidator.validateKey(requireContext(), KeyValidator.Module.INSUFFICIENT_CRYPTO_LESSON,
                    currentKey, correct -> Log.d(TAG, "Server submission: " + correct));
            Toast.makeText(getContext(), "Key found! Lesson complete.", Toast.LENGTH_LONG).show();
        }
    }

    private void tryDecryptSecretsWithKey(String key) {
        boolean anyDecrypted = false;
        for (EncryptedSecret secret : encryptedSecrets) {
            if (secret.decryptedValue == null) {
                String decrypted = desDecrypt(secret.encryptedValue, key);
                if (!decrypted.startsWith("[")) {
                    secret.decryptedValue = decrypted;
                    anyDecrypted = true;
                }
            }
        }
        if (anyDecrypted) {
            displaySecrets();
        }
    }

    // ── DES helpers ──────────────────────────────────────────────────────────

    private String desEncrypt(String plaintext, String key) {
        try {
            Cipher cipher = buildCipher(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "DES encrypt error: " + e.getMessage());
            return "[encrypt error]";
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
            return "[wrong key or invalid ciphertext]";
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
