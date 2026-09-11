# U1 — 실제 콘텐츠 등록·조회와 첫 HTTP 연결

vn-play-analytics / 실제 Unity 연동 학습 PLAN v2  
작성일: 2026-09-11  
상태: 계획 / 미착수

> 통합 문서 `PLAN_vn-play-analytics_Unity_Learning_v2.md`의 13절을 분리한 파일이다. 해당 단계 본문은 유지하고 독립적인 진행에 필요한 전제·공통 원칙을 덧붙였다. 통합 PLAN은 변경하지 않았다.

## 이 단계의 위치

완료 결과: 실제 콘텐츠 카탈로그를 등록하고 Unity에서 그 정보를 HTTP로 조회한다.

**시작 전제:** U0에서 qwer_scene의 장면 보고와 로컬 저장 경계를 확인했다. 현재 서버의 콘텐츠 등록·오류 처리 코드를 출발점으로 사용한다.

이전: `PLAN_U0_Runtime_Boundaries.md` / 다음: `PLAN_U2_Playthrough_Connection.md`

## 분석 기준

- Unity: `ked-presentation-runtime/server_DB_Test`, 커밋 `5ea9d8f9c5f80044a36c6f19ab2f424a1f8632eb`.
- 서버: `vn-play-analytics/dev`, 커밋 `8da0d202fae6fa59a3825ceb77902f34e2285b23`.
- 코드 분석에 근거한 계획이다. 실행·DB·Unity 검증 완료를 의미하지 않는다. 구현 시작 시 미푸시 변경과 Inspector 설정을 확인한다.

## 목표

기존 콘텐츠 기능을 실제 qwer_scene 데이터와 맞추고, Unity에서 서버 조회 응답을 확인한다.

## 서버 변경

- [ ] Episode의 `options`는 null을 거부하되 빈 리스트와 한 개를 허용한다.
- [ ] ChoiceOption에 `auto`를 추가한다. 요청에서는 누락과 false를 구분할 필요가 있는지 설명하고, 초기 계약은 필수 Boolean으로 받는다.
- [ ] 자동 간선의 빈 label은 허용하고 사용자 선택 간선의 빈 label은 거부한다.
- [ ] 기존 PK·FK·UNIQUE와 요청 내부 중복 검증은 유지한다. 중첩 리스트의 null 원소도 400으로 처리하도록 검증한다.
- [ ] `GET /chapters/{chapterId}`와 `GET /chapters?chapterKey=qwer_scene`을 구현한다. 키 조회는 0개 또는 1개인 요약 목록으로 반환한다.
- [ ] Episode의 현재 getter 부족은 실제 응답 조립에 필요한 범위만 보충한다.
- [ ] 등록 요청·응답 DTO와 실제 progression DTO의 대응을 정리한다.

실제 콘텐츠의 모든 노드를 등록한다. 선택지 번호는 `NextOptions` 원본 배열의 0 기반 인덱스다. 서버 콘텐츠는 분석용 카탈로그이며 Yarn 대사·스탯 계산·그래프 실행을 대체하지 않는다. 전체 저작 도구 importer는 이 단계의 선행 조건이 아니다. 표본 하나는 원본을 대조하며 명시적인 POST JSON으로 등록한다.

## Unity 변경

AnalyticsApi에 챕터 조회 하나만 추가한다. 학습 패널에서 현재 로드한 ChapterId로 조회하여 서버 chapterId·title을 표시한다. 재생할 콘텐츠 자체는 계속 로컬 TextAsset을 사용한다.

## 검증 순서

1. Postman에서 실제 표본을 POST하고 201·Location·본문을 확인한다.
2. GET 상세와 MySQL의 chapters/episodes/choice_options를 비교한다.
3. Unity에서 동일 키를 조회한다. 잘못된 주소의 통신 실패와 400·404의 HTTP 응답을 구분한다.
4. 0개·1개·2개 옵션, auto의 빈 label, 중복 키·순번을 확인한다.

**완료 기준:** qwer_scene의 다섯 Episode와 다섯 간선이 서버와 원본에서 대응한다. Unity가 받은 숫자 chapterId와 콘텐츠 문자열 key를 구분하여 설명한다.

**학습 질문:** `Integer`와 `int`, null과 빈 List, HashSet.add의 반환값, record 접근자, `@Valid`의 중첩 검증, URI·Location·ResponseEntity의 역할은 무엇인가?

## 공통 진행 원칙

- 실제 게임에서 바뀔 동작을 설명하고, 필요한 Java 개념 1~2개를 확인한 뒤 작은 코드 단위로 진행한다.
- Postman 요청 → DB 행·실행 SQL → 실제 Unity 조작 순서로 확인한다. 단계의 검증과 설명을 마친 뒤 다음 단계로 간다.
- DTO는 record, Entity는 일반 클래스, 오류 응답은 `{errorCode, message}`를 사용한다. 명시적 생성자 주입과 Service 트랜잭션을 유지한다.
- `chapterKey`, `episodeKey`는 문자열 콘텐츠 키이고 `chapterId`, `episodeId`, `playthroughId`는 서버 숫자 PK다. `clientPlaythroughId`는 Unity 로컬 GUID다.
- 학습 저장 루트는 기존 저장과 분리한다. 초기에는 한 기기·한 실행 인스턴스·고정 챕터를 사용한다.
- 기존 로컬 저장·현재 장면 롤백은 사용한다. 과거 회차 포크·수동 슬롯 로드의 서버 연동, 인증, 콘텐츠 버전, 자동 재전송은 보류한다.
- 기존 완전 동기화 스택의 서버 ID·revision·ACK를 학습 서버 상태로 재사용하지 않는다.
- 이 파일은 계획이며 구현 완료를 뜻하지 않는다. 체크박스는 실제 검증 후 갱신한다.


## 콘텐츠 표본 참고

`qwer_scene.progression.json`은 다음 구조다.

| Episode | Scene | 진행 |
| --- | --- | --- |
| EP01 | 사무실 | EP02_01 또는 EP02_02 선택 |
| EP02_01 | 사무실 | 선택지 하나로 EP03 이동 |
| EP02_02 | 사무실 | Via를 거쳐 EP03 이동 |
| EP03 | 복도 | Auto로 EP04 이동 |
| EP04 | 복도 | 선택지 없음, 챕터 종료 |

성실 루트라면 첫 장면 보고에는 `EP01[0]`, `EP02_01[0]` 두 간선이 들어가고 재개 Episode는 `EP03`이다. 복도 종료 보고에는 자동 간선 `EP03[0]`이 들어가며 `ChapterCompleted=true`다.

이 샘플의 EventKey는 모두 비어 있다. 따라서 `WatchedEpisodeIds`가 비어 있는 것은 정상이다. EP04처럼 진행 선택이 없는 노드는 선택 기록만으로 방문 여부를 복원할 수 없다.

## 단계 종료 기록

- 작업 브랜치·커밋:
- 변경한 서버 코드:
- 변경한 Unity 코드:
- Postman·DB 확인 결과:
- 실제 플레이 확인 결과:
- 실행한 자동 테스트와 결과:
- 직접 설명할 수 있게 된 개념:
- 남은 문제와 다음 작업:
