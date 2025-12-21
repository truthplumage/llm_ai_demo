@echo off
setlocal

rem pgvector 컨테이너 실행 (Windows CMD)
rem 환경변수로 조정: CONTAINER_NAME, IMAGE, PORT, POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_DB

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
