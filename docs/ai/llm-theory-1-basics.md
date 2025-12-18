# [이론1] LLM의 기본 개념

## 핵심 정의
- **LLM(Large Language Model)**: 대규모 텍스트 코퍼스를 자가지도 학습으로 사전학습(Pretraining)한 트랜스포머 계열 모델. 입력 토큰 시퀀스를 맥락으로 다음 토큰 확률 분포를 예측해 생성·요약·분류 등 언어 작업을 수행.
- **토큰**: 서브워드 단위의 최소 표현. 모델은 토큰 단위로 입력 길이(컨텍스트 윈도우)와 비용이 결정됨.
- **파라미터**: 모델이 학습한 가중치 수. 수십억~수천억 단위 파라미터가 일반적이며 파라미터 수가 모델 용량·메모리·추론 비용을 좌우.
- **컨텍스트 윈도우**: 한 번의 요청에서 모델이 참조할 수 있는 최대 토큰 길이. 윈도우 밖 정보는 모델이 기억하지 못하므로 긴 문서는 요약·슬라이딩 윈도우·RAG로 분할 제공.

## 학습·추론 흐름
- **사전학습(Pretraining)**: 대규모 범용 텍스트로 다음 토큰 예측(언어 모델링) 목표로 학습.
- **미세조정(Finetuning/Instruction tuning)**: 특정 태스크나 지침 따르기 위해 추가 데이터로 학습. RLHF(RL from Human Feedback)로 사용자 선호 반영.
- **추론(Inference)**: temperature/top_p 등 샘플링 하이퍼파라미터로 생성 다양성 제어. max_tokens로 응답 길이 제한.
- **제한점**: 최신성 부족(학습 시점 이후 지식 부재), 환각(hallucination), 훈련 데이터 바이어스.

## 대표 LLM API 서비스
- **OpenAI (GPT-4o, GPT-4 Turbo, GPT-3.5 Turbo)**: 높은 품질, 함수 호출/툴 사용, 비전·오디오 지원. Chat Completions/Assistants API 제공.
- **Azure OpenAI**: OpenAI 모델을 Azure 인프라로 제공. 가상 네트워크/프라이빗 엔드포인트/리전 선택 가능.
- **Google (Gemini)**: 멀티모달 강점, 토큰 길이 다양. JSON 모드 제공.
- **AWS Bedrock**: 다양한 파트너 모델(Claude, Mistral 등)을 통합 제공.
- **Anthropic (Claude)**: 긴 컨텍스트와 안전성 제어에 강점.
- 선택 기준: 모델 성능/가격, 컨텍스트 길이, 지연 시간, 멀티모달 필요 여부, 지역 규제.

## OpenAI Chat Completions 요청/응답 구조
- **요청 주요 필드**
  - `model`: 사용 모델 ID (예: `gpt-4o-mini`).
  - `messages`: 역할(`system`/`user`/`assistant`/`tool`)과 콘텐츠 배열.
  - 샘플링: `temperature`, `top_p`, `max_tokens`, `presence_penalty`, `frequency_penalty`.
  - **스트리밍**: `stream: true` 시 delta 단위 청크 전달.
  - **함수 호출/툴 사용**: `tools`, `tool_choice` 설정으로 구조화 호출.
- **응답 주요 필드**
  - `choices[].message.role/content`: 생성된 메시지.
  - `choices[].finish_reason`: `stop`, `length`, `content_filter`, `tool_calls` 등 종료 사유.
  - `usage`: `prompt_tokens`, `completion_tokens`, `total_tokens` 비용 집계.

### 예시: 단일 호출
```http
POST https://api.openai.com/v1/chat/completions
Authorization: Bearer $OPENAI_API_KEY
Content-Type: application/json

{
  "model": "gpt-4o-mini",
  "messages": [
    {"role": "system", "content": "너는 간결한 쇼핑 도우미야."},
    {"role": "user", "content": "무선 이어폰 추천해줘"}
  ],
  "temperature": 0.7,
  "max_tokens": 200
}
```

### 예시: 스트리밍
```json
{
  "stream": true,
  "model": "gpt-4o-mini",
  "messages": [{"role": "user", "content": "인기 전자책 리더기 알려줘"}]
}
```
SSE(EventSource) 혹은 WebSocket으로 `data: {"choices":[{"delta":{"content":"..."}}]}` 형식 청크를 수신.

## 활용 패턴과 모범 사례
- **시스템 메시지로 역할·톤·제약 명시**: 금지사항, 응답 포맷(JSON) 요구, 근거 없는 추측 금지.
- **구조화 출력 요구**: JSON Schema, 함수 호출을 활용해 파싱 안정성 확보.
- **컨텍스트 관리**: 최근 대화만 남기거나 요약/슬라이딩 윈도우 적용. 긴 문서는 RAG로 부분 제공.
- **온도 조정**: 창의성↑ → `temperature`↑, 사실성/일관성↑ → 낮춤.
- **비용/지연 최적화**: 짧은 모델로 1차 필터 후 고성능 모델 재랭킹; `max_tokens` 제한; 스트리밍으로 초기 응답 지연 감소.
- **보안/프라이버시**: 민감 데이터 마스킹, 로깅 시 토큰/키 노출 방지, 정책 위반 필터링.
