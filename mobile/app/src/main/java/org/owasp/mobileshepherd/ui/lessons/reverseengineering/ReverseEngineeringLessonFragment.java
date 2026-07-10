package org.owasp.mobileshepherd.ui.lessons.reverseengineering;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentLessonBinding;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class ReverseEngineeringLessonFragment extends Fragment {

    private FragmentLessonBinding binding;
    private LessonModel lessonViewModel;
    private ProgressTracker progressTracker;
    private String currentFlag = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        lessonViewModel = new ViewModelProvider(this).get(LessonModel.class);
        progressTracker = new ProgressTracker(requireContext());

        // FlagProvider returns the static offline flag for RE_LESSON ("Frozen_Clock_Melts_By_Noon").
        // This value is hardcoded inside FlagProvider and is discoverable by decompiling the APK —
        // that is the intended attack surface for the reverse engineering lesson.
        FlagProvider.getFlag(requireContext(), FlagValidator.Module.RE_LESSON,
                flagValue -> currentFlag = flagValue);

        // Observe and display device info
        lessonViewModel.getSerialText().observe(getViewLifecycleOwner(),
                s -> binding.textViewSerial.setText("Serial: " + s));
        lessonViewModel.getModelText().observe(getViewLifecycleOwner(),
                s -> binding.textViewBuild.setText("Model: " + s));
        lessonViewModel.getManufacturerText().observe(getViewLifecycleOwner(),
                s -> binding.textViewManufacturer.setText("Manufacturer: " + s));
        lessonViewModel.getBrandText().observe(getViewLifecycleOwner(),
                s -> binding.textViewBrand.setText("Brand: " + s));
        lessonViewModel.getSDKText().observe(getViewLifecycleOwner(),
                s -> binding.textViewSDK.setText("SDK: " + s));

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v ->
                    ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.RE_LESSON));
        }

        binding.verifyKeyButton.setOnClickListener(v -> verifyKey());

        return root;
    }

    private void verifyKey() {
        String entered = binding.keyInput.getText() != null
                ? binding.keyInput.getText().toString().trim() : "";
        if (entered.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the key you found", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.verifyKeyButton.setEnabled(false);
        FlagValidator.validateFlag(requireContext(), FlagValidator.Module.RE_LESSON,
                entered, correct -> {
                    if (correct) {
                        progressTracker.markCompleted(FlagValidator.Module.RE_LESSON);
                        Toast.makeText(getContext(), "Correct! Lesson complete.", Toast.LENGTH_LONG).show();
                    } else {
                        binding.verifyKeyButton.setEnabled(true);
                        Toast.makeText(getContext(),
                                "Incorrect. Decompile the APK and look in FlagProvider.java",
                                Toast.LENGTH_SHORT).show();
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
