#!/usr/bin/env bash
# Chroma DB 컨테이너 실행 스크립트
# 기본값: 포트 8000, 데이터 ./chroma-data
# 환경변수: CONTAINER_NAME, IMAGE, PORT, DATA_DIR

set -euo pipefail

CONTAINER_NAME=${CONTAINER_NAME:-chromadb}
IMAGE=${IMAGE:-ghcr.io/chroma-core/chroma:latest}
PORT=${PORT:-8000}
DATA_DIR=${DATA_DIR:-$PWD/chroma-data}

if docker ps -a --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
  echo "컨테이너 ${CONTAINER_NAME} 이미 존재. 먼저 제거: docker rm -f ${CONTAINER_NAME}" >&2
  exit 1
fi

mkdir -p "${DATA_DIR}"

docker run -d \
  --name "${CONTAINER_NAME}" \
  -p "${PORT}:8000" \
  -v "${DATA_DIR}:/chroma" \
  "${IMAGE}"

printf "\nContainer: %s\nImage: %s\nPort: %s -> 8000\nData dir: %s -> /chroma\n" \
  "${CONTAINER_NAME}" "${IMAGE}" "${PORT}" "${DATA_DIR}"
printf "\n상태 확인: curl http://localhost:%s/api/v1/collections\n" "${PORT}"
