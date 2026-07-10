package org.owasp.mobileshepherd.ui.lessons.reverseengineering;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentLessonBinding;
import org.owasp.mobileshepherd.ui.lessons.LessonModel;
import org.owasp.mobileshepherd.utils.FlagProvider;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ModuleInfoHelper;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class ReverseEngineeringLessonFragment extends Fragment {

    private FragmentLessonBinding binding;
    private LessonModel lessonViewModel;
    private ProgressTracker progressTracker;
    private String currentFlag = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLessonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        lessonViewModel = new ViewModelProvider(this).get(LessonModel.class);
        progressTracker = new ProgressTracker(requireContext());

        // FlagProvider returns the static offline flag for RE_LESSON ("Frozen_Clock_Melts_By_Noon").
        // This value is hardcoded inside FlagProvider and is discoverable by decompiling the APK —
        // that is the intended attack surface for the reverse engineering lesson.
        FlagProvider.getFlag(requireContext(), FlagValidator.Module.RE_LESSON,
                flagValue -> currentFlag = flagValue);

        // Observe and display device info
        lessonViewModel.getSerialText().observe(getViewLifecycleOwner(),
                s -> binding.textViewSerial.setText("Serial: " + s));
        lessonViewModel.getModelText().observe(getViewLifecycleOwner(),
                s -> binding.textViewBuild.setText("Model: " + s));
        lessonViewModel.getManufacturerText().observe(getViewLifecycleOwner(),
                s -> binding.textViewManufacturer.setText("Manufacturer: " + s));
        lessonViewModel.getBrandText().observe(getViewLifecycleOwner(),
                s -> binding.textViewBrand.setText("Brand: " + s));
        lessonViewModel.getSDKText().observe(getViewLifecycleOwner(),
                s -> binding.textViewSDK.setText("SDK: " + s));

        FloatingActionButton fab = requireActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v ->
                    ModuleInfoHelper.showDialog(requireContext(), FlagValidator.Module.RE_LESSON));
        }

        binding.verifyKeyButton.setOnClickListener(v -> verifyKey());

        return root;
    }

    private void verifyKey() {
        String entered = binding.keyInput.getText() != null
                ? binding.keyInput.getText().toString().trim() : "";
        if (entered.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the key you found", Toast.LENGTH_SHORT).show();
            return;
        }
        binding.verifyKeyButton.setEnabled(false);
        FlagValidator.validateFlag(requireContext(), FlagValidator.Module.RE_LESSON,
                entered, correct -> {
                    if (correct) {
                        progressTracker.markCompleted(FlagValidator.Module.RE_LESSON);
                        Toast.makeText(getContext(), "Correct! Lesson complete.", Toast.LENGTH_LONG).show();
                    } else {
                        binding.verifyKeyButton.setEnabled(true);
                        Toast.makeText(getContext(),
                                "Incorrect. Decompile the APK and look in FlagProvider.java",
                                Toast.LENGTH_SHORT).show();
                        binding.keyInput.setText("");
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
