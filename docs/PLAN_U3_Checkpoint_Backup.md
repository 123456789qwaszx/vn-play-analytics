# U3 — 장면 checkpoint 서버 백업

vn-play-analytics / 실제 Unity 연동 학습 PLAN v2  
작성일: 2026-09-11  
상태: 계획 / 미착수

> 통합 문서 `PLAN_vn-play-analytics_Unity_Learning_v2.md`의 15절을 분리한 파일이다. 해당 단계 본문은 유지하고 독립적인 진행에 필요한 전제·공통 원칙을 덧붙였다. 통합 PLAN은 변경하지 않았다.

## 이 단계의 위치

완료 결과: 실제 장면 snapshot을 서버에 저장하고 같은 내용으로 조회한다.

**시작 전제:** U2에서 실제 로컬 회차 GUID를 서버 회차 ID와 연결했다. 서버 등록 성공 후에 저장 요청을 보낼 수 있다.

이전: `PLAN_U2_Playthrough_Connection.md` / 다음: `PLAN_U4_Server_Resume.md`

## 분석 기준

- Unity: `ked-presentation-runtime/server_DB_Test`, 커밋 `5ea9d8f9c5f80044a36c6f19ab2f424a1f8632eb`.
- 서버: `vn-play-analytics/dev`, 커밋 `8da0d202fae6fa59a3825ceb77902f34e2285b23`.
- 코드 분석에 근거한 계획이다. 실행·DB·Unity 검증 완료를 의미하지 않는다. 구현 시작 시 미푸시 변경과 Inspector 설정을 확인한다.

## 목표

실제로 만들어진 LocalSaveFile snapshot을 서버에 쓰고 GET으로 돌려받는다. 이 단계는 명시적 백업으로 시작한다.

## 모델과 계약

`Checkpoint(id, playthrough, episodeKey, chapterCompleted, snapshotJson, savedAt)`를 추가한다. playthrough FK에는 UNIQUE를 두어 회차당 하나만 보관한다. 자식→부모 단방향 연관관계로 시작하며 부모 컬렉션·cascade는 필수가 아니다.

`snapshotJson`은 LocalSaveFile을 직렬화한 문자열이다. 첫 구현은 충분한 길이의 텍스트 열에 보관한다. MySQL JSON 타입·커스텀 Hibernate 매핑을 선행 학습으로 넣지 않는다. 문자열로 보관해도 외부 JSON 자체와 문자열 내부 JSON의 이중 인코딩을 구분해야 한다.

```http
PUT /playthroughs/{playthroughId}/checkpoint
GET /playthroughs/{playthroughId}/checkpoint
```

요청 필드:

| 필드 | 값·목적 |
| --- | --- |
| `episodeKey` | snapshot.CurrentEpisodeId. 목록에서 바로 읽을 재개 지점 |
| `chapterCompleted` | 다음 장면 재개 가능 여부 구분 |
| `snapshotJson` | 해당 회차의 확정 LocalSaveFile JSON 문자열 |

Unity는 snapshot 사본 하나에서 세 값을 함께 만든다. 서버는 snapshotJson이 JSON 객체인지, 내부 playthroughId·chapterId·currentEpisodeId·chapterCompleted가 회차와 바깥 메타데이터에 맞는지 최소 검증한다. 나머지 복원 데이터는 변형하지 않고 보관한다. 이때 JSON 파서의 실제 패키지·사용법은 현재 Boot가 관리하는 의존성에 맞춰 확인한다.

없는 회차는 404, 아직 checkpoint가 없는 회차의 GET은 204다. 정상 PUT은 200과 저장 결과를 반환한다. 첫 PUT과 갱신에 같은 성공 응답을 사용한다. U3에서는 학습 클라이언트 한 인스턴스가 한 번에 요청 하나만 보낸다.

## 서버 학습

기존 checkpoint를 조회하고 Entity의 `update(...)` 같은 의미 있는 메서드로 바꾼다. Service 트랜잭션 안의 영속 엔티티 수정과 SQL UPDATE가 연결되는 것을 확인한다. 넓은 setter나 외부에서 Entity를 직접 응답하는 방식은 사용하지 않는다.

## Unity 학습

“서버에 저장” 동작은 현재 대사 화면을 새로 캡처하지 않고, 이미 로컬 저장에 성공한 snapshot 사본을 전송한다. 장면 중간에 누르면 마지막 확정 지점을 보낸다. 성공 여부와 서버가 가진 episodeKey를 표시한다. 같은 payload를 명시적으로 다시 보내도 같은 checkpoint 행의 내용을 갱신하므로 행 수가 늘지 않는다.

## 완료 기준

- [ ] 첫 SceneEntered 뒤 초기 snapshot도 저장할 수 있다.
- [ ] 사무실을 나간 뒤 전송한 GET 결과는 EP03이다.
- [ ] 장면 중간 선택·롤백만으로 서버 값이 바뀌지 않는다.
- [ ] 실패한 요청은 성공으로 표시되지 않고 로컬 저장은 남는다.
- [ ] 마지막 장면 저장은 `chapterCompleted=true`다.
- [ ] 서로 다른 두 회차의 checkpoint가 섞이지 않는다.

`chapterCompleted`와 프로그램 종료를 구분한다. U3에서 앱을 닫았다는 이유로 회차를 완료 처리하지 않는다. `endedAt`이나 별도 종료 API는 필요가 생기기 전까지 추가하지 않는다.

**이 단계의 한계:** 다중 기기 경합과 오래된 요청의 늦은 덮어쓰기는 해결하지 않는다. 자동 재시도도 없다. 이를 해결하기 위해 U3에 revision·포크를 몰아넣지 않는다.

## 공통 진행 원칙

- 실제 게임에서 바뀔 동작을 설명하고, 필요한 Java 개념 1~2개를 확인한 뒤 작은 코드 단위로 진행한다.
- Postman 요청 → DB 행·실행 SQL → 실제 Unity 조작 순서로 확인한다. 단계의 검증과 설명을 마친 뒤 다음 단계로 간다.
- DTO는 record, Entity는 일반 클래스, 오류 응답은 `{errorCode, message}`를 사용한다. 명시적 생성자 주입과 Service 트랜잭션을 유지한다.
- `chapterKey`, `episodeKey`는 문자열 콘텐츠 키이고 `chapterId`, `episodeId`, `playthroughId`는 서버 숫자 PK다. `clientPlaythroughId`는 Unity 로컬 GUID다.
- 학습 저장 루트는 기존 저장과 분리한다. 초기에는 한 기기·한 실행 인스턴스·고정 챕터를 사용한다.
- 기존 로컬 저장·현재 장면 롤백은 사용한다. 과거 회차 포크·수동 슬롯 로드의 서버 연동, 인증, 콘텐츠 버전, 자동 재전송은 보류한다.
- 기존 완전 동기화 스택의 서버 ID·revision·ACK를 학습 서버 상태로 재사용하지 않는다.
- 이 파일은 계획이며 구현 완료를 뜻하지 않는다. 체크박스는 실제 검증 후 갱신한다.


## 연결 지점 참고 — 공통 어댑터 설계

다음 클래스 이름은 제안이며 아직 존재하지 않는다.

| 제안 구성 | 필요한 이유 | 책임 제한 |
| --- | --- | --- |
| `LearningProgressionReporter : IProgressionReporter` | 실제 장면 보고와 로컬 저장 성공을 관찰 | SaveCoordinator에 먼저 위임한 후 학습 연결에 알림 |
| `AnalyticsApi` | 새 서버의 작은 HTTP 계약 | 필요한 메서드만 하나씩 추가. 토큰·기존 저장 API를 호출하지 않음 |
| `LearningSession` | 로컬 GUID와 서버 회차 ID, 요청 중 상태 보관 | 최신 활성 회차를 응답 도착 후 다시 추측하지 않음 |
| 학습용 저장·복구 동작 | 업로드와 복구 결과를 직접 관찰 | 최초에는 한 요청만 실행하고 결과를 표시 |

초기 reporter의 흐름은 아래 정도면 충분하다.

```text
ReportSceneEntered(report)
  → SaveCoordinator.ReportSceneEntered(report)
  → 성공한 실제 로컬 회차 ID와 snapshot 관찰

ReportSceneCommitted(report)
  → SaveCoordinator.ReportSceneCommitted(report)
  → 성공한 해당 회차 snapshot의 사본 확보
  → 학습 패널에 서버로 보낼 확정 지점 표시
```

`IProgressionReporter`는 void 메서드다. 여기에 네트워크 await를 억지로 숨기거나 async void를 추가하지 않는다. 초기에는 보고가 HTTP를 직접 발사하지 않고 로컬 결과를 관찰한다. 회차 등록과 저장은 명시적 Task 메서드로 호출하여 실패를 관찰한다.

새 게임 등록의 자동 연결이 필요할 때는 최초 진입에서 확보한 ID를 LearningSession에 전달하고, 예외를 처리하는 등록 작업을 추적한다. 등록을 기다려야 하는 것은 서버 저장 동작이며, 대사 재생 전체를 서버 대기에 묶지 않는다.

장면 보고 직후 `ILocalSaveStore.LoadPlaythrough(capturedId)`로 저장된 사본을 읽는다. 어댑터에 같은 localStore 인스턴스를 명시적으로 주입하면 SaveCoordinator의 private 필드를 노출할 필요가 없다. 참조를 보관한 채 나중에 현재 활성 회차를 다시 읽어서 A 요청을 B 데이터로 바꾸지 않는다.

기존 `PlaythroughFile.Sync`의 서버 ID·revision·ACK는 학습용 연결에서 수정하지 않는다. 학습 서버가 받은 것은 완전 동기화 계약을 충족한 것이 아니기 때문이다.

## 데이터 이름 대응

| Unity | Java/API | 주의 |
| --- | --- | --- |
| `ChapterId` 문자열 | `chapterKey` | Long PK와 다름 |
| `EpisodeId` 문자열 | `episodeKey` | Chapter 안에서 해석 |
| `CommittedChoice.FromEpisodeId` | `episodeKey` | 선택이 출발한 노드 |
| `CommittedChoice.OptionIndex` | `optionIndex` | 원본 NextOptions 순번 |
| `LocalSaveFile.PlaythroughId` | `clientPlaythroughId` | 로컬 GUID |
| 새 서버 응답의 숫자 ID | `playthroughId` | 학습 서버 PK |
| `State.CurrentEpisodeId` | `episodeKey` 메타데이터 | 완료가 아니면 다음 장면 재개 지점 |
| `LocalSaveFile`의 직렬화 문자열 | `snapshotJson` | 복구할 실제 데이터 |
| `SceneRecord.Path` 누적 | U5 `choices` | 완료된 장면만, auto 제외 |
| `ErrorResponse.errorCode` | 학습 C# 오류 DTO의 `ErrorCode` | 기존 `Code` 파싱과 다름 |

모든 오류는 기존 record `{errorCode, message}`를 중심으로 유지한다. 새 enum 항목은 해당 단계의 실제 오류가 필요할 때 추가한다. HTTP 상태와 애플리케이션 오류 키의 역할을 구분한다.

## 단계 종료 기록

- 작업 브랜치·커밋:
- 변경한 서버 코드:
- 변경한 Unity 코드:
- Postman·DB 확인 결과:
- 실제 플레이 확인 결과:
- 실행한 자동 테스트와 결과:
- 직접 설명할 수 있게 된 개념:
- 남은 문제와 다음 작업:
