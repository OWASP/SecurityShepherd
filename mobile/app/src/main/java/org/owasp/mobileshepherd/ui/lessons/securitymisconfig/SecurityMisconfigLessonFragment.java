package org.owasp.mobileshepherd.ui.lessons.securitymisconfig;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class SecurityMisconfigLessonFragment extends Fragment {

    private static final String TAG = "SecurityMisconfig";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    private String currentFlag = "";

    private com.google.android.material.textfield.TextInputEditText flagInputView;
    private TextView flagResultView;
    
    private TextView debugStatus;
    private TextView backupStatus;
    private TextView exportedStatus;
    private TextView networkStatus;
    private TextView permissionsStatus;
    private TextView resultText;
    private LinearLayout componentsContainer;
    private View componentsCard;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_security_misconfig_lesson, container, false);
        
        progressTracker = new ProgressTracker(requireContext());

        debugStatus = root.findViewById(R.id.debug_status);
        backupStatus = root.findViewById(R.id.backup_status);
        exportedStatus = root.findViewById(R.id.exported_status);
        networkStatus = root.findViewById(R.id.network_status);
        permissionsStatus = root.findViewById(R.id.permissions_status);
        resultText = root.findViewById(R.id.result_text);
        componentsContainer = root.findViewById(R.id.components_container);
        componentsCard = root.findViewById(R.id.components_card);
        
        Button checkButton = root.findViewById(R.id.check_config_button);
        checkButton.setOnClickListener(v -> checkConfiguration());

        // Component scanning for exported component discovery
        Button scanComponentsButton = root.findViewById(R.id.scan_components_button);
        scanComponentsButton.setOnClickListener(v -> scanComponents());

        // Flag submission
        flagInputView = root.findViewById(R.id.flag_input_misconfig);
        flagResultView = root.findViewById(R.id.flag_result_misconfig);
        Button submitFlagButton = root.findViewById(R.id.submit_flag_misconfig);
        if (submitFlagButton != null) {
            submitFlagButton.setOnClickListener(v -> submitFlag());
        }

        // Fetch server flag (exposed via debug log during checkConfiguration)
        FlagProvider.getFlag(requireContext(), FlagValidator.Module.SECURITY_MISCONFIG_LESSON, flagValue -> {
            currentFlag = flagValue;
        });

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.SECURITY_MISCONFIG_LESSON));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }


        return root;
    }

    private void checkConfiguration() {
        int issues = 0;
        
        // Check if app is debuggable
        try {
            ApplicationInfo appInfo = requireContext().getApplicationInfo();
            boolean isDebuggable = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            
            if (isDebuggable) {
                debugStatus.setText(" Debug Mode: ENABLED (Insecure)");
                debugStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_text));
                issues++;
                Log.w(TAG, "Security Issue: Debug mode is enabled in production");
                Log.w(TAG, "DEBUG_SECRET=" + currentFlag);
            } else {
                debugStatus.setText("[OK] Debug Mode: Disabled (Secure)");
                debugStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_text));
            }
        } catch (Exception e) {
            debugStatus.setText("Debug Mode: Error checking");
        }

        // Check backup flag
        try {
            ApplicationInfo appInfo = requireContext().getApplicationInfo();
            boolean allowBackup = (appInfo.flags & ApplicationInfo.FLAG_ALLOW_BACKUP) != 0;
            
            if (allowBackup) {
                backupStatus.setText(" Backup Allowed: TRUE (Insecure)");
                backupStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_text));
                issues++;
                Log.w(TAG, "Security Issue: Backup is allowed, app data can be extracted");
            } else {
                backupStatus.setText("[OK] Backup Allowed: FALSE (Secure)");
                backupStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_text));
            }
        } catch (Exception e) {
            backupStatus.setText("Backup Allowed: Error checking");
        }

        // Check for exported components
        try {
            PackageManager pm = requireContext().getPackageManager();
            String packageName = requireContext().getPackageName();
            int exportedCount = 0;
            
            // This is a simplified check - in reality you'd enumerate activities, services, receivers
            exportedStatus.setText(" Exported Components: Found (Potential Risk)");
            exportedStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.card_warning_text));
            Log.w(TAG, "Security Warning: Exported components detected - verify they require permissions");
        } catch (Exception e) {
            exportedStatus.setText("Exported Components: Error checking");
        }

        // Check network security
        networkStatus.setText(" Network Security: Cleartext Traffic Allowed");
        networkStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_text));
        issues++;
        Log.w(TAG, "Security Issue: Cleartext traffic (HTTP) is allowed");

        // Check permissions
        permissionsStatus.setText("[OK] Permissions: Following Least Privilege");
        permissionsStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_text));

        // Show summary
        resultText.setVisibility(View.VISIBLE);
        if (issues >= 3) {
            resultText.setText(" CRITICAL: " + issues + " major security misconfigurations found!\n\nThis app is vulnerable to multiple attack vectors.");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_text));
            resultText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_bg));
        } else if (issues > 0) {
            resultText.setText(" WARNING: " + issues + " security issues detected.\n\nReview configuration settings.");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.card_warning_text));
            resultText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_warning_bg));
        } else {
            resultText.setText("[OK] SECURE: No major issues detected.\n\nConfiguration looks good!");
            resultText.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_text));
            resultText.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success_bg));
        }

        Log.i(TAG, "Configuration check complete. Issues found: " + issues);
    }

    private void submitFlag() {
        if (flagInputView == null || flagResultView == null) return;
        String entered = flagInputView.getText() != null ? flagInputView.getText().toString().trim() : "";
        if (entered.isEmpty()) {
            flagResultView.setVisibility(View.VISIBLE);
            flagResultView.setText("Please enter a flag.");
            flagResultView.setTextColor(ContextCompat.getColor(requireContext(), R.color.card_warning_text));
            return;
        }
        FlagValidator.validateFlag(requireContext(), FlagValidator.Module.SECURITY_MISCONFIG_LESSON,
                entered, isValid -> {
            flagResultView.setVisibility(View.VISIBLE);
            if (isValid) {
                progressTracker.markCompleted(FlagValidator.Module.SECURITY_MISCONFIG_LESSON);
                int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.SECURITY_MISCONFIG_LESSON);
                String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
                flagResultView.setText("\u2713 Correct!");
                flagResultView.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_text));
                new AlertDialog.Builder(requireContext())
                    .setTitle("\uD83C\uDF89 Lesson Complete!")
                    .setMessage("You found the flag exposed in the debug logs.\n\nFlag: " + entered + completionText)
                    .setPositiveButton("OK", null)
                    .show();
            } else {
                flagResultView.setText("\u2717 Incorrect. Run the config check and inspect logcat for the DEBUG_SECRET line.");
                flagResultView.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_text));
                flagInputView.setText("");
            }
        });
    }

    private void scanComponents() {
        componentsContainer.removeAllViews();
        
        try {
            PackageManager pm = requireContext().getPackageManager();
            String packageName = requireContext().getPackageName();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            
            ActivityInfo[] activities = packageInfo.activities;
            int exportedCount = 0;
            
            if (activities != null) {
                for (ActivityInfo activity : activities) {
                    boolean isExported = activity.exported;
                    if (isExported) {
                        exportedCount++;
                    }
                    
                    // Create view for each component
                    LinearLayout itemLayout = new LinearLayout(requireContext());
                    itemLayout.setOrientation(LinearLayout.VERTICAL);
                    itemLayout.setPadding(12, 8, 12, 8);
                    
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 0, 0, 8);
                    itemLayout.setLayoutParams(params);
                    
                    // Status indicator and activity name
                    TextView nameView = new TextView(requireContext());
                    String statusIcon = isExported ? "VULN" : "SAFE";
                    String statusText = isExported ? "EXPORTED" : "Safe";
                    String activityName = activity.name.replace("org.owasp.mobileshepherd.", "");
                    
                    nameView.setText(statusIcon + " " + activityName);
                    nameView.setTextSize(12);
                    nameView.setTextColor(isExported ? 
                        ContextCompat.getColor(requireContext(), R.color.error_text) : 
                        ContextCompat.getColor(requireContext(), R.color.success_text));
                    nameView.setTypeface(null, android.graphics.Typeface.BOLD);
                    itemLayout.addView(nameView);
                    
                    // Full path and label
                    TextView detailsView = new TextView(requireContext());
                    String labelText = activity.loadLabel(pm).toString();
                    detailsView.setText("   Label: " + labelText + "\n   " + statusText);
                    detailsView.setTextSize(10);
                    detailsView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                    detailsView.setTypeface(android.graphics.Typeface.MONOSPACE);
                    detailsView.setPadding(0, 4, 0, 0);
                    itemLayout.addView(detailsView);
                    
                    // Divider
                    View divider = new View(requireContext());
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    );
                    dividerParams.setMargins(0, 8, 0, 0);
                    divider.setLayoutParams(dividerParams);
                    divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_stroke));
                    itemLayout.addView(divider);
                    
                    componentsContainer.addView(itemLayout);
                }
                
                componentsCard.setVisibility(View.VISIBLE);
                
                android.widget.Toast.makeText(requireContext(), 
                    "Found " + activities.length + " activities (" + exportedCount + " exported)", 
                    android.widget.Toast.LENGTH_SHORT).show();
                
                Log.i(TAG, "Component scan complete. Total: " + activities.length + ", Exported: " + exportedCount);
            }
        } catch (Exception e) {
            android.widget.Toast.makeText(requireContext(), 
                "Error scanning components: " + e.getMessage(), 
                android.widget.Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error scanning components", e);
        }
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
        if (moduleBanner != null) moduleBanner.setText("com.owasp.security_misconfiguration");
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.security_misconfig_intro);
        // vulnerabilitiesText.setText(R.string.security_misconfig_vulnerabilities);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Security Misconfiguration\nOWASP M10: Extraneous Functionality")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
    }
}
