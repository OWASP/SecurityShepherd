package org.owasp.mobileshepherd.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized key validation system using SHA-256 hashes.
 * Provides secure client-side validation and supports progress tracking.
 */
public class KeyValidator {

    private static final String TAG = "KeyValidator";

    // Module type constants
    public static final String TYPE_LESSON = "lesson";
    public static final String TYPE_CHALLENGE = "challenge";

    // Module identifiers. Each lesson/challenge PR adds its own entries here.
    public enum Module {
        IDS_LESSON("ids_lesson", TYPE_LESSON),
        IDS_CHALLENGE_1("ids_challenge_1", TYPE_CHALLENGE);

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

    // Offline hashes, if any. Modules without an entry require a live server session.
    private static final Map<Module, String> KEY_HASHES = new HashMap<>();

    /**
     * Validates a key submission using SHA-256 hash comparison.
     *
     * @param module The module being validated
     * @param submittedKey The key submitted by the user
     * @return true if the key is correct, false otherwise
     */
    public static boolean validateKey(Module module, String submittedKey) {
        if (submittedKey == null || submittedKey.trim().isEmpty()) {
            return false;
        }

        String expectedHash = KEY_HASHES.get(module);
        if (expectedHash == null) {
            Log.e(TAG, "No hash found for module: " + module.getId());
            return false;
        }

        String submittedHash = sha256(submittedKey.trim());
        boolean isValid = expectedHash.equalsIgnoreCase(submittedHash);

        Log.d(TAG, isValid ? "[OK] Correct key for " + module.getId()
                             : "[FAIL] Incorrect key for " + module.getId());
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
     * Callback interface for asynchronous key validation results.
     *
     * <p>{@link #onResult(boolean)} is always invoked on the main (UI) thread.
     */
    public interface ValidationCallback {
        void onResult(boolean correct);
    }

    /**
     * Validates a key against the configured Shepherd server when a session is active.
     * Falls back to local SHA-256 comparison when no session is available,
     * so the app remains usable without a running server instance.
     *
     * <p>This method is non-blocking. The result is delivered on the main thread via
     * {@code callback}.
     *
     * @param context  Application context used to read shared preferences.
     * @param module   The module being validated.
     * @param key      The key string submitted by the student.
     * @param callback Receives {@code true} when the key is correct, {@code false} otherwise.
     */
    public static void validateKey(
            Context context,
            Module module,
            String key,
            ValidationCallback callback) {

        String serverUrl    = AuthManager.getServerUrl(context);
        String sessionCookie = AuthManager.getMobileSessionCookie(context);

        if (serverUrl.isEmpty() || sessionCookie.isEmpty()) {
            // No active server session — only modules with an offline hash can validate.
            Log.d(TAG, "No server session — offline validation for " + module.getId());
            boolean result = validateKey(module, key);
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(result));
            return;
        }

        final String endpointUrl = serverUrl.replaceAll("/+$", "") + "/mobileFlagSubmit";
        final String moduleId    = module.getId();
        final String trimmedKey = key.trim();
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            boolean correct = false;
            HttpURLConnection conn = null;
            try {
                String body =
                        "moduleId=" + URLEncoder.encode(moduleId, "UTF-8")
                        + "&flag="     + URLEncoder.encode(trimmedKey, "UTF-8");

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
                    correct = validateKey(module, trimmedKey);
                }
            } catch (Exception e) {
                Log.e(TAG, "Server validation failed for " + moduleId + ": " + e.getMessage());
                correct = validateKey(module, trimmedKey);
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
