package org.owasp.mobileshepherd.ui.adb;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.owasp.mobileshepherd.databinding.FragmentAdbReferenceBinding;

public class AdbReferenceFragment extends Fragment {

    private FragmentAdbReferenceBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAdbReferenceBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup copy buttons for each command category
        setupCopyButton(binding.copyLogcat, "adb logcat");
        setupCopyButton(binding.copyLogcatFilter, "adb logcat -s TAG_NAME");
        setupCopyButton(binding.copyLogcatGrep, "adb logcat | grep \"search_term\"");
        setupCopyButton(binding.copyLogcatClear, "adb logcat -c");
        
        setupCopyButton(binding.copyListDevices, "adb devices");
        setupCopyButton(binding.copyShell, "adb shell");
        setupCopyButton(binding.copyInstall, "adb install app.apk");
        setupCopyButton(binding.copyUninstall, "adb uninstall com.package.name");
        
        setupCopyButton(binding.copyPullFile, "adb pull /data/data/org.owasp.mobileshepherd/databases/Users.db");
        setupCopyButton(binding.copyPullSharedPrefs, "adb pull /data/data/org.owasp.mobileshepherd/shared_prefs/");
        setupCopyButton(binding.copyListFiles, "adb shell ls /data/data/org.owasp.mobileshepherd/");
        setupCopyButton(binding.copyListDatabases, "adb shell ls /data/data/org.owasp.mobileshepherd/databases/");
        
        setupCopyButton(binding.copySqlite, "adb shell sqlite3 /data/data/org.owasp.mobileshepherd/databases/Users.db");
        setupCopyButton(binding.copySqliteQuery, "sqlite> SELECT * FROM users;");
        setupCopyButton(binding.copyCat, "adb shell cat /data/data/org.owasp.mobileshepherd/shared_prefs/UserCredentials.xml");
        
        setupCopyButton(binding.copyGetPackages, "adb shell pm list packages | grep reverser");
        setupCopyButton(binding.copyPackagePath, "adb shell pm path org.owasp.mobileshepherd");
        setupCopyButton(binding.copyPullApk, "adb pull /data/app/org.owasp.mobileshepherd-*/base.apk");
        setupCopyButton(binding.copyClearData, "adb shell pm clear org.owasp.mobileshepherd");

        setupCopyButton(binding.copyRunAsVault, "adb shell run-as org.owasp.mobileshepherd cat shared_prefs/recovery_vault.xml");
        setupCopyButton(binding.copyPullVault, "adb pull /data/data/org.owasp.mobileshepherd/shared_prefs/recovery_vault.xml");
        setupCopyButton(binding.copyJadx, "jadx-gui app-debug.apk");
        setupCopyButton(binding.copyXorDecode, "python3 -c \"import base64; d=base64.b64decode('PASTE_BASE64_HERE'); print(''.join(chr(b^0x42) for b in d))\"");

        return root;
    }

    private void setupCopyButton(View button, String command) {
        button.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ADB Command", command);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
