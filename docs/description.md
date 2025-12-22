## DDD 전술적 설계(Tactical Design)

도메인 모델을 코드로 옮기는 실천 지침. Bounded Context 내부에서 일관된 유비쿼터스 언어를 엔티티, 값 객체, Aggregate로 표현한다.

핵심 요소:

* Entity: 식별자로 동일성을 추적하는 가변 객체.
* Value Object: 값으로 동등성을 판단하는 불변 객체.
* Aggregate/Root: 불변 조건을 묶어 트랜잭션 경계를 정의하고 외부 접근을 Root로 한정.
* Domain Event: 상태 변화를 명시적으로 표현해 다른 모델로 전달.
* Repository/Factory: 생성·수명 관리를 캡슐화해 도메인 로직과 인프라를 분리.

---

## Bounded Context와 Subdomain

* Subdomain: 비즈니스 문제를 업무 관점으로 나눈 영역(Core/Supporting/Generic 등).
* Bounded Context: 모델과 언어가 일관되게 유지되는 구현 경계(코드·스키마·API 계약 단위).

관계: 하나의 Context가 여러 Subdomain 일부를 포괄할 수 있고, 하나의 Subdomain이 여러 Context에 걸칠 수 있다. Subdomain 우선순위와 팀/배포 단위를 함께 고려해 Context를 자른다.

---

## Aggregate 설계 원칙

* Aggregate는 한 번의 트랜잭션으로 일관성을 보장해야 하는 최소 단위다.
* 모든 외부 접근은 Aggregate Root를 통해서만 이루어진다.
* 즉시 일관성이 필요한 것만 포함해 크기를 작게 유지하고, 다른 Aggregate는 ID로 참조한다.
* 불변 조건을 Root가 검증하며 이벤트로 외부와 느슨하게 연동한다.

예시: 주문 Aggregate에서는 `Order`가 Root, `OrderLine`이 내부 엔티티, `ShippingAddress`가 값 객체이며 합계·상태 전이가 불변 조건을 이룬다.

---

## 모놀리식 아키텍처(Monolithic)

단일 코드베이스와 배포 단위로 모든 기능을 묶는 구조. 단순한 배포/디버깅이 장점이나, 규모가 커지면 변경 영향 범위가 커지고 부분 확장/부분 배포가 어렵다. 다양한 언어를 섞는 Polyglot 설계와는 거리가 있다.

---

## 마이크로서비스 아키텍처(MSA) 장점

서비스를 독립 배포 단위로 쪼개 팀별 소유권을 부여한다. 서비스별 기술 스택 선택, 부분 확장, 빠른 롤아웃/롤백, 장애 격리가 가능하지만 운영 복잡성과 관측·추적 부담이 늘어난다.

---

## MSA 환경의 데이터베이스 설계

원칙: 서비스별 DB 소유로 스키마 변경 파급을 차단하고, 서비스 간 일관성은 이벤트/Saga로 맞춘다.

권장:

* 서비스마다 별도 DB 사용.
* 이벤트 기반 비동기 동기화, 필요 시 데이터 중복 허용(CQRS/Projection).

지양:

* 여러 서비스가 동일 스키마를 직접 공유하거나 하나의 트랜잭션으로 묶는 구조.

---

## API Gateway 역할

클라이언트와 내부 서비스 사이 단일 진입점. 라우팅, 인증/인가, 레이트 리미팅, 로깅, 프로토콜 변환, 응답 집계로 클라이언트 호출 수를 줄인다. 분산 트랜잭션 관리나 핵심 도메인 로직은 맡지 않는다.

---

## Saga 패턴

여러 서비스에 걸친 트랜잭션을 로컬 트랜잭션 시퀀스로 분해하고, 실패 시 보상 트랜잭션으로 되돌리는 분산 트랜잭션 패턴. Choreography(이벤트 기반)와 Orchestration(오케스트레이터 중심) 방식이 있으며 최종 일관성을 지향한다.

---

## Gateway 패턴 (단일 진입점)

클라이언트가 각 서비스와 직접 통신하지 않고 단일 진입점을 통해 요청을 위임하는 패턴. 엔드포인트 집약, 보안/정책 일원화, BFF 구성에 활용된다.

---

## Spring Batch – ExecutionContext

배치 실행 중 상태를 저장하는 Key-Value 저장소. JobExecution과 StepExecution 각각에 존재하며 직렬화되어 JobRepository에 저장된다. 재시작 시 이전 값을 복원할 수 있으며, Step 실패 시에도 자동 삭제되지 않는다.

---

## Spring Batch – Chunk-Oriented Processing

읽기→가공→쓰기 흐름을 Chunk 단위 트랜잭션으로 처리한다.

* Reader: 아이템을 하나씩 읽어 Chunk 크기만큼 모음.
* Processor: Chunk가 아니라 **개별 아이템 단위**로 호출.
* Writer: 모인 Chunk를 한 번에 기록.

Chunk 크기는 Writer 호출 빈도와 트랜잭션 범위에 영향을 주지만 Reader/Processor 호출 단위는 변하지 않는다.

---

## Spring Batch – JobInstance / JobExecution / StepExecution

* JobInstance: Job 이름 + JobParameters 조합을 대표하는 실행 단위.
* JobExecution: 하나의 JobInstance에 대한 실행 시도(여러 번 가능).
* StepExecution: JobExecution 내 각 Step의 실행 시도.

동일 JobParameters로 실행하면 같은 JobInstance로 간주하며, JobInstance는 여러 JobExecution을, JobExecution은 여러 StepExecution을 가진다.

---

## Elasticsearch 역색인(Inverted Index)

토큰(단어) → 해당 단어를 포함한 문서 목록을 매핑하는 구조. 특정 단어가 포함된 문서를 빠르게 찾기 위해 사용되며, 검색 엔진의 핵심 데이터 구조다.

---

## Kubernetes – Pod

컨테이너를 담는 가장 작은 배포 단위. 하나 이상의 컨테이너가 네트워크 네임스페이스와 볼륨을 공유하며 함께 스케줄된다. IP가 변동될 수 있어 일반적으로 Service를 통해 접근한다.

---

## Kubernetes – Deployment

Pod의 선언적 배포/업데이트를 관리하는 리소스. Replica 수를 유지하며 롤링 업데이트·자동 복구를 수행한다. 네트워크 로드밸런싱은 Service가 담당한다.

---

## LLM Hallucination

모델이 훈련 데이터에 없는 정보나 사실과 다른 내용을 그럴듯하게 생성하는 현상. 확률적 토큰 예측, 데이터 편향/불완전성이 원인이며, 프롬프트/파인튜닝으로 완벽 제거는 어렵고 검색 결합(RAG), 검증, 제약 프롬프트로 완화한다.

---

## Kafka 메시지 소비 – @KafkaListener

Spring에서 Kafka 토픽을 구독하는 메서드에 사용하는 어노테이션. 컨슈머 그룹/토픽/파티션을 설정해 메시지를 비동기 소비한다.

---

## RAG(Retrieval-Augmented Generation) 목적

LLM 생성 전에 외부 지식을 검색·리랭킹해 생성 입력에 포함함으로써 최신/정확 정보를 보완하는 기법. 모델 재학습 없이도 도메인 지식을 반영하고 근거 기반 응답을 제공하는 것이 목적이다.

---
