# The levels

This page lists every level in Security Shepherd 4.0: 58 web levels and 24 mobile levels, in 18 topics. It shows what each level is worth and where it sits in [Tournament Mode](how-shepherd-works.md#finding-a-level), but not how to complete it.

Organisers choose which levels are open, so your event may have fewer. Within a topic, the levels are listed from easiest to hardest.

## Before you start

- **Unsafe levels.** The levels marked *(unsafe)* make the server itself vulnerable, so they're closed unless the organiser opens them on an isolated server.
- **NoSQL Injection One** needs MongoDB, which the organiser turns on during setup.
- **The CSRF challenges** are only completed when another logged-in player triggers the request you forged, so you'll need someone else's help.
- **The mobile levels** need an Android emulator. They're listed on [Mobile levels](mobile-levels.md).

## Web levels

Each level shows its OWASP Top 10 2025 category and a CWE (Common Weakness Enumeration) number, so you can find every level for one kind of vulnerability, or match the levels to a training course. The CWE describes the topic, not the individual level, so it doesn't hint at how to complete it. The Top 10 category is the one OWASP maps that CWE to. Security Shepherd's topics were written against the 2013 Top 10, so a few have moved: XXE is under Security Misconfiguration in 2025, and poor data validation under Injection.

<!-- BEGIN GENERATED: level catalogue. Edit .github/scripts/level-classification.toml or coreSchema.sql, then run .github/scripts/generate-level-catalogue.py. -->

### By OWASP Top 10 2025

| Category | Topics | Levels |
| --- | --- | --- |
| [A01:2025 Broken Access Control](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [Insecure direct object references](#insecure-direct-object-references), [Failure to restrict URL access](#failure-to-restrict-url-access), [Cross-site request forgery (CSRF)](#cross-site-request-forgery-csrf), [Unvalidated redirects and forwards](#unvalidated-redirects-and-forwards) | 18 |
| [A02:2025 Security Misconfiguration](https://top10.owasp.org/2025/A02_2025-Security_Misconfiguration/) | [Security misconfiguration](#security-misconfiguration), [Injection](#injection) (XXE Injection, XXE Injection 1) | 4 |
| [A04:2025 Cryptographic Failures](https://top10.owasp.org/2025/A04_2025-Cryptographic_Failures/) | [Insecure cryptographic storage](#insecure-cryptographic-storage) | 6 |
| [A05:2025 Injection](https://top10.owasp.org/2025/A05_2025-Injection/) | [Poor data validation](#poor-data-validation), [Cross-site scripting (XSS)](#cross-site-scripting-xss), [Injection](#injection) (all except XXE Injection, XXE Injection 1) | 21 |
| [A07:2025 Authentication Failures](https://top10.owasp.org/2025/A07_2025-Authentication_Failures/) | [Session management](#session-management) | 9 |

### Insecure direct object references

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Insecure Direct Object References | Lesson | 10 | Field Training | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-639](https://cwe.mitre.org/data/definitions/639.html) |
| Insecure Direct Object Reference Challenge 1 | Challenge | 35 | Private | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-639](https://cwe.mitre.org/data/definitions/639.html) |
| Insecure Direct Object Reference Challenge 2 | Challenge | 65 | Sergeant | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-639](https://cwe.mitre.org/data/definitions/639.html) |
| Insecure Direct Object Reference Bank | Challenge | 60 | Lieutenant | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-639](https://cwe.mitre.org/data/definitions/639.html) |

### Poor data validation

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Poor Data Validation | Lesson | 10 | Field Training | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-20](https://cwe.mitre.org/data/definitions/20.html) |
| Poor Data Validation 1 | Challenge | 35 | Private | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-20](https://cwe.mitre.org/data/definitions/20.html) |
| Poor Data Validation 2 | Challenge | 80 | Major | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-20](https://cwe.mitre.org/data/definitions/20.html) |

### Security misconfiguration

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Security Misconfiguration | Lesson | 10 | Field Training | [A02](https://top10.owasp.org/2025/A02_2025-Security_Misconfiguration/) | [CWE-16](https://cwe.mitre.org/data/definitions/16.html) |
| Security Misconfig Cookie Flag | Challenge | 110 | Admiral | [A02](https://top10.owasp.org/2025/A02_2025-Security_Misconfiguration/) | [CWE-16](https://cwe.mitre.org/data/definitions/16.html) |

### Session management

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Broken Session Management | Lesson | 10 | Field Training | [A07](https://top10.owasp.org/2025/A07_2025-Authentication_Failures/) | [CWE-287](https://cwe.mitre.org/data/definitions/287.html) |
| Session Management Challenge 1 | Challenge | 40 | Private | [A07](https://top10.owasp.org/2025/A07_2025-Authentication_Failures/) | [CWE-287](https://cwe.mitre.org/data/definitions/287.html) |
| Session Management Challenge 2 | Challenge | 55 | Sergeant | [A07](https://top10.owasp.org/2025/A07_2025-Authentication_Failures/) | [CWE-287](https://cwe.mitre.org/data/definitions/287.html) |
| Session Management Challenge 3 | Challenge | 60 | Sergeant | [A07](https://top10.owasp.org/2025/A07_2025-Authentication_Failures/) | [CWE-287](https://cwe.mitre.org/data/definitions/287.html) |
| Session Management Challenge 4 | Challenge | 75 | Major | [A07](https://top10.owasp.org/2025/A07_2025-Authentication_Failures/) | [CWE-287](https://cwe.mitre.org/data/definitions/287.html) |
| Session Management Challenge 5 | Challenge | 110 | Admiral | [A07](https://top10.owasp.org/2025/A07_2025-Authentication_Failures/) | [CWE-287](https://cwe.mitre.org/data/definitions/287.html) |
| Session Management Challenge 6 | Challenge | 110 | Admiral | [A07](https://top10.owasp.org/2025/A07_2025-Authentication_Failures/) | [CWE-287](https://cwe.mitre.org/data/definitions/287.html) |
| Session Management Challenge 7 | Challenge | 110 | Admiral | [A07](https://top10.owasp.org/2025/A07_2025-Authentication_Failures/) | [CWE-287](https://cwe.mitre.org/data/definitions/287.html) |
| Session Management Challenge 8 | Challenge | 115 | Admiral | [A07](https://top10.owasp.org/2025/A07_2025-Authentication_Failures/) | [CWE-287](https://cwe.mitre.org/data/definitions/287.html) |

### Failure to restrict URL access

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Failure to Restrict URL Access | Lesson | 15 | Field Training | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) |
| Failure to Restrict URL Access 1 | Challenge | 40 | Private | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) |
| Failure to Restrict URL Access 2 | Challenge | 85 | Major | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) |
| Failure to Restrict URL Access 3 | Challenge | 110 | Admiral | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-862](https://cwe.mitre.org/data/definitions/862.html) |

### Cross-site scripting (XSS)

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Cross Site Scripting | Lesson | 15 | Field Training | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) |
| Cross Site Scripting 1 | Challenge | 20 | Field Training | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) |
| Cross Site Scripting 2 | Challenge | 60 | Sergeant | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) |
| Cross Site Scripting 3 | Challenge | 65 | Sergeant | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) |
| Cross Site Scripting 4 | Challenge | 75 | Major | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) |
| Cross Site Scripting 5 | Challenge | 85 | Major | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) |
| Cross Site Scripting 6 | Challenge | 95 | Admiral | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-79](https://cwe.mitre.org/data/definitions/79.html) |

### Insecure cryptographic storage

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Insecure Cryptographic Storage | Lesson | 25 | Private | [A04](https://top10.owasp.org/2025/A04_2025-Cryptographic_Failures/) | [CWE-327](https://cwe.mitre.org/data/definitions/327.html) |
| Insecure Cryptographic Storage Challenge 1 | Challenge | 35 | Private | [A04](https://top10.owasp.org/2025/A04_2025-Cryptographic_Failures/) | [CWE-327](https://cwe.mitre.org/data/definitions/327.html) |
| Insecure Cryptographic Storage Challenge 2 | Challenge | 65 | Sergeant | [A04](https://top10.owasp.org/2025/A04_2025-Cryptographic_Failures/) | [CWE-327](https://cwe.mitre.org/data/definitions/327.html) |
| Insecure Cryptographic Storage Challenge 3 | Challenge | 75 | Major | [A04](https://top10.owasp.org/2025/A04_2025-Cryptographic_Failures/) | [CWE-327](https://cwe.mitre.org/data/definitions/327.html) |
| Insecure Cryptographic Storage Challenge 4 | Challenge | 115 | Admiral | [A04](https://top10.owasp.org/2025/A04_2025-Cryptographic_Failures/) | [CWE-327](https://cwe.mitre.org/data/definitions/327.html) |
| Insecure Cryptographic Storage Home Made Key | Challenge | 140 | Admiral | [A04](https://top10.owasp.org/2025/A04_2025-Cryptographic_Failures/) | [CWE-327](https://cwe.mitre.org/data/definitions/327.html) |

### Injection

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| SQL Injection | Lesson | 30 | Private | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |
| XXE Injection (unsafe) | Lesson | 30 | Private | [A02](https://top10.owasp.org/2025/A02_2025-Security_Misconfiguration/) | [CWE-611](https://cwe.mitre.org/data/definitions/611.html) |
| XXE Injection 1 (unsafe) | Challenge | 40 | Private | [A02](https://top10.owasp.org/2025/A02_2025-Security_Misconfiguration/) | [CWE-611](https://cwe.mitre.org/data/definitions/611.html) |
| SQL Injection 1 | Challenge | 35 | Private | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |
| SQL Injection 2 | Challenge | 45 | Corporal | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |
| NoSQL Injection One | Challenge | 45 | Corporal | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-943](https://cwe.mitre.org/data/definitions/943.html) |
| SQL Injection Escaping | Challenge | 50 | Corporal | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |
| SQL Injection 3 | Challenge | 70 | Lieutenant | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |
| SQL Injection 4 | Challenge | 75 | Major | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |
| SQL Injection 5 | Challenge | 90 | Admiral | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |
| SQL Injection Stored Procedure | Challenge | 90 | Admiral | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |
| SQL Injection 6 | Challenge | 95 | Admiral | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |
| SQL Injection 7 | Challenge | 110 | Admiral | [A05](https://top10.owasp.org/2025/A05_2025-Injection/) | [CWE-89](https://cwe.mitre.org/data/definitions/89.html) |

### Cross-site request forgery (CSRF)

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Cross Site Request Forgery | Lesson | 40 | Private | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-352](https://cwe.mitre.org/data/definitions/352.html) |
| CSRF 1 | Challenge | 55 | Sergeant | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-352](https://cwe.mitre.org/data/definitions/352.html) |
| CSRF 2 | Challenge | 70 | Lieutenant | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-352](https://cwe.mitre.org/data/definitions/352.html) |
| CSRF 3 | Challenge | 70 | Lieutenant | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-352](https://cwe.mitre.org/data/definitions/352.html) |
| CSRF 4 | Challenge | 70 | Lieutenant | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-352](https://cwe.mitre.org/data/definitions/352.html) |
| CSRF JSON | Challenge | 70 | Lieutenant | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-352](https://cwe.mitre.org/data/definitions/352.html) |
| CSRF 5 | Challenge | 80 | Major | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-352](https://cwe.mitre.org/data/definitions/352.html) |
| CSRF 6 | Challenge | 90 | Admiral | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-352](https://cwe.mitre.org/data/definitions/352.html) |
| CSRF 7 | Challenge | 120 | Admiral | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-352](https://cwe.mitre.org/data/definitions/352.html) |

### Unvalidated redirects and forwards

| Level | Kind | Points | Tournament band | Top 10 | CWE |
| --- | --- | --- | --- | --- | --- |
| Unvalidated Redirects and Forwards | Lesson | 45 | Corporal | [A01](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/) | [CWE-601](https://cwe.mitre.org/data/definitions/601.html) |

<!-- END GENERATED: level catalogue -->
