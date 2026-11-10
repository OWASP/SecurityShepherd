package org.owasp.mobileshepherd.ui.lessons;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentInputValidationLessonBinding;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class InputValidationLessonFragment extends Fragment {

    private static final String TAG = "DeepLinkLoader";
    private static final String TRUSTED_HOST = "toomanysheep.com";
    private static final String TRUSTED_PREFIX = "https://" + TRUSTED_HOST;
    private static final String ASSET_DIR = "input_validation/";

    private FragmentInputValidationLessonBinding binding;
    private ProgressTracker progressTracker;
    private String currentKey = "";
    private boolean keyReady = false;
    private String pendingDeepLink = null;
    private boolean completed = false;
    private volatile String currentHost = "";

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInputValidationLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());
        KeyProvider.getKey(
                requireContext(),
                KeyValidator.Module.INPUT_VALIDATION_LESSON,
                keyValue -> {
                    currentKey = keyValue;
                    keyReady = true;
                    if (pendingDeepLink != null && binding != null && isAdded()) {
                        String url = pendingDeepLink;
                        pendingDeepLink = null;
                        processDeepLink(url);
                    }
                });

        setupWebView();

        binding.openLinkButton.setOnClickListener(v -> {
            String url = binding.urlInput.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a URL", Toast.LENGTH_SHORT).show();
                return;
            }
            processDeepLink(url);
        });

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.INPUT_VALIDATION_LESSON));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef);
            });
        }

        // Deep link entry point: myapp://open?url=<target_url>
        Intent launchIntent = requireActivity().getIntent();
        Uri deepLinkData = launchIntent.getData();
        if (deepLinkData != null) {
            launchIntent.setData(null);
            String targetUrl = deepLinkData.getQueryParameter("url");
            if (targetUrl != null) {
                binding.urlInput.setText(targetUrl);
                if (keyReady) {
                    processDeepLink(targetUrl);
                } else {
                    pendingDeepLink = targetUrl;
                }
            }
        }

        return root;
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void setupWebView() {
        WebView webView = binding.contentWebview;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        // VULNERABLE: this bridge is meant for the trusted partner page, but it is
        // registered on the WebView and therefore exposed to every page that gets loaded.
        webView.addJavascriptInterface(new AppBridge(), "Shepherd");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();
                if (request.isForMainFrame()) {
                    currentHost = host == null ? "" : host;
                }
                if (!request.isForMainFrame()) {
                    return notFound();
                }
                // The lesson runs offline: toomanysheep.com is served from the trusted asset page,
                // any other host is served from a page standing in for an attacker's server.
                String asset = TRUSTED_HOST.equals(host) ? "trusted.html" : "attacker.html";
                try {
                    InputStream in = requireContext().getAssets().open(ASSET_DIR + asset);
                    return new WebResourceResponse("text/html", "UTF-8", in);
                } catch (IOException | IllegalStateException e) {
                    Log.e(TAG, "Failed to open asset " + asset, e);
                    return notFound();
                }
            }
        });
    }

    private static WebResourceResponse notFound() {
        return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", null,
                new ByteArrayInputStream(new byte[0]));
    }

    private void processDeepLink(String url) {
        Log.d(TAG, "Processing deep link: myapp://open?url=" + url);

        // VULNERABLE: Weak URL validation
        if (!isUrlAllowed(url)) {
            binding.contentCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_bg));
            binding.contentStatus.setText("URL blocked: " + url + "\n\nOnly links to " + TRUSTED_HOST + " are allowed.");
            binding.contentWebview.setVisibility(View.GONE);
            Log.w(TAG, "URL validation failed: " + url);
            return;
        }

        Log.i(TAG, "URL validation passed: " + url);
        binding.contentCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_bg));
        binding.contentStatus.setText("Loading " + url);
        binding.contentWebview.setVisibility(View.VISIBLE);
        binding.contentWebview.loadUrl(url);
    }

    /**
     * VULNERABILITY: compares the raw URL string instead of the parsed host.
     * "https://toomanysheep.com@attacker.test/" and "https://toomanysheep.com.attacker.test/"
     * both start with "https://toomanysheep.com" but resolve to a different host.
     */
    private static boolean isUrlAllowed(String url) {
        return url.startsWith(TRUSTED_PREFIX);
    }

    /** Bridge exposed to page JavaScript. */
    private class AppBridge {
        @JavascriptInterface
        public String getSessionToken() {
            String host = currentHost;
            if (!TRUSTED_HOST.equals(host) && keyReady) {
                Log.i(TAG, "Session token read by untrusted origin: " + host);
                onUntrustedOriginReadToken();
            }
            return currentKey;
        }
    }

    private void onUntrustedOriginReadToken() {
        if (completed || getActivity() == null) return;
        requireActivity().runOnUiThread(() -> {
            if (completed || binding == null || !isAdded()) return;
            completed = true;
            KeyValidator.validateKey(requireContext(), KeyValidator.Module.INPUT_VALIDATION_LESSON,
                    currentKey, correct -> {
                        Log.d(TAG, "Server submission: " + correct);
                        if (correct && isAdded()) {
                            progressTracker.markCompleted(KeyValidator.Module.INPUT_VALIDATION_LESSON);
                            Toast.makeText(getContext(), "Lesson complete!", Toast.LENGTH_LONG).show();
                        } else {
                            completed = false;
                        }
                    });
        });
    }

    private void collapseFab(FloatingActionButton mainFab, FloatingActionButton fab1) {
        if (fab1 != null) fab1.setVisibility(View.GONE);
        if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_help);
    }


    private void showDetailedInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        TextView moduleBanner = dialogView.findViewById(R.id.module_path_banner);
        if (moduleBanner != null) moduleBanner.setVisibility(View.VISIBLE);
        if (moduleBanner != null) moduleBanner.setText("org.owasp.mobileshepherd.input_validation");

        TextView introText = dialogView.findViewById(R.id.intro_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);

        introText.setText(R.string.input_validation_lesson_intro);

        hintsSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);

        new AlertDialog.Builder(requireContext())
                .setTitle("Input/Output Validation\nOWASP M4: Insufficient Input/Output Validation")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }


    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        collapseFab(fab, fabCommandRef);
        if (binding != null) {
            binding.contentWebview.destroy();
        }
        binding = null;
    }
}