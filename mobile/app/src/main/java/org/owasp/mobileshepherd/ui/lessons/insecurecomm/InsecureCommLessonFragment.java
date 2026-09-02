package org.owasp.mobileshepherd.ui.lessons.insecurecomm;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentInsecureCommLessonBinding;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.net.HttpURLConnection;
import java.net.URL;

public class InsecureCommLessonFragment extends Fragment {

    private FragmentInsecureCommLessonBinding binding;
    private static final String TAG = "InsecureCommLesson";
    private String currentFlag = "";
    private ProgressTracker progressTracker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInsecureCommLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());

        FlagProvider.getFlag(requireContext(), FlagValidator.Module.INSECURE_COMM_LESSON, flagValue -> {
            if (!isAdded()) return;
            currentFlag = flagValue;
        });

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v ->
                    ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.INSECURE_COMM_LESSON));
        }

        binding.sendHttpButton.setOnClickListener(v -> sendAnalyticsEvent());
        binding.btnSubmitFlag.setOnClickListener(v -> submitFlag());

        return root;
    }

    /**
     * Sends an unencrypted HTTP analytics request that contains the flag as the
     * X-API-Key header.  The request is also logged to Logcat so students can
     * intercept it with either a proxy (Burp Suite / mitmproxy) or adb logcat.
     */
    private void sendAnalyticsEvent() {
        binding.sendHttpButton.setEnabled(false);
        binding.statusText.setText("Sending analytics event…");

        new Thread(() -> {
            String flag = currentFlag.isEmpty() ? "[connect to server]" : currentFlag;

            // Log the plaintext request (intentionally insecure)
            Log.d(TAG, "═══════════════════════════════════");
            Log.d(TAG, "Outbound HTTP analytics request");
            Log.d(TAG, "POST http://analytics.internal.app/events");
            Log.d(TAG, "Protocol: HTTP/1.1  WARNING: PLAINTEXT");
            Log.d(TAG, "Headers:");
            Log.d(TAG, "  Content-Type: application/json");
            Log.d(TAG, "  X-API-Key: " + flag);
            Log.d(TAG, "  User-Agent: MobileShepherd/1.0");
            Log.d(TAG, "Body: {\"event\":\"lesson_view\",\"module\":\"insecure_comm\"}");
            Log.d(TAG, "═══════════════════════════════════");

            // Actually attempt the HTTP connection so a proxy can intercept it
            try {
                URL url = new URL("http://analytics.internal.app/events");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-API-Key", flag);
                conn.setRequestProperty("User-Agent", "MobileShepherd/1.0");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                try {
                    conn.connect();
                } catch (Exception ignored) {
                    // Connection will fail – expected; the proxy intercepts it
                }
            } catch (Exception e) {
                Log.e(TAG, "HTTP send error: " + e.getMessage());
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                binding.statusText.setText(
                        "Request sent!\n\nSet up Burp Suite on 127.0.0.1:8080 and intercept the HTTP traffic,\n"
                        + "or run: adb logcat -s " + TAG + "\n\n"
                        + "Look for the X-API-Key header — that is the flag.");
                binding.sendHttpButton.setEnabled(true);
                Toast.makeText(getContext(), "Check logcat tag: " + TAG, Toast.LENGTH_LONG).show();
            });
        }).start();
    }

    private void submitFlag() {
        String entered = binding.editFlag.getText() != null
                ? binding.editFlag.getText().toString().trim() : "";
        if (entered.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the intercepted API key", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.btnSubmitFlag.setEnabled(false);
        FlagValidator.validateFlag(requireContext(), FlagValidator.Module.INSECURE_COMM_LESSON,
                entered, correct -> {
                    if (correct) {
                        binding.textFlagResult.setVisibility(View.VISIBLE);
                        binding.textFlagResult.setText("Correct! You intercepted the flag from the plaintext HTTP request.");
                        binding.textFlagResult.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.success_green));
                        progressTracker.markCompleted(FlagValidator.Module.INSECURE_COMM_LESSON);
                        Toast.makeText(getContext(), "Lesson complete!", Toast.LENGTH_LONG).show();
                    } else {
                        binding.btnSubmitFlag.setEnabled(true);
                        binding.textFlagResult.setVisibility(View.VISIBLE);
                        binding.textFlagResult.setText("Incorrect. Intercept the HTTP request and copy the X-API-Key value.");
                        binding.textFlagResult.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.error_red));
                        binding.editFlag.setText("");
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
