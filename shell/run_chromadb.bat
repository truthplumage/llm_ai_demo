@echo off
setlocal

rem Chroma DB 컨테이너 실행 (Windows CMD)
rem 환경변수: CONTAINER_NAME, IMAGE, PORT, DATA_DIR

if not defined CONTAINER_NAME set "CONTAINER_NAME=chromadb"
if not defined IMAGE set "IMAGE=ghcr.io/chroma-core/chroma:latest"
if not defined PORT set "PORT=8000"
if not defined DATA_DIR set "DATA_DIR=%CD%\chroma-data"

if not exist "%DATA_DIR%" mkdir "%DATA_DIR%"

docker run -d ^
  --name "%CONTAINER_NAME%" ^
  -p "%PORT%":8000 ^
  -v "%DATA_DIR%:/chroma" ^
  "%IMAGE%"

echo.
echo Container: %CONTAINER_NAME%
echo Image: %IMAGE%
echo Port: %PORT% -^> 8000
echo Data dir: %DATA_DIR% -^> /chroma
echo.
echo 상태 확인: curl http://localhost:%PORT%/api/v1/collections

endlocal
