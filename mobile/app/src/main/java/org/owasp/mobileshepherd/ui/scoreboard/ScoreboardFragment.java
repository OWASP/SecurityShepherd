package org.owasp.mobileshepherd.ui.scoreboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.mobileshepherd.MainActivity;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.utils.AuthManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ScoreboardFragment extends Fragment {

    private static final String TAG = "ScoreboardFragment";

    private View offlineView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private View emptyView;
    private View errorView;
    private TextView errorMessageText;

    private ScoreboardAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_scoreboard, container, false);

        offlineView = root.findViewById(R.id.scoreboard_offline_view);
        progressBar = root.findViewById(R.id.scoreboard_progress);
        refreshLayout = root.findViewById(R.id.scoreboard_refresh);
        recyclerView = root.findViewById(R.id.scoreboard_recycler_view);
        emptyView = root.findViewById(R.id.scoreboard_empty_view);
        errorView = root.findViewById(R.id.scoreboard_error_view);
        errorMessageText = root.findViewById(R.id.scoreboard_error_message);

        MaterialButton signInButton = root.findViewById(R.id.scoreboard_sign_in_button);
        signInButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openAuthDialog();
            }
        });

        MaterialButton retryButton = root.findViewById(R.id.scoreboard_retry_button);
        retryButton.setOnClickListener(v -> fetchScoreboard());

        adapter = new ScoreboardAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(this::fetchScoreboard);

        if (AuthManager.isAuthenticated(requireContext())) {
            fetchScoreboard();
        } else {
            offlineView.setVisibility(View.VISIBLE);
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // The in-content header below already shows the "Scoreboard" title/branding,
        // so blank the toolbar title to avoid showing it twice.
        if (getActivity() instanceof AppCompatActivity) {
            androidx.appcompat.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("");
            }
        }
        if (AuthManager.isAuthenticated(requireContext())) {
            fetchScoreboard();
        }
    }

    private void fetchScoreboard() {
        String serverUrl = AuthManager.getServerUrl(requireContext());
        String sessionCookie = AuthManager.getMobileSessionCookie(requireContext());

        if (serverUrl.isEmpty() || sessionCookie.isEmpty()) {
            showOfflineView();
            return;
        }

        showLoading();

        final String endpointUrl = serverUrl.replaceAll("/+$", "") + "/mobileScoreboard";
        final Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            int status = -1;
            String responseBody = null;
            Exception failure = null;
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(endpointUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(10_000);
                conn.setRequestProperty("Cookie", sessionCookie);

                status = conn.getResponseCode();
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        status == HttpURLConnection.HTTP_OK
                                ? conn.getInputStream() : conn.getErrorStream(),
                        StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                responseBody = sb.toString();
            } catch (Exception e) {
                Log.e(TAG, "Scoreboard fetch failed: " + e.getMessage());
                failure = e;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            final int finalStatus = status;
            final String finalBody = responseBody;
            final Exception finalFailure = failure;
            mainHandler.post(() -> handleResponse(finalStatus, finalBody, finalFailure));
        }).start();
    }

    private void handleResponse(int status, String responseBody, Exception failure) {
        if (!isAdded()) {
            return;
        }

        refreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.GONE);

        if (failure != null) {
            showErrorView("Could not reach server: " + failure.getMessage());
            return;
        }

        if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
            showOfflineView();
            return;
        }

        try {
            JSONObject json = new JSONObject(responseBody);
            boolean available = json.optBoolean("available", false);
            if (!available) {
                showErrorView(json.optString("message", "Could not load the scoreboard"));
                return;
            }

            JSONArray rows = json.optJSONArray("scoreboard");
            List<ScoreboardEntry> entries = new ArrayList<>();
            if (rows != null) {
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);
                    entries.add(new ScoreboardEntry(
                            row.optInt("place"),
                            row.optString("username"),
                            row.optInt("score"),
                            row.optInt("goldMedalCount"),
                            row.optInt("silverMedalCount"),
                            row.optInt("bronzeMedalCount")));
                }
            }

            if (entries.isEmpty()) {
                showEmptyView();
            } else {
                showScoreboard(entries);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse scoreboard response: " + e.getMessage());
            showErrorView("Could not load the scoreboard");
        }
    }

    private void showLoading() {
        offlineView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        if (!refreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void showOfflineView() {
        refreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        offlineView.setVisibility(View.VISIBLE);
    }

    private void showErrorView(String message) {
        refreshLayout.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        offlineView.setVisibility(View.GONE);
        errorMessageText.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private void showEmptyView() {
        refreshLayout.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        offlineView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void showScoreboard(List<ScoreboardEntry> entries) {
        errorView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        offlineView.setVisibility(View.GONE);
        adapter.setEntries(entries);
        refreshLayout.setVisibility(View.VISIBLE);
    }

    // Data class for a single scoreboard row
    static class ScoreboardEntry {
        int place;
        String username;
        int score;
        int goldMedalCount;
        int silverMedalCount;
        int bronzeMedalCount;

        ScoreboardEntry(int place, String username, int score,
                        int goldMedalCount, int silverMedalCount, int bronzeMedalCount) {
            this.place = place;
            this.username = username;
            this.score = score;
            this.goldMedalCount = goldMedalCount;
            this.silverMedalCount = silverMedalCount;
            this.bronzeMedalCount = bronzeMedalCount;
        }
    }

    // RecyclerView Adapter
    static class ScoreboardAdapter extends RecyclerView.Adapter<ScoreboardAdapter.ViewHolder> {
        private final List<ScoreboardEntry> entries;

        ScoreboardAdapter(List<ScoreboardEntry> entries) {
            this.entries = entries;
        }

        void setEntries(List<ScoreboardEntry> newEntries) {
            entries.clear();
            entries.addAll(newEntries);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_scoreboard_entry, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ScoreboardEntry entry = entries.get(position);
            holder.placeText.setText(String.valueOf(entry.place));
            holder.usernameText.setText(entry.username);
            holder.scoreText.setText(String.valueOf(entry.score));
            holder.goldText.setText(String.valueOf(entry.goldMedalCount));
            holder.silverText.setText(String.valueOf(entry.silverMedalCount));
            holder.bronzeText.setText(String.valueOf(entry.bronzeMedalCount));
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView placeText;
            TextView usernameText;
            TextView goldText;
            TextView silverText;
            TextView bronzeText;
            TextView scoreText;

            ViewHolder(View itemView) {
                super(itemView);
                placeText = itemView.findViewById(R.id.entry_place);
                usernameText = itemView.findViewById(R.id.entry_username);
                goldText = itemView.findViewById(R.id.entry_medal_gold);
                silverText = itemView.findViewById(R.id.entry_medal_silver);
                bronzeText = itemView.findViewById(R.id.entry_medal_bronze);
                scoreText = itemView.findViewById(R.id.entry_score);
            }
        }
    }
}
