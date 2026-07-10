# Mobile JSP Page Content Audit

All mobile lesson and challenge JSP pages, with the text displayed to the user on each page.

---

## Lessons

---

### What is Mobile Reverse Engineering?

**Fragment:** `ReverseEngineeringLessonFragment` | **Nav label:** Reverse Engineering Lesson

**Para 1:**
Reverse engineering remains one of the most significant threats to mobile applications. With relatively little effort, an attacker can extract source code, API keys, encryption keys, hidden administrative functions, or hard‑coded credentials. Developers often rely on code obfuscation to make this process more difficult, and in some cases, it is the only practical layer of defense available.

**Para 2:**
ProGuard ships with the Android SDK and helps optimize, shrink, and obfuscate code. Obfuscation complicates reverse engineering but doesn’t secure the app. It’s useful and should be used routinely, yet it only delays attackers rather than preventing exposure of sensitive data.

**Para 3:**
Beyond renaming symbols, developers may use algorithmic key validation — instead of comparing user input against a hardcoded string, the app might pass the input through a custom encoding function or hash and compare the result. Some apps use native C/C++ libraries (compiled with the Android NDK) to hide sensitive logic from Java-level decompilers, requiring additional tools such as Ghidra or IDA Pro to analyse. Modern Android toolchains include the R8/D8 minifier, which shrinks and renames identifiers at build time. None of these measures prevent a skilled attacker from understanding the logic — they only increase the time required. Sensitive values and secrets should never exist in the app binary at all.

**Challenge description:**
To reverse engineer an APK, use a tool such as jadx or apktool. Decompile the Security Shepherd APK and investigate the source code to retrieve the result key.

---

### What is Poor Authentication?

**Fragment:** `PoorAuthLessonFragment` | **Nav label:** Poor Authentication Lesson

**Para 1:**
Poor Authentication occurs when a mobile application relies on an insecure mechanism to verify a user's identity. Common examples include accepting weak or default passwords, storing credentials in plaintext, using predictable or short-lived session tokens, failing to enforce lockout policies after repeated failed attempts, or relying on a client-side value to determine whether a user is logged in. Because Android applications can be decompiled and run-time state can be inspected on rooted devices, any authentication check that happens purely on the device is vulnerable.

**Para 2:**
One frequently overlooked weakness is verbose application logging. Android's Logcat output is readable by any application with the READ_LOGS permission, and on many devices log history persists between sessions. An app that logs user input, session tokens, or authentication codes is inadvertently making that data available to an attacker with USB access or a malicious background process. Similarly, insecure password reset flows — for example, those that rely on a static token, a guessable email link, or a value visible in the UI — allow an attacker to take over an account without ever knowing the original password.

**Para 3:**
Authentication is the process of verifying that a user is who they claim to be. Authorization is the process of verifying that an authenticated user has permission to access a particular resource or perform a particular action. The two are often confused: an application that performs authentication correctly but grants all authenticated users admin privileges has an authorization failure, not an authentication failure. Both must be enforced server-side — the mobile app should never be trusted as the source of truth for either.

**Challenge description:**
Run `adb logcat -s PoorAuthLesson` while the lesson is open. The app logs the expected PIN hash to logcat on startup. Identify the PIN from the leaked hash and enter it in the app to reveal the key.

---

### What is Mobile Insecure Data Storage?

**Fragment:** `InsecureDataLessonFragment` | **Nav label:** Insecure Data Lesson

**Para 1:**
Insecure Data Storage occurs when an App stores sensitive data such as user credentials, API keys, Credit Card information insecurely. This issue occurs in numerous ways. Generally, for storing client side information, an App will use an SQLite database.

**Para 2:**
This can be a favoured, cheaper method of storage instead of using a more expensive back end service. As a result, any user can access the data stored by the App. Insecure Data Storage becomes a danger when a user's App caches sensitive data, their phone is stolen or the attacker steals this information from local databases. Malware can also access this information easily. This risk is increased by the popularity of rooting devices which makes it much easier for an attacker to access this information.

**Para 3:**
Data is not only stored in databases. Android apps can also write sensitive information to SharedPreferences XML files, external storage, or log output. SharedPreferences files live in the app's private directory, but on a rooted device they are trivially readable and writable. External storage is accessible to any application holding storage permissions, making it an unsafe location for sensitive data.

**Para 4:**
To store data securely, developers should use EncryptedSharedPreferences or an encrypted SQLite library such as SQLCipher. Sensitive data should never appear in log files, application backups, or unencrypted on external storage. The principle of data minimisation applies — only persist what is strictly necessary.

**Challenge description:**
Typically an Android app will store its database in the /data/data/org.owasp.mobileshepherd/databases/ directory. Anyone with a rooted device can access this directory. The Android App for this lesson stores credentials in an SQLite database. The Admin's password is the result key to this lesson.

---

### What is Mobile Client Side Injection?

**Fragment:** `ClientSideInjectionLessonFragment` | **Nav label:** Client-Side Injection Lesson

**Para 1:**
Client Side Injection occurs when the user can execute SQLite commands through application input in order to change the query run by an App. The lesson database is encrypted with SQLCipher, so stealing the raw `.db` file yields only ciphertext — the only viable attack is injection through the app's own query interface. The lesson presents a user search field that concatenates input directly into a SQL query. An attacker can inject SQL to return rows that are not visible through a normal search, including hidden records containing sensitive data.

**Para 2:**
An ineffective defence against Client Side Injection is Filtering user input. This technique involves predicting which keywords an attacker would use — such as SELECT, WHERE or FROM — and replacing them with a dot or blank space. Filtering will make exploitation more difficult but will not stop a determined attacker, since filters can often be bypassed with alternative syntax, SQL comments, or encoded characters. The correct defence is to use parameterised queries (prepared statements) with bound parameters, which separate data from SQL logic entirely and make injection structurally impossible.

**Para 3:**
A security system is only as strong as its weakest link. The lesson presents a user search that concatenates input directly into a SQL `SELECT` statement. If the query returns any rows, those rows are displayed — an attacker can inject conditions to broaden or change the results.

**Challenge description:**
Use SQL injection in the user search field to return rows that are not visible through a normal search. A hidden user account contains the key in one of its fields — inject a condition that forces the query to return all rows, then look for the unusual entry.

---

### What is Mobile Security Misconfiguration?

**Fragment:** `SecurityMisconfigLessonFragment` | **Nav label:** Security Misconfiguration Lesson

Android components — Activities, Services, BroadcastReceivers and ContentProviders — can be declared as exported in the application manifest. An exported component can be invoked by any other application installed on the device, without requiring any permissions. When an exported Activity performs a sensitive operation or displays sensitive data, this becomes a security vulnerability.

In the app, tap **Check Configuration**. If the app is running in debug mode the configuration check logs the key to logcat:
`adb logcat -s SecurityMisconfig`

Look for the `DEBUG_SECRET=` line in the output.

The lesson also provides a **Scan Components** button that enumerates exported Activities from the package manager, illustrating how an attacker could identify components to invoke via `adb shell am start`.

OWASP notes that apps should apply the principle of least privilege to their component exports: only export Activities, Services and ContentProviders that genuinely need to be reachable by other apps. Developers sometimes export components assuming they will only be accessed through the intended UI — but any exported component is reachable by any app on the device, regardless of how it is reached.

---

### What is Mobile Input Validation?

**Fragment:** `InputValidationLessonFragment` | **Nav label:** Input Validation Lesson

Input validation on mobile applications is intended to prevent malicious or malformed data from being processed. However, when validation logic runs purely on the client side, an attacker can bypass it without ever intercepting network traffic.

The app implements a URL allowlist check using `String.contains()`, which checks whether the input contains a trusted domain rather than whether it *is* that domain.

In the app, enter a URL into the deep link input field. Observe that URLs containing `example.com` are accepted. The goal is to access the hidden admin panel at `admin.internal`. Craft a URL that satisfies the allowlist filter but also contains `admin.internal` — for example: `https://admin.internal?ref=example.com`.

Insufficient input/output validation is not just a client-side problem: the same unvalidated data reaching a backend server can trigger SQL injection, command injection, XSS or path traversal. The correct fix is strict server-side validation — never relying on the client to enforce constraints.

---

### What is Mobile Inadequate Privacy Controls?

**Fragment:** `PrivacyControlsLessonFragment` | **Nav label:** Privacy Controls Lesson

Mobile applications sometimes embed sensitive information in file metadata without the developer realising. EXIF (Exchangeable Image File Format) data, for example, can contain GPS coordinates, device identifiers, timestamps and other details that were never intended to be shared publicly.

In the app, tap **Load Sample Image** to load the preloaded photo. All EXIF fields are displayed on screen. The key is hidden in the `ImageDescription` EXIF tag.

Mobile Inadequate Privacy Controls can be broadly classified as inadequate protection of Personally Identifiable Information (PII) — which includes not just obvious fields like names and addresses, but also GPS coordinates, device identifiers, and timestamps embedded in file metadata. The recommended first step is data minimisation: never collect or retain PII that is not strictly necessary, and strip sensitive metadata before sharing any user-generated files.

---

### What is Mobile Insecure Authorization?

**Fragment:** `InsecureAuthorizationLessonFragment` | **Nav label:** Insecure Authorization Lesson

Insecure authorization occurs when a mobile application enforces access controls purely on the client side. A common pattern is storing a user role or permission flag in Android SharedPreferences. Because SharedPreferences files are stored in the app's data directory, a user with a rooted device or ADB access can read and modify those values directly, escalating their own privileges without any server-side check.

Log in to the app with the demo credentials. Use ADB to inspect the app's SharedPreferences:
`adb shell run-as org.owasp.mobileshepherd cat shared_prefs/UserSession.xml`

Find the `user_role` value and change it from `user` to `admin`:
`adb shell run-as org.owasp.mobileshepherd sh -c 'sed -i s/user_role\" value=\"user\"/user_role\" value=\"admin\"/g shared_prefs/UserSession.xml'`

Then return to the app and tap **Access Admin Panel** to reveal the key.

Backend systems must independently verify the roles and permissions of the authenticated user — never trusting role or permission data transmitted from the mobile device. The risk is heightened when authorization decisions are made on the device rather than the server. All client-side authorization controls must be assumed bypassable.

---

### What is Mobile Insecure Communication?

**Fragment:** `InsecureCommLessonFragment` | **Nav label:** Insecure Communication Lesson

Mobile applications that transmit sensitive data over unencrypted HTTP channels expose that data to anyone with access to the same network — or to any intercepting proxy. This is a violation of the principle of secure transport, and is categorised as M5 in the OWASP Mobile Top 10.

Set up Burp Suite as a proxy on your machine and configure your Android device to route traffic through it. The app sends an HTTP (not HTTPS) POST request. Intercept it and look for the key in the `X-API-Key` request header.

Mobile Apps that use TLS can be vulnerable through implementation flaws such as accepting self-signed or expired certificates, negotiating a weak cipher suite, or applying HTTPS only on the login flow but not on subsequent API calls. A threat actor can exploit any of these inconsistencies to intercept credentials, session tokens or sensitive data in transit.

---

### What is Mobile Insufficient Cryptography?

**Fragment:** `InsufficientCryptoLessonFragment` | **Nav label:** Insufficient Cryptography Lesson

Insufficient cryptography occurs when a mobile application uses deprecated or weak encryption algorithms, short key lengths, insecure modes of operation, or poor key management. Common examples include using DES (56-bit key), RC4, MD5 or SHA-1 for security-sensitive operations, using ECB block cipher mode (which leaks patterns in plaintext), hardcoding encryption keys in source code, or deriving keys from static or guessable values.

Even when a well-known algorithm such as AES is chosen, a flawed implementation — for example, a static initialisation vector or a key that is simply the app's package name — can make the encryption trivially breakable. Because mobile applications can be decompiled, any key or algorithm choice embedded in the APK is visible to an attacker.

The app encrypts data using DES in ECB mode with the hardcoded 8-character key `SHEPHERD`, then Base64-encodes the result before storing it.

In the app, trigger the encryption action to see the ciphertext. Decompile the APK with jadx to locate the key and algorithm in the source. Use a tool such as CyberChef (DES Decrypt → ECB mode → key: `SHEPHERD` → Base64 input) to recover the plaintext.

---

### What is Mobile Inadequate Supply Chain Security?

**Fragment:** `SupplyChainLessonFragment` | **Nav label:** Supply Chain Security Lesson

Mobile applications commonly rely on third-party libraries and SDKs for functionality such as analytics, advertising and networking. Including a vulnerable or malicious dependency in an app introduces risk that may be invisible to the developer. This is categorised as M2 in the OWASP Mobile Top 10.

Open the Supply Chain lesson in the app, then run:
`adb logcat -s SupplyChainLesson`

Watch the log output — the vulnerable library initialisation logs a static internal debug key (`exif_debug_processor_key_1337`) to logcat. **Note:** the dynamic key is fetched from the server but is not currently written to logcat; this is a known gap in the lesson implementation.

A vulnerable dependency can be exploited to steal data, inject malware or gain unauthorized access — and the app developer bears responsibility even though the vulnerability originated outside their own code. Only include trusted, actively maintained dependencies, pin them to specific versions, and audit them regularly using tools such as OWASP Dependency-Check.

---

## Challenges

---

### Reverse Engineering Challenge

**Fragment:** `ReverseEngineering1Fragment` | **Nav label:** Reverse Engineering 1

Decompile the Security Shepherd mobile APK using a tool such as jadx or apktool. Search the decompiled source code for a hardcoded key and submit it in the app.

---

### Poor Authentication 1

**Fragment:** `PoorAuthChallengeFragment` | **Nav label:** Poor Authentication 1

The account is locked — you cannot log in directly. Run `adb logcat -s PoorAuthChallenge` and read the logged entries to find hints about the security question answers. Click **Forgot Password**, answer the security questions correctly, and collect the temporary password that is displayed. Log in with it to retrieve the key.

---

### Mobile Insecure Data Storage 1

**Fragment:** `InsecureData1Fragment` | **Nav label:** Insecure Data 1

User credentials are stored in an unencrypted SQLite database. The passwords are Base64-encoded but not encrypted. Decode the Admin's password to get the key.

---

### Mobile Client Side Injection 1

**Fragment:** `ClientSideInjectionChallenge1Fragment` | **Nav label:** Client-Side Injection Challenge 1

The database is encrypted with SQLCipher — direct file extraction will not reveal the credentials. The login view uses an input filter that converts input to uppercase and blocks common SQL keywords including `OR`, `AND`, and `--`. The filter can be bypassed using equivalent SQL operators that it does not block. Login as admin to get the key.

---

### Mobile Client Side Injection 2

**Fragment:** `ClientSideInjectionChallenge2Fragment` | **Nav label:** Client-Side Injection Challenge 2

The database is encrypted with SQLCipher — direct file extraction will not reveal the data. A product search screen is vulnerable to UNION-based SQL injection, allowing an attacker to query tables beyond the visible `products` table. A hidden `secrets` table contains the key.

---

### Mobile Insufficient Cryptography Challenge

**Fragment:** `InsufficientCryptoChallengeFragment` | **Nav label:** Insufficient Cryptography 1

A secret value is stored in SharedPreferences. The value is obfuscated using single-byte XOR with a hardcoded key, then Base64-encoded. Extract the stored value via ADB, decode and reverse the obfuscation to reveal the key.

---

### Mobile Insecure Communication Challenge

**Fragment:** `InsecureCommChallengeFragment` | **Nav label:** Insecure Communication 1

The challenge generates outbound HTTP traffic when triggered. Configure a network proxy on your device and intercept the plaintext HTTP request. Locate the key in the `X-Session-Token` request header and submit it in the app.

---

### Mobile Security Misconfiguration Challenge

**Fragment:** `SecurityMisconfigChallenge2Fragment` | **Nav label:** Security Misconfiguration 1

The app has `android:allowBackup="true"` enabled, meaning the full app data directory — including SharedPreferences — can be extracted without root access. Navigate to the challenge to trigger key storage, then extract the backup:

```
adb backup -f backup.ab -noapk org.owasp.mobileshepherd
```

Decode the backup archive (e.g. using Android Backup Extractor or `dd | zlib | tar`) and read `apps/org.owasp.mobileshepherd/sp/BackupChallengePrefs.xml`. The `secret_flag` key contains the result key.
