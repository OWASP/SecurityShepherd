package org.owasp.mobileshepherd.ui.lessons;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentInputValidationLessonBinding;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class InputValidationLessonFragment extends Fragment {

    private FragmentInputValidationLessonBinding binding;
    private static final String TAG = "DeepLinkLoader";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    private String currentFlag = "";
    
    private static final String ADMIN_URL = "https://admin.internal/dashboard";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInputValidationLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());
        FlagProvider.getFlag(
                requireContext(),
                FlagValidator.Module.INPUT_VALIDATION_LESSON,
                flagValue -> currentFlag = flagValue);

        // Quick link buttons
        binding.loadExampleButton.setOnClickListener(v -> 
            processDeepLink("https://example.com/welcome"));
        
        binding.loadTrustedButton.setOnClickListener(v -> 
            processDeepLink("https://trusted-site.com/home"));
        
        binding.loadOwaspButton.setOnClickListener(v -> 
            processDeepLink("https://owasp.org/about"));
        
        // Custom deep link
        binding.openLinkButton.setOnClickListener(v -> {
            String url = binding.urlInput.getText().toString().trim();
            if (url.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a URL", Toast.LENGTH_SHORT).show();
                return;
            }
            processDeepLink(url);
        });

        // Setup expandable FAB
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.INPUT_VALIDATION_LESSON));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }

        // Real deep link entry point: myapp://open?url=<target_url>, delivered via
        // adb shell am start -a android.intent.action.VIEW -d "myapp://open?url=..."
        Intent launchIntent = requireActivity().getIntent();
        Uri deepLinkData = launchIntent.getData();
        if (deepLinkData != null) {
            launchIntent.setData(null);
            String targetUrl = deepLinkData.getQueryParameter("url");
            if (targetUrl != null) {
                binding.urlInput.setText(targetUrl);
                processDeepLink(targetUrl);
            }
        }

        return root;
    }

    private void processDeepLink(String url) {
        Log.d(TAG, "Processing deep link: myapp://open?url=" + url);
        
        // VULNERABLE: Weak URL validation
        if (!isUrlAllowed(url)) {
            showError("Security Error", "URL blocked: " + url + "\n\nOnly example.com and trusted-site.com domains are allowed.");
            Log.w(TAG, "URL validation failed: " + url);
            return;
        }
        
        Log.i(TAG, "URL validation passed: " + url);
        loadContent(url);
    }
    
    /**
     * VULNERABILITY: Uses contains() on the host instead of proper domain validation
     * Can be bypassed with a crafted host such as: https://example.com.admin.internal/dashboard
     * (host "example.com.admin.internal" contains "example.com")
     */
    private boolean isUrlAllowed(String url) {
        String host = Uri.parse(url).getHost();
        if (host == null) return false;
        // Weak validation - checks if trusted domain appears anywhere in the host
        return host.contains("example.com") || host.contains("trusted-site.com") || host.contains("owasp.org");
    }

    /**
     * VULNERABILITY: Same weak contains()-on-host check guarding the hidden admin content.
     * A host like "example.com.admin.internal" satisfies both this check and isUrlAllowed().
     */
    private boolean isAdminHost(String url) {
        String host = Uri.parse(url).getHost();
        return host != null && host.contains("admin.internal");
    }
    
    private void loadContent(String url) {
        String title;
        String body;
        int cardColor = getResources().getColor(R.color.card_bg);
        
        // Simulate loading different content based on URL
        if (url.equals("https://example.com/welcome")) {
            title = "Example.com - Welcome";
            body = "Welcome to Example.com!\n\nThis is safe, trusted content from an approved domain.";
            
        } else if (url.equals("https://trusted-site.com/home")) {
            title = "Trusted Site - Home";
            body = "Trusted Site Homepage\n\nYou are viewing content from an approved source.";
            
        } else if (url.equals("https://owasp.org/about")) {
            title = "OWASP.org - About";
            body = "About OWASP\n\nThe Open Web Application Security Project (OWASP) is a nonprofit foundation that works to improve the security of software.\n\nThis is approved security education content.";
            
        } else if (isAdminHost(url)) {
            // Hidden admin content - only accessible via validation bypass!
            String flag = currentFlag;
            title = "Admin Dashboard";
            body = "ACCESS GRANTED\n\n"
                   + "You successfully bypassed the URL validation!\n\n"
                   + "The validation only checks if 'example.com' or 'trusted-site.com' appears "
                   + "anywhere in the host, instead of properly validating the domain.\n\n"
                   + "This allowed you to access restricted admin.internal content.\n\n"
                   + "FLAG: " + flag;
            cardColor = ContextCompat.getColor(requireContext(), R.color.success_bg);
            Log.i(TAG, "Admin content accessed via bypass!");

            progressTracker.markCompleted(FlagValidator.Module.INPUT_VALIDATION_LESSON);
            FlagValidator.validateFlag(requireContext(), FlagValidator.Module.INPUT_VALIDATION_LESSON,
                    currentFlag, correct -> Log.d(TAG, "Server submission: " + correct));
            
        } else {
            // Generic external content
            title = "External Content Loaded";
            body = "URL: " + url + "\n\n" +
                   "This URL passed validation and content was loaded.\n\n" +
                   "Try to find the hidden admin panel at admin.internal domain...";
        }
        
        binding.contentCard.setCardBackgroundColor(cardColor);
        binding.contentTitle.setText(title);
        binding.contentBody.setText(body);
        
        Toast.makeText(getContext(), "Content loaded successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void showError(String title, String message) {
        binding.contentCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_bg));
        binding.contentTitle.setText(title);
        binding.contentBody.setText(message);
        
        Toast.makeText(getContext(), "URL validation failed", Toast.LENGTH_SHORT).show();
    }

    private void toggleFabExpansion(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2) {
        fabExpanded = !fabExpanded;
        if (fabExpanded) {
            if (fab1 != null) fab1.setVisibility(View.VISIBLE);
            if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            collapseFab(mainFab, fab1, fab2);
        }
    }

    private void collapseFab(FloatingActionButton mainFab, FloatingActionButton fab1, FloatingActionButton fab2) {
        fabExpanded = false;
        if (fab1 != null) fab1.setVisibility(View.GONE);
        if (fab2 != null) fab2.setVisibility(View.GONE);
        if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_help);
    }


    private void showDetailedInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        TextView moduleBanner = dialogView.findViewById(R.id.module_path_banner);
        if (moduleBanner != null) moduleBanner.setVisibility(View.VISIBLE);
        if (moduleBanner != null) moduleBanner.setText("org.owasp.mobileshepherd.input_validation");
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.input_validation_lesson_intro);
        // vulnerabilitiesText.setText(R.string.input_validation_lesson_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
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
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
        binding = null;
    }
}
