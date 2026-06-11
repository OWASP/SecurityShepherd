package org.owasp.mobileshepherd.ui.scoreboard;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import org.owasp.mobileshepherd.MainActivity;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.utils.AuthManager;

public class ScoreboardFragment extends Fragment {

    private WebView webView;
    private ProgressBar progressBar;
    private View offlineView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_scoreboard, container, false);

        webView = root.findViewById(R.id.scoreboard_webview);
        progressBar = root.findViewById(R.id.scoreboard_progress);
        offlineView = root.findViewById(R.id.scoreboard_offline_view);

        MaterialButton signInButton = root.findViewById(R.id.scoreboard_sign_in_button);
        signInButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openAuthDialog();
            }
        });

        if (AuthManager.isAuthenticated(requireContext())) {
            setupWebView();
            loadWithCachedOrFreshSession();
        } else {
            offlineView.setVisibility(View.VISIBLE);
        }

        return root;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        // Fix 4: deny access to local files and content providers from the WebView
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        String serverUrl = AuthManager.getServerUrl(requireContext()).replaceAll("/+$", "");
        String serverHost = Uri.parse(serverUrl).getHost();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri requestedUri = request.getUrl();
                String requestedHost = requestedUri.getHost();

                // Fix 3: block navigation to any host other than the configured server
                if (requestedHost == null || !requestedHost.equals(serverHost)) {
                    return true; // block
                }

                // Detect session expiry — server redirects back to login.jsp
                String path = requestedUri.getPath();
                if (path != null && (path.endsWith("login.jsp") || path.endsWith("index.jsp"))) {
                    // Session expired: clear cache and re-authenticate silently
                    AuthManager.clearWebSessionCookie(requireContext());
                    CookieManager.getInstance().removeAllCookies(null);
                    loginAndLoad();
                    return true; // we handle the navigation
                }

                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar != null) {
                    progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    /**
     * Uses the cached web session cookie if one exists; otherwise falls back to the mobile
     * session cookie (same server-side session) and caches it as the web cookie for reuse.
     */
    private void loadWithCachedOrFreshSession() {
        String cached = AuthManager.getWebSessionCookie(requireContext());
        if (!cached.isEmpty()) {
            injectCookieAndLoad(cached);
        } else {
            loginAndLoad();
        }
    }

    /**
     * Uses the mobile session cookie (obtained at login) for the scoreboard WebView.
     * Caches it as the web session cookie to avoid repeated lookups.
     */
    private void loginAndLoad() {
        offlineView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        String mobileCookie = AuthManager.getMobileSessionCookie(requireContext());
        if (!mobileCookie.isEmpty()) {
            AuthManager.saveWebSessionCookie(requireContext(), mobileCookie);
        }
        injectCookieAndLoad(mobileCookie);
    }

    private void injectCookieAndLoad(String cookie) {
        String serverUrl = AuthManager.getServerUrl(requireContext()).replaceAll("/+$", "");

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (!cookie.isEmpty()) {
            for (String pair : cookie.split(";\\s*")) {
                cookieManager.setCookie(serverUrl, pair.trim());
            }
            cookieManager.flush();
        }

        offlineView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(serverUrl + "/scoreboard.jsp");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AuthManager.isAuthenticated(requireContext())) {
            if (webView.getVisibility() != View.VISIBLE) {
                setupWebView();
            }
            loadWithCachedOrFreshSession();
        }
    }

    @Override
    public void onDestroyView() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroyView();
    }
}
