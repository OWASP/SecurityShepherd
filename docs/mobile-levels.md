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
| M2: Inadequate Supply Chain Security | [Supply chain security](#supply-chain-security) | 1 |
| M3: Insecure Authentication/Authorization | [Poor authentication](#poor-authentication), [Insecure authorization](#insecure-authorization) | 3 |
| M4: Insufficient Input/Output Validation | [Client-side injection](#client-side-injection), [Input/output validation](#inputoutput-validation) | 4 |
| M6: Inadequate Privacy Controls | [Privacy controls](#privacy-controls) | 1 |
| M7: Insufficient Binary Protections | [Reverse engineering](#reverse-engineering) | 2 |
| M9: Insecure Data Storage | [Insecure data storage](#insecure-data-storage) | 2 |

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

### Insecure authorization

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Mobile Insecure Authorization | Lesson | 35 | Sergeant | M3 | [CWE-285](https://cwe.mitre.org/data/definitions/285.html) |

### Input/output validation

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Mobile Input Validation | Lesson | 35 | Sergeant | M4 | [CWE-20](https://cwe.mitre.org/data/definitions/20.html) |

### Privacy controls

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Mobile Privacy Controls | Lesson | 35 | Sergeant | M6 | [CWE-359](https://cwe.mitre.org/data/definitions/359.html) |

### Supply chain security

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Mobile Supply Chain | Lesson | 35 | Sergeant | M2 | [CWE-1104](https://cwe.mitre.org/data/definitions/1104.html) |

<!-- END GENERATED: level catalogue -->
