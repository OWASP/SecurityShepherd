package org.owasp.mobileshepherd.ui.challenges.reverseengineering;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentReverseEngineering1Binding;
import org.owasp.mobileshepherd.ui.challenges.reverseengineering.ReverseEngineering1Model;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class ReverseEngineering1Fragment extends Fragment {

    private FragmentReverseEngineering1Binding binding;
    private ReverseEngineering1Model challengeModel;
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        challengeModel = new ViewModelProvider(this).get(ReverseEngineering1Model.class);

        binding = FragmentReverseEngineering1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.RE_CHALLENGE_1));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showVulnerabilityInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }


        // Set initial FAB appearance based on completion status

        final EditText inputFlag = binding.inputFlag;
        final Button btnValidate = binding.btnValidate;
        final Button btnClear = binding.btnClear;
        final TextView textResult = binding.textResult;

        btnValidate.setOnClickListener(v -> {
            String userInput = inputFlag.getText().toString().trim();
            if (challengeModel.validateFlag(userInput)) {
                progressTracker.markCompleted(FlagValidator.Module.RE_CHALLENGE_1);
                int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.RE_CHALLENGE_1);
                String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";
                
                textResult.setText(R.string.reverse_engineering_1_success);
                textResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green));
                textResult.setVisibility(View.VISIBLE);
                
                new AlertDialog.Builder(requireContext())
                    .setTitle(" Success!")
                    .setMessage("Congratulations! You found the flag.\n\nFlag: " + userInput + completionText)
                    .setPositiveButton("OK", null)
                    .show();
            } else {
                textResult.setText(R.string.reverse_engineering_1_failure);
                textResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.security_red));
                textResult.setVisibility(View.VISIBLE);
            }
        });

        btnClear.setOnClickListener(v -> {
            inputFlag.setText("");
            textResult.setVisibility(View.GONE);
        });

        return root;
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


    private void showVulnerabilityInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.lesson_intro);
        // vulnerabilitiesText.setText(R.string.lesson_tools);
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Reverse Engineering Challenge 1\nOWASP M9: Reverse Engineering")
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
        binding = null;
    }
}