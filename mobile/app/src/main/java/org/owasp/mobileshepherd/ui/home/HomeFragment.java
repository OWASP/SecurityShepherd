package org.owasp.mobileshepherd.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import org.owasp.mobileshepherd.LoginActivity;
import org.owasp.mobileshepherd.MainActivity;
import org.owasp.mobileshepherd.databinding.FragmentHomeBinding;
import org.owasp.mobileshepherd.utils.AuthManager;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        
        updateAuthCard();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh card state when returning to this screen (e.g. after sign-in/out)
        updateAuthCard();
    }

    private void updateAuthCard() {
        TextView statusText = binding.getRoot().findViewById(org.owasp.mobileshepherd.R.id.home_auth_status_text);
        MaterialButton authButton = binding.getRoot().findViewById(org.owasp.mobileshepherd.R.id.home_auth_button);
        if (statusText == null || authButton == null) return;

        if (AuthManager.isAuthenticated(requireContext())) {
            String username = AuthManager.getUsername(requireContext());
            statusText.setText("Signed in as " + username + " — flags update from server");
            authButton.setText("Sign Out");
            authButton.setOnClickListener(v -> {
                AuthManager.logout(requireContext());
                updateAuthCard();
            });
        } else {
            statusText.setText("Offline mode — sign in for server-validated flags");
            authButton.setText("Sign In to Server");
            authButton.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                intent.putExtra(LoginActivity.EXTRA_FROM_APP, true);
                startActivity(intent);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
