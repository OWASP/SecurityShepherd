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

    private static final Map<FlagValidator.Module, ModuleInfo> REGISTRY =
            new EnumMap<>(FlagValidator.Module.class);

    static {
        // Lessons
        REGISTRY.put(
                FlagValidator.Module.RE_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Reverse Engineering",
                        "M7",
                        "org.owasp.mobileshepherd.ui.lessons"));
        REGISTRY.put(
                FlagValidator.Module.IDS_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Insecure Data Storage",
                        "M9",
                        "org.owasp.mobileshepherd.ui.lessons.insecuredata"));
        REGISTRY.put(
                FlagValidator.Module.POOR_AUTH_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Poor Authentication",
                        "M3",
                        "org.owasp.mobileshepherd.ui.lessons.poorauth"));
        REGISTRY.put(
                FlagValidator.Module.INSECURE_AUTH_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Insecure Authorization",
                        "M3",
                        "org.owasp.mobileshepherd.ui.lessons.insecureauthorization"));
        REGISTRY.put(
                FlagValidator.Module.SUPPLY_CHAIN_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Supply Chain Security",
                        "M2",
                        "org.owasp.mobileshepherd.ui.lessons.supplychain"));
        REGISTRY.put(
                FlagValidator.Module.INSECURE_COMM_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Insecure Communication",
                        "M5",
                        "org.owasp.mobileshepherd.ui.lessons.insecurecomm"));
        REGISTRY.put(
                FlagValidator.Module.INSUFFICIENT_CRYPTO_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Insufficient Cryptography",
                        "M10",
                        "org.owasp.mobileshepherd.ui.lessons.crypto"));
        REGISTRY.put(
                FlagValidator.Module.SECURITY_MISCONFIG_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Security Misconfiguration",
                        "M8",
                        "org.owasp.mobileshepherd.ui.lessons.securitymisconfig"));
        REGISTRY.put(
                FlagValidator.Module.INPUT_VALIDATION_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Input Validation",
                        "M4",
                        "org.owasp.mobileshepherd.ui.lessons"));
        REGISTRY.put(
                FlagValidator.Module.PRIVACY_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Privacy Controls",
                        "M6",
                        "org.owasp.mobileshepherd.ui.lessons.privacycontrols"));
        REGISTRY.put(
                FlagValidator.Module.CLIENT_SIDE_INJECTION_LESSON,
                new ModuleInfo(
                        "lesson",
                        "Client-Side Injection",
                        "M4",
                        "org.owasp.mobileshepherd.ui.lessons.clientsideinjection"));

        // Challenges
        REGISTRY.put(
                FlagValidator.Module.RE_CHALLENGE_1,
                new ModuleInfo(
                        "challenge",
                        "Reverse Engineering 1",
                        "M7",
                        "org.owasp.mobileshepherd.ui.challenges.reverseengineering"));
        REGISTRY.put(
                FlagValidator.Module.IDS_CHALLENGE_1,
                new ModuleInfo(
                        "challenge",
                        "Insecure Data Storage 1",
                        "M9",
                        "org.owasp.mobileshepherd.ui.challenges.insecuredata1"));
        REGISTRY.put(
                FlagValidator.Module.POOR_AUTH_CHALLENGE,
                new ModuleInfo(
                        "challenge",
                        "Poor Authentication",
                        "M3",
                        "org.owasp.mobileshepherd.ui.challenges.poorauth"));
        REGISTRY.put(
                FlagValidator.Module.INSECURE_COMM_CHALLENGE,
                new ModuleInfo(
                        "challenge",
                        "Insecure Communication 1",
                        "M5",
                        "org.owasp.mobileshepherd.ui.challenges.insecurecomm"));
        REGISTRY.put(
                FlagValidator.Module.INSUFFICIENT_CRYPTO_CHALLENGE,
                new ModuleInfo(
                        "challenge",
                        "Insufficient Cryptography 1",
                        "M10",
                        "org.owasp.mobileshepherd.ui.challenges.crypto"));
        REGISTRY.put(
                FlagValidator.Module.SECURITY_MISCONFIG_CHALLENGE_2,
                new ModuleInfo(
                        "challenge",
                        "Security Misconfiguration",
                        "M8",
                        "org.owasp.mobileshepherd.ui.challenges.securitymisconfig"));
        REGISTRY.put(
                FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_1,
                new ModuleInfo(
                        "challenge",
                        "Client-Side Injection 1",
                        "M4",
                        "org.owasp.mobileshepherd.ui.challenges.clientsideinjection"));
        REGISTRY.put(
                FlagValidator.Module.CLIENT_SIDE_INJECTION_CHALLENGE_2,
                new ModuleInfo(
                        "challenge",
                        "Client-Side Injection 2",
                        "M4",
                        "org.owasp.mobileshepherd.ui.challenges.clientsideinjection"));
    }

    public static void showDialog(Context context, FlagValidator.Module module) {
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
