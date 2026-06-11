#!/usr/bin/env python3
"""
Calculate SHA-256 hashes for all KEY{} format flags.
Converts from OWASP{} to KEY{} format and generates new hashes for FlagValidator.java
"""

import hashlib

def sha256_hash(text):
    """Calculate SHA-256 hash of text"""
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

# All flags in KEY{} format (converted from OWASP{})
flags = {
    # Lessons (11)
    "RE_LESSON": "KEY{R3v3rs3_Eng1n33r1ng_M4st3r_2024}",
    "IDS_LESSON": "KEY{1ns3cur3_D4t4_St0r4g3_L34k}",
    "POOR_AUTH_LESSON": "KEY{T4co_Sn0r3s_0n_4_C0uch}",
    "INSECURE_AUTH_LESSON": "KEY{Pr1v1l3g3_Esc4l4t10n_Pwn3d}",
    "SUPPLY_CHAIN_LESSON": "KEY{Vuln3r4bl3_D3p3nd3ncy}",
    "INSECURE_COMM_LESSON": "KEY{Unsecur3_HTTP_Tr4ff1c}",
    "INSUFFICIENT_CRYPTO_LESSON": "KEY{W3ak_DES_Encrypt10n}",
    "SECURITY_MISCONFIG_LESSON": "KEY{D3bugg4bl3_Fl4g_F0und}",
    "INPUT_VALIDATION_LESSON": "KEY{1nput_V4l1d4t10n_Byp4ss3d}",
    "PRIVACY_LESSON": "KEY{3x1f_M3t4d4t4_L34k5_L0c4t10n}",
    "CLIENT_SIDE_INJECTION_LESSON": "KEY{CL13NT_S1D3_SQL_1NJ3CT10N}",
    
    # Challenges (10)
    "RE_CHALLENGE_1": "KEY{S1mpl3_Fl4g_34sy_T0_F1nd}",
    "IDS_CHALLENGE_1": "KEY{SQLit3_D4t4_3xtr4ct3d}",
    "POOR_AUTH_CHALLENGE": "KEY{P00r_Auth_W34k_Qu3st10ns}",
    "INSECURE_COMM_CHALLENGE": "KEY{N3tw0rk_Sn1ff3d}",
    "INSUFFICIENT_CRYPTO_CHALLENGE": "KEY{ECB_M0d3_Vuln3r4bl3}",
    "SECURITY_MISCONFIG_CHALLENGE_2": "KEY{B4ckup_D4t4_3xtr4ct3d}",
    "CLIENT_SIDE_INJECTION_CHALLENGE_1": "KEY{SQL_1nj3ct10n_4dm1n_Pwn}",
    "CLIENT_SIDE_INJECTION_CHALLENGE_2": "KEY{UN10N_B4s3d_1nj3ct10n}",
}

print("=" * 80)
print("KEY{} FLAG SHA-256 HASHES")
print("=" * 80)
print()

print("// Lessons (11)")
for module in ["RE_LESSON", "IDS_LESSON", "POOR_AUTH_LESSON", "INSECURE_AUTH_LESSON", 
               "SUPPLY_CHAIN_LESSON", "INSECURE_COMM_LESSON", "INSUFFICIENT_CRYPTO_LESSON",
               "SECURITY_MISCONFIG_LESSON", "INPUT_VALIDATION_LESSON", "PRIVACY_LESSON",
               "CLIENT_SIDE_INJECTION_LESSON"]:
    flag = flags[module]
    hash_value = sha256_hash(flag)
    print(f'put(Module.{module}, "{hash_value}");')

print()
print("// Challenges (9)")
for module in ["RE_CHALLENGE_1",
               "IDS_CHALLENGE_1",
               "POOR_AUTH_CHALLENGE",
               "INSECURE_COMM_CHALLENGE", "INSUFFICIENT_CRYPTO_CHALLENGE",
               "SECURITY_MISCONFIG_CHALLENGE_2",
               "CLIENT_SIDE_INJECTION_CHALLENGE_1", "CLIENT_SIDE_INJECTION_CHALLENGE_2"]:
    flag = flags[module]
    hash_value = sha256_hash(flag)
    print(f'put(Module.{module}, "{hash_value}");')

print()
print("=" * 80)
print("VERIFICATION TABLE")
print("=" * 80)
print(f"{'Module':<40} {'Flag':<50}")
print("-" * 90)
for module, flag in flags.items():
    print(f"{module:<40} {flag:<50}")
