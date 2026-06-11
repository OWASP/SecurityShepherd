package org.owasp.mobileshepherd.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Tracks user progress through lessons and challenges.
 * Stores completion status in SharedPreferences.
 */
public class ProgressTracker {
    
    private static final String PREFS_NAME = "MobileShepherd_Progress";
    private static final String KEY_COMPLETED_MODULES = "completed_modules";
    private static final String KEY_FIRST_COMPLETION_TIME = "first_completion_";
    private static final String KEY_LAST_COMPLETION_TIME = "last_completion_";
    private static final String KEY_COMPLETION_COUNT = "completion_count_";
    
    private final SharedPreferences prefs;
    private static CompletionChangeListener globalListener;
    
    public interface CompletionChangeListener {
        void onCompletionChanged();
    }
    
    public static void setGlobalCompletionListener(CompletionChangeListener listener) {
        globalListener = listener;
    }

    public static CompletionChangeListener getGlobalCompletionListener() {
        return globalListener;
    }
    
    public ProgressTracker(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Marks a module as completed.
     * 
     * @param module The module that was completed
     */
    public void markCompleted(FlagValidator.Module module) {
        Set<String> completed = getCompletedModules();
        boolean wasNew = completed.add(module.getId());
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_COMPLETED_MODULES, completed);
        
        long currentTime = System.currentTimeMillis();
        
        // Track first completion time
        if (wasNew) {
            editor.putLong(KEY_FIRST_COMPLETION_TIME + module.getId(), currentTime);
        }
        
        // Always update last completion time
        editor.putLong(KEY_LAST_COMPLETION_TIME + module.getId(), currentTime);
        
        // Increment completion count
        int count = getCompletionCount(module);
        editor.putInt(KEY_COMPLETION_COUNT + module.getId(), count + 1);
        
        editor.apply();
        
        // Notify listener of change
        if (globalListener != null) {
            globalListener.onCompletionChanged();
        }
    }
    
    /**
     * Marks a module as not completed (undo completion).
     * 
     * @param module The module to unmark
     */
    public void unmarkCompleted(FlagValidator.Module module) {
        Set<String> completed = getCompletedModules();
        completed.remove(module.getId());
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_COMPLETED_MODULES, completed);
        editor.apply();
        
        // Notify listener of change
        if (globalListener != null) {
            globalListener.onCompletionChanged();
        }
    }
    
    /**
     * Toggles completion status of a module.
     * 
     * @param module The module to toggle
     * @return true if now marked as completed, false if now marked as incomplete
     */
    public boolean toggleCompleted(FlagValidator.Module module) {
        if (isCompleted(module)) {
            unmarkCompleted(module);
            return false;
        } else {
            markCompleted(module);
            return true;
        }
    }
    
    /**
     * Checks if a module has been completed.
     * 
     * @param module The module to check
     * @return true if completed, false otherwise
     */
    public boolean isCompleted(FlagValidator.Module module) {
        Set<String> completed = getCompletedModules();
        return completed.contains(module.getId());
    }
    
    /**
     * Gets the set of all completed module IDs.
     * 
     * @return Set of completed module IDs
     */
    public Set<String> getCompletedModules() {
        return new HashSet<>(prefs.getStringSet(KEY_COMPLETED_MODULES, new HashSet<>()));
    }
    
    /**
     * Gets the timestamp of when a module was first completed.
     * 
     * @param module The module to check
     * @return Timestamp in milliseconds, or 0 if not completed
     */
    public long getFirstCompletionTime(FlagValidator.Module module) {
        return prefs.getLong(KEY_FIRST_COMPLETION_TIME + module.getId(), 0);
    }
    
    /**
     * Gets the timestamp of when a module was last completed.
     * 
     * @param module The module to check
     * @return Timestamp in milliseconds, or 0 if not completed
     */
    public long getLastCompletionTime(FlagValidator.Module module) {
        return prefs.getLong(KEY_LAST_COMPLETION_TIME + module.getId(), 0);
    }
    
    /**
     * Gets the number of times a module has been completed.
     * 
     * @param module The module to check
     * @return Completion count
     */
    public int getCompletionCount(FlagValidator.Module module) {
        return prefs.getInt(KEY_COMPLETION_COUNT + module.getId(), 0);
    }
    
    /**
     * Gets the total number of completed modules.
     * 
     * @return Count of completed modules
     */
    public int getTotalCompletedCount() {
        return getCompletedModules().size();
    }
    
    /**
     * Gets the total number of completed challenges.
     * 
     * @return Count of completed challenges
     */
    public int getCompletedChallengesCount() {
        int count = 0;
        for (String moduleId : getCompletedModules()) {
            // Check if module is a challenge by looking it up
            for (FlagValidator.Module module : FlagValidator.Module.values()) {
                if (module.getId().equals(moduleId) && 
                    module.getType().equals(FlagValidator.TYPE_CHALLENGE)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }
    
    /**
     * Gets the total number of completed lessons.
     * 
     * @return Count of completed lessons
     */
    public int getCompletedLessonsCount() {
        int count = 0;
        for (String moduleId : getCompletedModules()) {
            // Check if module is a lesson by looking it up
            for (FlagValidator.Module module : FlagValidator.Module.values()) {
                if (module.getId().equals(moduleId) && 
                    module.getType().equals(FlagValidator.TYPE_LESSON)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }
    
    /**
     * Gets the total number of available challenges.
     * 
     * @return Total challenge count
     */
    public int getTotalChallengesCount() {
        int count = 0;
        for (FlagValidator.Module module : FlagValidator.Module.values()) {
            if (module.getType().equals(FlagValidator.TYPE_CHALLENGE)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Gets the total number of available lessons.
     * 
     * @return Total lesson count
     */
    public int getTotalLessonsCount() {
        int count = 0;
        for (FlagValidator.Module module : FlagValidator.Module.values()) {
            if (module.getType().equals(FlagValidator.TYPE_LESSON)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Calculates the completion percentage.
     * 
     * @return Percentage (0-100)
     */
    public int getCompletionPercentage() {
        int total = FlagValidator.Module.values().length;
        int completed = getTotalCompletedCount();
        if (total == 0) return 0;
        return (int) ((completed * 100.0) / total);
    }
    
    /**
     * Resets all progress (for testing or user request).
     */
    public void resetProgress() {
        prefs.edit().clear().apply();
    }

    /**
     * Clears all stored progress for any user. Call on logout so the next
     * user starts with a clean state.
     */
    public static void clearAll(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }
    
    /**
     * Removes completion status for a specific module.
     * 
     * @param module The module to reset
     */
    public void resetModule(FlagValidator.Module module) {
        Set<String> completed = getCompletedModules();
        completed.remove(module.getId());
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_COMPLETED_MODULES, completed);
        editor.remove(KEY_FIRST_COMPLETION_TIME + module.getId());
        editor.remove(KEY_LAST_COMPLETION_TIME + module.getId());
        editor.remove(KEY_COMPLETION_COUNT + module.getId());
        editor.apply();
    }
}
