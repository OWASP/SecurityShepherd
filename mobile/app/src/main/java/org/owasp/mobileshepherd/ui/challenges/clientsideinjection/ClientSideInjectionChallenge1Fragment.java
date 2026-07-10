package org.owasp.mobileshepherd.ui.challenges.clientsideinjection;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentClientSideInjectionChallenge1Binding;
import org.owasp.mobileshepherd.ui.challenges.clientsideinjection.helpers.Challenge1DatabaseHelper;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class ClientSideInjectionChallenge1Fragment extends Fragment {

    private FragmentClientSideInjectionChallenge1Binding binding;
    private ClientSideInjectionChallenge1Model viewModel;
    private Challenge1DatabaseHelper dbHelper;
    private static final String TAG = "CSI_Challenge1";
    private ProgressTracker progressTracker;
    private String currentFlag = "";
    private String dbKey = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClientSideInjectionChallenge1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(ClientSideInjectionChallenge1Model.class);
        progressTracker = new ProgressTracker(requireContext());
        dbHelper = new Challenge1DatabaseHelper(requireContext());

        FlagProvider.getFlag(requireContext(),
                FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1, flag -> {
                    if (!isAdded()) return;
                    currentFlag = flag;
                    dbKey = flag;
                    seedDatabase(flag);
                });

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(
                    requireContext(), FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1));
        }

        binding.loginButton.setOnClickListener(v -> attemptLogin());
        binding.submitFlagButton.setOnClickListener(v -> submitFlag());

        return root;
    }

    private void seedDatabase(String flagValue) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(dbKey);
        db.execSQL("DELETE FROM accounts");
        insertAccount(db, "admin",   flagValue,     "admin",  9999);
        insertAccount(db, "alice",   "alicePass99", "user",   100);
        insertAccount(db, "bob",     "B0bR0cks!",   "user",   50);
        insertAccount(db, "charlie", "charlie123",  "user",   25);
        db.close();
        Log.d(TAG, "Accounts DB seeded. admin password = flag.");
    }

    private void insertAccount(SQLiteDatabase db, String user, String pass, String role, int balance) {
        ContentValues cv = new ContentValues();
        cv.put("username", user);
        cv.put("password", pass);
        cv.put("role", role);
        cv.put("balance", balance);
        db.insert("accounts", null, cv);
    }

    private String applySloppyFilter(String input) {
        String upper = input.toUpperCase();
        upper = upper
                .replace("SELECT", ".")
                .replace("WHERE",  ".")
                .replace("FROM",   ".")
                .replace("UNION",  ".")
                .replace("INSERT", ".")
                .replace("UPDATE", ".")
                .replace("DELETE", ".")
                .replace("DROP",   ".")
                .replace(" OR ",   " . ")
                .replace(" AND ",  " . ")
                .replace("--",     ".");
        return upper;
    }

    private void attemptLogin() {
        String rawUser = binding.usernameInput.getText() != null
                ? binding.usernameInput.getText().toString() : "";
        String rawPass = binding.passwordInput.getText() != null
                ? binding.passwordInput.getText().toString() : "";

        if (rawUser.isEmpty() || rawPass.isEmpty()) {
            Toast.makeText(getContext(), "Enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Apply the sloppy filter (easily bypassed by case variation)
        String filteredUser = applySloppyFilter(rawUser);
        String filteredPass = applySloppyFilter(rawPass);

        // VULNERABLE: user input concatenated directly into query
        String query = "SELECT username, role, balance FROM accounts WHERE username = '"
                + filteredUser + "' AND password = '" + filteredPass + "'";

        Log.d(TAG, "Executing query: " + query);

        SQLiteDatabase db = dbHelper.getReadableDatabase(dbKey);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                String loggedInUser = cursor.getString(0);
                String role         = cursor.getString(1);
                int    balance      = cursor.getInt(2);

                Log.d(TAG, "Login success: user=" + loggedInUser + " role=" + role);

                if ("admin".equals(loggedInUser)) {
                    // Admin login — reveal the flag
                    binding.resultText.setText(
                            "ACCESS GRANTED — Admin\n\nFlag: " + currentFlag
                            + "\n\nYou bypassed the filter using SQL injection!");
                    binding.resultText.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.success_green));
                } else {
                    binding.resultText.setText(
                            "Logged in as: " + loggedInUser
                            + "\nRole: " + role + "  Balance: $" + balance
                            + "\n\nNot admin — try to inject as admin.");
                    binding.resultText.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.colorPrimary));
                }
            } else {
                Log.d(TAG, "Login failed for input: " + rawUser);
                binding.resultText.setText("Login failed.");
                binding.resultText.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.error_red));
            }
        } catch (Exception e) {
            Log.e(TAG, "Query error: " + e.getMessage());
            binding.resultText.setText("Query error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    private void submitFlag() {
        String entered = binding.flagInput.getText() != null
                ? binding.flagInput.getText().toString().trim() : "";
        if (entered.isEmpty()) {
            Toast.makeText(getContext(), "Enter the flag", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.submitFlagButton.setEnabled(false);
        FlagValidator.validateFlag(requireContext(),
                FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1,
                entered, correct -> {
                    if (correct) {
                        progressTracker.markCompleted(FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1);
                        Toast.makeText(getContext(), "Correct! Challenge complete.", Toast.LENGTH_LONG).show();
                    } else {
                        binding.submitFlagButton.setEnabled(true);
                        Toast.makeText(getContext(), "Incorrect flag.", Toast.LENGTH_SHORT).show();
                        binding.flagInput.setText("");
                    }
                });
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
