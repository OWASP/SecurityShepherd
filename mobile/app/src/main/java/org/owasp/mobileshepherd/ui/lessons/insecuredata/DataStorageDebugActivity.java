package org.owasp.mobileshepherd.ui.lessons.insecuredata;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.owasp.mobileshepherd.R;

public class DataStorageDebugActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create simple layout
        TextView textView = new TextView(this);
        textView.setPadding(48, 48, 48, 48);
        textView.setTextSize(14);
        
        // Read some stored data as demonstration
        SharedPreferences prefs = getSharedPreferences("app_data", MODE_PRIVATE);
        String userId = prefs.getString("user_id", "demo_user_12345");
        String apiToken = prefs.getString("api_token", "demo_token_abc123xyz789");
        
        String message = " Data Storage Debug Panel\n\n" +
                "[OK] This activity is SAFE (exported=\"false\")\n\n" +
                "This internal debugging tool shows stored application data:\n\n" +
                "User ID: " + userId + "\n" +
                "API Token: " + apiToken + "\n\n" +
                "Since this activity is NOT exported, only the app itself can access it.\n\n" +
                "Learn more in the Insecure Data Storage lesson.";
        
        textView.setText(message);
        setContentView(textView);
    }
}
