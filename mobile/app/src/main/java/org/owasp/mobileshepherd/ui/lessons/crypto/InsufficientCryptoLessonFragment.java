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

    
    private static final String TAG = "FlagValidator";
    
    // Module type constants
    public static final String TYPE_LESSON = "lesson";
    public static final String TYPE_CHALLENGE = "challenge";
    
    // Module identifiers
    public enum Module {
        // Reverse Engineering
        RE_LESSON("re_lesson", TYPE_LESSON),
        RE_CHALLENGE_1("re_challenge_1", TYPE_CHALLENGE),
        
        // Insecure Data Storage
        IDS_LESSON("ids_lesson", TYPE_LESSON),
        IDS_CHALLENGE_1("ids_challenge_1", TYPE_CHALLENGE),
        
        // Poor Authentication
        POOR_AUTH_LESSON("poor_auth_lesson", TYPE_LESSON),
        POOR_AUTH_CHALLENGE("poor_auth_challenge", TYPE_CHALLENGE),
        
        // Insecure Authorization
        INSECURE_AUTH_LESSON("insecure_auth_lesson", TYPE_LESSON),
        
        // Supply Chain
        SUPPLY_CHAIN_LESSON("supply_chain_lesson", TYPE_LESSON),
        
        // Insecure Communication
        INSECURE_COMM_LESSON("insecure_comm_lesson", TYPE_LESSON),
        INSECURE_COMM_CHALLENGE("insecure_comm_challenge", TYPE_CHALLENGE),

        // Insufficient Cryptography
        INSUFFICIENT_CRYPTO_LESSON("insufficient_crypto_lesson", TYPE_LESSON),
        INSUFFICIENT_CRYPTO_CHALLENGE("insufficient_crypto_challenge", TYPE_CHALLENGE),
        
        // Security Misconfiguration
        SECURITY_MISCONFIG_LESSON("security_misconfig_lesson", TYPE_LESSON),
        SECURITY_MISCONFIG_CHALLENGE_2("security_misconfig_challenge_2", TYPE_CHALLENGE),
        
        // Input Validation
        INPUT_VALIDATION_LESSON("input_validation_lesson", TYPE_LESSON),

        // Privacy Controls
        PRIVACY_LESSON("privacy_lesson", TYPE_LESSON),
        
        // Client-Side Injection
        CLIENT_SIDE_INJECTION_LESSON("client_side_injection_lesson", TYPE_LESSON),
        CLIENT_SIDE_INJECTION_CHALLENGE_1("client_side_injection_challenge_1", TYPE_CHALLENGE),
        CLIENT_SIDE_INJECTION_CHALLENGE_2("client_side_injection_challenge_2", TYPE_CHALLENGE);
        
        private final String id;
        private final String type;
        
        Module(String id, String type) {
            this.id = id;
            this.type = type;
        }
        
        public String getId() {
            return id;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // RE_LESSON and RE_CHALLENGE_1 retain local hashes — the SHA-256 in the APK is the
    // target of the reverse-engineering challenge itself. All other modules require a live
    // server session; no offline fallback is provided.
    private static final Map<Module, String> FLAG_HASHES = new HashMap<Module, String>() {{
        put(Module.RE_LESSON,      "a0c066b9cd89c084709330a943fb6b333d45c932ed613e428bc52078b5722e57");
        put(Module.RE_CHALLENGE_1, "f04a272a2d82a0168f44559f9957dc9e028ecb13195c12fce17ac08d4af91deb");
    }};
    
    /**
     * Validates a flag submission using SHA-256 hash comparison.
     * 
     * @param module The module being validated
     * @param submittedFlag The flag submitted by the user
     * @return true if the flag is correct, false otherwise
     */
    public static boolean validateFlag(Module module, String submittedFlag) {
        if (submittedFlag == null || submittedFlag.trim().isEmpty()) {
            return false;
        }
        
        String expectedHash = FLAG_HASHES.get(module);
        if (expectedHash == null) {
            Log.e(TAG, "No hash found for module: " + module.getId());
            return false;
        }
        
        String submittedHash = sha256(submittedFlag.trim());
        boolean isValid = expectedHash.equalsIgnoreCase(submittedHash);
        
        Log.d(TAG, isValid ? "[OK] Correct flag for " + module.getId()
                             : "[FAIL] Incorrect flag for " + module.getId());
        return isValid;
    }
    
    /**
     * Computes SHA-256 hash of the input string.
     * 
     * @param input The string to hash
     * @return Hexadecimal representation of the hash
     */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Convert bytes to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 algorithm not available", e);
            return "";
        }
    }
    
    // -------------------------------------------------------------------------
    // Server-side validation
    // -------------------------------------------------------------------------

    /**
     * Callback interface for asynchronous flag validation results.
     *
     * <p>{@link #onResult(boolean)} is always invoked on the main (UI) thread.
     */
    public interface ValidationCallback {
        void onResult(boolean correct);
    }

    /**
     * Validates a flag against the configured Shepherd server when a session is active.
     * Falls back to local SHA-256 comparison when no session is available,
     * so the app remains usable without a running server instance.
     *
     * <p>This method is non-blocking. The result is delivered on the main thread via
     * {@code callback}.
     *
     * @param context  Application context used to read shared preferences.
     * @param module   The module being validated.
     * @param flag     The flag string submitted by the student.
     * @param callback Receives {@code true} when the flag is correct, {@code false} otherwise.
     */
    public static void validateFlag(
            Context context,
            Module module,
            String flag,
            ValidationCallback callback) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl     = prefs.getString("server_preference", "").trim();
        String sessionCookie = AuthManager.getMobileSessionCookie(context);

        if (serverUrl.isEmpty() || sessionCookie.isEmpty()) {
            // No active server session — only RE modules have offline hashes.
            Log.d(TAG, "No server session — offline validation for " + module.getId());
            boolean result = validateFlag(module, flag);
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(result));
            return;
        }

        final String endpointUrl = serverUrl.replaceAll("/+$", "") + "/mobileFlagSubmit";
        final String moduleId    = module.getId();
        final String trimmedFlag = flag.trim();
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            boolean correct = false;
            HttpURLConnection conn = null;
            try {
                String body =
                        "moduleId=" + URLEncoder.encode(moduleId, "UTF-8")
                        + "&flag="     + URLEncoder.encode(trimmedFlag, "UTF-8");

                conn = (HttpURLConnection) new URL(endpointUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(10_000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Cookie", sessionCookie);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader =
                                 new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    }
                    JSONObject json = new JSONObject(sb.toString());
                    correct = json.optBoolean("correct", false);
                    Log.d(TAG, "Server validation for " + moduleId + ": " + correct);
                } else {
                    Log.w(TAG, "Server returned HTTP " + status + " for " + moduleId);
                    correct = validateFlag(module, trimmedFlag);
                }
            } catch (Exception e) {
                Log.e(TAG, "Server validation failed for " + moduleId + ": " + e.getMessage());
                correct = validateFlag(module, trimmedFlag);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            final boolean result = correct;
            mainHandler.post(() -> callback.onResult(result));
        }).start();
    }
}
