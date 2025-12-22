@echo off
setlocal

rem Chroma DB 컨테이너 실행 (Windows CMD)
rem 사용법: 환경변수로 포트/데이터 경로 조정 후 실행
rem 예) set PORT=8001 && set DATA_DIR=%CD%\chroma-data-dev && call run_chromadb.bat
rem 환경변수: CONTAINER_NAME, IMAGE, PORT, DATA_DIR (기본 chromadb, ghcr.io/chroma-core/chroma:latest, 8000, %CD%\chroma-data)
rem 필요 도구: Docker Desktop 실행 상태

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
