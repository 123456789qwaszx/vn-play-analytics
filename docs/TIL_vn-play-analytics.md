# 학습노트 - 직접 채우는 TIL

## 학습

---- lv 1 ----
[1] Chapter 작성

    @Table(
            name = "chapters",
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "uk_chapter_code",
                            columnNames = "code"
                    )
            }
    )

0) uniqueConstraints
- 지정한 컬럼에 중복을 허용하지 않는 제약을 검.
- name을 직접 준 이유: 없을 시 Hibernate가 해시값으로 만듬.

- columnNames는 필드명이 아니라 컬럼명.  
자바 필드가 chapterCode 면 컬럼은 chapter_code 라서 columnNames = "chapter_code"로 써야함.  
지금은 code 라 둘이 같아서 문제없지만, 카멜케이스 필드에서 필드명 그대로 적었다가 "컬럼을 못 찾는다"는 에러를 만나는 경우가 흔함

- DDL 생성 시에만 적용
-"저장 전에 코드로 조회해서 중복이면 막으면 되지 않나?" 싶을 수 있는데 — 그것만으론 부족해요. 두 요청이 동시에 들어오면 둘 다 "없네?" 하고 통과한 다음 둘 다 INSERT 해버립니다. DB 제약조건이 마지막 방어선이에요. 애플리케이션 체크는 사용자에게 친절한 에러 메시지를 주기 위한 거고, 진짜로 막는 건 이 제약입니다.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

1) @Id
- 이 필드가 테이블의 기본키(PK)라고 알려주는 것.
- 엔티티에 반드시 하나는 있어야함
- JPA는 이것으로 객체를 식별

2) @GeneratedValue(strategy = GenerationType.IDENTITY)
- PK값을 누가 만들지 결정. IDENTITY는 DB에게 맡긴다는 뜻.

IDENTITY: DB가 채번 (MySQL AUTO_INCREMENT)
SEQUENCE: DB 시퀀스 객체 사용 (Oracle, PostgreSQL)
TABLE	: 채번 전용 테이블 사용 (거의 안 씀)
AUTO	: DB 방언 보고 JPA가 알아서 선택 (기본값)

- IDENTITY는 id를 DB가 만들어 주기 때문에,
persist() 하는 순간 INSERT 쿼리가 바로 날아감.

- 원래 JPA는 쿼리를 모아뒀다가 한 번에 보내는 "쓰기 지연"을 하는데,
id를 알아야 영속성 컨텍스트에 넣을 수 있으니 기다릴 수가 없는 것임.

- 그래서 IDENTITY는 INSERT 배치 최적화가 안 되지만,
MYSQL을 쓰면 선택지가 이것뿐이라 대부분 그냥 씀.

3) @Column(nullable = false, length = 50)
- 컬럼 상세 설정.
nullable = false → NOT NULL 제약
length = 50 → VARCHAR 길이 (문자열에만 적용, 기본값 255)

- @Column 을 아예 안 붙여도 매핑은 됨.
필드명이 그대로 컬럼명이 되고,
Spring Boot 기본 설정에선 카멜케이스가 스네이크케이스로 바뀜 (memberName → member_name).


[2]JPA가 Java 클래스를 MySQL 테이블로 변환

```powershell
docker exec -it vn-analytics-mysql mysql -uvn_app -p vn_analytics
```

docker exec: 
 이미 돌아가는 컨테이너 안에서 명령 실행 (docker run 은 새로 만드는 것)

-it:
 -i 입력 받기 + -t 터미널 붙이기 → 대화형 콘솔용

vn-analytics-mysql:
 대상 컨테이너 이름

mysql:
 컨테이너 안에서 실행할 명령 (MySQL 클라이언트)

-uvn_app:
 사용자 vn_app 으로 로그인
 
-p:	
 비밀번호를 프롬프트로 입력받겠다

vn_analytics:
 접속할 데이터베이스 (USE vn_analytics 와 같은 효과)




```sql
SHOW TABLES;
```
+------------------------+
| Tables_in_vn_analytics |
+------------------------+
| chapters               |
+------------------------+



```sql
SHOW CREATE TABLE chapters\G
```
*************************** 1. row ***************************
       Table: chapters
Create Table: CREATE TABLE `chapters` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chapter_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
1 row in set (0.00 sec)


Java/JPA 설정                    MySQL 결과
@Entity	                        JPA가 관리하는 엔티티
@Table(name = "chapters")       chapters 테이블
@Id	                        기본 키
GenerationType.IDENTITY 	AUTO_INCREMENT
nullable = false	        NOT NULL
length = 50	                varchar(50)
@UniqueConstraint	        중복 code를 막는 유니크 키

===

---- lv1.1 ----

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "chapter_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_episode_chapter")
    )
    private Chapter chapter;

[1] Episode 작성

1) @ManyToOne(fetch = FetchType.LAZY, optional = false)
- 여러개의 Episode가 하나의 Chapter에 속한다는 뜻.
- Episode 입장에서는 그저 Chapter 하나를 참조함.

- fetch = FetchType.LAZY는 Episode를 조회할 때 연관된 Chapter를 항상 즉시 조회하지 않도록 명시한 것

- optional = false는 JPA 객체 관계에서 “Chapter 없는 Episode는 허용하지 않는다”는 뜻


2) @JoinColumn(name = "chapter_id", nullable = false)
- episodes 테이블에 chapter_id 외래 키 열을 만든다.
- nullable = false는 DB 열에도 NOT NULL 제약 적용.

3) 복합 유니크 제약:
columnNames = {"chapter_id", "code"}

- 에피소드 코드는 전체 시스템에서 유일할 필요는 없고, 같은 챕터 안에서만 유일하면 됨.

```powershell
docker exec -it vn-analytics-mysql mysql -uvn_app -p vn_analytics
```

```sql
SHOW CREATE TABLE episodes\G
```
*************************** 1. row ***************************
       Table: episodes
Create Table: CREATE TABLE `episodes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `chapter_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_episode_chapter_code` (`chapter_id`,`code`),
  CONSTRAINT `fk_episode_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
1 row in set (0.00 sec)

UNIQUE (chapter_id, code):
 UNIQUE는 같은 챕터 안에서 에피소드 코드가 중복되는 것을 막습니다.

FOREIGN KEY (chapter_id) REFERENCES chapters(id):
 FOREIGN KEY는 존재하지 않는 챕터를 참조하는 에피소드가 저장되는 것을 막습니다.



===