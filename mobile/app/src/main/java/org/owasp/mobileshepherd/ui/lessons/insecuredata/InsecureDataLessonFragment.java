package org.owasp.mobileshepherd.ui.lessons.insecuredata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
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
import org.owasp.mobileshepherd.databinding.FragmentInsecureDataLessonBinding;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class InsecureDataLessonFragment extends Fragment {

    private FragmentInsecureDataLessonBinding binding;
    private static final String TAG = "InsecureDataLesson";
    private static final String DB_NAME = "lesson_users.db";
    private SQLiteDatabase db;
    private String currentFlag = "";
    private ProgressTracker progressTracker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInsecureDataLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v ->
                    ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.IDS_LESSON));
        }

        openDatabase();

        FlagProvider.getFlag(requireContext(), FlagValidator.Module.IDS_LESSON, flagValue -> {
            if (!isAdded()) return;
            currentFlag = flagValue;
            seedDatabase(flagValue);
            displayUsers();
        });

        binding.btnSubmitCredentials.setOnClickListener(v -> submitCredentials());

        return root;
    }

    /** Opens (or creates) the unencrypted SQLite database for this lesson. */
    private void openDatabase() {
        SQLiteOpenHelper helper = new SQLiteOpenHelper(requireContext(), DB_NAME, null, 1) {
            @Override
            public void onCreate(SQLiteDatabase d) {
                d.execSQL("CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "role TEXT)");
            }

            @Override
            public void onUpgrade(SQLiteDatabase d, int oldVersion, int newVersion) {
                d.execSQL("DROP TABLE IF EXISTS users");
                onCreate(d);
            }
        };
        db = helper.getWritableDatabase();
        Log.d(TAG, "Database opened at: " + requireContext().getDatabasePath(DB_NAME).getAbsolutePath());
    }

    private void seedDatabase(String flagValue) {
        db.execSQL("DELETE FROM users");
        insertUser("admin", flagValue, "admin");
        insertUser("alice", "alice_pass_99", "user");
        insertUser("bob", "B0b$ecure!", "user");
        insertUser("charlie", "Ch4rl1e#2024", "user");
        Log.d(TAG, "Database seeded. Admin password stored in plaintext.");
    }

    private void insertUser(String username, String password, String role) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", role);
        db.insert("users", null, cv);
    }

    private void displayUsers() {
        LinearLayout container = binding.usersListContainer;
        container.removeAllViews();

        Cursor cursor = db.rawQuery("SELECT username, role FROM users ORDER BY id", null);
        while (cursor.moveToNext()) {
            String username = cursor.getString(0);
            String role = cursor.getString(1);
            TextView tv = new TextView(getContext());
            tv.setPadding(0, 8, 0, 8);
            tv.setTextSize(14f);
            tv.setText("• " + username + "  [" + role + "]  password: •••••");
            container.addView(tv);
        }
        cursor.close();
    }

    private void submitCredentials() {
        String input = binding.editCredentials.getText() != null
                ? binding.editCredentials.getText().toString().trim() : "";
        if (input.isEmpty()) {
            Toast.makeText(getContext(), "Enter credentials in username:password format", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] parts = input.split(":", 2);
        if (parts.length < 2) {
            Toast.makeText(getContext(), "Format: username:password", Toast.LENGTH_SHORT).show();
            return;
        }

        String enteredUser = parts[0].trim();
        String enteredPass = parts[1].trim();

        // Validate against what is actually stored in the DB (prevents timing guessing)
        Cursor cursor = db.rawQuery(
                "SELECT password FROM users WHERE username = ?",
                new String[]{enteredUser});

        boolean success = false;
        if (cursor.moveToFirst()) {
            String storedPass = cursor.getString(0);
            success = storedPass.equals(enteredPass) && "admin".equalsIgnoreCase(enteredUser);
        }
        cursor.close();

        if (success) {
            binding.textCredentialResult.setVisibility(View.VISIBLE);
            binding.textCredentialResult.setText("Access granted!\n\nFlag: " + currentFlag);
            binding.textCredentialResult.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.success_green));
            progressTracker.markCompleted(FlagValidator.Module.IDS_LESSON);
            Toast.makeText(getContext(), "Correct! Lesson complete.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Admin credentials verified. Flag: " + currentFlag);
        } else {
            binding.textCredentialResult.setVisibility(View.VISIBLE);
            binding.textCredentialResult.setText("Incorrect credentials. Try extracting the DB via ADB.");
            binding.textCredentialResult.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.error_red));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null && db.isOpen()) db.close();
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
