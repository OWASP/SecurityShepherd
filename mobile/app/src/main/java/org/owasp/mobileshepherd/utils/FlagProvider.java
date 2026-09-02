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
 * Provides the correct flag string for each mobile module in both offline and online modes.
 *
 * <p><b>Offline mode</b> — when no server credentials are configured, an empty string is returned
 * for all modules except the Reverse Engineering lesson, which retains a static offline flag for
 * introductory use. Lessons are still explorable offline, but flag submission requires a server
 * login so that completion is validated server-side.
 *
 * <p><b>Online mode</b> — when the student is signed in, the server-generated user-specific flag
 * is fetched from {@code /mobileFlagGet}. This flag is an HMAC of the base flag keyed with the
 * server's ephemeral key and the student's username — unique per user, per server session, and
 * unreachable via APK inspection.
 *
 * <p>In online mode the challenge DB is seeded with the dynamic flag before the student begins
 * searching, so the value they discover via exploitation is always the server-validated flag.
 */
public class FlagProvider {

    private static final String TAG = "FlagProvider";

    /**
     * Offline (static) flag values — only the Reverse Engineering lesson retains a static flag;
     * its SHA-256 hash in the APK is the target of the reverse-engineering challenge itself.
     * All other modules return an empty string when offline; a live server session is required.
     */
    private static final Map<FlagValidator.Module, String> OFFLINE_FLAGS = new HashMap<>();

    static {
        OFFLINE_FLAGS.put(
                FlagValidator.Module.RE_LESSON,
                "Frozen_Clock_Melts_By_Noon");
    }

    public interface FlagCallback {
        /** Always invoked on the main thread. */
        void onFlag(String flag);
    }

    /**
     * Returns the flag for the given module.
     *
     * <ul>
     *   <li>If the student is authenticated ({@link AuthManager#isAuthenticated}), a background
     *       HTTP request fetches the user-specific dynamic flag. On network failure the offline
     *       flag is returned as a fallback so the lesson remains usable.
     *   <li>Otherwise the offline static flag is returned synchronously (still via the main thread
     *       for API consistency).
     * </ul>
     *
     * @param ctx      Context used to read credentials.
     * @param module   The module whose flag is needed.
     * @param callback Receives the flag string on the main thread.
     */
    public static void getFlag(Context ctx, FlagValidator.Module module, FlagCallback callback) {
        String offlineFlag = OFFLINE_FLAGS.containsKey(module) ? OFFLINE_FLAGS.get(module) : "";

        if (!AuthManager.isAuthenticated(ctx)) {
            if (offlineFlag.isEmpty()) {
                Log.d(TAG, "Offline mode — no static flag for " + module.getId()
                        + "; sign in to a Shepherd server to obtain a flag");
            } else {
                Log.d(TAG, "Offline mode — returning static flag for " + module.getId());
            }
            new Handler(Looper.getMainLooper()).post(() -> callback.onFlag(offlineFlag));
            return;
        }

        String serverUrl = AuthManager.getServerUrl(ctx);
        String sessionCookie = AuthManager.getMobileSessionCookie(ctx);
        String startEndpoint = serverUrl.replaceAll("/+$", "") + "/mobileModuleStart";
        String endpoint  = serverUrl.replaceAll("/+$", "") + "/mobileFlagGet";
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            String resultFlag = offlineFlag;
            HttpURLConnection conn = null;
            try {
                // Notify the server that this module has been opened. This must succeed
                // before /mobileFlagGet will return a flag (prevents bulk API farming).
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
                    String fetchedFlag = json.optString("flag", "");
                    if (!fetchedFlag.isEmpty()) {
                        resultFlag = fetchedFlag;
                        Log.d(TAG, "Dynamic flag fetched for " + module.getId());
                    }
                } else {
                    Log.w(TAG, "Flag fetch HTTP " + status + " for " + module.getId()
                            + " — using offline flag");
                }
            } catch (Exception e) {
                Log.w(TAG, "Flag fetch failed for " + module.getId()
                        + ": " + e.getMessage() + " — using offline flag");
            } finally {
                if (conn != null) conn.disconnect();
            }

            final String flag = resultFlag;
            mainHandler.post(() -> callback.onFlag(flag));
        }).start();
    }
}
