package org.owasp.mobileshepherd.ui.lessons.privacycontrols;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import androidx.exifinterface.media.ExifInterface;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentPrivacyControlsLessonBinding;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.io.File;
import java.io.FileOutputStream;

public class PrivacyControlsLessonFragment extends Fragment {

    private FragmentPrivacyControlsLessonBinding binding;
    private static final String TAG = "PrivacyControls";
    private static final String SAMPLE_IMAGE_NAME = "sample_photo.jpg";

    private String currentFlag = "";
    private ProgressTracker progressTracker;
    private File imageFile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPrivacyControlsLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());

        FlagProvider.getFlag(requireContext(), FlagValidator.Module.PRIVACY_LESSON, flagValue -> {
            if (!isAdded()) return;
            currentFlag = flagValue;
            prepareSampleImage(flagValue);
        });

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v ->
                    ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.PRIVACY_LESSON));
        }

        binding.btnLoadSample.setOnClickListener(v -> loadSampleImage());
        binding.btnTakePhoto.setOnClickListener(v -> launchCamera());
        binding.btnSubmitFlag.setOnClickListener(v -> submitFlag());

        return root;
    }

    /**
     * Creates a JPEG in the app's files directory and writes the flag into the
     * EXIF ImageDescription tag.  The flag is deliberately embedded in metadata
     * that many developers forget to strip before sharing images.
     */
    private void prepareSampleImage(String flag) {
        try {
            imageFile = new File(requireContext().getFilesDir(), SAMPLE_IMAGE_NAME);

            // Build a simple coloured bitmap as the "photo"
            Bitmap bmp = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(Color.parseColor("#2C3E50"));
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(48f);
            canvas.drawText("Security Shepherd", 50f, 100f, paint);
            paint.setTextSize(28f);
            canvas.drawText("Sample Photo — Analyze the metadata!", 50f, 200f, paint);

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            }

            // Write the flag into EXIF ImageDescription
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, flag);
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,  "53/1,20/1,30/1");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "-6/1,15/1,45/1");
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,  "N");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            exif.setAttribute(ExifInterface.TAG_MAKE,  "SecurityShepherd");
            exif.setAttribute(ExifInterface.TAG_MODEL, "TrainingDevice v1.0");
            exif.setAttribute(ExifInterface.TAG_DATETIME, "2024:01:15 09:32:00");
            exif.saveAttributes();

            Log.d(TAG, "Sample image prepared. Flag embedded in EXIF ImageDescription.");
        } catch (Exception e) {
            Log.e(TAG, "Error preparing sample image: " + e.getMessage());
        }
    }

    private void loadSampleImage() {
        if (imageFile == null || !imageFile.exists()) {
            Toast.makeText(getContext(), "Image not ready yet, please wait…", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Display the bitmap
            android.graphics.BitmapFactory.Options opts = new android.graphics.BitmapFactory.Options();
            opts.inSampleSize = 2;
            Bitmap bmp = android.graphics.BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opts);
            binding.ivPhoto.setImageBitmap(bmp);

            // Read and display all EXIF fields
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            StringBuilder sb = new StringBuilder("EXIF Metadata:\n");
            sb.append("Make:        ").append(nvl(exif.getAttribute(ExifInterface.TAG_MAKE))).append("\n");
            sb.append("Model:       ").append(nvl(exif.getAttribute(ExifInterface.TAG_MODEL))).append("\n");
            sb.append("DateTime:    ").append(nvl(exif.getAttribute(ExifInterface.TAG_DATETIME))).append("\n");
            sb.append("GPS Lat:     ").append(nvl(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)))
              .append(" ").append(nvl(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF))).append("\n");
            sb.append("GPS Lon:     ").append(nvl(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)))
              .append(" ").append(nvl(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF))).append("\n");
            sb.append("Description: ").append(nvl(exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION)));

            binding.tvImageInfo.setText(sb.toString());
            Log.d(TAG, "EXIF data displayed. ImageDescription = " +
                    exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));

        } catch (Exception e) {
            Log.e(TAG, "Error reading EXIF: " + e.getMessage());
            binding.tvImageInfo.setText("Error reading metadata: " + e.getMessage());
        }
    }

    private void launchCamera() {
        try {
            Uri photoUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".provider",
                    new File(requireContext().getFilesDir(), "camera_photo.jpg"));
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Camera not available on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitFlag() {
        String entered = binding.editFlag.getText() != null
                ? binding.editFlag.getText().toString().trim() : "";
        if (entered.isEmpty()) {
            Toast.makeText(getContext(), "Enter the flag from the EXIF Description field", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.btnSubmitFlag.setEnabled(false);
        FlagValidator.validateFlag(requireContext(), FlagValidator.Module.PRIVACY_LESSON,
                entered, correct -> {
                    if (correct) {
                        binding.textFlagResult.setVisibility(View.VISIBLE);
                        binding.textFlagResult.setText("Correct! You found the flag hidden in the EXIF metadata.");
                        binding.textFlagResult.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.success_green));
                        progressTracker.markCompleted(FlagValidator.Module.PRIVACY_LESSON);
                        Toast.makeText(getContext(), "Lesson complete!", Toast.LENGTH_LONG).show();
                    } else {
                        binding.btnSubmitFlag.setEnabled(true);
                        binding.textFlagResult.setVisibility(View.VISIBLE);
                        binding.textFlagResult.setText("Incorrect. Load the sample image and check the ImageDescription tag.");
                        binding.textFlagResult.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.error_red));
                        binding.editFlag.setText("");
                    }
                });
    }

    private String nvl(String s) {
        return s != null ? s : "(not set)";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

    
    private static final String TAG = "FlagValidator";
    
    // Module type constants
    public static final String TYPE_LESSON = "lesson";
    public static final String TYPE_CHALLENGE = "challenge";
    
    // Module identifiers
    public enum Module {
        // Reverse Engineering
        RE_LESSON("re_lesson", TYPE_LESSON),
        RE_CHALLENGE_1("re_challenge_1", TYPE_CHALLENGE),
        
        // Insecure Data Storage
        IDS_LESSON("ids_lesson", TYPE_LESSON),
        IDS_CHALLENGE_1("ids_challenge_1", TYPE_CHALLENGE),
        
        // Poor Authentication
        POOR_AUTH_LESSON("poor_auth_lesson", TYPE_LESSON),
        POOR_AUTH_CHALLENGE("poor_auth_challenge", TYPE_CHALLENGE),
        
        // Insecure Authorization
        INSECURE_AUTH_LESSON("insecure_auth_lesson", TYPE_LESSON),
        
        // Supply Chain
        SUPPLY_CHAIN_LESSON("supply_chain_lesson", TYPE_LESSON),
        
        // Insecure Communication
        INSECURE_COMM_LESSON("insecure_comm_lesson", TYPE_LESSON),
        INSECURE_COMM_CHALLENGE("insecure_comm_challenge", TYPE_CHALLENGE),

        // Insufficient Cryptography
        INSUFFICIENT_CRYPTO_LESSON("insufficient_crypto_lesson", TYPE_LESSON),
        INSUFFICIENT_CRYPTO_CHALLENGE("insufficient_crypto_challenge", TYPE_CHALLENGE),
        
        // Security Misconfiguration
        SECURITY_MISCONFIG_LESSON("security_misconfig_lesson", TYPE_LESSON),
        SECURITY_MISCONFIG_CHALLENGE_2("security_misconfig_challenge_2", TYPE_CHALLENGE),
        
        // Input Validation
        INPUT_VALIDATION_LESSON("input_validation_lesson", TYPE_LESSON),

        // Privacy Controls
        PRIVACY_LESSON("privacy_lesson", TYPE_LESSON),
        
        // Client-Side Injection
        CLIENT_SIDE_INJECTION_LESSON("client_side_injection_lesson", TYPE_LESSON),
        CLIENT_SIDE_INJECTION_CHALLENGE_1("client_side_injection_challenge_1", TYPE_CHALLENGE),
        CLIENT_SIDE_INJECTION_CHALLENGE_2("client_side_injection_challenge_2", TYPE_CHALLENGE);
        
        private final String id;
        private final String type;
        
        Module(String id, String type) {
            this.id = id;
            this.type = type;
        }
        
        public String getId() {
            return id;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // RE_LESSON and RE_CHALLENGE_1 retain local hashes — the SHA-256 in the APK is the
    // target of the reverse-engineering challenge itself. All other modules require a live
    // server session; no offline fallback is provided.
    private static final Map<Module, String> FLAG_HASHES = new HashMap<Module, String>() {{
        put(Module.RE_LESSON,      "a0c066b9cd89c084709330a943fb6b333d45c932ed613e428bc52078b5722e57");
        put(Module.RE_CHALLENGE_1, "f04a272a2d82a0168f44559f9957dc9e028ecb13195c12fce17ac08d4af91deb");
    }};
    
    /**
     * Validates a flag submission using SHA-256 hash comparison.
     * 
     * @param module The module being validated
     * @param submittedFlag The flag submitted by the user
     * @return true if the flag is correct, false otherwise
     */
    public static boolean validateFlag(Module module, String submittedFlag) {
        if (submittedFlag == null || submittedFlag.trim().isEmpty()) {
            return false;
        }
        
        String expectedHash = FLAG_HASHES.get(module);
        if (expectedHash == null) {
            Log.e(TAG, "No hash found for module: " + module.getId());
            return false;
        }
        
        String submittedHash = sha256(submittedFlag.trim());
        boolean isValid = expectedHash.equalsIgnoreCase(submittedHash);
        
        Log.d(TAG, isValid ? "[OK] Correct flag for " + module.getId()
                             : "[FAIL] Incorrect flag for " + module.getId());
        return isValid;
    }
    
    /**
     * Computes SHA-256 hash of the input string.
     * 
     * @param input The string to hash
     * @return Hexadecimal representation of the hash
     */
    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Convert bytes to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 algorithm not available", e);
            return "";
        }
    }
    
    // -------------------------------------------------------------------------
    // Server-side validation
    // -------------------------------------------------------------------------

    /**
     * Callback interface for asynchronous flag validation results.
     *
     * <p>{@link #onResult(boolean)} is always invoked on the main (UI) thread.
     */
    public interface ValidationCallback {
        void onResult(boolean correct);
    }

    /**
     * Validates a flag against the configured Shepherd server when a session is active.
     * Falls back to local SHA-256 comparison when no session is available,
     * so the app remains usable without a running server instance.
     *
     * <p>This method is non-blocking. The result is delivered on the main thread via
     * {@code callback}.
     *
     * @param context  Application context used to read shared preferences.
     * @param module   The module being validated.
     * @param flag     The flag string submitted by the student.
     * @param callback Receives {@code true} when the flag is correct, {@code false} otherwise.
     */
    public static void validateFlag(
            Context context,
            Module module,
            String flag,
            ValidationCallback callback) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String serverUrl     = prefs.getString("server_preference", "").trim();
        String sessionCookie = AuthManager.getMobileSessionCookie(context);

        if (serverUrl.isEmpty() || sessionCookie.isEmpty()) {
            // No active server session — only RE modules have offline hashes.
            Log.d(TAG, "No server session — offline validation for " + module.getId());
            boolean result = validateFlag(module, flag);
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(result));
            return;
        }

        final String endpointUrl = serverUrl.replaceAll("/+$", "") + "/mobileFlagSubmit";
        final String moduleId    = module.getId();
        final String trimmedFlag = flag.trim();
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            boolean correct = false;
            HttpURLConnection conn = null;
            try {
                String body =
                        "moduleId=" + URLEncoder.encode(moduleId, "UTF-8")
                        + "&flag="     + URLEncoder.encode(trimmedFlag, "UTF-8");

                conn = (HttpURLConnection) new URL(endpointUrl).openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(10_000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Cookie", sessionCookie);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }

                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader =
                                 new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    }
                    JSONObject json = new JSONObject(sb.toString());
                    correct = json.optBoolean("correct", false);
                    Log.d(TAG, "Server validation for " + moduleId + ": " + correct);
                } else {
                    Log.w(TAG, "Server returned HTTP " + status + " for " + moduleId);
                    correct = validateFlag(module, trimmedFlag);
                }
            } catch (Exception e) {
                Log.e(TAG, "Server validation failed for " + moduleId + ": " + e.getMessage());
                correct = validateFlag(module, trimmedFlag);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            final boolean result = correct;
            mainHandler.post(() -> callback.onResult(result));
        }).start();
    }
}
