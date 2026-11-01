package org.owasp.mobileshepherd.ui.challenges.insecuredata1;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentInsecureData1Binding;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class InsecureData1Fragment extends Fragment {

    private FragmentInsecureData1Binding binding;
    private SQLiteDatabase passwordDB = null;
    private String currentKey = "";
    private ProgressTracker progressTracker;
    // Deliberately weak, unsalted password - tops common breach wordlists (e.g. rockyou.txt).
    private static final String ADMIN_PASSWORD = "Password1";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInsecureData1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());

        // Setup FAB for vulnerability information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.IDS_CHALLENGE_1));
        }

        // Create the vulnerable database, then seed it once the server key is available
        createDatabase();
        KeyProvider.getKey(requireContext(), KeyValidator.Module.IDS_CHALLENGE_1, keyValue -> {
            if (!isAdded()) return;
            currentKey = keyValue;
            insertUsers();
            displayUsers();
        });

        // The player recovers the plaintext from the extracted database's hash and enters it here
        binding.validateButton.setOnClickListener(v -> {
            String enteredPassword = binding.keyInput.getText().toString().trim();
            if (md5(enteredPassword).equals(md5(ADMIN_PASSWORD))) {
                binding.headerCard.setVisibility(View.GONE);
                KeyValidator.validateKey(requireContext(), KeyValidator.Module.IDS_CHALLENGE_1,
                        currentKey, isValid -> {
                    if (!isAdded()) return;
                    if (isValid) {
                        progressTracker.markCompleted(KeyValidator.Module.IDS_CHALLENGE_1);
                        int completionCount = progressTracker.getCompletionCount(KeyValidator.Module.IDS_CHALLENGE_1);
                        String completionText = completionCount > 1
                                ? getString(R.string.ids_completed_times, completionCount) : "";

                        binding.resultCard.setCardBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.success_bg));

                        new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.ids_success_title)
                            .setMessage(getString(R.string.ids_success_message, currentKey, completionText))
                            .setPositiveButton(R.string.ids_ok, null)
                            .show();
                    } else {
                        Toast.makeText(getContext(), R.string.ids_key_validation_failed, Toast.LENGTH_LONG).show();
                        binding.resultCard.setCardBackgroundColor(
                                ContextCompat.getColor(requireContext(), R.color.error_bg));
                    }
                });
            } else {
                Toast.makeText(getContext(), R.string.ids_incorrect_password, Toast.LENGTH_SHORT).show();
                binding.resultCard.setCardBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.error_bg));
                binding.keyInput.setText("");
            }
        });

        return root;
    }

    private void createDatabase() {
        try {
            passwordDB = requireContext().openOrCreateDatabase(
                    "passwordDB", android.content.Context.MODE_PRIVATE, null);
            passwordDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS passwordDB " +
                            "(id integer primary key, name VARCHAR, password VARCHAR);");
        } catch (Exception e) {
            Log.e("DB ERROR", "Error Creating Database", e);
        }
    }

    private void insertUsers() {
        if (passwordDB == null) return;
        try {
            passwordDB.execSQL("DELETE FROM passwordDB;");
            passwordDB.execSQL(
                    "INSERT INTO passwordDB (name, password) VALUES ('Admin', ?);",
                    new Object[]{md5(ADMIN_PASSWORD)});
            passwordDB.execSQL("INSERT INTO passwordDB (name, password) VALUES ('john_doe', ?);",
                    new Object[]{md5("password123")});
            passwordDB.execSQL("INSERT INTO passwordDB (name, password) VALUES ('alice_smith', ?);",
                    new Object[]{md5("welcome2024")});
            passwordDB.execSQL("INSERT INTO passwordDB (name, password) VALUES ('bob_johnson', ?);",
                    new Object[]{md5("qwerty456")});
        } catch (Exception e) {
            Log.e("DB ERROR", "Error Inserting Users", e);
        }
    }

    private void displayUsers() {
        if (passwordDB == null || binding == null) return;
        try {
            Cursor cursor = passwordDB.rawQuery("SELECT name FROM passwordDB", null);
            LinearLayout container = binding.usersListContainer;
            container.removeAllViews();
            while (cursor.moveToNext()) {
                String username = cursor.getString(0);

                LinearLayout userRow = new LinearLayout(requireContext());
                userRow.setOrientation(LinearLayout.HORIZONTAL);
                userRow.setPadding(0, 8, 0, 8);

                TextView userIcon = new TextView(requireContext());
                userIcon.setText("\u2022 ");
                userIcon.setTextSize(16);

                TextView userName = new TextView(requireContext());
                userName.setText(username);
                userName.setTextSize(16);

                TextView passwordHidden = new TextView(requireContext());
                passwordHidden.setText(R.string.ids_password_masked);
                passwordHidden.setTextSize(14);
                passwordHidden.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.text_secondary));

                userRow.addView(userIcon);
                userRow.addView(userName);
                userRow.addView(passwordHidden);
                container.addView(userRow);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DB ERROR", "Error Displaying Users", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (passwordDB != null) {
            passwordDB.close();
        }
        binding = null;
    }

    private String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
