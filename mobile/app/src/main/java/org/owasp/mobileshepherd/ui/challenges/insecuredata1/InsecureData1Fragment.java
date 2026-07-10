package org.owasp.mobileshepherd.ui.challenges.insecuredata1;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Base64;
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
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class InsecureData1Fragment extends Fragment {

    private FragmentInsecureData1Binding binding;
    private SQLiteDatabase passwordDB = null;
    private String currentFlag = "";
    private ProgressTracker progressTracker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInsecureData1Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());

        // Setup FAB for vulnerability information
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.IDS_CHALLENGE_1));
        }

        // Create the vulnerable database, then seed it once the server flag is available
        createDatabase();
        FlagProvider.getFlag(requireContext(), FlagValidator.Module.IDS_CHALLENGE_1, flagValue -> {
            currentFlag = flagValue;
            insertUsers();
            displayUsers();
        });

        // Wire up flag validation
        binding.validateButton.setOnClickListener(v -> {
            String enteredFlag = binding.flagInput.getText().toString().trim();
            FlagValidator.validateFlag(requireContext(), FlagValidator.Module.IDS_CHALLENGE_1,
                    enteredFlag, isValid -> {
                if (isValid) {
                    progressTracker.markCompleted(FlagValidator.Module.IDS_CHALLENGE_1);
                    int completionCount = progressTracker.getCompletionCount(FlagValidator.Module.IDS_CHALLENGE_1);
                    String completionText = completionCount > 1 ? " (Completed " + completionCount + " times)" : "";

                    Toast.makeText(getContext(), "Correct! Flag validated!", Toast.LENGTH_LONG).show();
                    binding.resultCard.setCardBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.success_bg));

                    new AlertDialog.Builder(requireContext())
                        .setTitle("\uD83C\uDF89 Success!")
                        .setMessage("Congratulations! You extracted the flag from the SQLite database.\n\nFlag: " + enteredFlag + completionText)
                        .setPositiveButton("OK", null)
                        .show();
                } else {
                    Toast.makeText(getContext(), "Incorrect flag. Keep looking!", Toast.LENGTH_SHORT).show();
                    binding.resultCard.setCardBackgroundColor(
                            ContextCompat.getColor(requireContext(), R.color.error_bg));
                    binding.flagInput.setText("");
                }
            });
        });

        return root;
    }

    private void showVulnerabilityInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        // View bestPracticesSection = dialogView.findViewById(R.id.best_practices_section);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.insecure_data_intro);
        // vulnerabilitiesText.setText(R.string.insecure_data_vulns);
        
        hintsSection.setVisibility(View.GONE);
        
        // bestPracticesSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Insecure Data Storage - SQLite")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
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
                    new Object[]{b64(currentFlag)});
            passwordDB.execSQL("INSERT INTO passwordDB (name, password) VALUES ('john_doe', ?);",
                    new Object[]{b64("password123")});
            passwordDB.execSQL("INSERT INTO passwordDB (name, password) VALUES ('alice_smith', ?);",
                    new Object[]{b64("welcome2024")});
            passwordDB.execSQL("INSERT INTO passwordDB (name, password) VALUES ('bob_johnson', ?);",
                    new Object[]{b64("qwerty456")});
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
                passwordHidden.setText("  \u2022  Password: ******");
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

    private String b64(String value) {
        return Base64.encodeToString(value.getBytes(), Base64.NO_WRAP);
    }
}
