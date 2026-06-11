package org.owasp.mobileshepherd.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Manages online/offline authentication state for the Security Shepherd mobile app.
 *
 * <p>When a server URL, username, and password are configured in the app preferences and a
 * successful login has been completed, the app operates in <em>online mode</em>: flag values are
 * fetched from the server as user-specific dynamic strings, and completion is validated
 * server-side.
 *
 * <p>When no credentials are configured, or when the user explicitly logs out, the app operates in
 * <em>offline mode</em>: static flag values are used and progress is tracked only on-device.
 */
public class AuthManager {

    private static final String TAG = "AuthManager";

    /**
     * Preference key used to persist the authenticated state. Cleared on logout. The username,
     * password and server URL are stored under their own preference keys so they are also visible
     * in the Settings screen.
     */
    private static final String PREF_AUTH_LOGGED_IN = "auth_logged_in";
    /** Preference key for the cached web JSESSIONID used by the scoreboard WebView. */
    private static final String PREF_WEB_SESSION_COOKIE = "auth_web_session_cookie";
    /** Preference key for the JSESSIONID captured at mobile login, used in API requests. */
    private static final String PREF_MOBILE_SESSION_COOKIE = "auth_mobile_session_cookie";

    public interface AuthCallback {
        /** Always invoked on the main thread. */
        void onResult(boolean success, String message);
    }

    // -------------------------------------------------------------------------
    // State queries
    // -------------------------------------------------------------------------

    /** Returns {@code true} when a session cookie is held AND a successful login has been recorded. */
    public static boolean isAuthenticated(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(PREF_AUTH_LOGGED_IN, false)
                && !getServerUrl(ctx).isEmpty()
                && !getUsername(ctx).isEmpty()
                && !getMobileSessionCookie(ctx).isEmpty();
    }

    public static String getServerUrl(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getString("server_preference", "").trim();
    }

    /** Returns the username of the currently signed-in user, or empty string if not signed in. */
    public static String getUsername(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getString("username_preference", "").trim();
    }

    /**
     * Returns the JSESSIONID captured during the last successful mobile login, formatted as
     * a ready-to-use Cookie header value (e.g. {@code "JSESSIONID=abc123"}), or empty string
     * if not yet obtained.
     */
    public static String getMobileSessionCookie(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getString(PREF_MOBILE_SESSION_COOKIE, "");
    }

    /**
     * Returns the cached web JSESSIONID cookie string (name=value) for use in the scoreboard
     * WebView, or empty string if not yet obtained.
     */
    public static String getWebSessionCookie(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getString(PREF_WEB_SESSION_COOKIE, "");
    }

    /** Persists a freshly-obtained web session cookie. Called by ScoreboardFragment. */
    public static void saveWebSessionCookie(Context ctx, String cookie) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putString(PREF_WEB_SESSION_COOKIE, cookie)
                .apply();
    }

    /** Clears the cached web session cookie (e.g. on confirmed session expiry). */
    public static void clearWebSessionCookie(Context ctx) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .remove(PREF_WEB_SESSION_COOKIE)
                .apply();
    }

    // -------------------------------------------------------------------------
    // Auth operations
    // -------------------------------------------------------------------------

    /**
     * Attempts to authenticate against the Shepherd server. On success, stores the supplied
     * credentials in the default shared preferences and sets the logged-in flag.
     *
     * @param ctx      Context used to access preferences.
     * @param serverUrl Base URL of the Shepherd server (e.g. {@code http://192.168.1.1:8080}).
     * @param username  Shepherd username.
     * @param password  Shepherd password.
     * @param callback  Receives the result on the main thread.
     */
    public static void login(
            Context ctx,
            String serverUrl,
            String username,
            String password,
            AuthCallback callback) {

        final Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String endpoint = serverUrl.replaceAll("/+$", "") + "/mobileLogin";
                String body = "login=" + URLEncoder.encode(username, "UTF-8")
                        + "&pwd=" + URLEncoder.encode(password, "UTF-8");

                conn = (HttpURLConnection) new URL(endpoint).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(10_000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader =
                             new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }

                String responseBody = sb.toString();
                if (status == HttpURLConnection.HTTP_OK && !responseBody.startsWith("ERROR")) {
                    // Capture the JSESSIONID for subsequent mobile API calls — avoids re-sending
                    // the raw password on every flag request.
                    String mobileCookie = "";
                    Map<String, List<String>> respHeaders = conn.getHeaderFields();
                    if (respHeaders != null) {
                        List<String> setCookies = respHeaders.get("Set-Cookie");
                        if (setCookies != null) {
                            for (String c : setCookies) {
                                if (c.regionMatches(true, 0, "JSESSIONID=", 0, 11)) {
                                    mobileCookie = c.split(";")[0];
                                    break;
                                }
                            }
                        }
                    }
                    // Persist server URL, username (for display), and session cookie
                    PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                            .putString("server_preference", serverUrl.trim())
                            .putString("username_preference", username.trim())
                            .putBoolean(PREF_AUTH_LOGGED_IN, true)
                            .putString(PREF_MOBILE_SESSION_COOKIE, mobileCookie)
                            .apply();
                    Log.d(TAG, "Login successful for: " + username);
                    mainHandler.post(() -> callback.onResult(true, "Signed in as " + username));
                } else {
                    Log.d(TAG, "Login rejected for: " + username + " (HTTP " + status + ")");
                    mainHandler.post(() -> callback.onResult(false, "Invalid username or password"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Login error: " + e.getMessage());
                mainHandler.post(() -> callback.onResult(
                        false, "Could not reach server: " + e.getMessage()));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    /**
     * Attempts to register a new student account on the Shepherd server. On success, automatically
     * logs in with the new credentials.
     *
     * @param ctx      Context used to access preferences.
     * @param serverUrl Base URL of the Shepherd server.
     * @param username  Desired username.
     * @param password  Desired password.
     * @param email     Email address (may be empty; validated server-side if provided).
     * @param callback  Receives the result on the main thread.
     */
    public static void register(
            Context ctx,
            String serverUrl,
            String username,
            String password,
            String email,
            AuthCallback callback) {

        final Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String endpoint = serverUrl.replaceAll("/+$", "") + "/mobileRegister";
                String body = "userName=" + URLEncoder.encode(username, "UTF-8")
                        + "&passWord=" + URLEncoder.encode(password, "UTF-8")
                        + "&userAddress=" + URLEncoder.encode(email, "UTF-8");

                conn = (HttpURLConnection) new URL(endpoint).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(10_000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader =
                             new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                }

                if (status == HttpURLConnection.HTTP_OK) {
                    JSONObject json = new JSONObject(sb.toString());
                    if (json.optBoolean("success", false)) {
                        Log.d(TAG, "Registration successful for: " + username);
                        // Auto-login after successful registration
                        login(ctx, serverUrl, username, password, callback);
                    } else {
                        String msg = json.optString("message", "Registration failed");
                        mainHandler.post(() -> callback.onResult(false, msg));
                    }
                } else {
                    mainHandler.post(() -> callback.onResult(
                            false, "Server error (HTTP " + status + ")"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Registration error: " + e.getMessage());
                mainHandler.post(() -> callback.onResult(
                        false, "Could not reach server: " + e.getMessage()));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    /**
     * Clears the logged-in flag. Stored credentials are retained in preferences so the user does
     * not need to retype them if they sign back in.
     */
    public static void logout(Context ctx) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(PREF_AUTH_LOGGED_IN, false)
                .remove(PREF_WEB_SESSION_COOKIE)
                .remove(PREF_MOBILE_SESSION_COOKIE)
                .apply();
        // Clear local lesson progress so the next user starts with a clean slate
        ProgressTracker.clearAll(ctx);
        // Also clear the WebView cookie store so the scoreboard can't be accessed after logout
        android.webkit.CookieManager.getInstance().removeAllCookies(null);
        Log.d(TAG, "User logged out");
    }
}
