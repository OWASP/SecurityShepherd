package org.owasp.mobileshepherd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        Button enterAppButton = findViewById(R.id.enter_app_button);
        enterAppButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        runEntranceAnimations();
    }

    private void runEntranceAnimations() {
        TextView title          = findViewById(R.id.landing_title);
        TextView subtitle       = findViewById(R.id.landing_subtitle);
        View     warningBox     = findViewById(R.id.landing_warning_box);
        TextView description    = findViewById(R.id.landing_description);
        View     enterAppButton = findViewById(R.id.enter_app_button);
        TextView license        = findViewById(R.id.landing_license);

        for (View v : new View[]{title, subtitle, warningBox, description, enterAppButton, license}) {
            v.setAlpha(0f);
            v.setTranslationY(dpToPx(40));
        }

        animateSlideUp(title,       500,  150);
        animateSlideUp(subtitle,    450,  300);
        animateSlideUp(warningBox,  400,  440);
        animateSlideUp(description, 400,  560);

        // Button with extra overshoot for emphasis
        enterAppButton.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(660)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();

        animateSlideUp(license, 300, 760);
    }

    private void animateSlideUp(View v, long duration, long delay) {
        v.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(duration)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
