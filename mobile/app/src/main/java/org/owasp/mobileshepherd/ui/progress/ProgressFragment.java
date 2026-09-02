package org.owasp.mobileshepherd.ui.progress;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.databinding.FragmentProgressBinding;
import org.owasp.mobileshepherd.utils.FlagValidator;
import org.owasp.mobileshepherd.utils.ProgressTracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProgressFragment extends Fragment {

    private FragmentProgressBinding binding;
    private ProgressTracker progressTracker;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProgressBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressTracker = new ProgressTracker(requireContext());

        setupProgressOverview();
        setupModuleList();

        return root;
    }

    private void setupProgressOverview() {
        int totalModules = FlagValidator.Module.values().length;
        int completedModules = progressTracker.getTotalCompletedCount();
        int completionPercentage = progressTracker.getCompletionPercentage();

        int completedChallenges = progressTracker.getCompletedChallengesCount();
        int totalChallenges = progressTracker.getTotalChallengesCount();

        int completedLessons = progressTracker.getCompletedLessonsCount();
        int totalLessons = progressTracker.getTotalLessonsCount();

        binding.overallProgress.setProgress(completionPercentage);
        binding.overallPercentageText.setText(completionPercentage + "%");
        binding.completedCountText.setText(completedModules + " / " + totalModules + " modules");

        binding.challengesProgressText.setText(completedChallenges + " / " + totalChallenges + " completed");
        binding.lessonsProgressText.setText(completedLessons + " / " + totalLessons + " completed");

        if (completionPercentage == 100) {
            binding.congratulationsText.setVisibility(View.VISIBLE);
        }
    }

    private void setupModuleList() {
        List<ModuleItem> modules = new ArrayList<>();

        for (FlagValidator.Module module : FlagValidator.Module.values()) {
            boolean isCompleted = progressTracker.isCompleted(module);
            long completionTime = progressTracker.getFirstCompletionTime(module);
            int completionCount = progressTracker.getCompletionCount(module);

            modules.add(new ModuleItem(
                    module,
                    getModuleDisplayName(module),
                    module.getType(),
                    isCompleted,
                    completionTime,
                    completionCount
            ));
        }

        ModuleAdapter adapter = new ModuleAdapter(modules);
        binding.moduleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.moduleRecyclerView.setAdapter(adapter);
    }

    private String getModuleDisplayName(FlagValidator.Module module) {
        // Convert enum name to readable format
        String name = module.name().replace("_", " ");
        StringBuilder result = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Data class for module items
    static class ModuleItem {
        FlagValidator.Module module;
        String displayName;
        String type;
        boolean isCompleted;
        long completionTime;
        int completionCount;

        ModuleItem(FlagValidator.Module module, String displayName, String type,
                   boolean isCompleted, long completionTime, int completionCount) {
            this.module = module;
            this.displayName = displayName;
            this.type = type;
            this.isCompleted = isCompleted;
            this.completionTime = completionTime;
            this.completionCount = completionCount;
        }
    }

    // RecyclerView Adapter
    static class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {
        private final List<ModuleItem> modules;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        ModuleAdapter(List<ModuleItem> modules) {
            this.modules = modules;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_module_progress, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ModuleItem item = modules.get(position);

            holder.nameText.setText(item.displayName);
            holder.typeText.setText(item.type.toUpperCase());

            if (item.isCompleted) {
                holder.statusIcon.setImageResource(R.drawable.ic_status_complete);
                holder.statusIcon.setImageTintList(ColorStateList.valueOf(
                        holder.itemView.getContext().getColor(R.color.success_green)));
                holder.statusIcon.setContentDescription("Completed");
                holder.statusText.setText("Completed");
                holder.statusText.setTextColor(holder.itemView.getContext()
                        .getColor(R.color.success_green));

                String completionInfo = dateFormat.format(new Date(item.completionTime));
                if (item.completionCount > 1) {
                    completionInfo += " (" + item.completionCount + "x)";
                }
                holder.dateText.setText(completionInfo);
                holder.dateText.setVisibility(View.VISIBLE);
            } else {
                holder.statusIcon.setImageResource(R.drawable.ic_status_incomplete);
                holder.statusIcon.setImageTintList(ColorStateList.valueOf(
                        holder.itemView.getContext().getColor(R.color.text_secondary)));
                holder.statusIcon.setContentDescription(
                        holder.itemView.getContext().getString(R.string.not_started));
                holder.statusText.setText("Not Started");
                holder.statusText.setTextColor(holder.itemView.getContext()
                        .getColor(R.color.text_secondary));
                holder.dateText.setVisibility(View.GONE);
            }

            // Set background for challenge vs lesson
            if (item.type.equals(FlagValidator.TYPE_CHALLENGE)) {
                holder.typeText.setBackgroundColor(holder.itemView.getContext()
                        .getColor(R.color.challenge_badge));
            } else {
                holder.typeText.setBackgroundColor(holder.itemView.getContext()
                        .getColor(R.color.lesson_badge));
            }
        }

        @Override
        public int getItemCount() {
            return modules.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            TextView typeText;
            ImageView statusIcon;
            TextView statusText;
            TextView dateText;

            ViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.module_name);
                typeText = itemView.findViewById(R.id.module_type);
                statusIcon = (ImageView) itemView.findViewById(R.id.status_icon);
                statusText = itemView.findViewById(R.id.status_text);
                dateText = itemView.findViewById(R.id.completion_date);
            }
        }
    }
}
