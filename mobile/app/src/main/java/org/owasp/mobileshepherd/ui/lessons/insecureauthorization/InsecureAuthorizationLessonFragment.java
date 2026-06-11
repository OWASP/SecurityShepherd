package org.owasp.mobileshepherd.ui.lessons.insecureauthorization;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentInsecureAuthorizationLessonBinding;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class InsecureAuthorizationLessonFragment extends Fragment {

    private FragmentInsecureAuthorizationLessonBinding binding;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "UserSession";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    private String currentFlag = "";
    
    // Demo credentials
    private static final String DEMO_USERNAME = "testuser";
    private static final String DEMO_PASSWORD = "password123";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInsecureAuthorizationLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        progressTracker = new ProgressTracker(requireContext());
        FlagProvider.getFlag(
                requireContext(),
                FlagValidator.Module.INSECURE_AUTH_LESSON,
                flagValue -> currentFlag = flagValue);

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.INSECURE_AUTH_LESSON));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }


        // Check if already logged in
        if (isLoggedIn()) {
            showDashboard();
        } else {
            showLoginForm();
        }

        // Log the vulnerability hint
        Log.d("AuthVulnerability", "Authorization check uses client-side role from SharedPreferences");
        Log.d("AuthVulnerability", "Role key: 'user_role' - Values: 'user' or 'admin'");

        return root;
    }

    private void showLoginForm() {
        binding.loginCard.setVisibility(View.VISIBLE);
        binding.dashboardCard.setVisibility(View.GONE);

        Button loginButton = binding.loginButton;
        EditText usernameInput = binding.usernameInput;
        EditText passwordInput = binding.passwordInput;

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.equals(DEMO_USERNAME) && password.equals(DEMO_PASSWORD)) {
                // Successful login - store session with basic user role
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("is_logged_in", true);
                editor.putString("username", username);
                editor.putString("user_role", "user"); // Insecure: stored client-side!
                editor.putLong("login_timestamp", System.currentTimeMillis());
                editor.apply();

                Log.i("Authorization", "User logged in: " + username + " with role: user");
                Log.d("Authorization", "Session stored in SharedPreferences: " + 
                      requireContext().getApplicationInfo().dataDir + "/shared_prefs/UserSession.xml");

                Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                showDashboard();
            } else {
                Toast.makeText(getContext(), "Invalid credentials. Try: testuser / password123", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDashboard() {
        binding.loginCard.setVisibility(View.GONE);
        binding.dashboardCard.setVisibility(View.VISIBLE);

        String username = prefs.getString("username", "User");
        String role = prefs.getString("user_role", "user");

        binding.welcomeText.setText("Welcome, " + username + "!");
        binding.roleText.setText("Current Role: " + role);

        Log.d("Authorization", "Dashboard loaded for user: " + username + " (role: " + role + ")");

        // Access Admin Panel button
        binding.adminPanelButton.setOnClickListener(v -> {
            accessAdminPanel();
        });

        // Logout button
        binding.logoutButton.setOnClickListener(v -> {
            logout();
        });
    }

    private void accessAdminPanel() {
        String role = prefs.getString("user_role", "user");
        
        Log.d("Authorization", "Admin panel access attempt - Current role: " + role);

        // INSECURE: Authorization check relies on client-controlled value
        if ("admin".equals(role)) {
            // Admin access granted
            String flag = currentFlag;
            binding.adminContentCard.setVisibility(View.VISIBLE);
            binding.flagText.setText("Admin Flag: " + flag);
            binding.accessDeniedText.setVisibility(View.GONE);
            
            Toast.makeText(getContext(), "Admin access granted! Flag revealed!", Toast.LENGTH_LONG).show();
            Log.i("Authorization", "ADMIN ACCESS GRANTED - Flag revealed");

            FlagValidator.validateFlag(requireContext(), FlagValidator.Module.INSECURE_AUTH_LESSON,
                    flag, correct -> {
                        if (!isAdded()) return;
                        if (correct) {
                            progressTracker.markCompleted(FlagValidator.Module.INSECURE_AUTH_LESSON);
                        }
                    });
        } else {
            // Access denied
            binding.adminContentCard.setVisibility(View.VISIBLE);
            binding.flagText.setText("");
            binding.accessDeniedText.setVisibility(View.VISIBLE);
            
            Toast.makeText(getContext(), "Access Denied: Admin privileges required", Toast.LENGTH_SHORT).show();
            Log.w("Authorization", "Admin access DENIED for role: " + role);
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
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.insecure_authorization_intro);
        // vulnerabilitiesText.setText("• Client-side authorization stored in SharedPreferences\n• Privilege escalation by modifying local role data\n• No server-side validation of permissions");
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Insecure Authorization\nOWASP M3: Insecure Authentication/Authorization")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
    
    private boolean isLoggedIn() {
        return prefs.getBoolean("is_logged_in", false);
    }

    private void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        showLoginForm();
        binding.adminContentCard.setVisibility(View.GONE);
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
