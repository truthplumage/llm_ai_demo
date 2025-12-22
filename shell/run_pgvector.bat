@echo off
setlocal

rem pgvector 컨테이너 실행 (Windows CMD)
rem 사용법: 환경변수를 설정한 뒤 run_pgvector.bat 실행
rem 예) set PORT=55432 && set POSTGRES_PASSWORD=pass && call run_pgvector.bat
rem 기본값: CONTAINER_NAME=pgvector, IMAGE=ankane/pgvector:latest, PORT=5432, USER/PW/DB=postgres
rem 필요 도구: Docker Desktop 실행 상태

if not defined CONTAINER_NAME set "CONTAINER_NAME=pgvector"
if not defined IMAGE set "IMAGE=ankane/pgvector:latest"
if not defined PORT set "PORT=5432"
if not defined POSTGRES_USER set "POSTGRES_USER=postgres"
if not defined POSTGRES_PASSWORD set "POSTGRES_PASSWORD=postgres"
if not defined POSTGRES_DB set "POSTGRES_DB=postgres"

docker run -d ^
  --name "%CONTAINER_NAME%" ^
  -p "%PORT%":5432 ^
  -e POSTGRES_USER="%POSTGRES_USER%" ^
  -e POSTGRES_PASSWORD="%POSTGRES_PASSWORD%" ^
  -e POSTGRES_DB="%POSTGRES_DB%" ^
  "%IMAGE%"

echo.
echo Container: %CONTAINER_NAME%
echo Image: %IMAGE%
echo Port: %PORT% -^> 5432
echo User: %POSTGRES_USER%
echo DB: %POSTGRES_DB%
echo.
echo 접속 예시: psql -h localhost -p %PORT% -U %POSTGRES_USER% -d %POSTGRES_DB%
echo 확장 설치: docker exec -it %CONTAINER_NAME% psql -U %POSTGRES_USER% -d %POSTGRES_DB% -c "CREATE EXTENSION IF NOT EXISTS vector;"

endlocal
