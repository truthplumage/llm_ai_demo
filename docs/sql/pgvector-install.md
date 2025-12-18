# pgvector 설치 가이드

## macOS (Homebrew, PostgreSQL 16 기준)
- 설치: `brew install pgvector`
- 재시작: `brew services restart postgresql@16` (직접 실행 중이면 서버 재기동)
- DB 접속 후: `CREATE EXTENSION IF NOT EXISTS vector;`

## Docker 사용 시
- pgvector 포함 이미지 사용: `docker pull pgvector/pgvector:pg16` 등
  - 컨테이너 실행 예: `docker run --name pgvector -e POSTGRES_PASSWORD=pass -p 5432:5432 pgvector/pgvector:pg16`
  - 컨테이너 내부에서 `CREATE EXTENSION vector;`
- 기존 이미지에 설치: Debian/Ubuntu 베이스라면 `apt-get update && apt-get install -y postgresql-16-pgvector` 후 재시작

## Debian/Ubuntu (패키지 설치)
- `sudo apt-get update`
- `sudo apt-get install -y postgresql-16-pgvector` (버전에 맞게 15/16 등 선택)
- Postgres 재시작 후 DB에서 `CREATE EXTENSION vector;`

## RHEL/CentOS/Amazon Linux
- `sudo yum install postgresql16-pgvector` (또는 repo 추가 후 설치)
- Postgres 재시작 후 `CREATE EXTENSION vector;`

## 클라우드 RDS/Aurora
- 해당 서비스가 pgvector를 지원하는지 확인 (예: RDS for PostgreSQL 16.x 지원 여부)
- 콘솔/파라미터 그룹에서 pgvector 활성화 또는 확장 설치 후, DB에서 `CREATE EXTENSION vector;`

## 설치 확인
- `SELECT extname FROM pg_extension WHERE extname = 'vector';`
- 없으면 확장 미설치 상태. 설치 후 다시 `CREATE EXTENSION vector;`
