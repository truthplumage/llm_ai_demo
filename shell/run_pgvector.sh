#!/usr/bin/env bash
# pgvector 컨테이너 실행 스크립트
# 기본값: 포트 5432, 유저/비번/DB 모두 postgres
# 환경변수로 조정: CONTAINER_NAME, IMAGE, PORT, POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_DB

set -euo pipefail

CONTAINER_NAME=${CONTAINER_NAME:-pgvector}
IMAGE=${IMAGE:-ankane/pgvector:pg16}
PORT=${PORT:-5432}

POSTGRES_USER=${POSTGRES_USER:-postgres}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-postgres}
POSTGRES_DB=${POSTGRES_DB:-postgres}

docker run -d \
  --name "${CONTAINER_NAME}" \
  -p "${PORT}:5432" \
  -e POSTGRES_USER="${POSTGRES_USER}" \
  -e POSTGRES_PASSWORD="${POSTGRES_PASSWORD}" \
  -e POSTGRES_DB="${POSTGRES_DB}" \
  "${IMAGE}"

printf "\nContainer: %s\nImage: %s\nPort: %s -> 5432\nUser: %s\nDB: %s\n" \
  "${CONTAINER_NAME}" "${IMAGE}" "${PORT}" "${POSTGRES_USER}" "${POSTGRES_DB}"

printf "\n접속 예시: psql -h localhost -p %s -U %s -d %s\n" \
  "${PORT}" "${POSTGRES_USER}" "${POSTGRES_DB}"

printf "확장 설치: docker exec -it %s psql -U %s -d %s -c 'CREATE EXTENSION IF NOT EXISTS vector;'\n" \
  "${CONTAINER_NAME}" "${POSTGRES_USER}" "${POSTGRES_DB}"
