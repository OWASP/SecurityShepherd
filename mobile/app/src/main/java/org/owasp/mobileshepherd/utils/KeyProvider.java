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
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the correct key string for each mobile module in both offline and online modes.
 *
 * <p><b>Offline mode</b> — when no server credentials are configured, an empty string is returned
 * for all modules except the Reverse Engineering lesson, which retains a static offline key for
 * introductory use. Lessons are still explorable offline, but key submission requires a server
 * login so that completion is validated server-side.
 *
 * <p><b>Online mode</b> — when the student is signed in, the server-generated user-specific key
 * is fetched from {@code /mobileFlagGet}. This key is an HMAC of the base key keyed with the
 * server's ephemeral key and the student's username — unique per user, per server session, and
 * unreachable via APK inspection.
 *
 * <p>In online mode the challenge DB is seeded with the dynamic key before the student begins
 * searching, so the value they discover via exploitation is always the server-validated key.
 */
public class KeyProvider {

    private static final String TAG = "KeyProvider";

    /**
     * Offline (static) key values — only the Reverse Engineering lesson retains a static key;
     * its SHA-256 hash in the APK is the target of the reverse-engineering challenge itself.
     * All other modules return an empty string when offline; a live server session is required.
     */
    private static final Map<KeyValidator.Module, String> OFFLINE_KEYS = new HashMap<>();

    static {
        OFFLINE_KEYS.put(
                KeyValidator.Module.RE_LESSON,
                "Frozen_Clock_Melts_By_Noon");
    }

    public interface KeyCallback {
        /** Always invoked on the main thread. */
        void onKey(String key);
    }

    /**
     * Returns the key for the given module.
     *
     * <ul>
     *   <li>If the student is authenticated ({@link AuthManager#isAuthenticated}), a background
     *       HTTP request fetches the user-specific dynamic key. On network failure the offline
     *       key is returned as a fallback so the lesson remains usable.
     *   <li>Otherwise the offline static key is returned synchronously (still via the main thread
     *       for API consistency).
     * </ul>
     *
     * @param ctx      Context used to read credentials.
     * @param module   The module whose key is needed.
     * @param callback Receives the key string on the main thread.
     */
    public static void getKey(Context ctx, KeyValidator.Module module, KeyCallback callback) {
        String offlineKey = OFFLINE_KEYS.containsKey(module) ? OFFLINE_KEYS.get(module) : "";

        if (!AuthManager.isAuthenticated(ctx)) {
            if (offlineKey.isEmpty()) {
                Log.d(TAG, "Offline mode — no static key for " + module.getId()
                        + "; sign in to a Shepherd server to obtain a key");
            } else {
                Log.d(TAG, "Offline mode — returning static key for " + module.getId());
            }
            new Handler(Looper.getMainLooper()).post(() -> callback.onKey(offlineKey));
            return;
        }

        String serverUrl = AuthManager.getServerUrl(ctx);
        String sessionCookie = AuthManager.getMobileSessionCookie(ctx);
        String startEndpoint = serverUrl.replaceAll("/+$", "") + "/mobileModuleStart";
        String endpoint  = serverUrl.replaceAll("/+$", "") + "/mobileFlagGet";
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            String resultKey = offlineKey;
            HttpURLConnection conn = null;
            try {
                // Notify the server that this module has been opened. This must succeed
                // before /mobileFlagGet will return a key (prevents bulk API farming).
                try {
                    String startBody = "moduleId=" + URLEncoder.encode(module.getId(), "UTF-8");
                    HttpURLConnection startConn =
                            (HttpURLConnection) new URL(startEndpoint).openConnection();
                    startConn.setRequestMethod("POST");
                    startConn.setDoOutput(true);
                    startConn.setConnectTimeout(5_000);
                    startConn.setReadTimeout(5_000);
                    startConn.setRequestProperty(
                            "Content-Type", "application/x-www-form-urlencoded");
                    if (!sessionCookie.isEmpty()) {
                        startConn.setRequestProperty("Cookie", sessionCookie);
                    }
                    try (OutputStream startOs = startConn.getOutputStream()) {
                        startOs.write(startBody.getBytes(StandardCharsets.UTF_8));
                    }
                    startConn.getResponseCode(); // consume response
                    startConn.disconnect();
                } catch (Exception startEx) {
                    Log.w(TAG, "Module start notification failed for " + module.getId()
                            + ": " + startEx.getMessage());
                }

                String body = "moduleId=" + URLEncoder.encode(module.getId(), "UTF-8");

                conn = (HttpURLConnection) new URL(endpoint).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(10_000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                if (!sessionCookie.isEmpty()) {
                    conn.setRequestProperty("Cookie", sessionCookie);
                }

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader =
                                 new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                    }
                    JSONObject json = new JSONObject(sb.toString());
                    String fetchedKey = json.optString("flag", "");
                    if (!fetchedKey.isEmpty()) {
                        resultKey = fetchedKey;
                        Log.d(TAG, "Dynamic key fetched for " + module.getId());
                    }
                } else {
                    Log.w(TAG, "Key fetch HTTP " + status + " for " + module.getId()
                            + " — using offline key");
                }
            } catch (Exception e) {
                Log.w(TAG, "Key fetch failed for " + module.getId()
                        + ": " + e.getMessage() + " — using offline key");
            } finally {
                if (conn != null) conn.disconnect();
            }

            final String key = resultKey;
            mainHandler.post(() -> callback.onKey(key));
        }).start();
    }
}
