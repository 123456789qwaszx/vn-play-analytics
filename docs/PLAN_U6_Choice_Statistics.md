# U6 — 실제 선택 통계와 조회 학습

vn-play-analytics / 실제 Unity 연동 학습 PLAN v2  
작성일: 2026-09-11  
상태: 계획 / 미착수

> 통합 문서 `PLAN_vn-play-analytics_Unity_Learning_v2.md`의 18절을 분리한 파일이다. 해당 단계 본문은 유지하고 독립적인 진행에 필요한 전제·공통 원칙을 덧붙였다. 통합 PLAN은 변경하지 않았다.

## 이 단계의 위치

완료 결과: 실제 플레이 경로가 서버 통계에 반영되고 Unity에서 확인된다. 여기까지가 두 번째 완성이다.

**시작 전제:** U5에서 checkpoint와 확정 사용자 선택 목록을 함께 저장하고, 반복 백업이 선택 수를 늘리지 않음을 확인했다.

이전: `PLAN_U5_Choice_Records.md`

## 분석 기준

- Unity: `ked-presentation-runtime/server_DB_Test`, 커밋 `5ea9d8f9c5f80044a36c6f19ab2f424a1f8632eb`.
- 서버: `vn-play-analytics/dev`, 커밋 `8da0d202fae6fa59a3825ceb77902f34e2285b23`.
- 코드 분석에 근거한 계획이다. 실행·DB·Unity 검증 완료를 의미하지 않는다. 구현 시작 시 미푸시 변경과 Inspector 설정을 확인한다.

## 목표

실제 Unity 경로가 만든 관계형 데이터를 집계해 게임의 학습 패널에서 확인한다.

API는 `GET /stats/episodes/{episodeId}/choices`로 한다. episodeId는 서버 PK다. U1 콘텐츠 상세 조회에서 키와 PK를 대응시킨다.

통계 규칙:

- 분자는 옵션별 확정 사용자 선택 행 수다.
- 분모는 해당 Episode의 전체 사용자 선택 행 수다.
- auto 간선은 사용자 선택 통계에서 제외한다.
- 사용자 옵션 중 선택되지 않은 옵션은 0건·0.0%로 포함한다.
- 옵션이 없는 종료 Episode와 auto 전용 Episode는 사용자 옵션 목록이 비어 있다. 존재하지 않는 Episode는 404다.
- 표시 조건 때문에 보지 못한 선택지까지 카탈로그에 포함될 수 있다. 노출 횟수 대비 클릭률은 별도 데이터가 필요한 다른 통계다.
- 백업하지 않은 로컬 진행은 서버 통계에 반영되지 않는다. 고유 사용자 비율·완주율로 부르지 않는다.

실제 qwer_scene을 여러 회차 플레이해 EP01의 3 대 1을 만들면 75.0% 대 25.0%를 확인할 수 있다. 0건 옵션과 3·1·0 비교는 별도의 작은 테스트 데이터로 만든다. 이를 실제 플레이에서 얻은 데이터라고 혼동하지 않는다.

SQL로 기대 결과를 먼저 확인한다. 옵션 기준 LEFT JOIN, COUNT(기록 ID), GROUP BY를 설명한다. 같은 결과를 JPQL projection과 비교하고 작은 집계 결과에서 비율을 계산한다. 조회 구현은 하나를 API에 채택한다.

그 뒤 실제 추가 조회가 보이는 지점 하나에 한해 N+1을 관찰하고 개선한다. 단방향 관계에 없는 부모 컬렉션을 JPQL 경로로 가정하지 않는다. 페이징·DTO projection·fetch join은 실행 SQL을 보며 필요한 곳에만 도입한다.

**완료 기준:** Unity의 두 경로를 다르게 플레이하면 서버 선택 통계가 달라진다. 반복 백업은 비율을 부풀리지 않는다. 서버 응답을 학습 패널에 표시하고 SQL 결과와 비교한다.

## 공통 진행 원칙

- 실제 게임에서 바뀔 동작을 설명하고, 필요한 Java 개념 1~2개를 확인한 뒤 작은 코드 단위로 진행한다.
- Postman 요청 → DB 행·실행 SQL → 실제 Unity 조작 순서로 확인한다. 단계의 검증과 설명을 마친 뒤 다음 단계로 간다.
- DTO는 record, Entity는 일반 클래스, 오류 응답은 `{errorCode, message}`를 사용한다. 명시적 생성자 주입과 Service 트랜잭션을 유지한다.
- `chapterKey`, `episodeKey`는 문자열 콘텐츠 키이고 `chapterId`, `episodeId`, `playthroughId`는 서버 숫자 PK다. `clientPlaythroughId`는 Unity 로컬 GUID다.
- 학습 저장 루트는 기존 저장과 분리한다. 초기에는 한 기기·한 실행 인스턴스·고정 챕터를 사용한다.
- 기존 로컬 저장·현재 장면 롤백은 사용한다. 과거 회차 포크·수동 슬롯 로드의 서버 연동, 인증, 콘텐츠 버전, 자동 재전송은 보류한다.
- 기존 완전 동기화 스택의 서버 ID·revision·ACK를 학습 서버 상태로 재사용하지 않는다.
- 이 파일은 계획이며 구현 완료를 뜻하지 않는다. 체크박스는 실제 검증 후 갱신한다.


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
