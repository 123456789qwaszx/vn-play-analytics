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

[3] Episode 작성

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

[4] ChoiceOption 작성

```sql
SHOW CREATE TABLE choice_options\G
```
*************************** 1. row ***************************
       Table: choice_options
Create Table: CREATE TABLE `choice_options` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `label` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `option_index` int NOT NULL,
  `episode_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_choice_option_episode_index` (`episode_id`,`option_index`),
  CONSTRAINT `fk_choice_option_episode` FOREIGN KEY (`episode_id`) REFERENCES `episodes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
1 row in set (0.00 sec)

===

---- lv2 ----

[1] ChapterRepository, EpisodeRepository, ChoiceOptionRepository 작성
- JpaRepository<Chapter, Long>
- 본문에 직접 구현하지 않더라도 메서드가 상속됨
 save(chapter);
 findById(id);
 findAll();
 existsById(id);
 deleteById(id);

- boolean existsByCode(String code);
 Spring Data JPA가 메서드 이름을 해석해 구현
   개념적으로: WHERE code = ?


- Chapter, Episode, ChoiceOption이 모두 들어오지만, 현재는 cascade를 사용하지 않기로 함.
 따라서 서비스에서는 Chapter 저장 -> Episode 저장 -> ChoiceOption으로 순서대로 저장.
  외래키를 가진 자식은 부모가 먼저 존재해야 하기 때문.

===

---- lv2.1 ----

[0]
	       null     ""      " "    적용 대상
1) @NotNull	✗	통과	통과	모든 타입
2) @NotEmpty	✗	✗      통과    String, 컬렉션, 배열, Map
3) @NotBlank	✗	✗	✗     String만

4) @Valid
- 검증을 중첩 객체 안쪽까지 전파

@Valid @NotNull List<LessonRequest> lessons
 @NotNull → 리스트 자체가 null이 아닌지만 검사
 @Valid → 리스트 안의 각 LessonRequest 를 열어서 그 안의 @NotBlank 들까지 검사

- @Valid 의 두 가지 동작
 이게 진짜 헷갈리는 부분인데, 같은 애노테이션이 위치에 따라 다른 일을 함

@PostMapping("/chapters")
public ResponseEntity<Void> create(
        @Valid @RequestBody ChapterCreateRequest request) {   // ← 검증 "시작"}

컨트롤러 파라미터의 @Valid 는 "이 객체 검증을 지금 실행해라"는 방아쇠고,  
필드의 @Valid 는 "안쪽까지 내려가라"는 전파임.

- 그래서 컨트롤러에 @Valid 를 안 붙이면 DTO에 애노테이션을 아무리 달아놔도 전부 무시됨.  
- 검증이 안 되는데 원인을 못 찾는 경우 열에 아홉이 이거.

@NotBlank(message = "챕터 코드는 필수입니다")
@Size(max = 50, message = "챕터 코드는 50자를 넘을 수 없습니다")
String code

검증에 걸리면 MethodArgumentNotValidException 이 터지고 기본적으로 400이 나가는데,    @RestControllerAdvice 로 잡아서 응답 형식을 다듬는 게 일반적.


[1] Request 작성

- 이번 DTO 검증으로
@NotBlank(문자열이 비었는가)
@Size(문자열이 너무 긴가)
@NotEmpty(Episode가 하나 이상인가)
@Size(선택지가 두 개 이상인가)
@PositiveOrZero(선택지 번호가 음수인가)

하지만, 다음 중복 검사는 어노테이션만으로 간단하게 처리가 안되는 종류
- 요청 안에서 Episode code가 중복되는가
- 같은 Episode 안에서 optionIndex가 중복되는가
- 이미 DB에 같은 Chapter code가 있는가

이 세 가지는 ContentService에서 감시.


[2] refactor: 콘텐츠 식별자 명칭 변경

- Chapter.code → Chapter.chapterKey
- Episode.code → Episode.episodeKey
- 숫자 PK인 id와 콘텐츠 작성자가 지정한 식별 키를 구분하기 위해 변경
- DB 컬럼도 code → chapter_key / episode_key로 재생성


mysql> SHOW TABLES;
+------------------------+
| Tables_in_vn_analytics |
+------------------------+
| chapters               |
| choice_options         |
| episodes               |
+------------------------+
3 rows in set (0.00 sec)

mysql> SHOW CREATE TABLE chapters\G
*************************** 1. row ***************************
       Table: chapters
Create Table: CREATE TABLE `chapters` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `chapter_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_chapter_key` (`chapter_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
1 row in set (0.01 sec)

mysql> SHOW CREATE TABLE episodes\G
*************************** 1. row ***************************
       Table: episodes
Create Table: CREATE TABLE `episodes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `episode_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `chapter_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_episode_chapter_key` (`chapter_id`,`episode_key`),
  CONSTRAINT `fk_episode_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
1 row in set (0.00 sec)

mysql> SHOW CREATE TABLE choice_options\G
*************************** 1. row ***************************
       Table: choice_options
Create Table: CREATE TABLE `choice_options` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `label` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `option_index` int NOT NULL,
  `episode_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_choice_option_episode_index` (`episode_id`,`option_index`),
  CONSTRAINT `fk_choice_option_episode` FOREIGN KEY (`episode_id`) REFERENCES `episodes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
1 row in set (0.00 sec)

===

---- lv3 ----
[1] service 작성

[2] Controller 작성


[3] URI란?
URI location = URI.create("/chapters/" + chapterId);
- URI:
 리소스의 위치를 나타내는 문자열을 구조화해서 다루는 Java 타입

예를 들어 chapterId가 12라면:
-> /chapters/12
그런다음
return ResponseEntity
        .created(location)
        .body(new CreateChapterResponse(chapterId));

여기서 created(location)은 HTTP 상태를 201 Created로 만들고, 응답 헤더에 Location도 넣음.

응답
->
HTTP/1.1 201 Created
Location: /chapters/12
Content-Type: application/json

{
  "chapterId": 12
}

즉 서버가 클라에게,
새 Chapter를 만들었고, 그 리소스는 '/chapters/12' 에 뒀다고 알리는 것.

URI
→ 리소스를 식별하는 표현

URL
→ 그 리소스가 어디에 있고 어떻게 접근하는지 나타내는 URI의 한 종류

- 지금 String이 아니라 URI를 쓰는 이유는,
ResponseEntity.created의 인자가 애초에 URI이기 때문.
개념적으로 ResponseEntity.created(URI location)

그래서 .created("/chapters/" + chapterId) 
 이렇게 바로 넣는 건 불가능.

대신,
URI location = URI.create("/chapters/" + chapterId);
 이렇게 바꿔서 전달.


즉 현재 흐름
POST /chapters
    ↓
Chapter 생성
    ↓
ID = 12
    ↓
URI.create("/chapters/12")
    ↓
201 Created
Location: /chapters/12


[4] ResponseEntity란?
 Spring에서 HTTP 응답 전체를 직접 구성할 때 사용하는 객체.

- 응답 데이터뿐만이 아니라,  
(1)상태 코드, (2)헤더, (3)본문 을 함게 다룰 수 있음.

```
return ResponseEntity
        .created(location)
        .body(new CreateChapterResponse(chapterId));
 ```
이걸 HTTP 응답으로 보면
```
HTTP/1.1 201 Created
Location: /chapters/12
Content-Type: application/json

{
  "chapterId": 12
}
```

[5] 이렇게 ResponseEntity를 쓰는 이유는? DTO를 바로 반환 가능한데.
@PostMapping
public CreateChapterResponse createChapter(...) {
    ...
    return new CreateChapterResponse(chapterId);
}

이렇게 해도 '200 OK'와 JSON BODY를 받을 수 있지만,

지금 객체를 생성한 것이기 때문에

'200 OK' 보다는, '201 Created'를 주고 싶고, Location 헤더까지 넣고 싶기 때문.

===


---- lv4 ----

[1] 서버의 DTO 구조를 클라와 동기화

[2] 테스트

요청
```http
POST http://localhost:8080/chapters
```
```json
{
  "chapterKey": "qwer_scene",
  "title": "qwer (장면 묶음 테스트)",
  "episodes": [
    {
      "episodeKey": "EP01",
      "title": "사무실 - 도착",
      "options": [
        {
          "optionIndex": 0,
          "label": "성실하게 (Via 있음, 같은 장면)",
          "auto": false
        },
        {
          "optionIndex": 1,
          "label": "요령있게 (Via 없음, 같은 장면)",
          "auto": false
        }
      ]
    },
    {
      "episodeKey": "EP02_01",
      "title": "사무실 - 성실 루트",
      "options": [
        {
          "optionIndex": 0,
          "label": "복도로 (장면 나감)",
          "auto": false
        }
      ]
    },
    {
      "episodeKey": "EP02_02",
      "title": "사무실 - 요령 루트",
      "options": [
        {
          "optionIndex": 0,
          "label": "복도로 (Via 있음, 장면 나감)",
          "auto": false
        }
      ]
    },
    {
      "episodeKey": "EP03",
      "title": "복도",
      "options": [
        {
          "optionIndex": 0,
          "label": "",
          "auto": true
        }
      ]
    },
    {
      "episodeKey": "EP04",
      "title": "복도 - 끝",
      "options": []
    }
  ]
}
```


응답 '201 Created'
```json
{
    "chapterId": 1
}
```

- 서버용 가공 데이터를 임의로 만든 것이 아니라 Unity NextOptions를 그대로 카탈로그화한 것
- 실제 콘텐츠 POST 등록은 성공

ContentController → @Valid → ContentService → Chapter 저장 → Episode 저장 → ChoiceOption 저장 → 트랜잭션 commit → 201 응답

[3] Intellij DB 스키마 연결

쿼리 콘솔 열기 - 데이터 소스 선택하고 Ctrl + Shift + Q.  
실행은 Ctrl + Enter.  
여기선 USE 안 써도 되고, 콘솔 상단 드롭다운에서 스키마를 고르면 됨.

테이블 더블클릭 - 데이터가 바로 그리드로 열림.
셀을 직접 수정하고 Ctrl+Enter 로 반영할 수도 있음.

ER 다이어그램 - 테이블 여러 개 선택 후 Ctrl + Alt + Shift + U.  
episodes ↔ choice_options 외래키 관계가 그림으로 나와서 구조 확인할 때 좋음.

@Query 안의 SQL을 검사해준다는 장점도 있음.

[4] MYSQL 확인

1) chapters
1,qwer_scene,qwer (장면 묶음 테스트)

2) episodes
1,EP01,사무실 - 도착,1
2,EP02_01,사무실 - 성실 루트,1
3,EP02_02,사무실 - 요령 루트,1
4,EP03,복도,1
5,EP04,복도 - 끝,1

3) choice_options
1,"성실하게 (Via 있음, 같은 장면)",0,1,false
2,"요령있게 (Via 없음, 같은 장면)",1,1,false
3,복도로 (장면 나감),0,2,false
4,"복도로 (Via 있음, 장면 나감)",0,3,false
5,"",0,4,true

===

---- lv5 ----

[1] ResponseDTO 작성

[2] getChapterAPI 작성

[3] 테스트

1) DB 상세조회
요청
```
GET http://localhost:8080/chapters/1
```

응답 '200 ok'
```json
{
    "chapterId": 1,
    "chapterKey": "qwer_scene",
    "title": "qwer (장면 묶음 테스트)",
    "episodes": [
        {
            "episodeId": 1,
            "episodeKey": "EP01",
            "title": "사무실 - 도착",
            "options": [
                {
                    "optionIndex": 0,
                    "label": "성실하게 (Via 있음, 같은 장면)",
                    "auto": false
                },
                {
                    "optionIndex": 1,
                    "label": "요령있게 (Via 없음, 같은 장면)",
                    "auto": false
                }
            ]
        },
        {
            "episodeId": 2,
            "episodeKey": "EP02_01",
            "title": "사무실 - 성실 루트",
            "options": [
                {
                    "optionIndex": 0,
                    "label": "복도로 (장면 나감)",
                    "auto": false
                }
            ]
        },
        {
            "episodeId": 3,
            "episodeKey": "EP02_02",
            "title": "사무실 - 요령 루트",
            "options": [
                {
                    "optionIndex": 0,
                    "label": "복도로 (Via 있음, 장면 나감)",
                    "auto": false
                }
            ]
        },
        {
            "episodeId": 4,
            "episodeKey": "EP03",
            "title": "복도",
            "options": [
                {
                    "optionIndex": 0,
                    "label": "",
                    "auto": true
                }
            ]
        },
        {
            "episodeId": 5,
            "episodeKey": "EP04",
            "title": "복도 - 끝",
            "options": []
        }
    ]
}
```

[2] 챕터 상세 작성

[3] 챕터키 조회 추가
- Optional<'Chapter> findByChapterKey(String chapterKey);

GET /chapters/1
→ 1번 Chapter의 상세 내용

GET /chapters?chapterKey=qwer_scene
→ qwer_scene이라는 Chapter를 검색

- 이것의 못적은 이 콘텐츠가 서버에 등록되어 있는가?
- 있다면 서버 PK가 몇 번인가? 를 묻는 것.

[4] PK를 알아내는 이유
- PK는 그 행에 도달하는 유일한 주소.
- JPA가 즉시 알아야함.

  영속성 컨텍스트(1차 캐시)는 사실 이런 구조. Map<식별자, 엔티티>
  즉 키가 id.
  
  그렇기에 IDENTITY 전략에서 persist() 가 INSERT를 즉시 날리는 것처럼 id를 확보해야 가능함.
- chapterKey는 유니크 제약으로 중복만 막고, 식별은 id가 맡는 구조.
BIGINT는 8바이트인데, VARCHAR(50)은 최대 50바이트. 이게 FK로 여기저기 퍼지면 저장 공간도, 조인 비용도 몇 배가 됨.

[5] 챕터 키 조회 API 작성

1) ChapterKey 기반 chapter의 PK 획득 및 조회
요청
```
http://localhost:8080/chapters?chapterKey=qwer_scene
```

응답
``` 200 OK
[
    {
        "chapterId": 1,
        "chapterKey": "qwer_scene",
        "title": "qwer (장면 묶음 테스트)"
    }
]
```

2) 없는 ChapterKey 빈 배열 반환 확인
요청
```
http://localhost:8080/chapters?chapterKey=없는챕터
```

응답
``` 200 OK
[]
```

[6]Unity 작업. 조회 추가.

[U1 서버 콘텐츠] 연결 초기화
server: http://localhost:8080
첫 장면 진입 대기

[U1 서버 콘텐츠] 조회 시작
GET http://localhost:8080/chapters?chapterKey=qwer_scene
UnityEngine.Debug:Log (object)

[U1 서버 콘텐츠]
local chapterKey: qwer_scene
HTTP 200
server chapterId: 1 / title: qwer (장면 묶음 테스트)
UnityEngine.Debug:Log (object)

- Unity의 실제 콘텐츠 식별자와 서버 DB의 PK가 연결되는 것까지 검증 완료

Unity 실제 progression
qwer_scene
    ↓ HTTP
GET /chapters?chapterKey=qwer_scene
    ↓
Spring Controller
    ↓
Service
    ↓
JPA / MySQL
    ↓
chapterId = 1
    ↓ HTTP Response
Unity

===

---- lv5 ----
[1] Playthrough Entity 작성 및 DB 반영 확인

mysql> SHOW TABLES;
+------------------------+
| Tables_in_vn_analytics |
+------------------------+
| chapters               |
| choice_options         |
| episodes               |
| playthroughs           |
+------------------------+

mysql> DESCRIBE playthroughs;
+-----------------------+-------------+------+-----+---------+----------------+
| Field                 | Type        | Null | Key | Default | Extra          |
+-----------------------+-------------+------+-----+---------+----------------+
| id                    | bigint      | NO   | PRI | NULL    | auto_increment |
| client_playthrough_id | varchar(32) | NO   | UNI | NULL    |                |
| started_at            | datetime(6) | NO   |     | NULL    |                |
| chapter_id            | bigint      | NO   | MUL | NULL    |                |
+-----------------------+-------------+------+-----+---------+----------------+

mysql> SHOW CREATE TABLE playthroughs;
+--------------
| Table        | Create Table                                                                                      
| playthroughs | CREATE TABLE `playthroughs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `client_playthrough_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `started_at` datetime(6) NOT NULL,
  `chapter_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_playthrough_client_id` (`client_playthrough_id`),
  KEY `fk_playthrough_chapter` (`chapter_id`),
  CONSTRAINT `fk_playthrough_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapters` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci |


[2] playthrough테이블에 chapter_id라는 FK를 만들었는데 KEY도 생긴 이유
KEY `fk_playthrough_chapter` (`chapter_id`),
CONSTRAINT `fk_playthrough_chapter`
FOREIGN KEY (`chapter_id`)
REFERENCES `chapters` (`id`)

1) FK와 인덱스는 서로 역할이 다름
 FOREIGN KEY:
  -> 데이터가 올바른 관계를 가지는 지 검사

 INDEX:
  - 데이터를 빨리 찾게 해주는 구조

  chapters
id
1  qwer_scene
2  chapter_b

playthroughs
id   chapter_id
1       1
2       1
3       1
4       2
위와 같을 때,

FOREIGN KEY (chapter_id)
REFERENCES chapters(id)
이 제약은

INSERT INTO playthroughs (..., chapter_id)
VALUES (..., 9999);
이걸 막음

즉, FK의 관심사는 chapter_id가 실제 존재하는 Chapter 를 가리키고 있는가? 임.

2) 
반면 INDEX는 chapter_id = 1인 행들이 어디에 있는가? 를 빨리 찾기 위함.


id       chapter_id
1        3
2        8
3        1
4        2
5        1
...
1000000  7
이런 상황에서 만약 INDEX가 없다면
DB는 Full Table Scan을 해야함.

3) 반면 INDEX가 있으면?

chapter_id에 인덱스가 있으면 DB는 별도의 검색 구조를 가짐.

chapter_id
1 → playthrough 3
1 → playthrough 5
1 → playthrough 71
1 → playthrough 800
2 → playthrough 4
2 → playthrough 20
3 → playthrough 1
...
위와 같은 정렬된 탐색 구조.

WHERE chapter_id = 1를 보고
chapter_id 인덱스
        ↓
1이 있는 위치 탐색
        ↓
해당 playthrough들만 접근

- 즉 DB 전체를 처음부터 끝까지 뒤질 필요가 줄어든다.

[2] 그런데 왜 FK를 만들면 인덱스까지 필요한가?

- MySQL/InnoDB는 외래키 검사를 빠르게 하기 위해 외래키 컬럼에 적절한 인덱스가 필요
- 참조하는 쪽의 FK컬럼이 선두 컬럼 인덱스가 없다면 InnoDB가 자동 생성.

현재 구조를 보면:

Chapter
   ↑
   │ chapter_id
Playthrough

playthroughs.chapter_id는 FK.


이제 Chapter를 삭제한다고 가정.

DELETE FROM chapters
WHERE id = 1;
DB는 그냥 지워버릴 수 없습니다.

먼저:
chapter_id = 1을 참조하는 Playthrough가 있나? 를 검사해야 함.


즉 내부적으로 "playthroughs 중에서 chapter_id = 1인 행이 있는가?" 라는 질문을 해야하는데,
인덱스가 없다면 playthroughs 모든 행을 전체 검사한다.
InnoDB는 FK를 만들 때 필요한 인덱스가 없으면 자동으로 만들어 둔다.
(FK 검사가 table scan 없이 빠르게 이루어지도록 인덱스를 요구.)

흐름:
@ManyToOne
    ↓
@JoinColumn(chapter_id)
    ↓
FOREIGN KEY 생성
    ↓
InnoDB가 FK 검사용 인덱스 필요
    ↓
chapter_id INDEX 생성

- @ManyToOne이 단순히 Java 객체끼리 연결되는 기능이 아니라, DB로 내려가면 FK와 그 관계를 효율적으로 유지하기 위한 인덱스 구조까지 이어질 수 있다

===

---- lv6 ----

[1] 

1) 구조
chapters

id | chapter_key
1  | qwer_scene

playthroughs

id | chapter_id | client_playthrough_id
1  |     1      | abc...
2  |     1      | def...
3  |     1      | ghi...

2) 관계
Chapter PK 1
    ↑
    ├── Playthrough PK 1
    ├── Playthrough PK 2
    └── Playthrough PK 3


3) 세 가지 DB 장치가 서로 다른 일을 함.

PRIMARY KEY (id)
→ Playthrough 자체를 식별

UNIQUE (client_playthrough_id)
→ 같은 Unity 회차 중복 등록 방지

FOREIGN KEY (chapter_id)
→ 존재하지 않는 Chapter 참조 방지

INDEX (chapter_id)
→ FK 검사 및 chapter 기준 탐색에 사용 가능

===