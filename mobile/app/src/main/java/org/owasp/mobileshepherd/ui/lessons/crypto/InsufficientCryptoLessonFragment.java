package org.owasp.mobileshepherd.ui.lessons.crypto;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentInsufficientCryptoLessonBinding;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class InsufficientCryptoLessonFragment extends Fragment {

    private FragmentInsufficientCryptoLessonBinding binding;
    private static final String TAG = "InsufficientCrypto";

    // Intentionally weak, hardcoded DES key — visible to anyone who decompiles the APK
    private static final String WEAK_DES_KEY = "SHEPHERD";

    private String currentFlag = "";
    private ProgressTracker progressTracker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInsufficientCryptoLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());

        FlagProvider.getFlag(requireContext(), FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON,
                flagValue -> {
                    if (!isAdded()) return;
                    currentFlag = flagValue;
                    populateSecrets(flagValue);
                });

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v ->
                    ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON));
        }

        binding.encryptButton.setOnClickListener(v -> performCrypto(true));
        binding.decryptButton.setOnClickListener(v -> performCrypto(false));

        return root;
    }

    /**
     * Pre-populate the encrypted secrets list.  The first two are decoys; the
     * third is the lesson flag encrypted with the WEAK_DES_KEY above.
     */
    private void populateSecrets(String flag) {
        String encryptedPassword = desEncrypt("SuperSecret123", WEAK_DES_KEY);
        String encryptedApiKey   = desEncrypt("prod-api-key-456", WEAK_DES_KEY);
        String encryptedFlag     = desEncrypt(flag, WEAK_DES_KEY);

        Log.d(TAG, "DES key used: " + WEAK_DES_KEY);
        Log.d(TAG, "Encrypted flag: " + encryptedFlag);

        addSecretRow("password_store",    encryptedPassword, "hint: common word");
        addSecretRow("prod_api_key",      encryptedApiKey,   "hint: prod-api-key-NNN");
        addSecretRow("admin_secret_flag", encryptedFlag,     "hint: key is hardcoded in source");
    }

    private void addSecretRow(String label, String ciphertext, String hint) {
        LinearLayout container = binding.secretsContainer;
        TextView tv = new TextView(getContext());
        tv.setPadding(0, 12, 0, 12);
        tv.setTextSize(12f);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        tv.setText(label + ":\n" + ciphertext + "\n(" + hint + ")");
        container.addView(tv);
    }

    private void performCrypto(boolean encrypt) {
        String key  = binding.keyInput.getText() != null
                ? binding.keyInput.getText().toString().trim() : "";
        String text = binding.plaintextInput.getText() != null
                ? binding.plaintextInput.getText().toString().trim() : "";

        if (key.isEmpty() || text.isEmpty()) {
            Toast.makeText(getContext(), "Enter both a key and text", Toast.LENGTH_SHORT).show();
            return;
        }
        if (key.length() < 8) {
            Toast.makeText(getContext(), "DES key must be exactly 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (key.length() > 8) key = key.substring(0, 8);

        String result;
        try {
            result = encrypt ? desEncrypt(text, key) : desDecrypt(text, key);
        } catch (Exception e) {
            result = "Error: " + e.getMessage();
        }

        binding.cryptoOutput.setVisibility(View.VISIBLE);
        binding.cryptoOutput.setText(result);

        // Auto-validate: if the student successfully decrypts the flag
        if (!encrypt && result.equals(currentFlag) && !currentFlag.isEmpty()) {
            binding.cryptoOutput.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.success_green));
            progressTracker.markCompleted(FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON);
            Toast.makeText(getContext(), "Flag found! Lesson complete.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Flag successfully decrypted: " + result);
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
