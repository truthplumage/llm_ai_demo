#!/usr/bin/env bash

set -euo pipefail

CONTAINER_NAME=${CONTAINER_NAME:-pgvector}
POSTGRES_USER=${POSTGRES_USER:-postgres}
POSTGRES_DB=${POSTGRES_DB:-postgres}

docker exec -it "${CONTAINER_NAME}" psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}"
