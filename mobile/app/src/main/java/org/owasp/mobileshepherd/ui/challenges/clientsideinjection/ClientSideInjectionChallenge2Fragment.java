package org.owasp.mobileshepherd.ui.challenges.clientsideinjection;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
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
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentClientSideInjectionChallenge2Binding;
import org.owasp.mobileshepherd.ui.challenges.clientsideinjection.helpers.Challenge2DatabaseHelper;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class ClientSideInjectionChallenge2Fragment extends Fragment {

    private FragmentClientSideInjectionChallenge2Binding binding;
    private ClientSideInjectionChallenge2Model viewModel;
    private Challenge2DatabaseHelper dbHelper;
    private static final String TAG = "CSI_Challenge2";
    private ProgressTracker progressTracker;
    private boolean fabExpanded = false;
    private String currentKey = "";
    private String dbKey = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentClientSideInjectionChallenge2Binding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewModel = new ViewModelProvider(this).get(ClientSideInjectionChallenge2Model.class);
        progressTracker = new ProgressTracker(requireContext());

        // Initialize database
        dbHelper = new Challenge2DatabaseHelper(requireContext());
        KeyProvider.getKey(
                requireContext(),
                KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_2,
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
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_2));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showVulnerabilityInfo();
                collapseFab(fab, fabCommandRef, fabOwaspLink);
            });
        }


        // Set initial FAB appearance based on completion status

        // Setup search button
        binding.searchProductButton.setOnClickListener(v -> searchProducts());

        return root;
    }

    private void initializeDatabase(String keyValue) {
        SQLiteDatabase db;
        try {
            db = dbHelper.getWritableDatabase(dbKey);
        } catch (android.database.sqlite.SQLiteException e) {
            // Key changed (e.g. offline local key vs server-issued per-user key); data is
            // regenerated on every open, so recreate the file with the new key.
            Log.w(TAG, "DB key mismatch, recreating database", e);
            requireContext().deleteDatabase(Challenge2DatabaseHelper.DATABASE_NAME);
            db = dbHelper.getWritableDatabase(dbKey);
        }

        // Clear existing data
        db.execSQL("DELETE FROM products");
        db.execSQL("DELETE FROM secrets");

        // Insert sample products
        insertProduct(db, "Laptop", "Electronics", 999.99, 15);
        insertProduct(db, "Mouse", "Electronics", 29.99, 50);
        insertProduct(db, "Keyboard", "Electronics", 79.99, 30);
        insertProduct(db, "Monitor", "Electronics", 299.99, 20);
        insertProduct(db, "Desk", "Furniture", 199.99, 10);

        // Insert secret table — admin_token value is the user-specific HMAC key
        insertSecret(db, "admin_token", keyValue);
        insertSecret(db, "api_key", "sk_test_1234567890");

        db.close();
    }

    private void insertProduct(SQLiteDatabase db, String name, String category, double price, int stock) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category", category);
        values.put("price", price);
        values.put("stock", stock);
        db.insert("products", null, values);
    }

    private void insertSecret(SQLiteDatabase db, String key, String value) {
        ContentValues values = new ContentValues();
        values.put("secret_key", key);
        values.put("secret_value", value);
        db.insert("secrets", null, values);
    }

    private void searchProducts() {
        String searchTerm = binding.productSearchInput.getText().toString();
        
        if (searchTerm.isEmpty()) {
            Toast.makeText(getContext(), R.string.csi_enter_search_term, Toast.LENGTH_SHORT).show();
            return;
        }

        // VULNERABLE: User input concatenated into SQL query - allows UNION-based injection
        String query = "SELECT name, category, price, stock FROM products WHERE name LIKE '%" + searchTerm + "%' OR category LIKE '%" + searchTerm + "%'";
        
        Log.d(TAG, "Executing query: " + query);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase(dbKey);
        Cursor cursor = null;
        
        try {
            cursor = db.rawQuery(query, null);
            
            StringBuilder results = new StringBuilder();
            int count = 0;
            int columnCount = cursor.getColumnCount();
            
            while (cursor.moveToNext()) {
                count++;
                
                // Handle results dynamically (supports UNION injection)
                for (int i = 0; i < columnCount; i++) {
                    String columnName = cursor.getColumnName(i);
                    String value = cursor.getString(i);
                    
                    results.append(columnName).append(": ").append(value).append("\n");
                    
                    // Check if key was extracted
                    if (value != null && !currentKey.isEmpty() && value.equals(currentKey)) {
                        results.append(getString(R.string.csi_c2_secret_discovered, value));
                        binding.headerCard.setVisibility(View.GONE);
                        submitKeyToServer(value);
                    }
                }
                results.append("---\n");
            }
            
            if (count == 0) {
                binding.resultText.setText(getString(R.string.csi_c2_no_products, searchTerm));
            } else {
                binding.resultText.setText(results.toString());
            }
            
        } catch (Exception e) {
            binding.resultText.setText(getString(R.string.csi_c2_error, e.getMessage()));
            Log.e(TAG, "SQL Error", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    /**
     * Submits the discovered key to the Shepherd server as soon as the UNION
     * injection surfaces it, so the player doesn't need to manually copy it into
     * the key field.
     */
    private void submitKeyToServer(String key) {
        KeyValidator.validateKey(
                requireContext(),
                KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_2,
                key,
                correct -> {
                    if (!isAdded()) return;
                    if (correct) {
                        progressTracker.markCompleted(KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_2);
                        int completionCount = progressTracker.getCompletionCount(KeyValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_2);
                        String completionText = completionCount > 1
                                ? getString(R.string.csi_c2_completed_times, completionCount) : "";

                        new AlertDialog.Builder(requireContext())
                                .setTitle(R.string.csi_c2_success_title)
                                .setMessage(getString(R.string.csi_c2_success_message, key, completionText))
                                .setPositiveButton(R.string.csi_ok, null)
                                .show();
                    }
                });
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


    private void showVulnerabilityInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        
        TextView introText = dialogView.findViewById(R.id.intro_text);
        // TextView vulnerabilitiesText = dialogView.findViewById(R.id.vulnerabilities_text);
        View hintsSection = dialogView.findViewById(R.id.hints_section);
        TextView hintsText = dialogView.findViewById(R.id.hints_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);
        
        introText.setText(R.string.client_side_injection_challenge2_intro);
        // vulnerabilitiesText.setText(R.string.client_side_injection_challenge2_vulnerabilities);
        
        hintsSection.setVisibility(View.GONE);
        additionalSection.setVisibility(View.GONE);
        
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.csi_info_title_challenge2)
                .setView(dialogView)
                .setPositiveButton(R.string.csi_close, null)
                .show();
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
