# U2 — 실제 새 게임과 서버 회차

vn-play-analytics / 실제 Unity 연동 학습 PLAN v2  
작성일: 2026-09-11  
상태: 계획 / 미착수

> 통합 문서 `PLAN_vn-play-analytics_Unity_Learning_v2.md`의 14절을 분리한 파일이다. 해당 단계 본문은 유지하고 독립적인 진행에 필요한 전제·공통 원칙을 덧붙였다. 통합 PLAN은 변경하지 않았다.

## 이 단계의 위치

완료 결과: 같은 로컬 회차는 같은 서버 회차에, 새 게임은 새 서버 회차에 연결된다.

**시작 전제:** U1에서 실제 콘텐츠를 등록하고 Unity에서 chapterKey로 조회했다. 문자열 콘텐츠 키와 숫자 DB ID를 구분할 수 있다.

이전: `PLAN_U1_Content_Connection.md` / 다음: `PLAN_U3_Checkpoint_Backup.md`

## 분석 기준

- Unity: `ked-presentation-runtime/server_DB_Test`, 커밋 `5ea9d8f9c5f80044a36c6f19ab2f424a1f8632eb`.
- 서버: `vn-play-analytics/dev`, 커밋 `8da0d202fae6fa59a3825ceb77902f34e2285b23`.
- 코드 분석에 근거한 계획이다. 실행·DB·Unity 검증 완료를 의미하지 않는다. 구현 시작 시 미푸시 변경과 Inspector 설정을 확인한다.

## 목표

Unity에서 생성된 실제 회차 ID와 서버의 회차 행을 연결한다.

## 작은 서버 모델

`Playthrough(id, chapter, clientPlaythroughId, startedAt)`부터 시작한다. id는 Long PK이고 clientPlaythroughId는 Unity의 GUID 문자열이다. UNIQUE를 둔다. 사용자가 없는 초기 개발 환경에서만 이 키를 전체 유일하게 사용한다. 인증·소유권 모델은 아직 없다.

API 초안:

```http
POST /playthroughs
Content-Type: application/json
```

```json
{
  "chapterKey": "qwer_scene",
  "clientPlaythroughId": "11111111111111111111111111111111"
}
```

신규면 201·Location·`{playthroughId, clientPlaythroughId}`를 반환한다. 같은 키와 같은 챕터의 반복 등록은 기존 회차를 200으로 반환한다. 같은 키인데 다른 챕터면 409다. `GET /playthroughs/{id}`와 `GET /playthroughs?clientPlaythroughId=...`로 조회할 수 있게 한다.

처음에는 직렬 요청에서 동작하는 멱등 등록을 학습한다. UNIQUE는 중복 행을 막지만 동시에 들어온 두 요청 모두가 친절한 200/201로 끝나는 것을 자동 보장하지 않는다. 그 경합 처리는 U7에 남긴다.

## Unity 연결

첫 SceneEntered의 로컬 저장 성공 후 실제 GUID를 확보한다. 서버 등록은 LearningSession의 명시적 Task로 실행하고 결과를 표시한다. 등록 실패 시 로컬 플레이는 계속 가능하고 “서버 등록 안 됨” 상태를 남긴다. 저장 동작은 등록 성공 후에만 가능하다.

프로그램 재시작 후 로컬 이어하기는 기존 GUID로 같은 서버 회차를 찾아야 한다. 매번 서버에서 새 회차를 만들지 않는다. 서버 ID는 별도 학습 연결 상태 또는 키 조회로 얻는다. 기존 완전 동기화용 `Sync.PlaythroughId`에 넣지 않는다.

## 완료 기준·검증

- [ ] 새 게임 두 번이면 서로 다른 GUID와 서버 회차 두 개다.
- [ ] 같은 로컬 회차 등록 두 번이면 같은 서버 ID다.
- [ ] 첫 장면 진입 실패는 서버 회차 생성으로 오인되지 않는다.
- [ ] A 요청 중 B로 바뀌어도 A 응답을 B의 서버 ID로 채택하지 않는다. 최초 버전에서 전환을 잠시 막는 방식도 가능하나 범위를 명시한다.
- [ ] 서버가 꺼져 있어도 로컬 진행은 가능하다.

**학습 질문:** PK와 업무 식별자 차이, `Optional.orElseThrow`, 생성자 주입으로 연결되는 객체, 사전 중복 확인과 UNIQUE의 차이를 설명한다.

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
