# Backups and restore

This page is for operators of a Docker installation. Back up before an event, before an upgrade, and after an event if you want to keep the results.

## What to back up

Docker Compose keeps Security Shepherd's data in volumes. Their names start with the Compose project name, which is the name of the folder you cloned into, for example `securityshepherd_data`. `docker volume ls` lists them.

| Volume | Holds | Back it up? |
| --- | --- | --- |
| `data` | The MariaDB databases: `core`, with the users, classes, results and settings, and a schema for each SQL-based level | Yes. This is the one that matters. |
| `conf` | Tomcat's configuration: the TLS keystore and its password, `database.properties` and `mongo.properties`, `server.xml` and `web.xml` | Yes, if you've changed anything in it, such as your own certificate |
| `mongodata`, `mongoconfig` | The data for the NoSQL injection level | No. The setup page recreates it. |
| `logs` | Tomcat's and Security Shepherd's logs | If you want to keep them, for example after an incident |

Both backups contain secrets. The database holds every user's password hash, and the `conf` volume holds the TLS private key and the database password. Keep them outside the repository, which is the Docker build context, and readable by you only.

## Back up the database

This dumps every database, with the stored procedures, while Security Shepherd keeps running:

```bash
BACKUP_DIR="$HOME/shepherd-backup"
mkdir -p "$BACKUP_DIR" && chmod 700 "$BACKUP_DIR"
(umask 077 && docker exec secshep_mariadb sh -c \
  'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; exec mariadb-dump --skip-ssl -uroot --all-databases --routines --events --single-transaction' \
  > "$BACKUP_DIR/shepherd-db-$(date +%F).sql")
```

`MYSQL_ROOT_PASSWORD` is the container's copy of `DB_PASS` from when it was created. If you've changed the root password since, use `mariadb-dump --skip-ssl -uroot -p …` in place of the `sh -c` line, and type the password when asked.

## Back up the `conf` volume

```bash
BACKUP_DIR="$HOME/shepherd-backup"
mkdir -p "$BACKUP_DIR" && chmod 700 "$BACKUP_DIR"
docker run --rm -v securityshepherd_conf:/conf -v "$BACKUP_DIR":/backup alpine sh -c \
  'umask 077 && touch /backup/shepherd-conf.tgz && chmod 600 /backup/shepherd-conf.tgz \
   && tar czf /backup/shepherd-conf.tgz -C /conf .'
```

## Restore the database

Restoring replaces every database with the one in the dump, so all progress since the backup is lost. Then restart Tomcat, because Security Shepherd keeps its settings in memory:

```bash
docker exec -i secshep_mariadb sh -c \
  'export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; exec mariadb --skip-ssl -uroot' \
  < "$HOME/shepherd-backup/shepherd-db-2026-11-14.sql"
docker compose restart web
```

## Restore the `conf` volume

Restore only the files you need. For example, the connection settings:

```bash
docker run --rm -v securityshepherd_conf:/conf -v "$HOME/shepherd-backup":/backup alpine \
  tar xzf /backup/shepherd-conf.tgz -C /conf ./database.properties ./mongo.properties
docker compose restart web
```

Don't restore `server.xml` or `web.xml` from a backup made with an older version of Security Shepherd, because they belong to that version's Tomcat. See [Upgrading](upgrading.md).

## Start again from scratch

To reset an installation to a fresh one, remove its volumes. This deletes every user, result and setting:

```bash
docker compose down -v
docker compose up -d
```

Then run the setup page again, as described in [Getting started](getting-started.md#first-time-setup).
