package org.owasp.mobileshepherd.ui.lessons.supplychain;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentSupplyChainLessonBinding;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;

public class SupplyChainLessonFragment extends Fragment {

    private FragmentSupplyChainLessonBinding binding;
    private static final String TAG = "SupplyChainLesson";
    
    // Simulated vulnerability in androidx.exifinterface:exifinterface:1.3.7
    // CVE-2024-XXXX: Debug mode exposes sensitive EXIF data processing keys
    private static final String VULNERABLE_LIB = "androidx.exifinterface:exifinterface:1.3.7";
    private static final String EXIF_DEBUG_KEY = "exif_debug_processor_key_1337";
    
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    private String currentFlag = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSupplyChainLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());
        FlagProvider.getFlag(
                requireContext(),
                FlagValidator.Module.SUPPLY_CHAIN_LESSON,
                flagValue -> currentFlag = flagValue);

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.SUPPLY_CHAIN_LESSON));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }


        // Initialize vulnerable Analytics SDK - logs hardcoded API key
        initializeAnalyticsSDK();
        
        // Setup flag submission
        binding.submitButton.setOnClickListener(v -> submitFlag());

        return root;
    }
    
    /**
     * Simulates vulnerability in androidx.exifinterface:exifinterface:1.3.7
     * This version has a debug mode that exposes internal processing keys
     */
    private void initializeAnalyticsSDK() {
        Log.d(TAG, "=================================================");
        Log.d(TAG, "ExifInterface Library Initialization");
        Log.d(TAG, "=================================================");
        Log.d(TAG, "Library: " + VULNERABLE_LIB);
        Log.d(TAG, "Debug Mode: ENABLED");
        Log.d(TAG, "Initializing EXIF data processor...");
        Log.d(TAG, "Loading debug configuration...");
        Log.d(TAG, "Debug processor key: " + EXIF_DEBUG_KEY);
        Log.d(TAG, "EXIF parser ready");
        Log.d(TAG, "Warning: Debug mode should be disabled in production");
        Log.d(TAG, "=================================================");
    }
    
    private void submitFlag() {
        String enteredFlag = binding.flagInput.getText().toString().trim();

        if (enteredFlag.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a flag", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.submitButton.setEnabled(false);

        FlagValidator.validateFlag(
                requireContext(),
                FlagValidator.Module.SUPPLY_CHAIN_LESSON,
                enteredFlag,
                correct -> {
                    if (correct) {
                        binding.submissionCard.setCardBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.success_bg));
                        binding.resultText.setText(
                                "SUCCESS!\n\nFlag: " + enteredFlag + "\n\nYou successfully identified"
                                + " the vulnerable dependency (" + VULNERABLE_LIB + ") which had"
                                + " debug mode enabled, exposing internal processing keys.\n\n"
                                + "OWASP Mobile Top 10 M6: Insufficient Supply Chain Security");
                        binding.resultText.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "Correct flag! Lesson completed!", Toast.LENGTH_LONG).show();
                        progressTracker.markCompleted(FlagValidator.Module.SUPPLY_CHAIN_LESSON);
                        binding.flagInput.setEnabled(false);
                    } else {
                        binding.submitButton.setEnabled(true);
                        binding.submissionCard.setCardBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.error_bg));
                        binding.resultText.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Incorrect flag", Toast.LENGTH_SHORT).show();
                        binding.flagInput.setText("");
                        binding.getRoot().postDelayed(() ->
                                binding.submissionCard.setCardBackgroundColor(
                                        getResources().getColor(R.color.card_bg)), 2000);
                    }
                });
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
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lesson_info, null);
        TextView moduleBanner = dialogView.findViewById(R.id.module_path_banner);
        if (moduleBanner != null) { moduleBanner.setVisibility(View.VISIBLE); moduleBanner.setText("com.owasp.supply_chain"); }
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(getString(R.string.supply_chain_intro));
        // vulnerabilitiesText.setText(getString(R.string.supply_chain_vulnerabilities));
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Supply Chain Security\nOWASP M6: Insufficient Supply Chain Security");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
        binding = null;
    }
}
