# Development Workflow

This guide explains the full development cycle for contributing to Security Shepherd.

## 1. Create Your Branch

Fork the repository or create a branch from the `dev` branch:

```bash
git checkout dev
git pull origin dev
git checkout -b "dev#<issue-number>"
```

Branch naming convention: `dev#<issueNumber>` (e.g., `dev#536`)

## 2. Make Your Changes

Edit the code in your branch. Key directories:

- `src/main/java/` - Java source code
- `src/main/webapp/` - Web resources (JSP, CSS, JS)
- `src/test/java/` - Unit tests

## 3. Build the WAR

Maven must run on JDK 17. On JDK 25 or newer the build stops at its formatting check with a `google-java-format(jvm-version)` error. Run `mvn -v` to see which JDK Maven is using. If it isn't 17, install JDK 17 and point `JAVA_HOME` at it:

```bash
# macOS
brew install --cask temurin@17
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Ubuntu
sudo apt install openjdk-17-jdk-headless
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-$(dpkg --print-architecture)
```

On other systems, install JDK 17 from [Adoptium](https://adoptium.net/) and set `JAVA_HOME` to its folder. `export` only applies to the current terminal.

Build the application with Maven:

```bash
mvn -Pdocker clean install -DskipTests
```

This generates:
- The WAR file in `target/`
- HTTPS certificates for Docker

## 4. Test in Runtime Environment

Start the full application stack using Docker Compose:

```bash
# Build and start all containers (MariaDB, MongoDB, Tomcat)
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build
```

The Tomcat image installs the Ubuntu security updates released since its base image was published. Docker caches the base image and that build step, so `--build` can reuse an old base image and old packages. Before testing or scanning an image, for example for a release, rebuild it without the cache:

```bash
docker-compose build --pull --no-cache web
docker-compose up -d
```

Access the application:
- **HTTP**: http://localhost
- **HTTPS**: https://localhost:8443

Default login credentials:
- Username: `admin`
- Password: `password`

### Viewing Logs

```bash
# View all logs
docker-compose logs -f

# View only web container logs
docker-compose logs -f web
```

### Rebuilding After Code Changes

After making additional changes:

```bash
# Rebuild the WAR
mvn -Pdocker clean install -DskipTests

# Rebuild and restart only the web container
docker-compose up -d --build web
```

### Stopping the Environment

```bash
docker-compose down
```

To also remove volumes (database data):

```bash
docker-compose down -v
```

## 5. Run Automated Tests

See [testing.md](testing.md) for instructions on running the unit and integration test suite.

## 6. Submit Your Pull Request

When your changes are complete and tests pass:

1. Push your branch to your fork/origin
2. Create a Pull Request targeting the `dev` branch
3. Ensure all CI checks pass

See [CONTRIBUTING.md](https://github.com/OWASP/SecurityShepherd/blob/dev/CONTRIBUTING.md) for code formatting and PR guidelines.

## Environment Configuration

The `.env` file in the project root contains environment variables for Docker. Key variables:

| Variable | Description |
|----------|-------------|
| `DB_PASS` | MariaDB root password |
| `HTTP_PORT` | HTTP port (default: 80) |
| `HTTPS_PORT` | HTTPS port (default: 8443) |

## Troubleshooting

### Container Won't Start

Check if ports are already in use:

```bash
# Check if port 80 is in use
lsof -i :80
```

### Database Connection Issues

Ensure the database container is healthy:

```bash
docker-compose ps
docker-compose logs db
```

### Changes Not Reflected

Make sure you rebuilt both the WAR and the container:

```bash
mvn -Pdocker clean install -DskipTests
docker-compose up -d --build web
```
