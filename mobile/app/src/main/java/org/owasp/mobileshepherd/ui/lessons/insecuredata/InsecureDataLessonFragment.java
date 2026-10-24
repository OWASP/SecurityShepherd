package org.owasp.mobileshepherd.ui.lessons.insecuredata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentInsecureDataLessonBinding;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class InsecureDataLessonFragment extends Fragment {

    private FragmentInsecureDataLessonBinding binding;
    private static final String TAG = "InsecureDataLesson";
    private static final String DB_NAME = "lesson_users.db";
    private SQLiteDatabase db;
    private String currentKey = "";
    private ProgressTracker progressTracker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInsecureDataLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v ->
                    ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.IDS_LESSON));
        }

        openDatabase();

        KeyProvider.getKey(requireContext(), KeyValidator.Module.IDS_LESSON, keyValue -> {
            if (!isAdded()) return;
            currentKey = keyValue;
            seedDatabase(keyValue);
            displayUsers();
        });

        binding.btnSubmitCredentials.setOnClickListener(v -> submitCredentials());

        return root;
    }

    /** Opens (or creates) the unencrypted SQLite database for this lesson. */
    private void openDatabase() {
        SQLiteOpenHelper helper = new SQLiteOpenHelper(requireContext(), DB_NAME, null, 1) {
            @Override
            public void onCreate(SQLiteDatabase d) {
                d.execSQL("CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT NOT NULL, " +
                        "password TEXT NOT NULL, " +
                        "role TEXT)");
            }

            @Override
            public void onUpgrade(SQLiteDatabase d, int oldVersion, int newVersion) {
                d.execSQL("DROP TABLE IF EXISTS users");
                onCreate(d);
            }
        };
        db = helper.getWritableDatabase();
        Log.d(TAG, "Database opened at: " + requireContext().getDatabasePath(DB_NAME).getAbsolutePath());
    }

    private void seedDatabase(String keyValue) {
        db.execSQL("DELETE FROM users");
        insertUser("admin", keyValue, "admin");
        insertUser("alice", "alice_pass_99", "user");
        insertUser("bob", "B0b$ecure!", "user");
        insertUser("charlie", "Ch4rl1e#2024", "user");
        Log.d(TAG, "Database seeded. Admin password stored in plaintext.");
    }

    private void insertUser(String username, String password, String role) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", role);
        db.insert("users", null, cv);
    }

    private void displayUsers() {
        LinearLayout container = binding.usersListContainer;
        container.removeAllViews();

        Cursor cursor = db.rawQuery("SELECT username, role FROM users ORDER BY id", null);
        while (cursor.moveToNext()) {
            String username = cursor.getString(0);
            String role = cursor.getString(1);
            TextView tv = new TextView(getContext());
            tv.setPadding(0, 8, 0, 8);
            tv.setTextSize(14f);
            tv.setText(getString(R.string.ids_user_row, username, role));
            container.addView(tv);
        }
        cursor.close();
    }

    private void submitCredentials() {
        String input = binding.editCredentials.getText() != null
                ? binding.editCredentials.getText().toString().trim() : "";
        if (input.isEmpty()) {
            Toast.makeText(getContext(), R.string.ids_enter_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] parts = input.split(":", 2);
        if (parts.length < 2) {
            Toast.makeText(getContext(), R.string.ids_credentials_format, Toast.LENGTH_SHORT).show();
            return;
        }

        String enteredUser = parts[0].trim();
        String enteredPass = parts[1].trim();

        // Validate against what is actually stored in the DB (prevents timing guessing)
        Cursor cursor = db.rawQuery(
                "SELECT password FROM users WHERE username = ?",
                new String[]{enteredUser});

        boolean success = false;
        if (cursor.moveToFirst()) {
            String storedPass = cursor.getString(0);
            success = storedPass.equals(enteredPass) && "admin".equalsIgnoreCase(enteredUser);
        }
        cursor.close();

        binding.textCredentialResult.setVisibility(View.VISIBLE);
        if (success) {
            binding.headerCard.setVisibility(View.GONE);
            submitKeyToServer(currentKey);
        } else {
            binding.textCredentialResult.setText(R.string.ids_credentials_incorrect);
            binding.textCredentialResult.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.error_red));
        }
    }

    private void submitKeyToServer(String key) {
        KeyValidator.validateKey(requireContext(), KeyValidator.Module.IDS_LESSON, key, correct -> {
            if (!isAdded()) return;
            if (correct) {
                progressTracker.markCompleted(KeyValidator.Module.IDS_LESSON);
                int completionCount = progressTracker.getCompletionCount(KeyValidator.Module.IDS_LESSON);
                String completionText = completionCount > 1
                        ? getString(R.string.ids_completed_times, completionCount) : "";
                binding.textCredentialResult.setText(
                        getString(R.string.ids_key_result, key, completionText));
                binding.textCredentialResult.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.success_green));
            } else {
                binding.textCredentialResult.setText(R.string.ids_key_validation_failed);
                binding.textCredentialResult.setTextColor(
                        ContextCompat.getColor(requireContext(), R.color.error_red));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null && db.isOpen()) db.close();
        binding = null;
    }
}
