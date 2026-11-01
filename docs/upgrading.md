# Upgrading

This page is for operators of a Docker installation: how to rebuild the images with the latest updates, and how to move an existing installation to Security Shepherd 4.0.

Back up first, as described in [Backups and restore](backups.md).

## Building an up-to-date image

When the Tomcat image is built, it installs the Ubuntu security updates released since its base image was published. Docker caches the base image and that build step, so rebuilding with `docker compose up --build` can reuse an old base image and old packages. To build an image with the latest updates, rebuild it without the cache and recreate the container. Maven needs JDK 17 or newer, as the [prerequisites](getting-started.md#prerequisites) explain.

```bash
mvn -Pdocker clean package -DskipTests
docker compose build --pull --no-cache web
docker compose up -d
```

## Upgrading to 4.0

Security Shepherd 4.0 runs on Tomcat 11 and MariaDB 12.3. An existing Docker installation needs two extra steps: recreating Tomcat's `conf` volume, and letting MariaDB upgrade the `data` volume. Do them in this order.

### 1. Back up the database

The database image now runs MariaDB 12.3 (long-term support) instead of 10.6, which is no longer supported. The `data` volume is kept. On its first start, `MARIADB_AUTO_UPGRADE=1` in `docker-compose.yml` upgrades the volume's system tables, after taking a backup of them inside the volume.

Back up your data before rebuilding anyway, because a volume can't be moved back to 10.6 once 12.3 has opened it. Run this while the old container is still up. The dump holds every user's password hash, so keep it outside the repository and readable by you only:

```bash
BACKUP_DIR="$HOME/shepherd-db-backup"
mkdir -p "$BACKUP_DIR" && chmod 700 "$BACKUP_DIR"
(umask 077 && docker exec secshep_mariadb sh -c \
  'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; exec mysqldump -uroot --all-databases --routines --events' \
  > "$BACKUP_DIR/shepherd-db-backup.sql")
```

### 2. Recreate the `conf` volume

Tomcat's `conf` directory lives in the `conf` Docker volume, and Docker only copies the image's files into a volume when the volume is first created. An existing installation would therefore start Tomcat 11 with its old Tomcat 9 or 10.1 `server.xml` and `web.xml`, so the volume has to be recreated. Database data is in the separate `data` and `mongodata` volumes, so it's kept.

The `conf` volume also holds the TLS keystore and the `database.properties` and `mongo.properties` connection settings, so **back it up first**:

```bash
docker compose down
# The volume is named <project>_conf; check with: docker volume ls
# The archive holds the TLS private key and database password. Keep it outside the repository
# (the repository root is the Docker build context) and readable by you only
BACKUP_DIR="$HOME/shepherd-conf-backup"
mkdir -p "$BACKUP_DIR" && chmod 700 "$BACKUP_DIR"
docker run --rm -v securityshepherd_conf:/conf -v "$BACKUP_DIR":/backup alpine sh -c \
  'umask 077 && touch /backup/shepherd-conf-backup.tgz && chmod 600 /backup/shepherd-conf-backup.tgz \
   && tar czf /backup/shepherd-conf-backup.tgz -C /conf .'
docker volume rm securityshepherd_conf
mvn -Pdocker clean package -DskipTests
# --pull and --no-cache pick up the latest base image and Ubuntu security updates
docker compose build --pull --no-cache web
# Detached, so the databases keep running while you restore files below
docker compose up -d --build
```

The new volume starts from the image: a newly generated keystore, and connection settings taken from `.env`. If you had customised either, copy just those files back from the backup. Leave `server.xml` and `web.xml` alone, because they're the Tomcat 11 versions.

```bash
BACKUP_DIR="$HOME/shepherd-conf-backup"
# Connection settings changed through the setup page:
docker run --rm -v securityshepherd_conf:/conf -v "$BACKUP_DIR":/backup alpine \
  tar xzf /backup/shepherd-conf-backup.tgz -C /conf ./database.properties ./mongo.properties
# Your own TLS certificate: restore the keystore file named by TLS_KEYSTORE_FILE, and make sure
# TLS_KEYSTORE_PASS and ALIAS in .env match it
docker run --rm -v securityshepherd_conf:/conf -v "$BACKUP_DIR":/backup alpine \
  tar xzf /backup/shepherd-conf-backup.tgz -C /conf ./<your keystore file>
docker compose restart web
```

If you had hand-edited the old `server.xml` or `web.xml` (custom connectors, ports, ciphers and so on), re-apply those edits to the new Tomcat 11 files, using the backup as a reference. Don't copy the old files back.

### 3. Check it, then clean up

Log in, open a lesson, and check the scoreboard. Then delete `$HOME/shepherd-conf-backup` and `$HOME/shepherd-db-backup`, or move them somewhere safe. On Linux hosts the archives are owned by root, but you can still delete them because you own the directories.

## From the 3.1 virtual machine or a manual installation

The 3.1 virtual machine and the manual installation packs aren't maintained for 4.0, and there's no tested way to move their data across. Set up a new Docker installation as described in [Getting started](getting-started.md), then create the classes and players again.
