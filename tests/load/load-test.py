#!/usr/bin/env python3
"""
Load test for Security Shepherd connection pooling (issue #536).

Simulates 20 users: 17 doing normal browsing, 3 running aggressive
automated scanning. Monitors DB connections and app responsiveness
to verify the connection pool prevents exhaustion.

Usage:
    python3 load-test.py [--skip-build] [--duration MINUTES] [--users NORMAL AGGRESSIVE]
"""

import argparse
import csv
import http.cookiejar
import os
import re
import ssl
import subprocess
import sys
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime
from pathlib import Path

# Disable SSL verification globally (self-signed cert)
SSL_CTX = ssl.create_default_context()
SSL_CTX.check_hostname = False
SSL_CTX.verify_mode = ssl.CERT_NONE

BASE_URL = "https://localhost"
DB_CONTAINER = "secshep_mariadb"
TOMCAT_CONTAINER = "secshep_tomcat"
DB_PASS = "CowSaysMoo"
ADMIN_USER = "admin"
ADMIN_DEFAULT_PASS = "password"
ADMIN_NEW_PASS = "LoadTestAdmin1"

SPIDER_PATHS = [
    "/login.jsp", "/register.jsp", "/index.jsp", "/logout",
    "/admin/", "/challenges/", "/lessons/", "/setup.jsp",
    "/css/theCss.css", "/js/jquery.js", "/login", "/register",
    "/passwordChange", "/usernameChange", "/refreshMenu",
    "/mobileLogin", "/setup", "/getModule", "/getCheat",
    "/feedbackSubmit", "/solutionSubmit",
]

NORMAL_PAGES = ["/login.jsp", "/index.jsp", "/register.jsp", "/logout"]


# ── Utilities ──────────────────────────────────────────────────────


def log(msg):
    print(f"\033[0;32m[+]\033[0m {msg}", flush=True)


def warn(msg):
    print(f"\033[1;33m[!]\033[0m {msg}", flush=True)


def fail(msg):
    print(f"\033[0;31m[-]\033[0m {msg}", flush=True)
    sys.exit(1)


def docker_exec(container, cmd):
    """Run a command in a Docker container and return stdout."""
    result = subprocess.run(
        ["docker", "exec", container] + cmd,
        capture_output=True, text=True, timeout=30
    )
    return result.stdout.strip(), result.returncode


def docker_compose(*args):
    """Run docker compose command."""
    result = subprocess.run(
        ["docker", "compose"] + list(args),
        capture_output=True, text=True, timeout=300
    )
    return result.stdout, result.stderr, result.returncode


def db_query(sql):
    """Execute a SQL query against MariaDB and return stdout."""
    stdout, rc = docker_exec(DB_CONTAINER, [
        "mariadb", f"-uroot", f"-p{DB_PASS}", "-sN", "-e", sql
    ])
    return stdout if rc == 0 else None


def get_connections():
    """Get current DB thread count."""
    result = db_query("SHOW STATUS LIKE 'Threads_connected';")
    if result:
        parts = result.split()
        if len(parts) >= 2:
            return int(parts[1])
    return None


class ShepherdSession:
    """HTTP session with cookie handling for Security Shepherd."""

    def __init__(self):
        self.cookie_jar = http.cookiejar.CookieJar()
        self.opener = urllib.request.build_opener(
            urllib.request.HTTPCookieProcessor(self.cookie_jar),
            urllib.request.HTTPSHandler(context=SSL_CTX),
        )

    def get(self, path, follow_redirects=True):
        """GET request, returns (status_code, body, headers)."""
        url = BASE_URL + path
        try:
            req = urllib.request.Request(url)
            resp = self.opener.open(req, timeout=10)
            return resp.status, resp.read().decode("utf-8", errors="replace"), dict(resp.headers)
        except urllib.error.HTTPError as e:
            return e.code, e.read().decode("utf-8", errors="replace"), dict(e.headers)
        except Exception:
            return 0, "", {}

    def post(self, path, data, follow_redirects=False):
        """POST request, returns (status_code, body, location_header)."""
        url = BASE_URL + path
        encoded = urllib.parse.urlencode(data).encode("utf-8")
        req = urllib.request.Request(url, data=encoded, method="POST")
        req.add_header("Content-Type", "application/x-www-form-urlencoded")
        try:
            resp = self.opener.open(req, timeout=10)
            return resp.status, resp.read().decode("utf-8", errors="replace"), resp.url
        except urllib.error.HTTPError as e:
            location = e.headers.get("Location", "")
            return e.code, e.read().decode("utf-8", errors="replace"), location
        except Exception:
            return 0, "", ""

    @property
    def token(self):
        """Get the CSRF token cookie value."""
        for cookie in self.cookie_jar:
            if cookie.name == "token":
                return cookie.value
        return None

    def get_csrf_from_page(self, path):
        """Extract CSRF token from page HTML."""
        _, body, _ = self.get(path)
        match = re.search(r'csrfToken:\s*"([^"]+)"', body)
        return match.group(1) if match else None


# ── Setup Steps ────────────────────────────────────────────────────


def build_and_start(skip_build, project_root):
    """Build and start the Docker stack."""
    os.chdir(project_root)

    if not skip_build:
        log("Building WAR and Docker artifacts...")
        result = subprocess.run(
            ["mvn", "-Pdocker", "clean", "install", "-DskipTests", "-B", "-q"],
            timeout=300
        )
        if result.returncode != 0:
            fail("Maven build failed")
        subprocess.run(["docker", "compose", "build", "--no-cache", "-q"], timeout=300)
    else:
        log("Skipping build (--skip-build)")

    # Check for existing volumes
    stdout, _, _ = docker_compose("down", "-v")
    # Remove any orphaned volumes
    result = subprocess.run(
        ["docker", "volume", "ls", "-q", "--filter", "name=securityshepherd"],
        capture_output=True, text=True
    )
    for vol in result.stdout.strip().split("\n"):
        if vol:
            subprocess.run(["docker", "volume", "rm", vol], capture_output=True)

    log("Starting stack...")
    stdout, stderr, rc = docker_compose("up", "-d")


def wait_for_services():
    """Wait for MariaDB and Tomcat to be ready."""
    log("Waiting for MariaDB...")
    for i in range(60):
        _, rc = docker_exec(DB_CONTAINER, ["mariadb", f"-uroot", f"-p{DB_PASS}", "-e", "SELECT 1"])
        if rc == 0:
            break
        time.sleep(2)
    else:
        fail("MariaDB did not start in time")
    log("MariaDB ready")

    log("Waiting for Tomcat...")
    for i in range(60):
        try:
            req = urllib.request.Request(BASE_URL)
            urllib.request.urlopen(req, timeout=3, context=SSL_CTX)
            break
        except Exception:
            time.sleep(2)
    else:
        fail("Tomcat did not start in time")
    log("Tomcat ready")


def configure_platform():
    """Login as admin, change password, and enable registration."""
    session = ShepherdSession()

    # Get initial session
    log("Logging in as admin (admin/password)...")
    session.get("/login.jsp")

    # Login
    status, body, location = session.post("/login", {
        "login": ADMIN_USER,
        "pwd": ADMIN_DEFAULT_PASS,
    })

    # Change temporary password
    log("Changing admin password...")
    token = session.token
    if not token:
        fail("No CSRF token cookie after admin login")

    session.post("/passwordChange", {
        "currentPassword": ADMIN_DEFAULT_PASS,
        "newPassword": ADMIN_NEW_PASS,
        "passwordConfirmation": ADMIN_NEW_PASS,
        "csrfToken": token,
    })
    log("Admin password changed")

    # Enable registration
    log("Enabling registration...")
    token = session.token
    status, body, _ = session.post("/updateRegistration", {"csrfToken": token})

    if "Opened" in body:
        log("Registration enabled")
    elif "Closed" in body:
        # Was already open, got toggled closed — toggle again
        session.post("/updateRegistration", {"csrfToken": token})
        log("Registration enabled (toggled twice)")
    else:
        warn(f"Toggle response: {body[:200]}")
        fail("Could not enable registration")

    return session


def register_users(num_users):
    """Register test users via the web UI."""
    log(f"Registering {num_users} test users...")
    created = 0

    for i in range(1, num_users + 1):
        username = f"loadtest_user_{i}"
        session = ShepherdSession()

        csrf = session.get_csrf_from_page("/register.jsp")
        if not csrf:
            warn(f"Could not get CSRF token for {username}")
            continue

        status, body, location = session.post("/register", {
            "userName": username,
            "passWord": username,
            "passWordConfirm": username,
            "userAddress": f"{username}@test.com",
            "userAddressCnf": f"{username}@test.com",
            "csrfToken": csrf,
        })

        if status == 302 or "login.jsp" in str(location):
            created += 1
        else:
            warn(f"Failed to register {username} (HTTP {status})")

    if created == 0:
        fail("No users were registered")

    log(f"{created} / {num_users} users registered")
    return created


def login_users(num_users):
    """Login all test users and return their sessions."""
    log("Logging in test users...")
    sessions = {}
    logged_in = 0

    for i in range(1, num_users + 1):
        username = f"loadtest_user_{i}"
        session = ShepherdSession()
        session.get("/login.jsp")

        status, body, location = session.post("/login", {
            "login": username,
            "pwd": username,
        })

        if "index.jsp" in str(location):
            sessions[i] = session
            logged_in += 1
        else:
            warn(f"Login failed for {username} (HTTP {status}, location: {location})")

    if logged_in == 0:
        fail("No users could log in")

    log(f"{logged_in} / {num_users} users logged in")
    return sessions


# ── Traffic Simulation ─────────────────────────────────────────────


def normal_user_traffic(session, duration):
    """Simulate a normal user browsing every 3-8 seconds."""
    import random
    end_time = time.time() + duration
    requests_made = 0

    while time.time() < end_time:
        page = random.choice(NORMAL_PAGES)
        try:
            session.get(page)
            requests_made += 1
        except Exception:
            pass
        time.sleep(random.uniform(3, 8))

    return requests_made


def aggressive_user_traffic(session, duration):
    """Simulate aggressive automated scanning (~10 req/s)."""
    import random
    end_time = time.time() + duration
    requests_made = 0

    while time.time() < end_time:
        path = random.choice(SPIDER_PATHS)

        # GET (spider)
        try:
            session.get(path)
            requests_made += 1
        except Exception:
            pass

        # POST with random params (fuzzer)
        try:
            session.post(path, {
                "param1": "test",
                "param2": os.urandom(8).hex(),
            })
            requests_made += 1
        except Exception:
            pass

        time.sleep(random.uniform(0, 0.2))

    return requests_made


# ── Monitoring ─────────────────────────────────────────────────────


def monitor_loop(duration, interval, results_file, stop_event):
    """Monitor DB connections and app responsiveness."""
    with open(results_file, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["timestamp", "db_connections", "http_status", "response_time_ms"])

        while not stop_event.is_set():
            ts = datetime.now().strftime("%H:%M:%S")
            conns = get_connections()

            # Health check
            start = time.time()
            try:
                req = urllib.request.Request(BASE_URL + "/login.jsp")
                resp = urllib.request.urlopen(req, timeout=10, context=SSL_CTX)
                http_status = resp.status
            except urllib.error.HTTPError as e:
                http_status = e.code
            except Exception:
                http_status = 0
            response_ms = int((time.time() - start) * 1000)

            writer.writerow([ts, conns or "N/A", http_status, response_ms])
            f.flush()

            conns_str = str(conns) if conns else "N/A"
            print(
                f"  {ts} | Connections: {conns_str:<4} | HTTP: {http_status} | Response: {response_ms}ms",
                flush=True,
            )

            stop_event.wait(interval)


# ── Report ─────────────────────────────────────────────────────────


def generate_report(results_file, config, aggressive_total):
    """Parse monitor CSV and print results."""
    connections = []
    responses = []
    failed = 0
    total = 0

    with open(results_file) as f:
        reader = csv.DictReader(f)
        for row in reader:
            total += 1
            try:
                conns = int(row["db_connections"])
                connections.append(conns)
            except (ValueError, KeyError):
                pass
            try:
                resp = int(row["response_time_ms"])
                responses.append(resp)
            except (ValueError, KeyError):
                pass
            try:
                status = int(row["http_status"])
                if status == 0 or status >= 500:
                    failed += 1
            except (ValueError, KeyError):
                failed += 1

    if not connections or not responses:
        fail("No monitoring data collected")

    max_conns = max(connections)
    min_conns = min(connections)
    avg_conns = sum(connections) // len(connections)
    max_resp = max(responses)
    avg_resp = sum(responses) // len(responses)

    print()
    print("=" * 59)
    print("  LOAD TEST RESULTS")
    print("=" * 59)
    print()
    print("  Configuration:")
    print(f"    Normal users:      {config['normal']} (request every 3-8s)")
    print(f"    Aggressive users:  {config['aggressive']} (automated scanning, ~10 req/s each)")
    print(f"    Duration:          {config['duration'] // 60} minutes")
    print()
    print("  Database Connections:")
    print(f"    Baseline:          {config['baseline']}")
    print(f"    Min:               {min_conns}")
    print(f"    Max:               {max_conns}")
    print(f"    Average:           {avg_conns}")
    print()
    print("  App Responsiveness:")
    print(f"    Avg response:      {avg_resp}ms")
    print(f"    Max response:      {max_resp}ms")
    print(f"    Failed checks:     {failed} / {total}")
    print()
    print("  Aggressive Traffic:")
    print(f"    Total requests:    {aggressive_total}")
    print()

    passed = True
    if max_conns > 50:
        print(f"  \033[0;31mFAIL: Max connections ({max_conns}) exceeded 50\033[0m")
        passed = False
    if failed > 0:
        print(f"  \033[0;31mFAIL: {failed} health checks failed\033[0m")
        passed = False
    if max_resp > 10000:
        print(f"  \033[0;31mFAIL: Max response time ({max_resp}ms) exceeded 10s\033[0m")
        passed = False
    if passed:
        print("  \033[0;32mPASS: Connection pool held under load\033[0m")

    print()
    print(f"  Full results: {results_file}")
    print("=" * 59)

    return passed


# ── Main ───────────────────────────────────────────────────────────


def main():
    parser = argparse.ArgumentParser(description="Security Shepherd load test")
    parser.add_argument("--skip-build", action="store_true", help="Skip Maven/Docker build")
    parser.add_argument("--duration", type=int, default=5, help="Test duration in minutes (default: 5)")
    parser.add_argument("--normal-users", type=int, default=17, help="Number of normal users (default: 17)")
    parser.add_argument("--aggressive-users", type=int, default=3, help="Number of aggressive users (default: 3)")
    parser.add_argument("--monitor-interval", type=int, default=10, help="Monitor interval in seconds (default: 10)")
    args = parser.parse_args()

    duration = args.duration * 60
    total_users = args.normal_users + args.aggressive_users

    script_dir = Path(__file__).resolve().parent
    project_root = script_dir.parent.parent
    results_dir = script_dir / "results" / datetime.now().strftime("%Y%m%d-%H%M%S")
    results_dir.mkdir(parents=True, exist_ok=True)
    monitor_file = str(results_dir / "monitor.csv")

    # Step 1: Build and start
    build_and_start(args.skip_build, str(project_root))

    # Step 2: Wait for services
    wait_for_services()

    # Step 3: Configure platform (admin login, password change, enable registration)
    configure_platform()

    # Step 4: Register users
    register_users(total_users)

    # Step 5: Login users
    sessions = login_users(total_users)

    # Step 6: Record baseline
    baseline = get_connections() or 0
    log(f"Baseline DB connections: {baseline}")

    # Step 7: Start monitoring
    log("Starting monitor...")
    stop_monitor = threading.Event()
    monitor_thread = threading.Thread(
        target=monitor_loop,
        args=(duration, args.monitor_interval, monitor_file, stop_monitor),
        daemon=True,
    )
    monitor_thread.start()

    # Step 8: Start traffic
    log(f"Starting {args.normal_users} normal + {args.aggressive_users} aggressive users for {args.duration} minutes...")
    print()

    with ThreadPoolExecutor(max_workers=total_users) as executor:
        futures = {}

        # Normal users
        for i in range(1, args.normal_users + 1):
            if i in sessions:
                f = executor.submit(normal_user_traffic, sessions[i], duration)
                futures[f] = ("normal", i)

        # Aggressive users
        for i in range(args.normal_users + 1, total_users + 1):
            if i in sessions:
                f = executor.submit(aggressive_user_traffic, sessions[i], duration)
                futures[f] = ("aggressive", i)

        # Wait for all to complete
        aggressive_total = 0
        for future in as_completed(futures):
            kind, user_id = futures[future]
            try:
                count = future.result()
                if kind == "aggressive":
                    aggressive_total += count
            except Exception as e:
                warn(f"User {user_id} ({kind}) error: {e}")

    # Step 9: Stop monitoring and report
    time.sleep(5)
    stop_monitor.set()
    monitor_thread.join(timeout=10)

    print()
    log("Load test complete. Analyzing results...")

    config = {
        "normal": args.normal_users,
        "aggressive": args.aggressive_users,
        "duration": duration,
        "baseline": baseline,
    }

    passed = generate_report(monitor_file, config, aggressive_total)
    sys.exit(0 if passed else 1)


if __name__ == "__main__":
    main()
