package org.owasp.mobileshepherd.utils;

import android.graphics.drawable.GradientDrawable;
import android.view.View;
import androidx.core.content.ContextCompat;
import org.owasp.mobileshepherd.R;

/**
 * Utility class for applying visual feedback to completed lessons and challenges
 */
public class CompletionVisualFeedback {
    
    public static void applyCompletedBorder(View rootView, boolean isCompleted) {
        if (isCompleted) {
            // Create a green border drawable
            GradientDrawable border = new GradientDrawable();
            border.setShape(GradientDrawable.RECTANGLE);
            border.setStroke(8, ContextCompat.getColor(rootView.getContext(), R.color.shepherd_green));
            border.setCornerRadius(16f);
            
            // Apply the border to the root view
            rootView.setBackground(border);
            rootView.setPadding(4, 4, 4, 4);
        }
    }
    
    public static void applyCompletedBorderToCard(View cardView, boolean isCompleted) {
        if (isCompleted && cardView != null) {
            // For MaterialCardView, we can set stroke color
            try {
                com.google.android.material.card.MaterialCardView materialCard = 
                    (com.google.android.material.card.MaterialCardView) cardView;
                materialCard.setStrokeColor(ContextCompat.getColor(cardView.getContext(), R.color.shepherd_green));
                materialCard.setStrokeWidth(8);
            } catch (ClassCastException e) {
                // If it's not a MaterialCardView, apply regular border
                applyCompletedBorder(cardView, isCompleted);
            }
        }
    }
}
