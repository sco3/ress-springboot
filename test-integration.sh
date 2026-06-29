#!/bin/bash
set -euo pipefail

#
# Integration test for ress using Docker Compose.
#
# Scenarios mirror DockerCassandraTest.java:
#   1. Cassandra connectivity & keyspace init
#   2. Table existence (hrcc_subscriber, hrcc_msisdn_imsi, hrcc_historical_*)
#   3. IMSI resolution from MSISDN
#   4. Subscriber profile lookup
#   5. Historical data search (hourly + daily)
#   6. Schema generation endpoints
#   7. BSON handling
#
# Usage:
#   ./test-integration.sh              # run all tests
#   ./test-integration.sh --no-build   # skip mvn + docker build, reuse existing image
#

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

COMPOSE_FILE="docker-compose.yml"
APP_BASE="http://localhost:8009/ress/api/v1"
HTTPS_BASE="https://localhost:8010/ress/api/v1"
CASSANDRA_HOST="localhost"
CASSANDRA_PORT=9042
HEALTH_TIMEOUT=120
HEALTH_INTERVAL=3
TOKEN=""
PASS=0
FAIL=0
TOTAL=0
SKIP_BUILD=false

for arg in "$@"; do
  case "$arg" in
    --no-build) SKIP_BUILD=true ;;
  esac
done

# ─── Helpers ──────────────────────────────────────────────────────────────────

log()   { printf "\n\033[1;36m==>\033[0m %s\n" "$1"; }
pass()  { printf "  \033[1;32m[PASS]\033[0m %s\n" "$1"; PASS=$((PASS+1)); TOTAL=$((TOTAL+1)); }
fail()  { printf "  \033[1;31m[FAIL]\033[0m %s\n" "$1"; FAIL=$((FAIL+1)); TOTAL=$((TOTAL+1)); }
skip()  { printf "  \033[1;33m[SKIP]\033[0m %s\n" "$1"; TOTAL=$((TOTAL+1)); }
die()   { printf "\n\033[1;31mFATAL:\033[0m %s\n" "$1" >&2; cleanup; exit 1; }

assert_eq() {
  local label="$1" expected="$2" actual="$3"
  if [ "$expected" = "$actual" ]; then
    pass "$label"
  else
    fail "$label (expected='$expected', actual='$actual')"
  fi
}

assert_contains() {
  local label="$1" haystack="$2" needle="$3"
  if echo "$haystack" | grep -qF "$needle"; then
    pass "$label"
  else
    fail "$label (missing '$needle')"
  fi
}

assert_not_empty() {
  local label="$1" value="$2"
  if [ -n "$value" ]; then
    pass "$label"
  else
    fail "$label (empty)"
  fi
}

# curl wrapper that ignores SSL errors for HTTPS
curl_k() {
  curl -sk --max-time 30 "$@"
}

wait_for_url() {
  local url="$1" label="$2" timeout="${3:-$HEALTH_TIMEOUT}"
  local elapsed=0
  while [ "$elapsed" -lt "$timeout" ]; do
    if curl_k -o /dev/null -w '%{http_code}' "$url" 2>/dev/null | grep -qE '^[23]'; then
      return 0
    fi
    sleep "$HEALTH_INTERVAL"
    elapsed=$((elapsed + HEALTH_INTERVAL))
  done
  return 1
}

# ─── Cleanup ──────────────────────────────────────────────────────────────────

cleanup() {
  log "Tearing down docker-compose"
  docker-compose -f "$COMPOSE_FILE" down -v --remove-orphans 2>/dev/null || true
}

trap cleanup EXIT

# ─── Build ────────────────────────────────────────────────────────────────────

if [ "$SKIP_BUILD" = false ]; then
  log "Building Maven project"
  mvn clean package -DskipTests -q || die "Maven build failed"

  log "Building Docker image (ress-srv:latest)"
  docker build -t ress-srv:latest . || die "Docker build failed"
fi

# ─── Start stack ──────────────────────────────────────────────────────────────

log "Starting docker-compose stack"
cleanup 2>/dev/null || true
docker-compose -f "$COMPOSE_FILE" up -d || die "docker-compose up failed"

# ─── Wait for Cassandra ──────────────────────────────────────────────────────

log "Waiting for Cassandra to be ready on port $CASSANDRA_PORT"
elapsed=0
while [ "$elapsed" -lt "$HEALTH_TIMEOUT" ]; do
  if docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e "describe cluster" >/dev/null 2>&1; then
    break
  fi
  sleep "$HEALTH_INTERVAL"
  elapsed=$((elapsed + HEALTH_INTERVAL))
done

if [ "$elapsed" -ge "$HEALTH_TIMEOUT" ]; then
  die "Cassandra did not become ready within ${HEALTH_TIMEOUT}s"
fi
pass "Cassandra is ready"

# Extra settle time for custom entrypoint to finish running init.cql / ress-data.cql
log "Waiting for Cassandra init scripts to complete"
sleep 15

# ─── Wait for webserver ──────────────────────────────────────────────────────

log "Waiting for webserver to be ready on port 8009"
if wait_for_url "$APP_BASE/testUnprotected" "webserver health"; then
  pass "Webserver is ready"
else
  die "Webserver did not become ready within ${HEALTH_TIMEOUT}s"
fi

# ─── Scenario 1: Cassandra connectivity & keyspace init ──────────────────────

log "Scenario 1: Cassandra connectivity & keyspace"

result=$(docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e \
  "describe keyspace ress" 2>&1) || true
assert_contains "Keyspace 'ress' exists" "$result" "CREATE KEYSPACE"

# ─── Scenario 2: Table existence ─────────────────────────────────────────────

log "Scenario 2: Table existence"

result=$(docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e \
  "describe table ress.hrcc_subscriber" 2>&1) || true
assert_contains "Table hrcc_subscriber exists" "$result" "hrcc_subscriber"

result=$(docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e \
  "describe table ress.hrcc_msisdn_imsi" 2>&1) || true
assert_contains "Table hrcc_msisdn_imsi exists" "$result" "hrcc_msisdn_imsi"

result=$(docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e \
  "describe table ress.hrcc_historical_h_1" 2>&1) || true
assert_contains "Table hrcc_historical_h_1 exists" "$result" "hrcc_historical_h_1"

result=$(docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e \
  "describe table ress.hrcc_historical_d_1" 2>&1) || true
assert_contains "Table hrcc_historical_d_1 exists" "$result" "hrcc_historical_d_1"

result=$(docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e \
  "describe table ress.hrcc_historical_h_1i" 2>&1) || true
assert_contains "Table hrcc_historical_h_1i exists" "$result" "hrcc_historical_h_1i"

result=$(docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e \
  "describe table ress.cas_properties" 2>&1) || true
assert_contains "Table cas_properties exists" "$result" "cas_properties"

# ─── Scenario 3: Unprotected endpoint (no auth) ─────────────────────────────

log "Scenario 3: Unprotected endpoint"

response=$(curl_k -w '\n%{http_code}' "$APP_BASE/testUnprotected" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "GET /testUnprotected returns 200" "200" "$http_code"
assert_not_empty "testUnprotected body not empty" "$body"

# ─── Scenario 4: Authentication ──────────────────────────────────────────────

log "Scenario 4: Authentication"

response=$(curl_k -w '\n%{http_code}' \
  "$APP_BASE/login?user=ress&password=ress" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "POST /login returns 200" "200" "$http_code"

TOKEN=$(echo "$body" | python3 -c "import sys,json; print(json.load(sys.stdin).get('token',''))" 2>/dev/null || true)
assert_not_empty "Login returns token" "$TOKEN"

# ─── Scenario 5: Protected endpoint without token → 403 ─────────────────────

log "Scenario 5: Protected endpoint without token"

response=$(curl_k -w '\n%{http_code}' \
  "$APP_BASE/subscriber/profile?imsi=1" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
assert_eq "Profile without token returns 403" "403" "$http_code"

# ─── Scenario 6: IMSI resolution ─────────────────────────────────────────────

log "Scenario 6: IMSI resolution (testImsiResolver scenario)"

response=$(curl_k -w '\n%{http_code}' \
  -b "token=$TOKEN" \
  "$APP_BASE/subscriber/imsi?msisdn=msisdn-1" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "IMSI lookup msisdn-1 returns 200" "200" "$http_code"
count=$(echo "$body" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
assert_eq "msisdn-1 resolves to 1 IMSI" "1" "$count"

response=$(curl_k -w '\n%{http_code}' \
  -b "token=$TOKEN" \
  "$APP_BASE/subscriber/imsi?msisdn=msisdn-2" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "IMSI lookup msisdn-2 returns 200" "200" "$http_code"
count=$(echo "$body" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
assert_eq "msisdn-2 resolves to 2 IMSIs" "2" "$count"

response=$(curl_k -w '\n%{http_code}' \
  -b "token=$TOKEN" \
  "$APP_BASE/subscriber/imsi?msisdn=msisdn-3" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "IMSI lookup msisdn-3 returns 200" "200" "$http_code"
count=$(echo "$body" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
assert_eq "msisdn-3 resolves to 3 IMSIs" "3" "$count"

response=$(curl_k -w '\n%{http_code}' \
  -b "token=$TOKEN" \
  "$APP_BASE/subscriber/imsi?msisdn=msisdn-2&msisdn=msisdn-3" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "IMSI lookup msisdn-2+msisdn-3 returns 200" "200" "$http_code"
count=$(echo "$body" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
assert_eq "msisdn-2+msisdn-3 resolves to 5 IMSIs" "5" "$count"

# ─── Scenario 7: Subscriber profile (testProfiler scenario) ──────────────────

log "Scenario 7: Subscriber profile"

response=$(curl_k -w '\n%{http_code}' \
  -b "token=$TOKEN" \
  "$APP_BASE/subscriber/profile?imsi=1" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "Profile imsi=1 returns 200" "200" "$http_code"
count=$(echo "$body" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
assert_eq "Profile imsi=1 returns 1 record" "1" "$count"

response=$(curl_k -w '\n%{http_code}' \
  -b "token=$TOKEN" \
  "$APP_BASE/subscriber/profile?imsi=1&imsi=2" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "Profile imsi=1+2 returns 200" "200" "$http_code"
count=$(echo "$body" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
assert_eq "Profile imsi=1+2 returns 2 records" "2" "$count"

# ─── Scenario 8: Schema generation (testSchemaGenerator scenario) ────────────

log "Scenario 8: Profile schema generation"

response=$(curl_k -w '\n%{http_code}' \
  -b "token=$TOKEN" \
  "$APP_BASE/subscriber/profile/def" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "Profile schema returns 200" "200" "$http_code"
assert_contains "Schema has 'imsi' field" "$body" "imsi"
assert_contains "Schema has 'type' field" "$body" "type"

# ─── Scenario 9: Historical data - daily (testSearchDaily scenario) ──────────

log "Scenario 9: Historical data search (daily)"

response=$(curl_k -w '\n%{http_code}' \
  -b "token=$TOKEN" \
  "$APP_BASE/subscriber/histdata?imsi=234304100455762&aggr=d&timefrom=20110731000000&timeto=20110732000000" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "Daily histdata returns 200" "200" "$http_code"
count=$(echo "$body" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
assert_eq "Daily histdata returns 1 record" "1" "$count"

# ─── Scenario 10: Historical data - hourly (testSearch scenario) ─────────────

log "Scenario 10: Historical data search (hourly)"

response=$(curl_k -w '\n%{http_code}' \
  -b "token=$TOKEN" \
  "$APP_BASE/subscriber/histdata?imsi=1&aggr=h&timefrom=20160809010000&timeto=20160809030000" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "Hourly histdata returns 200" "200" "$http_code"
count=$(echo "$body" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
if [ "$count" -ge 1 ]; then
  pass "Hourly histdata returns data ($count records)"
else
  fail "Hourly histdata expected >=1 records, got $count"
fi

# ─── Scenario 11: Historical schema (testHistSchemaGenerator scenario) ──────

log "Scenario 11: Historical schema generation"

response=$(curl_k -w '\n%{http_code}' \
  -b "token=$TOKEN" \
  "$APP_BASE/subscriber/histdata/def" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "Hist schema returns 200" "200" "$http_code"

# ─── Scenario 12: 404 handling (test404 scenario) ───────────────────────────

log "Scenario 12: 404 for non-existent endpoint"

response=$(curl_k -w '\n%{http_code}' \
  "$APP_BASE/nonexistent" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
body=$(echo "$response" | sed '$d')
assert_eq "Non-existent endpoint returns 404" "404" "$http_code"

# ─── Scenario 13: Missing params → 400 (testHistoricalData scenario) ────────

log "Scenario 13: Missing required parameters"

response=$(curl_k -w '\n%{http_code}' \
  -b "token=$TOKEN" \
  "$APP_BASE/subscriber/histdata" 2>/dev/null)
http_code=$(echo "$response" | tail -1)
assert_eq "histdata without params returns 400" "400" "$http_code"

# ─── Scenario 14: Data in Cassandra (direct CQL verification) ───────────────

log "Scenario 14: Data integrity via CQL"

result=$(docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e \
  "SELECT count(*) FROM ress.hrcc_msisdn_imsi" 2>&1) || true
assert_contains "hrcc_msisdn_imsi has data" "$result" "14"

result=$(docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e \
  "SELECT count(*) FROM ress.hrcc_subscriber" 2>&1) || true
assert_contains "hrcc_subscriber has data" "$result" "7"

result=$(docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e \
  "SELECT count(*) FROM ress.hrcc_historical_h_1" 2>&1) || true
assert_contains "hrcc_historical_h_1 has data" "$result" "9"

result=$(docker-compose -f "$COMPOSE_FILE" exec -T cassandra cqlsh -e \
  "SELECT count(*) FROM ress.hrcc_historical_h_1i" 2>&1) || true
assert_contains "hrcc_historical_h_1i has data" "$result" "9"

# ─── Scenario 15: Docker logs check ──────────────────────────────────────────

log "Scenario 15: Container health"

for svc in cassandra webserver; do
  status=$(docker-compose -f "$COMPOSE_FILE" ps --format json "$svc" 2>/dev/null \
    | python3 -c "import sys,json; print(json.load(sys.stdin).get('State',''))" 2>/dev/null || echo "unknown")
  if [ "$status" = "running" ]; then
    pass "Container '$svc' is running"
  else
    fail "Container '$svc' is '$status' (expected running)"
  fi
done

# ─── Summary ──────────────────────────────────────────────────────────────────

log "Test Results: $TOTAL total, $PASS passed, $FAIL failed"

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi

exit 0
