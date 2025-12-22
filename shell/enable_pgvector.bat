@echo off
setlocal

rem 기존 Postgres 컨테이너에 pgvector 확장을 설치/생성 (Windows CMD)
rem 사용법: 컨테이너가 실행 중이어야 하며, 필요 시 INSTALL_PGVECTOR=1 로 패키지 설치
rem 예) set CONTAINER_NAME=pgvector && set INSTALL_PGVECTOR=1 && call enable_pgvector.bat
rem 환경변수: CONTAINER_NAME, POSTGRES_USER, POSTGRES_DB, PG_MAJOR, INSTALL_PGVECTOR(=1 시 apt 설치 시도)
rem 기본값: CONTAINER_NAME=pgvector, POSTGRES_USER=postgres, POSTGRES_DB=postgres, PG_MAJOR=16
rem 필요 도구: Docker Desktop 실행 상태, 컨테이너 내부 apt 사용 가능(옵션)

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
