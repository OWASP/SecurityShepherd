package org.owasp.mobileshepherd;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.owasp.mobileshepherd.utils.AuthManager;

/**
 * Full-screen login/register screen matching the Security Shepherd web app style.
 *
 * <p>Launched from {@link LandingActivity} (after the warning splash) and from
 * {@link MainActivity} when the user taps "Sign In to Server" on the Home screen.
 * On successful authentication the user proceeds to {@link MainActivity}.
 * The "Continue offline" button skips authentication entirely.
 */
public class LoginActivity extends AppCompatActivity {

    /** Extra key: when {@code true}, the user arrived from inside the app (HomeFragment). */
    public static final String EXTRA_FROM_APP = "from_app";

    private boolean isRegisterMode = false;

    private TextInputEditText serverField;
    private TextInputEditText usernameField;
    private TextInputEditText passwordField;
    private TextInputEditText emailField;
    private TextInputLayout emailLayout;
    private TextView statusText;
    private MaterialButton tabSignIn;
    private MaterialButton tabRegister;
    private MaterialButton submitButton;
    private TextView step3Text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If already authenticated, skip straight to the main app
        if (AuthManager.isAuthenticated(this)) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        serverField   = findViewById(R.id.login_server_url);
        usernameField = findViewById(R.id.login_username);
        passwordField = findViewById(R.id.login_password);
        emailField    = findViewById(R.id.login_email);
        emailLayout   = findViewById(R.id.login_email_layout);
        statusText    = findViewById(R.id.login_status_text);
        tabSignIn     = findViewById(R.id.tab_sign_in);
        tabRegister   = findViewById(R.id.tab_register);
        submitButton  = findViewById(R.id.login_submit_button);
        step3Text     = findViewById(R.id.login_step3_text);

        // Pre-fill saved credentials so returning users don't have to re-enter everything
        String savedServer = AuthManager.getServerUrl(this);
        if (!savedServer.isEmpty()) serverField.setText(savedServer);
        String savedUser = AuthManager.getUsername(this);
        if (!savedUser.isEmpty()) usernameField.setText(savedUser);

        tabSignIn.setOnClickListener(v -> setMode(false));
        tabRegister.setOnClickListener(v -> setMode(true));
        submitButton.setOnClickListener(v -> onSubmit());

        boolean fromApp = getIntent().getBooleanExtra(EXTRA_FROM_APP, false);
        MaterialButton offlineButton = findViewById(R.id.login_offline_button);
        if (fromApp) {
            offlineButton.setText(R.string.cancel);
        }
        offlineButton.setOnClickListener(v -> {
            // Offline mode: go back to the main app (or finish if launched from within it)
            goToMain();
        });

        setMode(false);
    }

    private void setMode(boolean registerMode) {
        isRegisterMode = registerMode;
        if (registerMode) {
            emailLayout.setVisibility(View.VISIBLE);
            submitButton.setText(R.string.auth_dialog_title_register);
            step3Text.setText(R.string.login_step3_register);
        } else {
            emailLayout.setVisibility(View.GONE);
            submitButton.setText(R.string.auth_button_sign_in);
            step3Text.setText(R.string.login_step3_signin);
        }
        statusText.setVisibility(View.GONE);
    }

    private void onSubmit() {
        String serverUrl = serverField.getText() != null ? serverField.getText().toString().trim() : "";
        String username  = usernameField.getText() != null ? usernameField.getText().toString().trim() : "";
        String password  = passwordField.getText() != null ? passwordField.getText().toString() : "";
        String email     = emailField.getText() != null ? emailField.getText().toString().trim() : "";

        if (TextUtils.isEmpty(serverUrl) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showStatus(getString(R.string.login_error_fill_fields), true);
            return;
        }

        submitButton.setEnabled(false);
        tabSignIn.setEnabled(false);
        tabRegister.setEnabled(false);
        showStatus(getString(R.string.login_connecting), false);

        AuthManager.AuthCallback callback = (success, message) -> {
            if (success) {
                goToMain();
            } else {
                submitButton.setEnabled(true);
                tabSignIn.setEnabled(true);
                tabRegister.setEnabled(true);
                showStatus(message, true);
            }
        };

        if (isRegisterMode) {
            AuthManager.register(this, serverUrl, username, password, email, callback);
        } else {
            AuthManager.login(this, serverUrl, username, password, callback);
        }
    }

    private void showStatus(String message, boolean isError) {
        statusText.setText(message);
        statusText.setTextColor(isError
                ? getResources().getColor(android.R.color.holo_red_dark, getTheme())
                : getResources().getColor(android.R.color.darker_gray, getTheme()));
        statusText.setVisibility(View.VISIBLE);
    }

    private void goToMain() {
        boolean fromApp = getIntent().getBooleanExtra(EXTRA_FROM_APP, false);
        if (!fromApp) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
