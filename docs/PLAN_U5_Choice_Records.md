# U5 — 확정 선택을 관계형 데이터로 연결

vn-play-analytics / 실제 Unity 연동 학습 PLAN v2  
작성일: 2026-09-11  
상태: 계획 / 미착수

> 통합 문서 `PLAN_vn-play-analytics_Unity_Learning_v2.md`의 17절을 분리한 파일이다. 해당 단계 본문은 유지하고 독립적인 진행에 필요한 전제·공통 원칙을 덧붙였다. 통합 PLAN은 변경하지 않았다.

## 이 단계의 위치

완료 결과: 실제 확정 사용자 선택을 FK로 연결하고 순서대로 조회한다.

**시작 전제:** U4에서 서버 snapshot으로 실제 이어하기를 확인했다. U1의 전체 Episode·옵션 카탈로그와 auto 정보가 유지되어 있다.

이전: `PLAN_U4_Server_Resume.md` / 다음: `PLAN_U6_Choice_Statistics.md`

## 분석 기준

- Unity: `ked-presentation-runtime/server_DB_Test`, 커밋 `5ea9d8f9c5f80044a36c6f19ab2f424a1f8632eb`.
- 서버: `vn-play-analytics/dev`, 커밋 `8da0d202fae6fa59a3825ceb77902f34e2285b23`.
- 코드 분석에 근거한 계획이다. 실행·DB·Unity 검증 완료를 의미하지 않는다. 구현 시작 시 미푸시 변경과 Inspector 설정을 확인한다.

## 목표

서버에 단순히 JSON을 보관하는 단계에서, 서버가 선택지를 FK로 이해하고 조회하는 단계로 확장한다.

## 수집 의미를 먼저 고정한다

초기 통계는 **서버에 마지막으로 백업된 회차 경로에서 확정된 사용자 progression 선택**을 센다. 모든 클릭, 모든 에피소드 방문, Yarn 인라인 선택, 고유 사용자 수를 센다고 주장하지 않는다.

학습 표본은 작고 포크가 없으므로, 첫 구현은 snapshot의 완료된 `Scenes`를 순서대로 순회하여 **회차의 전체 확정 선택 목록을 함께 업로드**하는 방식을 권장한다. 서버는 그 회차의 기존 파생 선택 행을 새 목록으로 교체한다. 이 선택은 append-only 이벤트 수집과 다른 계약이다. 반복 전송 때 같은 클릭을 새 행으로 누적하지 않으면서, 영속 outbox·ACK 프로토콜을 지금 추가하지 않기 위한 학습 범위의 결정이다.

## 서버 모델

`ChoiceRecord(id, playthrough, choiceOption, sequence)`를 추가한다. `(playthrough_id, sequence)` UNIQUE를 둔다. sequence는 업로드된 전체 사용자 선택 목록의 1 기반 순서다. DB 생성 ID는 교체 때 바뀔 수 있으므로 플레이 순서로 사용하지 않는다. 원래 Sync.NextSeq와도 구분한다.

U3 PUT 요청에 `choices`를 추가한다. 각 항목은 `{episodeKey, optionIndex}`다. 현재 학습 카탈로그의 `auto`를 대조해 사용자 간선만 포함한다. 원본 배열 인덱스는 유지하며 auto를 제외한 뒤 optionIndex를 다시 매기지 않는다.

`CommittedChoice` 자체에는 IsAuto가 없다. snapshot.Path를 순회할 때 로컬 고정 ChapterProgression에서 `(FromEpisodeId, OptionIndex)`를 찾아 `IsAuto`를 확인한다. 서버도 등록된 auto 간선이 들어오면 거부한다. 모든 노드와 순번의 카탈로그 대응이 U1에서 필요한 이유다.

## 트랜잭션 범위

한 Service 메서드에서 회차·콘텐츠 소속과 선택지 존재를 검증한 뒤 checkpoint 갱신과 해당 회차의 선택 행 교체를 함께 처리한다. 실패하면 이전 checkpoint·선택 행 모두 보존되어야 한다.

학습용 작은 목록에서는 명시적으로 기존 자식 조회·삭제, 필요한 flush, 새 자식 저장 순서를 관찰할 수 있다. 같은 UNIQUE 값으로 재삽입할 때 SQL 실행 순서가 영향을 준다는 점을 확인한다. 처음부터 bulk delete·영속성 컨텍스트 clear·orphanRemoval을 한꺼번에 도입하지 않는다.

전체 경로와 snapshot이 다른 회차/버전에서 섞이지 않도록 같은 사본에서 요청을 만든다. 서버는 목록의 키·순번과 snapshot의 확정 경로를 대조하는 변환·검증을 이 단계에서 추가한다. snapshotJson과 choices를 서로 무관한 두 진실로 방치하지 않는다.

## 검증

- [ ] 첫 사무실 경로는 사용자 선택 두 행이다.
- [ ] 마지막 복도 자동 간선은 사용자 선택 행으로 증가하지 않는다.
- [ ] 같은 전체 경로를 두 번 PUT해도 선택 수가 두 배가 되지 않는다.
- [ ] 같은 Episode를 실제로 다시 방문한 경로는 다른 sequence로 유지한다.
- [ ] 장면 안에서 되돌린 선택과 Yarn 인라인 선택이 들어가지 않는다.
- [ ] 잘못된 선택지 하나가 포함되면 전체 저장을 거부한다.
- [ ] 실제 쓰기 후 예외를 발생시키는 통합 테스트로 checkpoint와 선택의 공동 롤백을 확인한다. 사전 검증 실패만으로 트랜잭션 롤백 검증을 대체하지 않는다.
- [ ] GET `/playthroughs/{id}/choices`는 sequence 순이며 다른 회차 기록을 섞지 않는다.

**한계:** 작은 챕터용 전체 교체 방식이다. 큰 기록·자동 재전송·장시간 오프라인을 효율적으로 지원하는 프로토콜은 아니다. 이 한계가 관찰될 때 U7에서 증분 수집을 설계한다.

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
