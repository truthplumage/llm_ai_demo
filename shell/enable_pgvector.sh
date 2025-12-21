#!/usr/bin/env bash
# 기존 Postgres 컨테이너에 pgvector 확장을 설치/생성
# 기본값: 컨테이너(pgvector), 유저/DB postgres, 포트는 컨테이너 내부 5432
# 환경변수:
#   CONTAINER_NAME (기본 pgvector)
#   POSTGRES_USER (기본 postgres)
#   POSTGRES_DB (기본 postgres)
#   PG_MAJOR (apt 설치용 버전, 기본 16)
#   INSTALL_PGVECTOR=1 설정 시 apt-get으로 postgresql-<버전>-pgvector 설치 시도

set -euo pipefail

CONTAINER_NAME=${CONTAINER_NAME:-pgvector}
POSTGRES_USER=${POSTGRES_USER:-postgres}
POSTGRES_DB=${POSTGRES_DB:-postgres}
PG_MAJOR=${PG_MAJOR:-16}
INSTALL_PGVECTOR=${INSTALL_PGVECTOR:-0}

if [[ "${INSTALL_PGVECTOR}" == "1" ]]; then
  docker exec -it "${CONTAINER_NAME}" bash -lc "\
    if command -v apt-get >/dev/null 2>&1; then \
      apt-get update && apt-get install -y postgresql-${PG_MAJOR}-pgvector; \
    else \
      echo 'apt-get을 찾을 수 없습니다. Debian/Ubuntu 기반이 아닌 이미지일 수 있습니다.' >&2; \
      exit 1; \
    fi"
fi

docker exec -it "${CONTAINER_NAME}" \
  psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" \
  -c "CREATE EXTENSION IF NOT EXISTS vector;"

printf "\n컨테이너 %s 에서 pgvector 활성화 완료\n" "${CONTAINER_NAME}"
