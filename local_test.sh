#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
export JWT_SECRET="${JWT_SECRET:-dev-local-secret-change-me-please-and-keep-32-bytes-min}"
export DB_PASSWORD="${DB_PASSWORD:-respond!qu1cklY}"
export SERVER_PORT="${SERVER_PORT:-0}"
LOCAL_TEST_WEB="${LOCAL_TEST_WEB:-0}"

printf 'Starting Spring Boot (profile=%s)\n' "$SPRING_PROFILES_ACTIVE"
printf 'Using database user=mrr_user (override via application-local.properties)\n'
printf 'JWT secret length: %d characters\n' "${#JWT_SECRET}"
printf 'Binding server port: %s (set SERVER_PORT to override)\n' "$SERVER_PORT"
printf 'Web server enabled: %s (set LOCAL_TEST_WEB=1 to enable Tomcat)\n' "$LOCAL_TEST_WEB"

if [[ "$LOCAL_TEST_WEB" == "1" ]]; then
  unset SPRING_MAIN_WEB_APPLICATION_TYPE
else
  export SPRING_MAIN_WEB_APPLICATION_TYPE="none"
fi

exec ./mvnw spring-boot:run \
  "-Dspring-boot.run.profiles=${SPRING_PROFILES_ACTIVE}" \
  "$@"
