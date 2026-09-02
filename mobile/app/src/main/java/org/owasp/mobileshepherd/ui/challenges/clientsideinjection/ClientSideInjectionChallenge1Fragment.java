package org.owasp.mobileshepherd.ui.challenges.clientsideinjection;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentClientSideInjectionChallenge1Binding;
import org.owasp.mobileshepherd.ui.challenges.clientsideinjection.helpers.Challenge1DatabaseHelper;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class ClientSideInjectionChallenge1Fragment extends Fragment {

    private FragmentClientSideInjectionChallenge1Binding binding;
    private ClientSideInjectionChallenge1Model viewModel;
    private Challenge1DatabaseHelper dbHelper;
    private static final String TAG = "CSI_Challenge1";
    private ProgressTracker progressTracker;
    private String currentFlag = "";
    private String dbKey = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClientSideInjectionChallenge1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(ClientSideInjectionChallenge1Model.class);
        progressTracker = new ProgressTracker(requireContext());
        dbHelper = new Challenge1DatabaseHelper(requireContext());

        FlagProvider.getFlag(requireContext(),
                FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1, flag -> {
                    if (!isAdded()) return;
                    currentFlag = flag;
                    dbKey = flag;
                    seedDatabase(flag);
                });

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(
                    requireContext(), FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1));
        }

        binding.loginButton.setOnClickListener(v -> attemptLogin());
        binding.submitFlagButton.setOnClickListener(v -> submitFlag());

        return root;
    }

    private void seedDatabase(String flagValue) {
        SQLiteDatabase db = dbHelper.getWritableDatabase(dbKey);
        db.execSQL("DELETE FROM accounts");
        insertAccount(db, "admin",   flagValue,     "admin",  9999);
        insertAccount(db, "alice",   "alicePass99", "user",   100);
        insertAccount(db, "bob",     "B0bR0cks!",   "user",   50);
        insertAccount(db, "charlie", "charlie123",  "user",   25);
        db.close();
        Log.d(TAG, "Accounts DB seeded. admin password = flag.");
    }

    private void insertAccount(SQLiteDatabase db, String user, String pass, String role, int balance) {
        ContentValues cv = new ContentValues();
        cv.put("username", user);
        cv.put("password", pass);
        cv.put("role", role);
        cv.put("balance", balance);
        db.insert("accounts", null, cv);
    }

    private String applySloppyFilter(String input) {
        String upper = input.toUpperCase();
        String filtered = upper
                .replace("SELECT", ".")
                .replace("WHERE",  ".")
                .replace("FROM",   ".")
                .replace("UNION",  ".")
                .replace("INSERT", ".")
                .replace("UPDATE", ".")
                .replace("DELETE", ".")
                .replace("DROP",   ".")
                .replace(" OR ",   " . ")
                .replace(" AND ",  " . ")
                .replace("--",     ".");
        // Keywords are matched case-insensitively (via the uppercase copy above), but
        // only exact single-space/keyword matches are stripped. If nothing matched,
        // pass the original input through unchanged instead of the uppercased copy -
        // otherwise every legitimate credential would be case-mangled before it ever
        // reaches the query. Non-standard spacing (double space, tabs) around a
        // keyword, and the raw quote character itself, still slip past untouched.
        return filtered.equals(upper) ? input : filtered;
    }

    private void attemptLogin() {
        String rawUser = binding.usernameInput.getText() != null
                ? binding.usernameInput.getText().toString() : "";
        String rawPass = binding.passwordInput.getText() != null
                ? binding.passwordInput.getText().toString() : "";

        if (rawUser.isEmpty() || rawPass.isEmpty()) {
            Toast.makeText(getContext(), "Enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Apply the sloppy filter (bypassable via non-standard whitespace around a
        // keyword, or the unfiltered quote character itself)
        String filteredUser = applySloppyFilter(rawUser);
        String filteredPass = applySloppyFilter(rawPass);

        // VULNERABLE: user input concatenated directly into query
        String query = "SELECT username, role, balance FROM accounts WHERE username = '"
                + filteredUser + "' AND password = '" + filteredPass + "'";

        Log.d(TAG, "Executing query: " + query);

        SQLiteDatabase db = dbHelper.getReadableDatabase(dbKey);
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                String loggedInUser = cursor.getString(0);
                String role         = cursor.getString(1);
                int    balance      = cursor.getInt(2);

                Log.d(TAG, "Login success: user=" + loggedInUser + " role=" + role);

                if ("admin".equals(loggedInUser)) {
                    // Admin login â€” reveal the flag
                    binding.resultText.setText(
                            "ACCESS GRANTED â€” Admin\n\nFlag: " + currentFlag
                            + "\n\nYou bypassed the filter using SQL injection!");
                    binding.resultText.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.success_green));
                } else {
                    binding.resultText.setText(
                            "Logged in as: " + loggedInUser
                            + "\nRole: " + role + "  Balance: $" + balance
                            + "\n\nNot admin â€” try to inject as admin.");
                    binding.resultText.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.colorPrimary));
                }
            } else {
                Log.d(TAG, "Login failed for input: " + rawUser);
                binding.resultText.setText("Login failed.");
                binding.resultText.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.error_red));
            }
        } catch (Exception e) {
            Log.e(TAG, "Query error: " + e.getMessage());
            binding.resultText.setText("Query error: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    private void submitFlag() {
        String entered = binding.flagInput.getText() != null
                ? binding.flagInput.getText().toString().trim() : "";
        if (entered.isEmpty()) {
            Toast.makeText(getContext(), "Enter the flag", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.submitFlagButton.setEnabled(false);
        FlagValidator.validateFlag(requireContext(),
                FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1,
                entered, correct -> {
                    if (correct) {
                        progressTracker.markCompleted(FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1);
                        Toast.makeText(getContext(), "Correct! Challenge complete.", Toast.LENGTH_LONG).show();
                    } else {
                        binding.submitFlagButton.setEnabled(true);
                        Toast.makeText(getContext(), "Incorrect flag.", Toast.LENGTH_SHORT).show();
                        binding.flagInput.setText("");
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
