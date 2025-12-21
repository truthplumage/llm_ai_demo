@echo off
setlocal

rem pgvector 컨테이너 내부 psql 접속 (Windows CMD)
rem 환경변수: CONTAINER_NAME, POSTGRES_USER, POSTGRES_DB

if not defined CONTAINER_NAME set "CONTAINER_NAME=pgvector"
if not defined POSTGRES_USER set "POSTGRES_USER=postgres"
if not defined POSTGRES_DB set "POSTGRES_DB=postgres"

docker exec -it "%CONTAINER_NAME%" psql -U "%POSTGRES_USER%" -d "%POSTGRES_DB%"

endlocal
