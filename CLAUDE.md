# CLAUDE.md — 김영한 JPA 로드맵 학습 정리 규칙

이 저장소는 김영한 JPA 로드맵 강의를 들으며 직접 코드를 작성하고,
각 챕터를 마크다운으로 정리해 나가는 학습용 repo다.

- **기본편**: 자바 ORM 표준 JPA 프로그래밍 — ✅ 완료
- **활용 1편**: 실전! 스프링 부트와 JPA 활용 1편 — ✅ 완료
- **활용 2편**: 실전! 스프링 부트와 JPA 활용 2편 (API 개발과 성능 최적화) — 🚀 진행 중
- **데이터 JPA**: 실전! 스프링 데이터 JPA — 🚀 진행 중

## 학습 정리 작업 트리거

사용자가 아래와 같은 요청을 하면 **이 규칙대로 학습 정리 마크다운을 작성**한다.

- "N장 정리해줘", "OO 챕터 정리해줘", "지금까지 배운 내용 정리해줘"
- "`N. 제목.pdf` 기반으로 정리해줘" 등

## 정리 규칙 (반드시 이 형식 유지)

1. **위치 / 파일명** — 강의별로 구분한다.

   | 강의 | 정리 md 위치 | PDF 강의자료 위치 | 실습 코드 |
   |------|-------------|------------------|-----------|
   | 기본편 | `study/docs/jpaBasic/` | `강의자료/JPA 기본편 - 강의자료/` (git 무시) | `study/jpaBasic/` (ex1-hello-jpa, hello-jpql, jpabook) |
   | 활용 1편 | `study/docs/SpringBootJPA/활용1/` | `강의자료/Spring Boot 와 JPA 활용 - 1/` (git 무시) | `study/SpringBootJPA/jpashop` |
   | 활용 2편 | `study/docs/SpringBootJPA/활용2/` | `study/docs/SpringBootJPA/활용2/` (md와 같은 폴더, git 무시) | `study/SpringBootJPA/jpashop` (1편에서 이어서 개발) |
   | 데이터 JPA | `study/SpringDataJPA/docs/` | `study/SpringDataJPA/docs/강의자료/` (git 무시) | `study/SpringDataJPA` |

   - 파일명은 PDF와 동일하게 맞춘다. 활용 1편은 `NN. 제목.md`(`01. 프로젝트 환경설정.md`),
     활용 2편·데이터 JPA PDF는 한 자리 번호이므로 `N. 제목.md`(`1. API 개발 기본.md`, `2. 예제 도메인 모델.md`).

2. **정리 소스 (우선순위)**
   - ① **내가 직접 작성한 코드와 주석** — 가장 우선. 내 코드 스니펫과 주석을 실제로 인용해서 설명한다.
   - ② 해당 챕터 **PDF 강의자료** 내용.
   - 둘을 결합하되, **"꼭 알아야 하는 핵심"만** 추린다. 슬라이드 전체 복붙 금지.
   - 아직 내 코드로 옮기지 않은 절은 강의자료 기준으로 쓰되 `🚧 미구현` 으로 표시한다.

3. **작성 스타일 (앞 챕터와 통일)**
   - 맨 위 제목 `# NN. 제목 — 부제`, 그 아래 인용구로 근거(PDF/내 코드) 명시.
   - 섹션마다 `##` 헤딩, 표·코드블록·다이어그램 적극 활용.
   - `⚠️` 주의사항, `💡` 팁, `✅ 핵심 요약` 섹션으로 강조.
   - 내 실제 코드와 강의(구버전)의 차이가 있으면 명시
     (예: `javax.persistence` → `jakarta.persistence`, JUnit4 → JUnit5, 부트 2.x → 3.x).
   - 서술은 한국어.
   - 참고 템플릿: 기본편은 `study/docs/jpaBasic/02. JPA 시작.md`,
     활용 1편은 `study/docs/SpringBootJPA/활용1/01. 프로젝트 환경설정.md`,
     활용 2편은 `study/docs/SpringBootJPA/활용2/1. API 개발 기본.md`.
   - 스프링 기초 개념이 나오면 [spring-study/issues](https://github.com/titianfall/spring-study/tree/main/issues) 링크로 연결한다.

4. **브랜치 / 커밋 / PR 워크플로우**
   - 챕터 단위로 `feature/NN-제목` 브랜치를 만들어 작업한다 (예: `feature/01-initalize-repo`).
   - 커밋 메시지는 **한 줄**, `feat: ...` / `docs: ...` / `chore: ...` 형식 (한국어 요약 허용). 트레일러(Co-Authored-By 등) 금지.
   - 실습 코드와 학습 정리 md는 **커밋을 분리**한다.
   - **진행 상황 표(CLAUDE.md·README.md) 갱신은 별도 커밋으로 나누지 말고
     해당 챕터 정리 md 커밋에 함께 넣는다.**
   - 챕터 완료 시 PR을 만들어 main에 머지하고, 머지 후 로컬 브랜치는 삭제한다.
   - **예외 — 오타/문서 수정은 PR 없이 main 직행.** 오타 교정, README·CLAUDE.md 갱신처럼
     학습 내용이 바뀌지 않는 자잘한 수정은 PR을 만들지 않고 main에 바로 커밋·푸시한다.
     이미 브랜치를 파서 작업했다면 `git merge --ff-only`로 fast-forward 해서
     불필요한 머지 커밋을 남기지 않는다 (예: PR #18 → `62fc316` fast-forward).

## 진행 계획 (챕터 목록)

### 데이터 JPA — 실전! 스프링 데이터 JPA

| # | 제목 | 정리 상태 |
|---|------|-----------|
| 1 | 프로젝트 환경설정 | ✅ `1. 프로젝트 환경설정.md` |
| 2 | 예제 도메인 모델 | ✅ `2. 예제 도메인 모델.md` |
| 3 | 공통 인터페이스 기능 | ⬜ |
| 4 | 쿼리 메소드 기능 | ⬜ |
| 5 | 확장 기능 | ⬜ |
| 6 | 스프링 데이터 JPA 분석 | ⬜ |
| 7 | 나머지 기능들 | ⬜ |

### 활용 2편 — 실전! 스프링 부트와 JPA 활용 2 (API 개발과 성능 최적화)

| # | 제목 | 정리 상태 |
|---|------|-----------|
| 1 | API 개발 기본 | ✅ `1. API 개발 기본.md` |
| 2 | API 개발 고급 - 준비 | ✅ `2. API 개발 고급 - 준비.md` |
| 3 | API 개발 고급 - 지연 로딩과 조회 성능 최적화 | ✅ `3. API 개발 고급 - 지연 로딩과 조회 성능 최적화.md` |
| 4 | API 개발 고급 - 컬렉션 조회 최적화 | ✅ `4. API 개발 고급 - 컬렉션 조회 최적화.md` |
| 5 | API 개발 고급 - 실무 필수 최적화 | ✅ `5. API 개발 고급 - 실무 필수 최적화.md` (🚧 코드 미구현) |
| 6 | 다음으로 | ⬜ |

### 활용 1편 — 실전! 스프링 부트와 JPA 활용 1 (완료)

| # | 제목 | 정리 상태 |
|---|------|-----------|
| 1 | 프로젝트 환경설정 | ✅ `01. 프로젝트 환경설정.md` |
| 2 | 도메인 분석 설계 | ✅ `02. 도메인 분석 및 설계.md` |
| 3 | 애플리케이션 구현 준비 | ✅ `03. 애플리케이션 구현 준비.md` |
| 4 | 회원 도메인 개발 | ✅ `04. 회원 도메인 개발.md` |
| 5 | 상품 도메인 개발 | ✅ `05. 상품 도메인 개발.md` |
| 6 | 주문 도메인 개발 | ✅ `06. 주문 도메인 개발.md` |
| 7 | 웹 계층 개발 | ✅ `07. 웹 계층 개발.md` |

### 기본편 — 자바 ORM 표준 JPA 프로그래밍 (완료)

| # | 제목 | 정리 상태 |
|---|------|-----------|
| 02 | JPA 시작 | ✅ `02. JPA 시작.md` |
| 03 | 영속성 관리 | ✅ `03. 영속성 관리.md` |
| 04 | 엔티티 매핑 | ✅ `04. 엔티티 매핑.md` |
| 05 | 연관관계 매핑 기초 | ✅ `05. 연관관계 매핑 기초.md` |
| 06 | 다양한 연관관계 매핑 | ✅ `06. 다양한 연관관계 매핑.md` |
| 07 | 고급 매핑 | ✅ `07. 고급 매핑.md` |
| 08 | 프록시와 연관관계 관리 | ✅ `08. 프록시와 연관관계 관리.md` |
| 09 | 값 타입 | ✅ `09. 값 타입.md` |
| 10 | 객체지향 쿼리 언어 | ✅ `10.1`, `10.2 객체지향 쿼리 언어.md` |

> 새 챕터 정리를 완료하면 이 표의 상태를 `✅ 파일명` 으로 갱신하고, README.md의 진행 상황 표도 함께 갱신한다.

## 프로젝트 메모

### 데이터 JPA — `study/SpringDataJPA`
- Gradle(Groovy), **Spring Boot 4.1.0**, Java 17, **Hibernate 7.4.1**, H2
- 의존성: webmvc, data-jpa, h2(+h2console), lombok, p6spy 2.0.1
  (강의는 p6spy 1.9.0 — 부트 4.x는 2.x를 써야 한다)
- 설정 파일: `application.yml`, `ddl-auto: create`, URL `jdbc:h2:tcp://localhost/~/springDataJpa`
- 실행 전 H2 TCP 서버 필요: `h2/bin/h2.bat`
- 강의 영상은 부트 2.x + JUnit4 — `javax`→`jakarta`, `@RunWith(SpringRunner.class)` 불필요 등 차이를 명시한다.
- ⚠️ 부트 4.x는 스타터 이름이 바뀌었다: `spring-boot-starter-web` → `spring-boot-starter-webmvc`,
  H2 콘솔은 `spring-boot-h2console` 별도 의존성.

### 활용 1·2편 — `study/SpringBootJPA/jpashop` (2편은 1편 프로젝트를 이어서 개발)
- Gradle(Groovy), Spring Boot 3.5.16, Java 17, Hibernate 6.6.x, H2
- 의존성: web, thymeleaf, data-jpa, h2, lombok, validation, p6spy(쿼리 파라미터 로그),
  jackson-datatype-hibernate6(활용 2편 3장에서 추가 — 미초기화 프록시를 `null`로 직렬화)
- 설정 파일: `application.yml` (강의 기준. properties 아님)
- 실행 전 H2 TCP 서버 필요: `h2/bin/h2.bat` (접속 URL `jdbc:h2:tcp://localhost/~/jpashop`)
- 강의 영상은 부트 2.x — 정리 시 3.x 차이점(`jakarta.persistence`, JUnit5, `org.hibernate.orm.jdbc.bind: trace` 등)을 명시한다.
- ⚠️ Jackson 하이버네이트 모듈은 강의(`Hibernate5Module`)와 다르다. 부트 3.5 + Hibernate 6이라 `Hibernate6Module`을 써야 한다.
- ⚠️ `Order`의 주문 상태 필드명은 강의의 `status`가 아니라 `orderStatus` — DTO 작성 시 `getOrderStatus()`.

### 기본편 — `study/jpaBasic`
- Maven, Java 17, Hibernate 6.x, H2. 순수 JPA (`persistence.xml`).
- JPA 표준 패키지는 `jakarta.persistence.*` 사용.
