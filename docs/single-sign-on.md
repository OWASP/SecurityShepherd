# Single sign-on

Security Shepherd can log players and admins in through your organisation's SAML 2.0 identity provider (IdP), instead of with a username and password. It's off by default. Single sign-on (SSO) is new in 4.0, so it isn't in 3.1.

This page explains what changes when SSO is on, then takes you through the setup: the two configuration files, building them into the image, and registering Security Shepherd with your IdP.

## What changes when SSO is on

- **SSO replaces passwords.** The login page shows only **Login via SAML**, registration is turned off, and password logins are refused, including the built-in `admin` account. Before you turn SSO on, make sure your IdP puts at least one organiser in the admin group (see [Map users and roles](#4-map-users-and-roles)). Accounts created under **Admin → User Management** have passwords, so they can't log in either.
- **Logins start at Security Shepherd.** Players click **Login via SAML**, log in at the IdP, and are sent back. Logging in from the IdP's own dashboard or app launcher (IdP-initiated login) isn't supported, and those logins are refused: they can't be tied to the browser that's logging in, which would let an attacker log a player into the attacker's account. A login has to be completed within 10 minutes of clicking the button.
- **Accounts are created at the first login.** The account is named after the IdP's display name attribute, with a number added if that username is taken, and the player is asked to choose their own username. If a [default class](running-a-ctf.md#players-classes-and-admins) is set, new accounts join it.
- **Accounts are matched by a unique ID from the IdP.** On every later login, Security Shepherd finds the account by the attribute you choose as `sso.saml.ssoName`. Use a value that never changes and is never given to someone else, such as an employee or student ID, not an email address.
- **Roles come from IdP groups.** Each login checks the player's groups. A member of an admin group is an admin, a member of a player group is a player, and anyone in neither is refused.
- **Suspensions still work.** A suspended player can't log in through SSO until the suspension ends.
- **Logout can log out of the IdP too.** If you configure single logout, logging out of Security Shepherd also logs the player out of the IdP, and logging out at the IdP ends their Security Shepherd session.

### How a login works

```mermaid
sequenceDiagram
    actor Player
    participant Browser
    participant Shepherd as Security Shepherd
    participant IdP as Identity provider

    Player->>Browser: Click Login via SAML
    Browser->>Shepherd: /saml/dologin
    Shepherd-->>Browser: Login request, and a cookie
    Browser->>IdP: Login request
    Player->>IdP: Log in
    IdP-->>Browser: Signed response
    Browser->>Shepherd: /saml/acs: response and cookie
    Shepherd->>Shepherd: Check the response
    Shepherd-->>Browser: Logged in
```

The cookie ties the login request to the browser that started it. When the response comes back, Security Shepherd checks its signature, that it answers a request from the same browser, and the player's groups, which decide whether they log in as a player or an admin. A response that doesn't answer a request from that browser, including one started from the IdP's dashboard, is refused.

## Before you start

You need:

- Security Shepherd running over HTTPS on its real hostname, with a certificate browsers trust. The login depends on a `Secure` cookie, so it doesn't work over plain HTTP. [Deploying into production](deploying-into-production.md#use-your-own-tls-certificate) explains how to use your own certificate.
- An application (a SAML "service provider" or "relying party") registered for Security Shepherd in your IdP, or someone who can create one.
- The source code, built as in [Getting started](getting-started.md#run-it-with-docker). The configuration files are built into the WAR, so you rebuild the image to change them.

## 1. Create the configuration files

SSO uses two files in `src/main/resources/`:

- `onelogin.saml.properties` describes Security Shepherd, your IdP and the signing settings. Its key names come from the java-saml library that Security Shepherd used before 4.0, so files from an older installation keep working.
- `sso.properties` turns SSO on and maps the IdP's attributes to users and roles.

Copy the samples to create them:

```bash
cp src/main/resources/onelogin.saml.properties.sample src/main/resources/onelogin.saml.properties
cp src/main/resources/sso.properties.sample src/main/resources/sso.properties
```

Git ignores both files, so they can't be committed by accident.

## 2. Describe Security Shepherd

In `onelogin.saml.properties`, replace `https://localhost` with the address players use, for example `https://shepherd.example.org`:

| Setting | Value |
|---|---|
| `onelogin.saml2.sp.entityid` | Security Shepherd's identifier at the IdP. The usual choice is the metadata URL, `https://shepherd.example.org/saml/metadata.xml`. |
| `onelogin.saml2.sp.assertion_consumer_service.url` | `https://shepherd.example.org/saml/acs`, where the IdP sends players back after they log in. Leave the binding as HTTP-POST. |
| `onelogin.saml2.sp.single_logout_service.url` | `https://shepherd.example.org/saml/sls` for single logout. Leave it empty to turn single logout off. |
| `onelogin.saml2.sp.nameidformat` | The NameID format to ask the IdP for. Security Shepherd doesn't use the NameID to find accounts, so the default is fine. |
| `onelogin.saml2.sp.x509cert`, `onelogin.saml2.sp.privatekey` | Security Shepherd's certificate and private key, used to sign its requests and metadata. |

### The signing key

With the sample's settings, Security Shepherd signs its login and logout requests and its metadata, so it needs a key pair. A self-signed certificate is normal for SAML signing. To create one that lasts two years:

```bash
openssl req -x509 -newkey rsa:3072 -sha256 -days 730 -nodes \
  -subj "/CN=shepherd.example.org" -keyout sp-key.pem -out sp-cert.pem
```

Paste each file into its setting on one line. The `BEGIN` and `END` lines and the line breaks are optional, so this prints the value to paste:

```bash
grep -v -- ----- sp-cert.pem | tr -d '\n'
grep -v -- ----- sp-key.pem | tr -d '\n'
```

The private key ends up inside the WAR and the Docker image. Keep the image in a private registry or on the server that runs it, and never push it to a public one. Delete `sp-key.pem` once it's in the file.

If you don't want Security Shepherd to sign anything, leave both settings empty and set `authnrequest_signed`, `logoutrequest_signed` and `sign_metadata` to `false`. A temporary key is then used, and the IdP can't encrypt its assertions.

## 3. Describe your identity provider

Your IdP shows these values on the application's setup page, or in its metadata XML:

| Setting | Value |
|---|---|
| `onelogin.saml2.idp.entityid` | The IdP's entity ID (issuer). |
| `onelogin.saml2.idp.single_sign_on_service.url` | The IdP's SSO URL for the HTTP-Redirect binding. |
| `onelogin.saml2.idp.single_logout_service.url` | The IdP's single logout URL. Leave it empty if you don't use single logout. |
| `onelogin.saml2.idp.single_logout_service.response.url` | Only if the IdP takes logout responses at a different URL from logout requests. |
| `onelogin.saml2.idp.x509cert` | The IdP's signing certificate, on one line as above. A certificate fingerprint on its own (`idp.certfingerprint`) isn't supported. |

### Security settings

| Setting | Sample value | What it does |
|---|---|---|
| `onelogin.saml2.security.authnrequest_signed` | `true` | Signs login requests. |
| `onelogin.saml2.security.logoutrequest_signed` | `true` | Signs logout requests. |
| `onelogin.saml2.security.want_messages_signed` | `true` | Requires the IdP to sign the whole SAML response, not only the assertion inside it. Some IdPs sign only the assertion by default. Either turn on response signing in the IdP, or set this to `false`. |
| `onelogin.saml2.security.want_assertions_signed` | `true` | Requires a signed assertion. Leave it on. |
| `onelogin.saml2.security.sign_metadata` | `true` | Signs Security Shepherd's metadata. |
| `onelogin.saml2.security.requested_authncontext` | empty | Asks the IdP for a particular way of logging in, for example with multi-factor authentication. Separate several values with commas. Empty sends no request. |
| `onelogin.saml2.security.requested_authncontextcomparison` | `exact` | How the IdP compares its login method with the one requested. |
| `onelogin.saml2.security.signature_algorithm` | RSA-SHA256 | The algorithm Security Shepherd signs with. |

Responses are always checked strictly. These settings from older java-saml files are ignored: `strict`, `debug`, `nameid_encrypted`, `logoutresponse_signed`, `want_assertions_encrypted`, `want_nameid_encrypted` and `want_xml_validation`. Encrypted assertions and NameIDs are still decrypted when the IdP sends them.

## 4. Map users and roles

In `sso.properties`, set `sso.enabled=true` and name the attributes your IdP sends. Use the attribute names exactly as they appear in the SAML assertion, which are often URIs. Your IdP's documentation, or a SAML browser extension, shows them.

| Setting | Value |
|---|---|
| `sso.enabled` | `true` turns SSO on. With `false`, every `/saml/` address returns 404, even if `onelogin.saml.properties` is filled in. |
| `sso.saml.ssoName` | The attribute with each user's unique, permanent ID. Accounts are matched on it. |
| `sso.saml.userName` | The attribute with the name to start the username from, such as the display name. |
| `sso.saml.affiliation` | The attribute listing the user's groups. |
| `sso.saml.adminAffiliation` | Groups whose members are admins, separated by commas. |
| `sso.saml.playerAffiliation` | Groups whose members are players, separated by commas. |

For example, with an IdP that sends `employeeID`, `displayName` and `groups` attributes:

```properties
sso.enabled=true
sso.saml.ssoName=employeeID
sso.saml.userName=displayName
sso.saml.affiliation=groups
sso.saml.adminAffiliation=shepherd-admins
sso.saml.playerAffiliation=shepherd-players,security-training
```

A user in both an admin group and a player group is an admin.

## 5. Build and start Security Shepherd

Rebuild the WAR and the image, and restart the containers, as in [Getting started](getting-started.md#run-it-with-docker):

```bash
mvn -Pdocker clean package -DskipTests
docker compose up -d --build
```

Security Shepherd reads `sso.enabled` once, the first time it's needed, so rebuild and restart after every change to either file.

## 6. Register Security Shepherd with your IdP

Once SSO is on, Security Shepherd publishes its metadata at `https://shepherd.example.org/saml/metadata.xml`. Most IdPs can import it from that URL or from the downloaded file. Otherwise, enter these by hand:

- **Entity ID** (audience): the `sp.entityid` value.
- **Assertion consumer service** (ACS, reply or single sign-on URL): `https://shepherd.example.org/saml/acs`, with the HTTP-POST binding.
- **Single logout URL**: `https://shepherd.example.org/saml/sls`, if you use single logout.
- **Signing certificate**: `sp-cert.pem`, if Security Shepherd signs its requests.

Then set the IdP to send the attributes you named in `sso.properties`, and give the right people access to the application.

To test it, open Security Shepherd in a private window, click **Login via SAML**, and log in as a member of a player group, then as a member of an admin group.

## Troubleshooting

Security Shepherd's log shows why a login failed: `docker logs secshep_tomcat`, or the files in the `logs` volume.

- **The `/saml/` addresses return 404, or there's no Login via SAML button.** `sso.enabled` isn't `true`, the files aren't in the WAR, or Security Shepherd hasn't been restarted since you changed them. Check that both files are in `src/main/resources/`, then rebuild and restart.
- **"Required SSO attributes are missing".** The IdP didn't send the attributes named in `sso.saml.ssoName` or `sso.saml.userName`, or sent them under different names, or sent them empty.
- **"Authorization failed. Please ensure that you are a member of one of the following groups".** The user isn't in any of the groups in `sso.saml.adminAffiliation` or `sso.saml.playerAffiliation`, or the IdP isn't sending the `sso.saml.affiliation` attribute.
- **"SSO login failed" after logging in at the IdP.** Check the log. The usual causes are a signature check failing (the wrong `idp.x509cert`, or `want_messages_signed` set while the IdP signs only the assertion), a login started from the IdP's dashboard, a login that took more than 10 minutes, or Security Shepherd being reached over HTTP or on a different hostname from the one in the ACS URL.
- **The IdP rejects the login request.** The entity ID, ACS URL or signing certificate registered at the IdP doesn't match `onelogin.saml.properties`. Import the metadata again after changing them.
