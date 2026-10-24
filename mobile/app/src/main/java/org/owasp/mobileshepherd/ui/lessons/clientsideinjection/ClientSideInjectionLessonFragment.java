package org.owasp.mobileshepherd.ui.lessons.clientsideinjection;

import android.content.ContentValues;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.owasp.mobileshepherd.databinding.FragmentClientSideInjectionLessonBinding;
import org.owasp.mobileshepherd.ui.lessons.clientsideinjection.helpers.DatabaseHelper;
import org.owasp.mobileshepherd.utils.AuthManager;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class ClientSideInjectionLessonFragment extends Fragment {

    private FragmentClientSideInjectionLessonBinding binding;
    private DatabaseHelper dbHelper;
    private static final String TAG = "ClientSideInjection";
    private boolean fabExpanded = false;
    private ProgressTracker progressTracker;
    private String currentKey = "";
    private String dbKey = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClientSideInjectionLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize database
        dbHelper = new DatabaseHelper(requireContext());
        progressTracker = new ProgressTracker(requireContext());

        // Seed with offline key immediately so the lesson is usable right away,
        // then asynchronously replace with the dynamic server key if online.
        KeyProvider.getKey(
                requireContext(),
                KeyValidator.Module.CLIENT_SIDE_INJECTION_LESSON,
                key -> {
                    if (!isAdded()) return;
                    currentKey = key;
                    dbKey = key;
                    initializeDatabase(key);
                });

        // Setup expandable FAB with command reference and OWASP link
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.CLIENT_SIDE_INJECTION_LESSON));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }


        // Setup search button
        binding.searchButton.setOnClickListener(v -> performSearch());

        return root;
    }

    private void initializeDatabase(String keyValue) {
        SQLiteDatabase db;
        try {
            db = dbHelper.getWritableDatabase(dbKey);
        } catch (android.database.sqlite.SQLiteException e) {
            // Key no longer matches the previously-encrypted file (e.g. key changed
            // between offline and server-fetched values). The data here is regenerated
            // on every open, so just recreate the file with the new key.
            Log.w(TAG, "DB key mismatch, recreating database", e);
            requireContext().deleteDatabase(DatabaseHelper.DATABASE_NAME);
            db = dbHelper.getWritableDatabase(dbKey);
        }

        // Clear existing data
        db.execSQL("DELETE FROM users");
        
        // Insert sample data
        insertUser(db, "admin", "admin@app.com", "Administrator", false);
        insertUser(db, "alice", "alice@app.com", "Alice Smith", false);
        insertUser(db, "bob", "bob@app.com", "Bob Jones", false);
        insertUser(db, "charlie", "charlie@app.com", "Charlie Brown", false);
        
        // Insert hidden admin user with key (obscure username)
        insertUser(db, "sys_root", "root@system.internal", keyValue, true);
        
        db.close();
    }

    private void insertUser(SQLiteDatabase db, String username, String email, String fullName, boolean isAdmin) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("full_name", fullName);
        values.put("is_admin", isAdmin ? 1 : 0);
        db.insert("users", null, values);
    }

    private void performSearch() {
        String searchTerm = binding.searchInput.getText().toString();
        
        if (searchTerm.isEmpty()) {
            Toast.makeText(getContext(), R.string.csi_enter_search_term, Toast.LENGTH_SHORT).show();
            return;
        }

        // VULNERABLE: Concatenating user input directly into SQL query
        String query = "SELECT username, email, full_name, is_admin FROM users WHERE username = '" + searchTerm + "'";
        
        Log.d(TAG, "Executing query: " + query);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase(dbKey);
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(query, null);

            int count = 0;
            LinearLayout container = binding.resultContainer;
            container.removeAllViews();

            while (cursor.moveToNext()) {
                count++;
                String username = cursor.getString(0);
                String email = cursor.getString(1);
                String fullName = cursor.getString(2);
                int isAdmin = cursor.getInt(3);

                View row = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_user_result, container, false);
                ((TextView) row.findViewById(R.id.item_username)).setText(username);
                ((TextView) row.findViewById(R.id.item_email)).setText(email);
                ((TextView) row.findViewById(R.id.item_full_name)).setText(
                        fullName != null ? fullName : "");
                TextView adminView = row.findViewById(R.id.item_is_admin);
                if (isAdmin == 1) {
                    adminView.setText(R.string.csi_yes);
                    adminView.setTextColor(getResources().getColor(
                            android.R.color.holo_red_light, null));
                } else {
                    adminView.setText(R.string.csi_no);
                    adminView.setTextColor(getResources().getColor(
                            android.R.color.darker_gray, null));
                }
                container.addView(row);

                // Check if this is the hidden key row
                if (fullName != null && !currentKey.isEmpty()
                        && fullName.equals(currentKey)) {
                    submitKeyToServer(fullName);
                    binding.headerCard.setVisibility(View.GONE);
                }
            }

            if (count == 0) {
                binding.resultEmptyText.setText(getString(R.string.csi_lesson_no_users, searchTerm));
                binding.resultEmptyText.setVisibility(View.VISIBLE);
                binding.resultContainer.setVisibility(View.GONE);
            } else {
                binding.resultEmptyText.setVisibility(View.GONE);
                binding.resultContainer.setVisibility(View.VISIBLE);
            }
            
        } catch (Exception e) {
            binding.resultEmptyText.setText(getString(R.string.csi_lesson_error, e.getMessage()));
            binding.resultEmptyText.setVisibility(View.VISIBLE);
            binding.resultContainer.setVisibility(View.GONE);
            Log.e(TAG, "SQL Error", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
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
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.client_side_injection_lesson_intro);
        // vulnerabilitiesText.setText(R.string.client_side_injection_lesson_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.csi_info_title_lesson)
                .setView(dialogView)
                .setPositiveButton(R.string.csi_close, null)
                .show();
    }
    
    /**
     * Submits the discovered key to the Shepherd server for validation.
     * Falls back to local SHA-256 comparison when no server is configured.
     * Marks the lesson complete and updates the FAB appearance on success.
     */
    private void submitKeyToServer(String key) {
        // In case the DB was seeded before KeyProvider returned, use the
        // most recent key value rather than the one passed by the search results.
        String keyToSubmit = currentKey.isEmpty() ? key : currentKey;
        KeyValidator.validateKey(
                requireContext(),
                KeyValidator.Module.CLIENT_SIDE_INJECTION_LESSON,
                keyToSubmit,
                correct -> {
                    if (!isAdded()) return;
                    if (correct) {
                        progressTracker.markCompleted(KeyValidator.Module.CLIENT_SIDE_INJECTION_LESSON);
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle(R.string.csi_lesson_complete_title)
                                .setMessage(R.string.csi_lesson_complete_message)
                                .setPositiveButton(R.string.csi_ok, null)
                                .show();
                    } else {
                        Toast.makeText(requireContext(),
                                R.string.csi_lesson_key_incorrect, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        FloatingActionButton fabOwaspLink = requireActivity().findViewById(R.id.fab_owasp_link);
        collapseFab(fab, fabCommandRef, fabOwaspLink);
        if (dbHelper != null) {
            dbHelper.close();
        }
        binding = null;
    }
}
