# Load Tests

Tests to verify Security Shepherd survives aggressive automated scanning tools without database connection exhaustion. See [issue #536](https://github.com/OWASP/SecurityShepherd/issues/536).

## load-test.sh

End-to-end load test that:

1. Builds and starts the full Docker stack
2. Runs the initial database setup automatically
3. Creates 20 test users
4. Simulates 17 normal users (browsing every 3-8s) and 3 aggressive users (automated scanning, ~10 req/s each)
5. Monitors DB connections and app response times throughout
6. Reports pass/fail based on:
   - DB connections stay under 50 (pooling prevents exhaustion)
   - No health check failures (app stays responsive)
   - Response times stay under 10 seconds

### Usage

```bash
# Full run (builds everything from scratch)
./load-test.sh

# Skip the Maven/Docker build if stack images are already current
./load-test.sh --skip-build
```

### Requirements

- Docker
- curl
- Maven (unless using --skip-build)
- Ports 80, 443, 3306, 27017 available

### Results

Each run creates a timestamped directory under `results/` with:

- `monitor.csv` — time-series of DB connections, HTTP status, and response time
- `cookies_*.txt` — session cookies (auto-cleaned)
- `aggressive_*_requests.txt` — request counts per aggressive user
