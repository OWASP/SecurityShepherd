package org.owasp.mobileshepherd.ui.lessons.securitymisconfig;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.owasp.mobileshepherd.R;
import org.owasp.mobileshepherd.utils.KeyProvider;
import org.owasp.mobileshepherd.utils.KeyValidator;
import org.owasp.mobileshepherd.utils.ProgressTracker;

public class MisconfigLessonSecretActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_misconfig_lesson_secret);

        ProgressTracker progressTracker = new ProgressTracker(this);

        TextView keyText = findViewById(R.id.secret_key_text);
        TextView messageText = findViewById(R.id.secret_message_text);

        keyText.setText("Loading...");
        KeyProvider.getKey(this, KeyValidator.Module.SECURITY_MISCONFIG_LESSON,
                keyValue -> {
                    keyText.setText(keyValue);
                    KeyValidator.validateKey(MisconfigLessonSecretActivity.this,
                            KeyValidator.Module.SECURITY_MISCONFIG_LESSON,
                            keyValue,
                            correct -> {
                                Log.d("MisconfigLesson", "Server submission: " + correct);
                                if (isFinishing() || isDestroyed() || !correct) return;
                                progressTracker.markCompleted(KeyValidator.Module.SECURITY_MISCONFIG_LESSON);
                                Toast.makeText(MisconfigLessonSecretActivity.this,
                                        "Lesson complete!", Toast.LENGTH_LONG).show();
                            });
                });

        String message = "SECURITY VULNERABILITY EXPLOITED!\n\n" +
                "This activity is marked as 'exported=\"true\"' in AndroidManifest.xml, " +
                "allowing ANY app (or ADB command) to invoke it without permission checks.\n\n" +
                "Attacker Command Used:\n" +
                "adb shell am start -n org.owasp.mobileshepherd/.ui.lessons.securitymisconfig.MisconfigLessonSecretActivity\n\n" +
                "In a real app, this could expose:\n" +
                "- Administrative functions\n" +
                "- Password reset screens\n" +
                "- Data export utilities\n" +
                "- Debug/testing features\n\n" +
                "FIX: Set android:exported=\"false\" or add proper permission requirements!";

        messageText.setText(message);
    }
}
