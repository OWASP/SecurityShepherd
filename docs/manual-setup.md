# Manual setup

This page is for operators who want to run Security Shepherd in their own Tomcat, without Docker. Docker Compose, as in [Getting started](getting-started.md), is simpler and is what the project tests on every change. Use this page if you can't run Docker, or already run Tomcat.

These steps were tested with Tomcat 11.0.27 on Java 17 and MariaDB 12.3, on Ubuntu. Paths assume Tomcat is in `/opt/tomcat`.

## What you need

- **Java 17 or newer,** for Tomcat 11, and JDK 17 with Maven 3.9.2 or newer to build the WAR.
- **Tomcat 11.** Tomcat 10 and older won't run Security Shepherd 4.0.
- **MariaDB, on the same machine as Tomcat.** Each level's database account is created for `localhost` only, so Tomcat must connect to MariaDB as `localhost`.
- **The Argon2 library,** which hashes passwords: `libargon2-1` on Debian and Ubuntu.
- **MongoDB,** only if you want the NoSQL injection level. The Docker setup uses MongoDB 4.1.13. The steps below leave it out.

On Ubuntu:

```bash
sudo apt install openjdk-17-jre-headless mariadb-server libargon2-1
```

Tomcat 11 isn't packaged by most distributions yet. Download it from [tomcat.apache.org](https://tomcat.apache.org/download-11.cgi), check the download against its published SHA-512, unpack it to `/opt/tomcat`, and run it as a user of its own, not root. The [Tomcat documentation](https://tomcat.apache.org/tomcat-11.0-doc/setup.html) explains how to run it as a service.

## 1. Build the WAR

The package above is only the Java runtime, so install the JDK to build the WAR. Run `mvn -v` to see which JDK Maven is using. If it isn't 17, install JDK 17 and point `JAVA_HOME` at it. `export` only applies to the current terminal.

```bash
sudo apt install openjdk-17-jdk-headless
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-$(dpkg --print-architecture)
```

Then build the WAR:

```bash
git clone https://github.com/OWASP/SecurityShepherd.git
cd SecurityShepherd
mvn clean package -DskipTests
```

This builds `target/owaspSecurityShepherd.war`.

## 2. Prepare MariaDB

The setup page creates the databases and the levels' accounts, so it needs an account that can create databases and users, such as `root` with a password. Set the root password if it has none:

```bash
sudo mariadb -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'a long random password'"
```

On Debian and Ubuntu this replaces `root`'s login through the system's `root` user, so from then on connect with `mariadb -uroot -p`.

Leave host name resolution on, which is the default for the distribution's packages: don't add `skip-name-resolve` to MariaDB's configuration. With it, a connection from Tomcat to `localhost` arrives as `127.0.0.1`, doesn't match the levels' `localhost` accounts, and the database-backed levels fail with "Access denied".

## 3. Turn on HTTPS

Security Shepherd expects HTTPS. Create a keystore, as Tomcat's user, in Tomcat's `conf` folder. For a real server, use your own certificate instead, as [Deploying into production](deploying-into-production.md#use-your-own-tls-certificate) describes.

```bash
cd /opt/tomcat
keytool -genkeypair -alias tomcat -keyalg RSA -keysize 3072 -validity 825 \
  -dname "CN=shepherd.example.com" -ext "SAN=dns:shepherd.example.com" \
  -storetype PKCS12 -keystore conf/shepherd.p12
```

Add an HTTPS connector to `conf/server.xml`, inside `<Service name="Catalina">`, with the keystore's password:

```xml
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true" maxThreads="150"
           scheme="https" secure="true" maxHttpHeaderSize="65536">
    <SSLHostConfig>
        <Certificate certificateKeystoreFile="conf/shepherd.p12"
                     certificateKeystorePassword="the keystore password"
                     certificateKeyAlias="tomcat" />
    </SSLHostConfig>
</Connector>
```

The HTTP connector on port 8080 already redirects to port 8443. To send every request there, add this before the closing `</web-app>` in Tomcat's `conf/web.xml`:

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>Entire Application</web-resource-name>
        <url-pattern>/*</url-pattern>
    </web-resource-collection>
    <user-data-constraint>
        <transport-guarantee>CONFIDENTIAL</transport-guarantee>
    </user-data-constraint>
</security-constraint>
```

These are the same changes the Docker image makes, in `docker/tomcat/serverxml.patch` and `webxml.patch`.

## 4. Write the database settings

Create `conf/database.properties`, readable by Tomcat's user only, with the root password from step 2:

```properties
databaseConnectionURL=jdbc:mariadb://localhost:3306/
DriverType=org.mariadb.jdbc.Driver
databaseSchema=core
databaseUsername=root
databasePassword=a long random password
databaseOptions=useUnicode=true&character_set_server=utf8mb4
```

```bash
chmod 600 /opt/tomcat/conf/database.properties
```

The setup page normally writes this file, but it can't connect to the database unless the file already exists when Tomcat starts ([#1082](https://github.com/OWASP/SecurityShepherd/issues/1082)). The `core` database doesn't exist yet, so Security Shepherd still shows the setup page.

## 5. Deploy

Remove Tomcat's own start page and deploy Security Shepherd in its place, so it's served at `/`:

```bash
rm -rf /opt/tomcat/webapps/ROOT
cp target/owaspSecurityShepherd.war /opt/tomcat/webapps/ROOT.war
/opt/tomcat/bin/startup.sh
```

Tomcat unpacks the WAR within a minute. Its log is in `/opt/tomcat/logs/catalina.out`, and Security Shepherd's in `/opt/tomcat/logs/security-shepherd.log`. While the database is empty, the log says `Unknown database 'core'`, which is expected.

## 6. Run setup

Open `https://<your server>:8443`. You're sent to the **No Database Configuration** page.

1. Paste the authentication token from `/opt/tomcat/conf/SecurityShepherd.auth` into **Authentication token**.
2. Enter **Hostname** `localhost`, **Port** `3306`, **DB Username** `root` and the password.
3. Choose **Fresh Database** under **Override Databases**. This creates the `core` database, the levels' databases and their accounts.
4. Leave **Enable NoSQL Challenge** off unless you've installed MongoDB, and **Enable Unsafe Challenges** off unless the server is isolated.
5. Submit the form.

Then log in as `admin` with the password `password`, and change it when asked. [Getting started](getting-started.md#try-the-levels) continues from there, and [Deploying into production](deploying-into-production.md) lists what else to change before other people use it.

## Upgrading

To move to a new version, build its WAR, stop Tomcat, replace `webapps/ROOT.war` and delete the unpacked `webapps/ROOT` folder, then start Tomcat. Back up the database first, with `mariadb-dump` as in [Backups and restore](backups.md#back-up-the-database). `conf/database.properties` and your Tomcat configuration are kept.
