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
