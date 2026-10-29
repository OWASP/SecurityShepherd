# Deploying into production

This page is for operators who run Security Shepherd for other people: a class, a team or a public event. The Docker Compose setup in [Getting started](getting-started.md) is a quick start for a laptop or a classroom lab. It uses well-known default passwords and a self-signed certificate, so change what's below before anyone else uses it.

## Before the first start

Some settings are applied only when the containers or volumes are first created, so set them in `.env` before you first run `docker compose up`. [Configuration](configuration.md#env) lists every variable.

- **`DB_PASS`:** the database password. See [Change the database password](#change-the-database-password).
- **`TLS_KEYSTORE_PASS`:** the TLS keystore's password. Leave it empty to have a random one generated.
- **`TZ`:** the server's time zone, used in the logs and for the [event times](configuration.md#event-times). It defaults to `Europe/Dublin`.

## Change the database password

`DB_PASS` in `.env` is MariaDB's root password, and Tomcat uses it to connect.

- MariaDB applies it only when it creates the `data` volume.
- The Tomcat image copies it into its connection settings when it's built ([#814](https://github.com/OWASP/SecurityShepherd/issues/814)), and the `conf` volume keeps that copy from the first start.
- Because the password is inside the image you built, don't push that image to a public registry.

To change the password of an existing installation, first change MariaDB's root password. Open the database with the current password:

```bash
docker exec -it secshep_mariadb mariadb --skip-ssl -uroot -p
```

At the `MariaDB [(none)]>` prompt, enter the following with your new password, then type `exit`:

```sql
ALTER USER 'root'@'%' IDENTIFIED BY 'new password';
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new password';
```

Then change `databasePassword` in Tomcat's `database.properties`, and restart Tomcat. If the password contains `/`, `&` or `\`, put a `\` before each one in the `sed` command.

```bash
docker exec secshep_tomcat sed -i 's/^databasePassword=.*/databasePassword=new password/' \
  /usr/local/tomcat/conf/database.properties
docker compose restart web
```

Then set `DB_PASS` in `.env` to the same password, so it's used the next time the image is built.

## Use your own TLS certificate

The image contains no TLS certificate or private key. On its first start, the container generates a self-signed certificate for `localhost` and keeps it in the `conf` volume, so every installation has its own key, and it survives restarts and rebuilds. Browsers warn about it because it's self-signed.

The keystore's file name, password and alias come from `TLS_KEYSTORE_FILE`, `TLS_KEYSTORE_PASS` and `ALIAS` in `.env`, and the certificate's subject from `DNAME`. Without `TLS_KEYSTORE_PASS`, a random password is generated and kept next to the keystore, in `conf/.tls-keystore-pass`.

To use your own certificate, copy a PKCS12 keystore into the `conf` volume under the name in `TLS_KEYSTORE_FILE`, set `TLS_KEYSTORE_PASS` and `ALIAS` in `.env` to match it, and recreate the container so it picks up the new `.env`:

```bash
# Written by the container's tomcat user, so Tomcat can read it
docker exec -i secshep_tomcat sh -c 'cat > /usr/local/tomcat/conf/shepherdKeystore.p12' < my-keystore.p12
docker compose up -d web
```

To go back to a new self-signed certificate, delete the keystore, and `conf/.tls-keystore-pass` if there is one, then restart:

```bash
docker exec secshep_tomcat rm -f /usr/local/tomcat/conf/shepherdKeystore.p12 /usr/local/tomcat/conf/.tls-keystore-pass
docker compose restart web
```

If you ran an `owasp/security-shepherd:dev` or `4.0.0-SNAPSHOT` image published on 25 October 2026, get a new certificate this way once. Those images still shipped a keystore, the same one in every copy of the image, so anyone who pulled them has its private key. Images you built yourself had a key generated on your machine and aren't affected.

### A certificate from Let's Encrypt

[Certbot](https://certbot.eff.org/) gets a free certificate from Let's Encrypt for a domain name that points at your server. Security Shepherd already listens on port 80, so either stop it while Certbot runs in standalone mode, or use a DNS challenge:

```bash
docker compose stop web
sudo certbot certonly --standalone -d shepherd.example.com
docker compose start web
```

Then turn the certificate and its key into a PKCS12 keystore, and install it as above. The alias and password must match `ALIAS` and `TLS_KEYSTORE_PASS` in `.env`:

```bash
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/shepherd.example.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/shepherd.example.com/privkey.pem \
  -name tomcat -passout env:TLS_KEYSTORE_PASS -out my-keystore.p12
```

Let's Encrypt certificates last 90 days. Each time Certbot renews it, build the keystore again, copy it into the volume, and restart Tomcat.

## Admin accounts

- The default `admin` password is public, and you're asked to change it at the first login. Do that before the server is reachable by anyone else.
- Give every organiser an admin account of their own, with **Create New Admin**, rather than sharing `admin`. Each organiser changes their temporary password at their first login.
- Remove admin rights that are no longer needed with **Downgrade Admin to Player**.

## Network

Security Shepherd is deliberately vulnerable. Run it on a network that's only for the event, and don't put anything else that matters on the same server.

- The Compose file publishes MariaDB and MongoDB on `127.0.0.1` only, so they aren't reachable from other machines. Keep it that way.
- Keep the unsafe levels closed unless the server is isolated: they make the server itself vulnerable.
- Only HTTPS (port 443) and the redirect from HTTP (port 80) need to be reachable by players.

## Keep it up to date

- Rebuild the Tomcat image regularly, without the cache, so it gets the latest Ubuntu security updates. See [Building an up-to-date image](upgrading.md#building-an-up-to-date-image).
- [Vulnerability management](vulnerability-management.md) explains how vulnerabilities are handled, and how to scan an image.
- If you use the published images, [Verifying the images](verifying-images.md) shows how to check one before you run it.

## Back up

Back up the database before an event, and keep a copy of the `conf` volume. [Backups and restore](backups.md) shows how.
