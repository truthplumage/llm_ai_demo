@echo off
setlocal

rem 기존 Postgres 컨테이너에 pgvector 확장을 설치/생성 (Windows CMD)
rem 환경변수: CONTAINER_NAME, POSTGRES_USER, POSTGRES_DB, PG_MAJOR, INSTALL_PGVECTOR(=1 시 apt 설치 시도)

if not defined CONTAINER_NAME set "CONTAINER_NAME=pgvector"
if not defined POSTGRES_USER set "POSTGRES_USER=postgres"
if not defined POSTGRES_DB set "POSTGRES_DB=postgres"
if not defined PG_MAJOR set "PG_MAJOR=16"
if not defined INSTALL_PGVECTOR set "INSTALL_PGVECTOR=0"

if "%INSTALL_PGVECTOR%"=="1" (
  echo pgvector 패키지 설치 시도 중...
  docker exec -it "%CONTAINER_NAME%" bash -lc "if command -v apt-get >/dev/null 2>&1; then apt-get update && apt-get install -y postgresql-%PG_MAJOR%-pgvector; else echo 'apt-get을 찾을 수 없습니다. Debian/Ubuntu 기반이 아닌 이미지일 수 있습니다.' >&2; exit 1; fi"
)

docker exec -it "%CONTAINER_NAME%" ^
  psql -U "%POSTGRES_USER%" -d "%POSTGRES_DB%" ^
  -c "CREATE EXTENSION IF NOT EXISTS vector;"

echo.
echo 컨테이너 %CONTAINER_NAME% 에서 pgvector 활성화 완료

endlocal
