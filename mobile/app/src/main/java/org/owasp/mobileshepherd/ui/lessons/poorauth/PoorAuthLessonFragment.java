package org.owasp.mobileshepherd.ui.lessons.poorauth;

import android.content.Intent;
import android.net.Uri;
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
import org.owasp.mobileshepherd.databinding.FragmentPoorAuthLessonBinding;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PoorAuthLessonFragment extends Fragment {

    private FragmentPoorAuthLessonBinding binding;
    private static final String TAG = "PoorAuthLesson";
    // SHA-256 hash of "654321"
    private static final String HARDCODED_PIN_HASH = "481f6cc0511143ccdd7e2d1b1b94faf0a700a8b49cd13922a70b5ae28acaa8c5";
    
    // Obfuscated flag - "Taco_Snores_On_A_Couch"
    // XOR encoded with key 0x42, then Base64 encoded, then split
    private static final String[] F = {
        "FiMhLR", "0RLC0w", "JzEdDS", "wdAx", 
        "0BLTch", "Kg", "=="
    };
    private static final byte K = 0x42; // XOR key
    
    private int attemptCount = 0;
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    private String currentFlag = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPoorAuthLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        progressTracker = new ProgressTracker(requireContext());
        FlagProvider.getFlag(
                requireContext(),
                FlagValidator.Module.POOR_AUTH_LESSON,
                flagValue -> currentFlag = flagValue);

        // Setup FAB expansion
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        
        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.POOR_AUTH_LESSON));
        }
        
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }
        

        

        // Set initial FAB appearance based on completion status

        // Log the hardcoded PIN hash (intentionally insecure for demonstration)
        Log.d(TAG, "Initializing authentication system...");
        Log.d(TAG, "PIN hash configured: " + HARDCODED_PIN_HASH);
        Log.d(TAG, "System ready. PIN verification enabled.");

        binding.verifyButton.setOnClickListener(v -> verifyPin());

        return root;
    }

    private void verifyPin() {
        String enteredPin = binding.pinInput.getText().toString().trim();
        attemptCount++;

        if (enteredPin.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        String enteredPinHash = hashPin(enteredPin);

        Log.d(TAG, "PIN verification attempt #" + attemptCount);
        Log.d(TAG, "Entered PIN hash: " + enteredPinHash);
        Log.d(TAG, "Expected PIN hash: " + HARDCODED_PIN_HASH);

        if (enteredPinHash != null && enteredPinHash.equals(HARDCODED_PIN_HASH)) {
            // Successful authentication
            String flag = currentFlag.isEmpty() ? d() : currentFlag;
            
            binding.demoCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success_bg));
            binding.flagText.setText("\u2713 Authentication Successful!\n\nFlag: " + flag);
            binding.flagText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Access Granted! Flag revealed!", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Authentication successful! Flag: " + flag);
            
            progressTracker.markCompleted(FlagValidator.Module.POOR_AUTH_LESSON);
            FlagValidator.validateFlag(requireContext(), FlagValidator.Module.POOR_AUTH_LESSON,
                    flag, correct -> Log.d(TAG, "Server submission result: " + correct));
            
            binding.verifyButton.setEnabled(false);
            binding.pinInput.setEnabled(false);
        } else {
            // Failed authentication
            binding.demoCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_bg));
            binding.flagText.setVisibility(View.GONE);
            
            Toast.makeText(getContext(), "Access Denied! Incorrect PIN", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Authentication failed. Invalid PIN provided.");
            
            binding.pinInput.setText("");
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


    // Decode obfuscated flag - resist static analysis
    private String d() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String p : F) {
                sb.append(p);
            }
            
            byte[] decoded = Base64.getDecoder().decode(sb.toString());
            
            byte[] result = new byte[decoded.length];
            for (int i = 0; i < decoded.length; i++) {
                result[i] = (byte) (decoded[i] ^ K);
            }
            
            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Flag decode error", e);
            return "[DECODE_ERROR]";
        }
    }

    private String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing PIN", e);
            return null;
        }
    }

    private void showDetailedInfo() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lesson_info, null);
        TextView moduleBanner = dialogView.findViewById(R.id.module_path_banner);
        if (moduleBanner != null) moduleBanner.setVisibility(View.VISIBLE);
        if (moduleBanner != null) moduleBanner.setText("com.owasp.poor_authentication");
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(getString(R.string.poor_auth_intro));
        // vulnerabilitiesText.setText(getString(R.string.poor_auth_vulnerabilities));
        
        hintsSection.setVisibility(View.GONE);
        
        additionalSection.setVisibility(View.GONE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Poor Authentication\nOWASP M3: Insecure Authentication/Authorization");
        builder.setView(dialogView);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Hide mini FABs when leaving fragment
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
        
        binding = null;
    }
}
