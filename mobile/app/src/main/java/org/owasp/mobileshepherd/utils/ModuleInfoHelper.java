package org.owasp.mobileshepherd.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.EnumMap;
import java.util.Map;

public final class ModuleInfoHelper {

    public static final class ModuleInfo {
        public final String levelType;
        public final String name;
        public final String topTenRisk;
        public final String location;

        ModuleInfo(String levelType, String name, String topTenRisk, String location) {
            this.levelType = levelType;
            this.name = name;
            this.topTenRisk = topTenRisk;
            this.location = location;
        }

        public String toJson() {
            return "{\n"
                    + "  \"level_type\": \""
                    + levelType
                    + "\",\n"
                    + "  \"name\": \""
                    + name
                    + "\",\n"
                    + "  \"top_ten_risk\": \""
                    + topTenRisk
                    + "\",\n"
                    + "  \"location\": \""
                    + location
                    + "\"\n"
                    + "}";
        }
    }

    private static final Map<KeyValidator.Module, ModuleInfo> REGISTRY =
            new EnumMap<>(KeyValidator.Module.class);

    static {
        REGISTRY.put(
                KeyValidator.Module.IDS_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Insecure Data Storage",
                        "M9",
                        "org.owasp.mobileshepherd.ui.lessons.insecuredata"));
        REGISTRY.put(
                KeyValidator.Module.IDS_CHALLENGE_1,
                new ModuleInfo(
                        "challenge",
                        "Insecure Data Storage 1",
                        "M9",
                        "org.owasp.mobileshepherd.ui.challenges.insecuredata1"));
    }

    public static void showDialog(Context context, KeyValidator.Module module) {
        ModuleInfo info = REGISTRY.get(module);
        String json =
                info != null ? info.toJson() : "{\n  \"error\": \"Module info not found\"\n}";

        TextView tv = new TextView(context);
        tv.setText(json);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setPadding(48, 32, 48, 32);
        tv.setTextSize(13f);

        ScrollView scrollView = new ScrollView(context);
        scrollView.addView(tv);

        new AlertDialog.Builder(context)
                .setTitle("Module Info")
                .setView(scrollView)
                .setPositiveButton("OK", null)
                .show();
    }

    private ModuleInfoHelper() {}
}
