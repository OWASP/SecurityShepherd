package org.owasp.mobileshepherd.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Tracks which modules the signed-in student can currently open, so the UI can grey out the rest.
 *
 * <p>The answer comes from the server's {@code /mobileModuleStatus} endpoint, which applies the same
 * rules as the platform: module closed, event not running (open floor / CTF mode) and modules held
 * by the module blocker. It is advisory only; the server re-checks every module start and key
 * request, so a stale or missing answer never grants access.
 *
 * <p>Fails open: when offline, signed out, or when the request fails, nothing is greyed out.
 */
public class ModuleAvailability {

    private static final String TAG = "ModuleAvailability";

    // module id -> reason, for modules the server reported as not ALLOWED
    private static volatile Map<String, String> unavailable = Collections.emptyMap();

    public interface Listener {
        /** Always invoked on the main thread, after the cached state has been updated. */
        void onUpdated();
    }

    public static boolean isAvailable(KeyValidator.Module module) {
        return !unavailable.containsKey(module.getId());
    }

    /** The server's reason the module is unavailable, or an empty string if it is available. */
    public static String getReason(KeyValidator.Module module) {
        String reason = unavailable.get(module.getId());
        return reason == null ? "" : reason;
    }

    public static void clear() {
        unavailable = Collections.emptyMap();
    }

    /**
     * Fetches the current module status. When signed out the cache is cleared immediately and the
     * listener is still notified, so callers can use one code path for sign in, sign out and resume.
     */
    public static void refresh(Context ctx, Listener listener) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        if (!AuthManager.isAuthenticated(ctx)) {
            clear();
            mainHandler.post(listener::onUpdated);
            return;
        }

        final String endpoint =
                AuthManager.getServerUrl(ctx).replaceAll("/+$", "") + "/mobileModuleStatus";
        final String sessionCookie = AuthManager.getMobileSessionCookie(ctx);

        new Thread(() -> {
            Map<String, String> result = Collections.emptyMap();
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(endpoint).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5_000);
                conn.setReadTimeout(5_000);
                if (!sessionCookie.isEmpty()) {
                    conn.setRequestProperty("Cookie", sessionCookie);
                }

                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader =
                                 new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                    }
                    result = parse(sb.toString());
                } else {
                    Log.w(TAG, "Module status HTTP " + status + " - not greying out any module");
                }
            } catch (Exception e) {
                Log.w(TAG, "Module status fetch failed: " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }

            unavailable = result;
            mainHandler.post(listener::onUpdated);
        }).start();
    }

    /** Extracts module id -> reason for every module the response reports as not ALLOWED. */
    static Map<String, String> parse(String body) {
        Map<String, String> denied = new HashMap<>();
        try {
            JSONObject json = new JSONObject(body);
            if (!json.optBoolean("available", false)) {
                return Collections.emptyMap();
            }
            JSONObject modules = json.optJSONObject("modules");
            if (modules == null) {
                return Collections.emptyMap();
            }
            Iterator<String> ids = modules.keys();
            while (ids.hasNext()) {
                String id = ids.next();
                JSONObject entry = modules.optJSONObject(id);
                if (entry != null && !"ALLOWED".equals(entry.optString("status", "ALLOWED"))) {
                    denied.put(id, entry.optString("message", ""));
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Unreadable module status response: " + e.getMessage());
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(denied);
    }
}
