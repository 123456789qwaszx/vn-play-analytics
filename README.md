# vn-play-analytics

비주얼 노벨의 콘텐츠와 플레이 선택 기록을 관리하고 통계를 조회하는 Spring 학습 프로젝트입니다.

## 학습 목표

- JPA 엔티티와 영속성 컨텍스트
- 연관관계와 PK·FK·UNIQUE 제약
- 트랜잭션과 변경 감지
- 요청 DTO·엔티티·응답 DTO 분리
- SQL과 JPQL 집계 비교
- 페이징과 N+1 관찰
- 인덱스와 실행 계획 확인

자세한 구현 순서는 [PLAN.md](PLAN.md)를 참고합니다.

## 기술 환경

- Java 21
- Spring Boot 4.1.1
- Gradle 9.7.1
- Spring Data JPA
- Spring Web MVC
- MySQL 8.0

## 데이터베이스

| DB | 용도 |
| --- | --- |
| `vn_analytics` | 개발 및 수동 API 확인 |
| `vn_analytics_test` | 자동 테스트 |

MySQL Docker 컨테이너:

```powershell
docker start vn-analytics-mysql