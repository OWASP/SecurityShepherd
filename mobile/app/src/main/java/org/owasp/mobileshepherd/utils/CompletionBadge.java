package org.owasp.mobileshepherd.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import org.owasp.mobileshepherd.R;

/**
 * Manages completion badges and visual indicators for progress tracking.
 */
public class CompletionBadge {
    
    public enum BadgeType {
        FIRST_BLOOD("First Blood", "Complete your first challenge", android.R.drawable.star_on),
        LESSON_MASTER("Lesson Master", "Complete all lessons", android.R.drawable.star_on),
        CHALLENGE_CRUSHER("Challenge Crusher", "Complete all challenges", android.R.drawable.star_on),
        SPEED_RUNNER("Speed Runner", "Complete a challenge in under 5 minutes", android.R.drawable.star_on),
        PERFECTIONIST("Perfectionist", "100% completion", android.R.drawable.star_on),
        SQL_NINJA("SQL Ninja", "Complete all injection challenges", android.R.drawable.star_on),
        CRYPTO_BREAKER("Crypto Breaker", "Complete all cryptography challenges", android.R.drawable.star_on),
        REVERSE_ENGINEER("Reverse Engineer", "Complete all RE challenges", android.R.drawable.star_on);
        
        private final String title;
        private final String description;
        private final int iconResId;
        
        BadgeType(String title, String description, int iconResId) {
            this.title = title;
            this.description = description;
            this.iconResId = iconResId;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getIconResId() {
            return iconResId;
        }
    }
    
    private final Context context;
    private final ProgressTracker progressTracker;
    
    public CompletionBadge(Context context) {
        this.context = context;
        this.progressTracker = new ProgressTracker(context);
    }
    
    /**
     * Checks if user has earned a specific badge.
     */
    public boolean hasBadge(BadgeType badge) {
        switch (badge) {
            case FIRST_BLOOD:
                return progressTracker.getTotalCompletedCount() >= 1;
                
            case LESSON_MASTER:
                return progressTracker.getCompletedLessonsCount() == 
                       progressTracker.getTotalLessonsCount();
                
            case CHALLENGE_CRUSHER:
                return progressTracker.getCompletedChallengesCount() == 
                       progressTracker.getTotalChallengesCount();
                
            case PERFECTIONIST:
                return progressTracker.getCompletionPercentage() == 100;
                
            case SPEED_RUNNER:
                // TODO: Implement time tracking for speed runs
                return false;
                
            case SQL_NINJA:
                return isCategoryComplete("injection");
                
            case CRYPTO_BREAKER:
                return isCategoryComplete("crypto");
                
            case REVERSE_ENGINEER:
                return isCategoryComplete("re");
                
            default:
                return false;
        }
    }
    
    /**
     * Checks if all modules in a category are completed.
     */
    private boolean isCategoryComplete(String category) {
        int total = 0;
        int completed = 0;
        
        for (FlagValidator.Module module : FlagValidator.Module.values()) {
            if (module.getId().contains(category)) {
                total++;
                if (progressTracker.isCompleted(module)) {
                    completed++;
                }
            }
        }
        
        return total > 0 && completed == total;
    }
    
    /**
     * Gets the completion icon for a module.
     */
    public static Drawable getCompletionIcon(Context context, boolean isCompleted) {
        int resId = isCompleted ? android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background;
        return ContextCompat.getDrawable(context, resId);
    }
    
    /**
     * Gets a progress summary string.
     */
    public String getProgressSummary() {
        int completed = progressTracker.getTotalCompletedCount();
        int total = FlagValidator.Module.values().length;
        int percentage = progressTracker.getCompletionPercentage();
        
        StringBuilder summary = new StringBuilder();
        summary.append("Progress: ").append(completed).append("/").append(total);
        summary.append(" (").append(percentage).append("%)");
        
        if (percentage == 100) {
            summary.append(" ");
        } else if (percentage >= 75) {
            summary.append(" ");
        } else if (percentage >= 50) {
            summary.append(" [STRONG]");
        } else if (percentage >= 25) {
            summary.append(" [START]");
        } else if (percentage > 0) {
            summary.append(" ");
        }
        
        return summary.toString();
    }
    
    /**
     * Gets motivational message based on progress.
     */
    public String getMotivationalMessage() {
        int percentage = progressTracker.getCompletionPercentage();
        
        if (percentage == 0) {
            return "Start your security journey! ";
        } else if (percentage < 25) {
            return "Great start! Keep going! [START]";
        } else if (percentage < 50) {
            return "You're making progress! [STRONG]";
        } else if (percentage < 75) {
            return "Over halfway there! ";
        } else if (percentage < 100) {
            return "Almost done! Finish strong! ⭐";
        } else {
            return "Perfect score! You're a security expert! [TROPHY]";
        }
    }
}
