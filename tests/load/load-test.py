#!/usr/bin/env python3
"""
Load test for Security Shepherd connection pooling (issue #536).

Modes:
  --target all       Full soak test (default): 20 concurrent users, monitors
                     DB connections and app responsiveness over time.
  --target getter    Targeted: tight loops against endpoints backed by Getter.java.
  --target setter    Targeted: tight loops against endpoints backed by Setter.java.
  --target getter setter   Both targeted classes sequentially.
  --methods authUser refreshMenu   Only specific methods within the target class.

Targeted mode runs each endpoint in isolation, checking DB connection count
before and after to detect leaks. Soak mode runs concurrent traffic.

Usage:
    python3 load-test.py --skip-setup --target getter
    python3 load-test.py --skip-setup --target getter --concurrency 10
    python3 load-test.py --skip-setup --target getter --methods authUser refreshMenu
    python3 load-test.py --skip-setup --target all --duration 5
    python3 load-test.py --skip-setup --target getter setter --iterations 200 --concurrency 5
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

MAX_ALLOWED_CONNS = 50

# ── Target Definitions ────────────────────────────────────────────
#
# Each entry maps a human-readable method name to the HTTP request
# that exercises it plus the Java method(s) it covers.  The runner
# calls these in a tight loop and measures connection growth.

GETTER_TARGETS = {
    "authUser": {
        "http": "POST", "path": "/login",
        "data": {"login": "nonexistent_leak_probe_{i}", "pwd": "wrong"},
        "auth_required": False,
        "java": "Getter.authUser — core login, was leaking on failed lookups",
    },
    "refreshMenu": {
        "http": "GET", "path": "/refreshMenu",
        "auth_required": True,
        "java": "Getter.getChallenges / getLessons / getTournamentModules / getIncrementalModules",
    },
    "apiLevels": {
        "http": "GET", "path": "/api/levels",
        "auth_required": True,
        "java": "Getter.getModulesJson",
    },
    "getModule": {
        "http": "GET", "path": "/getModule",
        "auth_required": True,
        "java": "Getter.getModuleAddress + checkPlayerResult",
    },
    "getProgress": {
        "http": "GET", "path": "/getProgress",
        "auth_required": True,
        "java": "Getter.getProgress / getJsonScore (admin)",
    },
    "getFeedback": {
        "http": "GET", "path": "/getFeedback",
        "auth_required": True,
        "java": "Getter.getFeedback (admin)",
    },
    "scoreboard": {
        "http": "GET", "path": "/scoreboard",
        "auth_required": True,
        "java": "Getter.getJsonScore + getScoreboardStatus",
    },
    "loginPage": {
        "http": "GET", "path": "/login.jsp",
        "auth_required": False,
        "java": "Getter settings: getRegistrationStatus / getStartTimeStatus / etc.",
    },
}

SETTER_TARGETS = {
    "register": {
        "http": "POST", "path": "/register",
        "data": {
            "userName": "setter_probe_{i}", "passWord": "probe",
            "passWordConfirm": "probe",
            "userAddress": "probe_{i}@test.com",
            "userAddressCnf": "probe_{i}@test.com",
        },
        "needs_csrf": True,
        "csrf_page": "/register.jsp",
        "auth_required": False,
        "java": "Setter.userCreate — registration path",
    },
    "passwordChange": {
        "http": "POST", "path": "/passwordChange",
        "data": {
            "currentPassword": "{user_pass}",
            "newPassword": "{user_pass}",
            "passwordConfirmation": "{user_pass}",
        },
        "needs_csrf": True,
        "auth_required": True,
        "java": "Setter.updatePassword",
    },
}

ALL_TARGETS = {"getter": GETTER_TARGETS, "setter": SETTER_TARGETS}


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

    stdout, _, _ = docker_compose("down", "-v")
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

    log("Logging in as admin (admin/password)...")
    session.get("/login.jsp")

    status, body, location = session.post("/login", {
        "login": ADMIN_USER,
        "pwd": ADMIN_DEFAULT_PASS,
    })

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

    log("Enabling registration...")
    token = session.token
    status, body, _ = session.post("/updateRegistration", {"csrfToken": token})

    if "Opened" in body:
        log("Registration enabled")
    elif "Closed" in body:
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


def login_user(username, password):
    """Login a single user and return the session, or None on failure."""
    session = ShepherdSession()
    session.get("/login.jsp")
    status, body, location = session.post("/login", {"login": username, "pwd": password})
    if "index.jsp" in str(location):
        return session
    return None


def login_users(num_users):
    """Login all test users and return their sessions."""
    log("Logging in test users...")
    sessions = {}
    logged_in = 0

    for i in range(1, num_users + 1):
        username = f"loadtest_user_{i}"
        session = login_user(username, username)
        if session:
            sessions[i] = session
            logged_in += 1
        else:
            warn(f"Login failed for {username}")

    if logged_in == 0:
        fail("No users could log in")

    log(f"{logged_in} / {num_users} users logged in")
    return sessions


# ── Targeted Scenario Runner ──────────────────────────────────────


def _warmup_pool():
    """Hit the app a few times to let Hikari establish idle connections."""
    s = ShepherdSession()
    for _ in range(5):
        s.get("/login.jsp")
    time.sleep(2)


def _worker_loop(spec, iterations, worker_id, session, conn_samples, errors_count):
    """Single worker thread: fires requests and periodically samples connections."""
    user_pass = "loadtest_user_1"

    for i in range(iterations):
        try:
            if spec["http"] == "GET":
                session.get(spec["path"])
            else:
                raw_data = spec.get("data", {})
                data = {}
                for k, v in raw_data.items():
                    v = v.replace("{i}", f"{worker_id}_{i}")
                    v = v.replace("{user_pass}", user_pass)
                    data[k] = v

                if spec.get("needs_csrf"):
                    csrf_page = spec.get("csrf_page", spec["path"])
                    csrf = session.get_csrf_from_page(csrf_page)
                    if csrf:
                        data["csrfToken"] = csrf
                    elif session.token:
                        data["csrfToken"] = session.token

                session.post(spec["path"], data)
        except Exception:
            errors_count.append(1)

        if i % 25 == 24:
            current = get_connections()
            if current:
                conn_samples.append(current)


def run_scenario(name, spec, iterations, session=None, concurrency=1):
    """
    Run a single endpoint scenario, optionally with concurrent threads.
    Returns dict with baseline, peak, final connection counts and request stats.
    """
    _warmup_pool()

    baseline = get_connections() or 0
    conn_samples = [baseline]
    errors_list = []

    if concurrency <= 1:
        _worker_loop(spec, iterations, 0, session, conn_samples, errors_list)
    else:
        def make_session(spec, base_session):
            if spec.get("auth_required"):
                s = login_user("loadtest_user_1", "loadtest_user_1")
                return s if s else base_session
            return ShepherdSession()

        with ThreadPoolExecutor(max_workers=concurrency) as pool:
            futures = []
            for w in range(concurrency):
                worker_session = session if w == 0 else make_session(spec, session)
                f = pool.submit(
                    _worker_loop, spec, iterations, w,
                    worker_session, conn_samples, errors_list,
                )
                futures.append(f)
            for f in futures:
                f.result()

    time.sleep(1)
    final = get_connections() or 0
    conn_samples.append(final)
    peak = max(conn_samples)

    return {
        "baseline": baseline,
        "peak": peak,
        "final": final,
        "iterations": iterations * concurrency,
        "errors": len(errors_list),
        "concurrency": concurrency,
    }


def run_targeted_tests(targets_to_run, methods_filter, iterations, concurrency,
                       admin_session):
    """Run targeted scenarios and print per-method results."""
    user_session = login_user("loadtest_user_1", "loadtest_user_1")
    if not user_session:
        user_session = admin_session

    results = {}
    all_passed = True

    for class_name, method_map in targets_to_run.items():
        print()
        log(f"Running targeted tests for {class_name.upper()}")
        print("-" * 59)

        for method_name, spec in method_map.items():
            if methods_filter and method_name not in methods_filter:
                continue

            session = admin_session if spec.get("auth_required") else ShepherdSession()
            if spec.get("auth_required") and user_session:
                session = user_session

            label = f"{class_name}.{method_name}"
            conc_label = f" x{concurrency} threads" if concurrency > 1 else ""
            log(f"  {label} ({spec['http']} {spec['path']}{conc_label})")
            log(f"    {spec['java']}")

            result = run_scenario(method_name, spec, iterations, session,
                                  concurrency=concurrency)
            results[label] = result

            bounded = result["peak"] <= MAX_ALLOWED_CONNS
            leaked = result["final"] > MAX_ALLOWED_CONNS

            status_color = "\033[0;32m" if (bounded and not leaked) else "\033[0;31m"
            status_word = "PASS" if (bounded and not leaked) else "FAIL"

            print(f"    Total reqs:  {result['iterations']}"
                  f" ({iterations}/thread x {concurrency})" if concurrency > 1
                  else f"    Iterations:  {result['iterations']}")
            print(f"    Baseline:    {result['baseline']} connections")
            print(f"    Peak:        {result['peak']} connections")
            print(f"    Final:       {result['final']} connections")
            if result["errors"]:
                print(f"    Errors:      {result['errors']}")
            print(f"    {status_color}{status_word}\033[0m")
            print()

            if not bounded or leaked:
                all_passed = False

    return all_passed, results


def print_targeted_summary(results, all_passed):
    """Print a final summary table for targeted tests."""
    print()
    print("=" * 76)
    print("  TARGETED TEST SUMMARY")
    print("=" * 76)
    print()
    print(f"  {'Scenario':<30} {'Reqs':>6} {'Conc':>5} {'Base':>5} {'Peak':>5} {'Final':>5}  Result")
    print(f"  {'-'*30} {'-'*6} {'-'*5} {'-'*5} {'-'*5} {'-'*5}  ------")

    for label, r in results.items():
        bounded = r["peak"] <= MAX_ALLOWED_CONNS
        leaked = r["final"] > MAX_ALLOWED_CONNS
        ok = bounded and not leaked
        mark = "\033[0;32mPASS\033[0m" if ok else "\033[0;31mFAIL\033[0m"
        conc = r.get("concurrency", 1)
        print(f"  {label:<30} {r['iterations']:>6} {conc:>5} "
              f"{r['baseline']:>5} {r['peak']:>5} {r['final']:>5}  {mark}")

    print()
    if all_passed:
        print("  \033[0;32mALL SCENARIOS PASSED\033[0m")
    else:
        print("  \033[0;31mSOME SCENARIOS FAILED\033[0m")
    print("=" * 76)


# ── Soak Test (original behavior) ─────────────────────────────────


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

        try:
            session.get(path)
            requests_made += 1
        except Exception:
            pass

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


# ── Soak Report ────────────────────────────────────────────────────


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
    print("  SOAK TEST RESULTS")
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
    if max_conns > MAX_ALLOWED_CONNS:
        print(f"  \033[0;31mFAIL: Max connections ({max_conns}) exceeded {MAX_ALLOWED_CONNS}\033[0m")
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


def run_soak_test(args):
    """Run the original soak test with concurrent users."""
    duration = args.duration * 60
    total_users = args.normal_users + args.aggressive_users

    script_dir = Path(__file__).resolve().parent
    results_dir = script_dir / "results" / datetime.now().strftime("%Y%m%d-%H%M%S")
    results_dir.mkdir(parents=True, exist_ok=True)
    monitor_file = str(results_dir / "monitor.csv")

    register_users(total_users)
    sessions = login_users(total_users)

    baseline = get_connections() or 0
    log(f"Baseline DB connections: {baseline}")

    log("Starting monitor...")
    stop_monitor = threading.Event()
    monitor_thread = threading.Thread(
        target=monitor_loop,
        args=(duration, args.monitor_interval, monitor_file, stop_monitor),
        daemon=True,
    )
    monitor_thread.start()

    log(f"Starting {args.normal_users} normal + {args.aggressive_users} aggressive users for {args.duration} minutes...")
    print()

    with ThreadPoolExecutor(max_workers=total_users) as executor:
        futures = {}

        for i in range(1, args.normal_users + 1):
            if i in sessions:
                f = executor.submit(normal_user_traffic, sessions[i], duration)
                futures[f] = ("normal", i)

        for i in range(args.normal_users + 1, total_users + 1):
            if i in sessions:
                f = executor.submit(aggressive_user_traffic, sessions[i], duration)
                futures[f] = ("aggressive", i)

        aggressive_total = 0
        for future in as_completed(futures):
            kind, user_id = futures[future]
            try:
                count = future.result()
                if kind == "aggressive":
                    aggressive_total += count
            except Exception as e:
                warn(f"User {user_id} ({kind}) error: {e}")

    time.sleep(5)
    stop_monitor.set()
    monitor_thread.join(timeout=10)

    print()
    log("Soak test complete. Analyzing results...")

    config = {
        "normal": args.normal_users,
        "aggressive": args.aggressive_users,
        "duration": duration,
        "baseline": baseline,
    }

    return generate_report(monitor_file, config, aggressive_total)


# ── Main ───────────────────────────────────────────────────────────


def main():
    parser = argparse.ArgumentParser(
        description="Security Shepherd load test — targeted or full soak",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
examples:
  %(prog)s --skip-setup --target getter
  %(prog)s --skip-setup --target getter --concurrency 10
  %(prog)s --skip-setup --target getter --methods authUser --iterations 500 --concurrency 20
  %(prog)s --skip-setup --target getter setter --iterations 200 --concurrency 5
  %(prog)s --skip-setup --target all --duration 3
  %(prog)s --list-targets
""",
    )

    parser.add_argument(
        "--target", nargs="+", default=["all"],
        choices=["all", "getter", "setter"],
        help="Which classes to test: getter, setter, or all (soak). Default: all",
    )
    parser.add_argument(
        "--methods", nargs="+", default=None,
        help="Specific methods within the target class (e.g. authUser refreshMenu)",
    )
    parser.add_argument(
        "--iterations", type=int, default=300,
        help="Requests per thread per scenario in targeted mode (default: 300)",
    )
    parser.add_argument(
        "--concurrency", type=int, default=1,
        help="Concurrent threads per scenario in targeted mode (default: 1)",
    )
    parser.add_argument(
        "--list-targets", action="store_true",
        help="List available targets and methods, then exit",
    )

    parser.add_argument("--skip-build", action="store_true", help="Skip Maven/Docker build")
    parser.add_argument("--skip-setup", action="store_true",
                        help="Skip build, service wait, and platform config (app already running)")
    parser.add_argument("--duration", type=int, default=5,
                        help="Soak test duration in minutes (default: 5)")
    parser.add_argument("--normal-users", type=int, default=17,
                        help="Normal users for soak test (default: 17)")
    parser.add_argument("--aggressive-users", type=int, default=3,
                        help="Aggressive users for soak test (default: 3)")
    parser.add_argument("--monitor-interval", type=int, default=10,
                        help="Monitor interval in seconds for soak test (default: 10)")

    args = parser.parse_args()

    if args.list_targets:
        for class_name, methods in ALL_TARGETS.items():
            print(f"\n  {class_name}:")
            for method_name, spec in methods.items():
                auth = "auth" if spec.get("auth_required") else "anon"
                print(f"    {method_name:<20} {spec['http']:<4} {spec['path']:<20} [{auth}]  {spec['java']}")
        print()
        sys.exit(0)

    script_dir = Path(__file__).resolve().parent
    project_root = script_dir.parent.parent

    is_targeted = "all" not in args.target

    if not args.skip_setup:
        build_and_start(args.skip_build, str(project_root))
        wait_for_services()
        admin_session = configure_platform()
    else:
        log("Skipping setup (--skip-setup), assuming app is running")
        admin_session = ShepherdSession()
        admin_session.get("/login.jsp")
        status, body, location = admin_session.post("/login", {
            "login": ADMIN_USER, "pwd": ADMIN_NEW_PASS,
        })
        if "index.jsp" not in str(location):
            admin_session.post("/login", {
                "login": ADMIN_USER, "pwd": ADMIN_DEFAULT_PASS,
            })

    if is_targeted:
        targets_to_run = {}
        for t in args.target:
            if t in ALL_TARGETS:
                targets_to_run[t] = ALL_TARGETS[t]

        if not args.skip_setup:
            register_users(2)

        all_passed, results = run_targeted_tests(
            targets_to_run, args.methods, args.iterations,
            args.concurrency, admin_session
        )
        print_targeted_summary(results, all_passed)
        sys.exit(0 if all_passed else 1)
    else:
        passed = run_soak_test(args)
        sys.exit(0 if passed else 1)


if __name__ == "__main__":
    main()
