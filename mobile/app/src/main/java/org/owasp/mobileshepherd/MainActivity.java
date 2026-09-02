package org.owasp.mobileshepherd;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import org.owasp.mobileshepherd.databinding.ActivityMainBinding;
import org.owasp.mobileshepherd.utils.AuthManager;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private ProgressTracker progressTracker;
    private NavigationAdapter navigationAdapter;
    private static final Map<Integer, FlagValidator.Module> NAV_TO_MODULE_MAP = new HashMap<>();
    
    static {
        // Lessons
        NAV_TO_MODULE_MAP.put(R.id.nav_lesson, FlagValidator.Module.RE_LESSON);
        NAV_TO_MODULE_MAP.put(R.id.nav_insecure_data_lesson, FlagValidator.Module.IDS_LESSON);
        NAV_TO_MODULE_MAP.put(R.id.nav_poor_auth_lesson, FlagValidator.Module.POOR_AUTH_LESSON);
        NAV_TO_MODULE_MAP.put(R.id.nav_insecure_authorization_lesson, FlagValidator.Module.INSECURE_AUTH_LESSON);
        NAV_TO_MODULE_MAP.put(R.id.nav_supply_chain_lesson, FlagValidator.Module.SUPPLY_CHAIN_LESSON);
        NAV_TO_MODULE_MAP.put(R.id.nav_insecure_comm_lesson, FlagValidator.Module.INSECURE_COMM_LESSON);
        NAV_TO_MODULE_MAP.put(R.id.nav_insufficient_crypto_lesson, FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON);
        NAV_TO_MODULE_MAP.put(R.id.nav_security_misconfig_lesson, FlagValidator.Module.SECURITY_MISCONFIG_LESSON);
        NAV_TO_MODULE_MAP.put(R.id.nav_input_validation_lesson, FlagValidator.Module.INPUT_VALIDATION_LESSON);
        NAV_TO_MODULE_MAP.put(R.id.nav_privacy_lesson, FlagValidator.Module.PRIVACY_LESSON);
        NAV_TO_MODULE_MAP.put(R.id.nav_client_side_injection_lesson, FlagValidator.Module.CLIENT_SIDE_INJECTION_LESSON);
        
        // Challenges
        NAV_TO_MODULE_MAP.put(R.id.nav_challenge1, FlagValidator.Module.RE_CHALLENGE_1);
        NAV_TO_MODULE_MAP.put(R.id.nav_insecure_data1, FlagValidator.Module.IDS_CHALLENGE_1);
        NAV_TO_MODULE_MAP.put(R.id.nav_poor_auth_challenge, FlagValidator.Module.POOR_AUTH_CHALLENGE);
        NAV_TO_MODULE_MAP.put(R.id.nav_insecure_comm_challenge, FlagValidator.Module.INSECURE_COMM_CHALLENGE);
        NAV_TO_MODULE_MAP.put(R.id.nav_insufficient_crypto_challenge, FlagValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE);
        NAV_TO_MODULE_MAP.put(R.id.nav_security_misconfig_challenge2, FlagValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2);
        NAV_TO_MODULE_MAP.put(R.id.nav_client_side_injection_challenge1, FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1);
        NAV_TO_MODULE_MAP.put(R.id.nav_client_side_injection_challenge2, FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before calling super.onCreate
        applyTheme();
        
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        
        // Add scale animation to FAB on click
        binding.appBarMain.fab.setScaleX(1f);
        binding.appBarMain.fab.setScaleY(1f);
        
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Animate FAB scale on click
                view.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                    })
                    .start();
                    
                //create alert dialogue for floating "help" action button.
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                //set the message
                String message = "Mobile Shepherd - Security Training Platform\n\n" +
                        "This application is designed to teach mobile application security through " +
                        "hands-on lessons and challenges based on the OWASP Mobile Top 10.\n\n" +
                        "Part of the OWASP Security Shepherd project.";

                builder.setMessage(message);
                builder.setTitle("About Mobile Shepherd");
                builder.setCancelable(true);
                
                builder.setPositiveButton("OWASP Mobile Top 10", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                            android.net.Uri.parse("https://owasp.org/www-project-mobile-top-10/"));
                        startActivity(browserIntent);
                    }
                });
                
                builder.setNeutralButton("Security Shepherd", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                            android.net.Uri.parse("https://owasp.org/www-project-security-shepherd/"));
                        startActivity(browserIntent);
                    }
                });
                
                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        DrawerLayout drawer = binding.drawerLayout;

        // Wire up nav header auth UI (header is included directly in nav_drawer_layout)
        TextView authStatus = findViewById(R.id.nav_header_auth_status);
        Button authButton = findViewById(R.id.nav_header_auth_button);
        updateNavHeader(authStatus, authButton);
        authButton.setOnClickListener(v -> {
            if (AuthManager.isAuthenticated(this)) {
                AuthManager.logout(this);
                updateNavHeader(authStatus, authButton);
                refreshNavigation();
                Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
            } else {
                showAuthDialog(authStatus, authButton);
            }
        });
        // Refresh header when drawer opens
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                updateNavHeader(authStatus, authButton);
            }
        });

        // Initialize ProgressTracker
        progressTracker = new ProgressTracker(this);
        
        // Set up completion change listener to refresh navigation
        ProgressTracker.setGlobalCompletionListener(() -> {
            runOnUiThread(() -> refreshNavigation());
        });
        
        // Setup RecyclerView for navigation
        RecyclerView navRecyclerView = findViewById(R.id.nav_recycler_view);
        navRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        List<NavigationItem> navigationItems = createNavigationItems();
        navigationAdapter = new NavigationAdapter(navigationItems, item -> {
            // Handle navigation item click
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(item.getNavigationId());
            drawer.closeDrawers();
        }, progressTracker, NAV_TO_MODULE_MAP);
        navRecyclerView.setAdapter(navigationAdapter);
        
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_lesson, R.id.nav_insecure_data_lesson, R.id.nav_poor_auth_lesson, R.id.nav_insecure_authorization_lesson, R.id.nav_supply_chain_lesson, R.id.nav_insecure_comm_lesson, R.id.nav_insufficient_crypto_lesson, R.id.nav_security_misconfig_lesson,
                R.id.nav_challenge1,
            R.id.nav_insecure_data1,
                R.id.nav_poor_auth_challenge, R.id.nav_insecure_comm_challenge, R.id.nav_insufficient_crypto_challenge,
                R.id.nav_security_misconfig_challenge2,
                R.id.nav_input_validation_lesson,
                R.id.nav_privacy_lesson,
                R.id.nav_client_side_injection_lesson, R.id.nav_client_side_injection_challenge1, R.id.nav_client_side_injection_challenge2,
                R.id.nav_adb_reference, R.id.nav_scoreboard
        ).setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        // Hide FAB on the Home screen; show it on all other destinations
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.nav_home) {
                binding.appBarMain.fab.hide();
            } else {
                binding.appBarMain.fab.show();
            }
        });

        handleDeepLinkIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLinkIntent(intent);
    }

    /**
     * Input Validation Lesson: real external deep link entry point.
     * Routes myapp://open?url=... intents straight to the lesson so it can be
     * triggered via adb shell am start, not just typed into the in-app field.
     */
    private void handleDeepLinkIntent(Intent intent) {
        if (intent == null) return;
        Uri data = intent.getData();
        if (data == null) return;
        if ("myapp".equals(data.getScheme()) && "open".equals(data.getHost())) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_input_validation_lesson);
        }
    }

    private List<NavigationItem> createNavigationItems() {
        List<NavigationItem> items = new ArrayList<>();
        
        // Home
        items.add(new NavigationItem(1, "Home", R.drawable.ic_menu_home, R.id.nav_home));
        
        // Lessons group
        NavigationItem lessonsGroup = new NavigationItem(2, "Lessons", R.drawable.ic_menu_camera);
        addChildIfNotCompleted(lessonsGroup, new NavigationItem(21, "Reverse Engineering", 0, R.id.nav_lesson));
        addChildIfNotCompleted(lessonsGroup, new NavigationItem(22, "Insecure Data Storage", 0, R.id.nav_insecure_data_lesson));
        addChildIfNotCompleted(lessonsGroup, new NavigationItem(23, "Poor Authentication", 0, R.id.nav_poor_auth_lesson));
        addChildIfNotCompleted(lessonsGroup, new NavigationItem(24, "Insecure Authorization", 0, R.id.nav_insecure_authorization_lesson));
        addChildIfNotCompleted(lessonsGroup, new NavigationItem(25, "Supply Chain Security", 0, R.id.nav_supply_chain_lesson));
        addChildIfNotCompleted(lessonsGroup, new NavigationItem(26, "Insecure Communication", 0, R.id.nav_insecure_comm_lesson));
        addChildIfNotCompleted(lessonsGroup, new NavigationItem(27, "Insufficient Cryptography", 0, R.id.nav_insufficient_crypto_lesson));
        addChildIfNotCompleted(lessonsGroup, new NavigationItem(28, "Security Misconfiguration", 0, R.id.nav_security_misconfig_lesson));
        addChildIfNotCompleted(lessonsGroup, new NavigationItem(29, "Input Validation", 0, R.id.nav_input_validation_lesson));
        addChildIfNotCompleted(lessonsGroup, new NavigationItem(30, "Privacy Controls", 0, R.id.nav_privacy_lesson));
        addChildIfNotCompleted(lessonsGroup, new NavigationItem(31, "Client-Side Injection", 0, R.id.nav_client_side_injection_lesson));
        if (lessonsGroup.getChildren().size() > 0) items.add(lessonsGroup);
        
        // Challenges group
        NavigationItem challengesGroup = new NavigationItem(3, "Challenges", R.drawable.ic_menu_code);
        
        // Reverse Engineering sub-group
        NavigationItem reverseEngGroup = new NavigationItem(40, "Reverse Engineering", 0);
        addChildIfNotCompleted(reverseEngGroup, new NavigationItem(41, "Reverse Engineering 1", 0, R.id.nav_challenge1));
        if (reverseEngGroup.getChildren().size() > 0) challengesGroup.addChild(reverseEngGroup);
        
        // Insecure Data Storage sub-group
        NavigationItem insecureDataGroup = new NavigationItem(44, "Insecure Data Storage", 0);
        addChildIfNotCompleted(insecureDataGroup, new NavigationItem(45, "Insecure Data Storage 1", 0, R.id.nav_insecure_data1));
        if (insecureDataGroup.getChildren().size() > 0) challengesGroup.addChild(insecureDataGroup);
        
        // Individual challenges
        addChildIfNotCompleted(challengesGroup, new NavigationItem(47, "Poor Authentication", 0, R.id.nav_poor_auth_challenge));
        addChildIfNotCompleted(challengesGroup, new NavigationItem(49, "Insecure Communication", 0, R.id.nav_insecure_comm_challenge));
        addChildIfNotCompleted(challengesGroup, new NavigationItem(50, "Insufficient Cryptography", 0, R.id.nav_insufficient_crypto_challenge));
        
        // Security Misconfiguration sub-group
        NavigationItem securityMisconfigGroup = new NavigationItem(51, "Security Misconfiguration", 0);
        addChildIfNotCompleted(securityMisconfigGroup, new NavigationItem(52, "Security Misconfiguration 1", 0, R.id.nav_security_misconfig_challenge2));
        if (securityMisconfigGroup.getChildren().size() > 0) challengesGroup.addChild(securityMisconfigGroup);
        
        // Client-Side Injection sub-group
        NavigationItem clientSideGroup = new NavigationItem(55, "Client-Side Injection", 0);
        addChildIfNotCompleted(clientSideGroup, new NavigationItem(56, "Client-Side Injection 1", 0, R.id.nav_client_side_injection_challenge1));
        addChildIfNotCompleted(clientSideGroup, new NavigationItem(57, "Client-Side Injection 2", 0, R.id.nav_client_side_injection_challenge2));
        if (clientSideGroup.getChildren().size() > 0) challengesGroup.addChild(clientSideGroup);
        
        if (challengesGroup.getChildren().size() > 0) items.add(challengesGroup);
        
        // ADB Reference
        items.add(new NavigationItem(4, "ADB Reference", R.drawable.ic_menu_code, R.id.nav_adb_reference));

        // Scoreboard — only shown when signed in to a server
        if (AuthManager.isAuthenticated(this)) {
            items.add(new NavigationItem(6, "Scoreboard", R.drawable.ic_menu_home, R.id.nav_scoreboard));
        }
        
        // Completed group - show all completed lessons and challenges
        NavigationItem completedGroup = new NavigationItem(5, "Completed", R.drawable.ic_menu_camera);
        boolean hasCompleted = false;
        
        for (Map.Entry<Integer, FlagValidator.Module> entry : NAV_TO_MODULE_MAP.entrySet()) {
            if (progressTracker.isCompleted(entry.getValue())) {
                hasCompleted = true;
                String title = getTitleForNavId(entry.getKey());
                completedGroup.addChild(new NavigationItem(1000 + entry.getKey(), title, 0, entry.getKey()));
            }
        }
        
        if (hasCompleted) {
            items.add(completedGroup);
        }
        
        return items;
    }
    
    private String getTitleForNavId(int navId) {
        // Map navigation IDs to readable titles
        if (navId == R.id.nav_lesson) return "Reverse Engineering";
        if (navId == R.id.nav_insecure_data_lesson) return "Insecure Data Storage";
        if (navId == R.id.nav_poor_auth_lesson) return "Poor Authentication";
        if (navId == R.id.nav_insecure_authorization_lesson) return "Insecure Authorization";
        if (navId == R.id.nav_supply_chain_lesson) return "Supply Chain Security";
        if (navId == R.id.nav_insecure_comm_lesson) return "Insecure Communication";
        if (navId == R.id.nav_insufficient_crypto_lesson) return "Insufficient Cryptography";
        if (navId == R.id.nav_security_misconfig_lesson) return "Security Misconfiguration";
        if (navId == R.id.nav_input_validation_lesson) return "Input Validation";
        if (navId == R.id.nav_privacy_lesson) return "Privacy Controls";
        if (navId == R.id.nav_client_side_injection_lesson) return "Client-Side Injection";
        if (navId == R.id.nav_challenge1) return "Reverse Engineering 1";
        if (navId == R.id.nav_insecure_data1) return "Insecure Data Storage 1";
        if (navId == R.id.nav_poor_auth_challenge) return "Poor Auth Challenge";
        if (navId == R.id.nav_insecure_comm_challenge) return "Insecure Comm Challenge";
        if (navId == R.id.nav_insufficient_crypto_challenge) return "Crypto Challenge";
        if (navId == R.id.nav_security_misconfig_challenge2) return "Security Misconfiguration 1";
        if (navId == R.id.nav_client_side_injection_challenge1) return "Client-Side Injection 1";
        if (navId == R.id.nav_client_side_injection_challenge2) return "Client-Side Injection 2";
        return "Module";
    }
    
    private void addChildIfNotCompleted(NavigationItem parent, NavigationItem child) {
        // Only add the child if it's not completed
        if (child.getNavigationId() != 0) {
            FlagValidator.Module module = NAV_TO_MODULE_MAP.get(child.getNavigationId());
            if (module != null && progressTracker.isCompleted(module)) {
                return; // Skip completed items
            }
        }
        parent.addChild(child);
    }
    
    public void refreshNavigation() {
        List<NavigationItem> navigationItems = createNavigationItems();
        navigationAdapter.updateItems(navigationItems);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_adb_reference: {
                    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
                    navController.navigate(R.id.nav_adb_reference);
                }
                return true;
            case R.id.action_settings: {
                    Intent goToSettings = new Intent(this, Preferences.class);
                    startActivity(goToSettings);
                Toast.makeText(this, "Settings Selected", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_exit:
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void updateNavHeader(TextView statusView, Button button) {
        if (AuthManager.isAuthenticated(this)) {
            String username = AuthManager.getUsername(this);
            statusView.setText(getString(R.string.auth_status_online, username));
            button.setText(R.string.auth_button_sign_out);
        } else {
            statusView.setText(R.string.auth_status_offline);
            button.setText(R.string.auth_button_sign_in);
        }
    }

    /** Called by HomeFragment to open the sign-in dialog from the home screen card. */
    public void openAuthDialog() {
        TextView authStatus = findViewById(R.id.nav_header_auth_status);
        Button authButton = findViewById(R.id.nav_header_auth_button);
        showAuthDialog(authStatus, authButton);
    }

    private void showAuthDialog(TextView statusView, Button button) {
        final boolean[] isRegisterMode = {false};

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_auth, null);
        EditText serverField  = dialogView.findViewById(R.id.auth_server_url);
        EditText usernameField = dialogView.findViewById(R.id.auth_username);
        EditText passwordField = dialogView.findViewById(R.id.auth_password);
        EditText emailField   = dialogView.findViewById(R.id.auth_email);
        View emailLabel       = dialogView.findViewById(R.id.auth_email_label);
        TextView statusText   = dialogView.findViewById(R.id.auth_status_text);

        // Pre-fill saved server URL
        String savedServer = AuthManager.getServerUrl(this);
        if (!savedServer.isEmpty()) serverField.setText(savedServer);
        String savedUser = AuthManager.getUsername(this);
        if (!savedUser.isEmpty()) usernameField.setText(savedUser);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.auth_dialog_title_login)
                .setView(dialogView)
                .setPositiveButton(R.string.auth_button_sign_in, null) // overridden below
                .setNeutralButton(R.string.auth_switch_to_register, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button neutral  = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            positive.setOnClickListener(v -> {
                String serverUrl = serverField.getText().toString().trim();
                String username  = usernameField.getText().toString().trim();
                String password  = passwordField.getText().toString();
                String email     = emailField.getText().toString().trim();

                if (TextUtils.isEmpty(serverUrl) || TextUtils.isEmpty(username)
                        || TextUtils.isEmpty(password)) {
                    statusText.setText("Please fill in all required fields");
                    statusText.setVisibility(View.VISIBLE);
                    return;
                }

                positive.setEnabled(false);
                neutral.setEnabled(false);
                statusText.setText("Connecting...");
                statusText.setVisibility(View.VISIBLE);

                AuthManager.AuthCallback callback = (success, message) -> {
                    if (success) {
                        updateNavHeader(statusView, button);
                        refreshNavigation();
                        dialog.dismiss();
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    } else {
                        positive.setEnabled(true);
                        neutral.setEnabled(true);
                        statusText.setText(message);
                    }
                };

                if (isRegisterMode[0]) {
                    AuthManager.register(this, serverUrl, username, password, email, callback);
                } else {
                    AuthManager.login(this, serverUrl, username, password, callback);
                }
            });

            neutral.setOnClickListener(v -> {
                isRegisterMode[0] = !isRegisterMode[0];
                if (isRegisterMode[0]) {
                    dialog.setTitle(getString(R.string.auth_dialog_title_register));
                    positive.setText(R.string.auth_dialog_title_register);
                    neutral.setText(R.string.auth_switch_to_login);
                    emailField.setVisibility(View.VISIBLE);
                    emailLabel.setVisibility(View.VISIBLE);
                } else {
                    dialog.setTitle(getString(R.string.auth_dialog_title_login));
                    positive.setText(R.string.auth_button_sign_in);
                    neutral.setText(R.string.auth_switch_to_register);
                    emailField.setVisibility(View.GONE);
                    emailLabel.setVisibility(View.GONE);
                }
                statusText.setVisibility(View.GONE);
            });
        });

        dialog.show();
    }

    private void applyTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String themeValue = preferences.getString("theme_preference", "system");
        
        switch (themeValue) {
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "system":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}