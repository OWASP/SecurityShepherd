package org.owasp.mobileshepherd.ui.lessons.securitymisconfig;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;

public class SecurityMisconfigLessonFragment extends Fragment {

    private static final String TAG = "SecurityMisconfig";
    private boolean fabExpanded = false;

    private LinearLayout componentsContainer;
    private View componentsCard;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_security_misconfig_lesson, container, false);

        componentsContainer = root.findViewById(R.id.components_container);
        componentsCard = root.findViewById(R.id.components_card);

        Button scanComponentsButton = root.findViewById(R.id.scan_components_button);
        scanComponentsButton.setOnClickListener(v -> scanComponents());

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);

        if (fab != null) {
            fab.setOnClickListener(v -> ModuleInfoHelper.showDialog(requireContext(), KeyValidator.Module.SECURITY_MISCONFIG_LESSON));
        }
        if (fabCommandRef != null) {
            fabCommandRef.setOnClickListener(v -> {
                showDetailedInfo();
                collapseFab(fab, fabCommandRef);
            });
        }

        return root;
    }

    private void scanComponents() {
        componentsContainer.removeAllViews();

        try {
            PackageManager pm = requireContext().getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(requireContext().getPackageName(), PackageManager.GET_ACTIVITIES);

            ActivityInfo[] activities = packageInfo.activities;
            if (activities == null) {
                return;
            }

            int exportedCount = 0;
            for (ActivityInfo activity : activities) {
                boolean isExported = activity.exported;
                if (isExported) {
                    exportedCount++;
                }

                LinearLayout itemLayout = new LinearLayout(requireContext());
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setPadding(12, 8, 12, 8);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 8);
                itemLayout.setLayoutParams(params);

                TextView nameView = new TextView(requireContext());
                String activityName = activity.name.replace("org.owasp.mobileshepherd.", "");
                nameView.setText((isExported ? "EXPORTED " : "Safe ") + activityName);
                nameView.setTextSize(12);
                nameView.setTextColor(ContextCompat.getColor(requireContext(),
                        isExported ? R.color.error_text : R.color.success_text));
                nameView.setTypeface(null, Typeface.BOLD);
                itemLayout.addView(nameView);

                TextView detailsView = new TextView(requireContext());
                detailsView.setText("   Label: " + activity.loadLabel(pm));
                detailsView.setTextSize(10);
                detailsView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                detailsView.setTypeface(Typeface.MONOSPACE);
                detailsView.setPadding(0, 4, 0, 0);
                itemLayout.addView(detailsView);

                View divider = new View(requireContext());
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                );
                dividerParams.setMargins(0, 8, 0, 0);
                divider.setLayoutParams(dividerParams);
                divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_stroke));
                itemLayout.addView(divider);

                componentsContainer.addView(itemLayout);
            }

            componentsCard.setVisibility(View.VISIBLE);
            Toast.makeText(requireContext(),
                    "Found " + activities.length + " activities (" + exportedCount + " exported)",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error scanning components: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error scanning components", e);
        }
    }

    private void collapseFab(FloatingActionButton mainFab, FloatingActionButton fab1) {
        fabExpanded = false;
        if (fab1 != null) fab1.setVisibility(View.GONE);
        if (mainFab != null) mainFab.setImageResource(android.R.drawable.ic_menu_help);
    }

    private void showDetailedInfo() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_lesson_info, null);
        TextView moduleBanner = dialogView.findViewById(R.id.module_path_banner);
        if (moduleBanner != null) {
            moduleBanner.setVisibility(View.VISIBLE);
            moduleBanner.setText("com.owasp.security_misconfiguration");
        }

        TextView introText = dialogView.findViewById(R.id.intro_text);
        View additionalSection = dialogView.findViewById(R.id.additional_section);

        introText.setText(R.string.security_misconfig_intro);
        additionalSection.setVisibility(View.GONE);

        new AlertDialog.Builder(requireContext())
                .setTitle("Security Misconfiguration\nOWASP M8: Security Misconfiguration")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        FloatingActionButton fabCommandRef = requireActivity().findViewById(R.id.fab_command_reference);
        collapseFab(fab, fabCommandRef);
    }
}
