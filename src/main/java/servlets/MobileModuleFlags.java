package servlets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Server-side base flag values for mobile modules.
 *
 * <p>These plaintext strings are <strong>never</strong> sent to the Android client. Only their HMAC
 * — keyed with the server's ephemeral key and the authenticated user's name — is returned via
 * {@code MobileFlagGet}. This ensures every student receives a unique flag that cannot be
 * precomputed by decompiling the APK.
 *
 * <p>Add an entry here when a new mobile module is promoted to server-side validation.
 *
 * <p>This file is part of the Security Shepherd Project.
 *
 * <p>The Security Shepherd project is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.<br>
 *
 * <p>You should have received a copy of the GNU General Public License along with the Security
 * Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Sean Duggan
 */
final class MobileModuleFlags {

  private MobileModuleFlags() {}

  /**
   * Maps mobile module ID strings to their corresponding database moduleId UUIDs. Used by
   * MobileFlagSubmit to record completion and award points via Setter.updatePlayerResult.
   */
  static final Map<String, String> MODULE_DB_IDS;

  static final Map<String, String> BASE_FLAGS;

  static {
    Map<String, String> m = new HashMap<>();
    // Client-Side Injection (M7 / SQLite)
    m.put("client_side_injection_lesson", "Marble_Rolls_Past_The_Cat");
    m.put("client_side_injection_challenge_1", "Bitter_Snake_Finds_The_Lock");
    m.put("client_side_injection_challenge_2", "Hollow_Bridge_Holds_No_Rain");
    // Poor Authentication (M3) — flag revealed after cracking hardcoded PIN
    m.put("poor_auth_lesson", "Taco_Snores_On_A_Couch");
    m.put("poor_auth_challenge", "Cracked_Jar_Holds_Old_Honey");
    // Insecure Authorization (M3) — flag revealed after privilege escalation via SharedPreferences
    m.put("insecure_auth_lesson", "Purple_Boots_Dance_On_Ice");
    // Input Validation (M4) — flag revealed after URL validation bypass
    m.put("input_validation_lesson", "Seven_Clouds_Chase_A_Kite");
    // Supply Chain (M6) — flag obtained from vulnerable dependency debug logs
    m.put("supply_chain_lesson", "Yellow_Lamp_Hums_All_Night");
    // Reverse Engineering (M9) — offline-only lesson; static flag lives in the APK by design.
    // Not included here: MobileFlagGet will reject requests for this module, preventing API
    // farming.
    // Security Misconfiguration (M1) — flag exposed via exported activity
    m.put("security_misconfig_lesson", "Quiet_Frogs_Jump_On_Tuesday");
    m.put("security_misconfig_challenge_2", "Faded_Map_Leads_Nowhere_Fast");
    // Privacy Controls (M8) — flag embedded in image EXIF metadata
    m.put("privacy_lesson", "Silver_Moths_Dream_Of_Keys");
    // Insecure Data Storage (M2) — flag stored as plaintext password in SQLite and
    // SharedPreferences
    m.put("ids_lesson", "Copper_Bridge_Stands_In_Rain");
    m.put("ids_challenge_1", "Tangled_Rope_Skips_A_Stone");
    // Insecure Communication (M5) — flag transmitted in HTTP JSON response body; intercept with
    // proxy
    m.put("insecure_comm_lesson", "Green_Socks_Float_Past_Reeds");
    m.put("insecure_comm_challenge", "Empty_Wave_Breaks_On_Shore");
    // Insufficient Cryptography (M6) — flag encrypted with weak DES key
    m.put("insufficient_crypto_lesson", "Dusty_Mirror_Speaks_In_Whispers");
    m.put("insufficient_crypto_challenge", "Blind_Fish_Swims_In_Salt");
    BASE_FLAGS = Collections.unmodifiableMap(m);

    Map<String, String> ids = new HashMap<>();
    // Existing mobile modules already in the platform DB
    ids.put("ids_lesson", "53a53a66cb3bf3e4c665c442425ca90e29536edd");
    ids.put("ids_challenge_1", "307f78f18fd6a87e50ed6705231a9f24cd582574");
    // RE challenges use hardcodedKey=1 (client-side SHA-256); DB IDs registered for module list
    // display
    ids.put("re_challenge_1", "52885a3db5b09adc24f38bc453fe348f850649b3");
    ids.put("poor_auth_lesson", "0cdd1549e7c74084d7059ce748b93ef657b44457");
    ids.put("poor_auth_challenge", "ba6e65e4881c8499b5e53eb33b5be6b5d0f1fb2c");
    ids.put("client_side_injection_lesson", "335440fef02d19259254ed88293b62f31cccdd41");
    ids.put("client_side_injection_challenge_1", "a3f7ffd0f9c3d15564428d4df0b91bd927e4e5e4");
    ids.put("client_side_injection_challenge_2", "e635fce334aa61fdaa459c21c286d6332eddcdd3");
    // New mobile-specific modules added to the platform
    ids.put("insecure_auth_lesson", "0f40ae03b9339cb88fbd834213ee1c597791274a");
    ids.put("insecure_comm_lesson", "a76d11ebd575aecfba5d69441cbd90c95e8abe31");
    ids.put("insecure_comm_challenge", "d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7");
    ids.put("insufficient_crypto_lesson", "3385d879b0da97597e16e5c8a7511a6ec331d1d9");
    ids.put("insufficient_crypto_challenge", "3f010a976bcbd6a37fba4a10e4a057acc80bdc09");
    ids.put("security_misconfig_lesson", "c85dad7f468a333e53edaca90a435528db76d118");
    ids.put("security_misconfig_challenge_2", "e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8");
    ids.put("input_validation_lesson", "708b76213e50409e138fc68eba81ed7ec8fccf08");
    ids.put("privacy_lesson", "952c4c3785d8bd8d51bb0d0161c3f6997dd01863");
    ids.put("supply_chain_lesson", "b9c1f4a7e2d3b5c8f6e1a4d7c2b9f3e8a5d1c6b3");
    MODULE_DB_IDS = Collections.unmodifiableMap(ids);
  }

  /**
   * Strips CR, LF, and tab characters from a user-supplied string before it is interpolated into a
   * log message, preventing log injection attacks.
   */
  static String sanitize(String input) {
    if (input == null) return "(null)";
    return input.replaceAll("[\r\n\t]", "_");
  }
}
