#!/usr/bin/env bash
set -euo pipefail

create_database_if_missing() {
  local database_name="$1"
  if ! psql --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -tAc "SELECT 1 FROM pg_database WHERE datname='${database_name}'" | grep -q 1; then
    createdb --username "$POSTGRES_USER" --owner "$POSTGRES_USER" "$database_name"
  fi
}

create_database_if_missing "${AUTH_DB_NAME:-fmud_auth}"
create_database_if_missing "${STUDENT_DB_NAME:-fmud_students}"
