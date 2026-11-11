# Mobile levels

Security Shepherd's 24 mobile levels are Android apps, in [`mobile/MobileShepherd/`](https://github.com/OWASP/SecurityShepherd/tree/dev/mobile/MobileShepherd). Each one has a page in Security Shepherd, where you submit its result key, but the vulnerability is in the app, so you need an Android emulator or device to play them.

Instructions for running the apps with Security Shepherd 4.0 are being prepared separately from the web documentation. Until then, organisers usually open only the web levels, with **Open Web App Levels Only**.

## The levels

Within a topic, the levels are listed from easiest to hardest. Tournament bands are described in [How Shepherd works](how-shepherd-works.md#finding-a-level).

Each level shows its OWASP Mobile Top 10 2024 risk and a CWE (Common Weakness Enumeration) number. The CWE describes the topic, not the individual level, so it doesn't hint at how to complete it.

<!-- BEGIN GENERATED: level catalogue. Edit .github/scripts/level-classification.toml or coreSchema.sql, then run .github/scripts/generate-level-catalogue.py. -->

### By OWASP Mobile Top 10 2024

| Category | Topics | Levels |
| --- | --- | --- |
| M3: Insecure Authentication/Authorization | [Poor authentication](#poor-authentication) | 2 |
| M4: Insufficient Input/Output Validation | [Client-side injection](#client-side-injection) | 3 |
| M7: Insufficient Binary Protections | [Reverse engineering](#reverse-engineering) | 2 |
| M9: Insecure Data Storage | [Insecure data storage](#insecure-data-storage) | 2 |
| M10: Insufficient Cryptography | [Insufficient cryptography](#insufficient-cryptography) | 2 |

### Insecure data storage

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Mobile Insecure Data Storage | Lesson | 25 | Private | M9 | [CWE-922](https://cwe.mitre.org/data/definitions/922.html) |
| Mobile Insecure Data Storage 1 | Challenge | 60 | Sergeant | M9 | [CWE-922](https://cwe.mitre.org/data/definitions/922.html) |

### Reverse engineering

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Reverse Engineering | Lesson | 40 | Private | M7 | [CWE-656](https://cwe.mitre.org/data/definitions/656.html) |
| Reverse Engineering 1 | Challenge | 50 | Corporal | M7 | [CWE-656](https://cwe.mitre.org/data/definitions/656.html) |

### Client-side injection

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Mobile Client Side Injection | Lesson | 50 | Corporal | M4 | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |
| Mobile Client Side Injection 1 | Challenge | 70 | Lieutenant | M4 | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |
| Mobile Client Side Injection 2 | Challenge | 80 | Major | M4 | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |

### Poor authentication

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Mobile Poor Authentication | Lesson | 50 | Corporal | M3 | [CWE-287](https://cwe.mitre.org/data/definitions/287.html) |
| Mobile Poor Authentication 1 | Challenge | 60 | Major | M3 | [CWE-287](https://cwe.mitre.org/data/definitions/287.html) |

### Insufficient cryptography

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Mobile Insufficient Cryptography | Lesson | 35 | Sergeant | M10 | [CWE-327](https://cwe.mitre.org/data/definitions/327.html) |
| Mobile Insufficient Cryptography Challenge | Challenge | 60 | Sergeant | M10 | [CWE-327](https://cwe.mitre.org/data/definitions/327.html) |

<!-- END GENERATED: level catalogue -->
