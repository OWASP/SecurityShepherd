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
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class ClientSideInjectionChallenge1Fragment extends Fragment {

    private FragmentClientSideInjectionChallenge1Binding binding;
    private ClientSideInjectionChallenge1Model viewModel;
    private Challenge1DatabaseHelper dbHelper;
    private static final String TAG = "CSI_Challenge1";
    private ProgressTracker progressTracker;
    private String currentKey = "";
    private String dbKey = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClientSideInjectionChallenge1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(ClientSideInjectionChallenge1Model.class);
        progressTracker = new ProgressTracker(requireContext());
        dbHelper = new Challenge1DatabaseHelper(requireContext());

        KeyProvider.getKey(requireContext(),
                KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1, key -> {
                    if (!isAdded()) return;
                    currentKey = key;
                    dbKey = key;
                    seedDatabase(key);
                });

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(
                    requireContext(), KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1));
        }

        binding.loginButton.setOnClickListener(v -> attemptLogin());

        return root;
    }

    private void seedDatabase(String keyValue) {
        SQLiteDatabase db;
        try {
            db = dbHelper.getWritableDatabase(dbKey);
        } catch (android.database.sqlite.SQLiteException e) {
            // Key changed (e.g. offline local key vs server-issued per-user key); data is
            // regenerated on every open, so recreate the file with the new key.
            Log.w(TAG, "DB key mismatch, recreating database", e);
            requireContext().deleteDatabase(Challenge1DatabaseHelper.DATABASE_NAME);
            db = dbHelper.getWritableDatabase(dbKey);
        }
        db.execSQL("DELETE FROM accounts");
        insertAccount(db, "admin",   keyValue,     "admin",  9999);
        insertAccount(db, "alice",   "alicePass99", "user",   100);
        insertAccount(db, "bob",     "B0bR0cks!",   "user",   50);
        insertAccount(db, "charlie", "charlie123",  "user",   25);
        db.close();
        Log.d(TAG, "Accounts DB seeded. admin password = key.");
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
        // reaches the query. Replacing the surrounding spaces with an inline SQL
        // comment (e.g. OR written as /**/OR/**/), and the raw quote character itself,
        // slip past untouched.
        return filtered.equals(upper) ? input : filtered;
    }

    private void attemptLogin() {
        String rawUser = binding.usernameInput.getText() != null
                ? binding.usernameInput.getText().toString() : "";
        String rawPass = binding.passwordInput.getText() != null
                ? binding.passwordInput.getText().toString() : "";

        if (rawUser.isEmpty() || rawPass.isEmpty()) {
            Toast.makeText(getContext(), R.string.csi_c1_enter_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        // Apply the sloppy filter (bypassable via inline SQL comments in place of the
        // spaces around a keyword, or the unfiltered quote character itself)
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
                    // Admin login: reveal the key and submit it automatically
                    binding.resultText.setText(
                            getString(R.string.csi_c1_access_granted, currentKey));
                    binding.resultText.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.success_green));
                    submitKeyToServer(currentKey);
                    binding.headerCard.setVisibility(View.GONE);
                } else {
                    binding.resultText.setText(
                            getString(R.string.csi_c1_logged_in, loggedInUser, role, balance));
                    binding.resultText.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.colorPrimary));
                }
            } else {
                Log.d(TAG, "Login failed for input: " + rawUser);
                binding.resultText.setText(R.string.csi_c1_login_failed);
                binding.resultText.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.error_red));
            }
        } catch (Exception e) {
            Log.e(TAG, "Query error: " + e.getMessage());
            binding.resultText.setText(getString(R.string.csi_c1_query_error, e.getMessage()));
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    /**
     * Submits the discovered key to the Shepherd server as soon as the injection
     * reveals it, so the player doesn't need to manually copy it into the key field.
     */
    private void submitKeyToServer(String key) {
        KeyValidator.validateKey(requireContext(),
                KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1,
                key, correct -> {
                    if (!isAdded()) return;
                    if (correct) {
                        progressTracker.markCompleted(KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1);
                        Toast.makeText(getContext(), R.string.csi_c1_complete, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
