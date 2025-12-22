@echo off
setlocal

rem pgvector 컨테이너 내부 psql 접속 (Windows CMD)
rem 사용법: 컨테이너가 실행 중이어야 하며, 환경변수로 접속 정보 조정 가능
rem 예) set POSTGRES_USER=demo && set POSTGRES_DB=demo && call psql_pgvector.bat
rem 환경변수: CONTAINER_NAME, POSTGRES_USER, POSTGRES_DB (기본값 pgvector / postgres / postgres)
rem 필요 도구: Docker Desktop, 컨테이너에 psql 포함

if not defined CONTAINER_NAME set "CONTAINER_NAME=pgvector"
if not defined POSTGRES_USER set "POSTGRES_USER=postgres"
if not defined POSTGRES_DB set "POSTGRES_DB=postgres"

docker exec -it "%CONTAINER_NAME%" psql -U "%POSTGRES_USER%" -d "%POSTGRES_DB%"

endlocal
