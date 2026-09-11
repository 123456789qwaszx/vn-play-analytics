# U4 — 서버 snapshot으로 실제 이어하기

vn-play-analytics / 실제 Unity 연동 학습 PLAN v2  
작성일: 2026-09-11  
상태: 계획 / 미착수

> 통합 문서 `PLAN_vn-play-analytics_Unity_Learning_v2.md`의 16절을 분리한 파일이다. 해당 단계 본문은 유지하고 독립적인 진행에 필요한 전제·공통 원칙을 덧붙였다. 통합 PLAN은 변경하지 않았다.

## 이 단계의 위치

완료 결과: 서버 데이터가 실제 Unity 재생을 이어 준다. 여기까지가 첫 완성이다.

**시작 전제:** U3에서 EP03 checkpoint를 서버에 백업하고 GET으로 확인했다. 원래 저장을 보존한 별도의 빈 학습 복구 경로를 준비한다.

이전: `PLAN_U3_Checkpoint_Backup.md` / 다음: `PLAN_U5_Choice_Records.md`

## 분석 기준

- Unity: `ked-presentation-runtime/server_DB_Test`, 커밋 `5ea9d8f9c5f80044a36c6f19ab2f424a1f8632eb`.
- 서버: `vn-play-analytics/dev`, 커밋 `8da0d202fae6fa59a3825ceb77902f34e2285b23`.
- 코드 분석에 근거한 계획이다. 실행·DB·Unity 검증 완료를 의미하지 않는다. 구현 시작 시 미푸시 변경과 Inspector 설정을 확인한다.

## 목표

U3의 GET 응답을 출력하는 데서 멈추지 않고 실제 Launcher·Driver에 연결한다.

## 복구 절차

1. 서버에 EP03 checkpoint를 저장한 회차를 명시적으로 선택한다.
2. 게임을 재생하지 않는 상태에서 별도의 비어 있는 학습 복구 루트를 선택한다. 원래 저장을 삭제하지 않는다.
3. GET 응답의 snapshotJson을 LocalSaveFile로 역직렬화한다.
4. GUID, 콘텐츠 키, 완료 여부, 필수 딕셔너리·목록, 콘텐츠의 장면 루트인지 검사한다. 초기 계약은 포크·PendingLoad를 지원하지 않는다.
5. 새 localStore의 `Create(snapshot)` 후 `SetActive(snapshot.PlaythroughId)`로 설치한다. 기존 회차가 있으면 자동 덮어쓰지 않는다.
6. 그 localStore를 사용하는 SaveCoordinator의 `LoadActiveResumePoint`를 통해 기존 Launcher로 재개한다.
7. 같은 GUID에 대한 U2 조회로 학습 서버 ID를 다시 연결한다.

기존 `ImportRestored(save, serverId, revision, nextSeq)`는 완전 동기화 메타데이터를 요구한다. 학습 서버에 없는 revision·nextSeq를 꾸며 넣어 호출하지 않는다. 기존 ServerRestore 전체를 연결하지 않고 작은 학습용 복구 절차를 둔다.

## 왜 snapshot을 먼저 그대로 보관하는가

episodeKey만으로는 진행 스탯, Yarn 변수, 이전 백로그가 돌아오지 않는다. 이미 검증된 로컬 복원 형식을 활용하면 서버 학습을 위해 Unity 저장계를 다시 만들 필요가 없다. 첫 단계에서 원본 snapshot을 보관하고, 서버가 조회·집계해야 할 값만 점차 관계형 데이터로 꺼낸다.

## 완료 기준

- [ ] 원래 학습 저장을 사용하지 않는 새 경로에서 EP03으로 이어진다.
- [ ] 성실 경로의 진행 스탯 값이 원래 저장과 같다.
- [ ] Yarn 변수와 이전 장면 백로그가 복원된다.
- [ ] 같은 회차를 계속하므로 불필요한 서버 새 회차가 생기지 않는다.
- [ ] 204·통신 실패·잘못된 콘텐츠는 복원 성공으로 표시되지 않는다.
- [ ] 완료 checkpoint는 “완료됨”으로 표시하고 이어하기 대상으로 실행하지 않는다. 기존 Launcher의 자동 새 게임 동작과 혼동하지 않는다.

**첫 완성:** 실제 게임에서 생성한 저장 데이터를 DB에 넣고, DB에서 받은 데이터가 실제 게임을 다시 재생시킨다.

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
