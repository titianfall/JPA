# JPA 활용 2편 핵심 정리 — 전체 챕터 통합

> 김영한 「실전! 스프링 부트와 JPA 활용 2편 — API 개발과 성능 최적화」 1~5장 학습 정리 통합본.
> 각 장의 상세 내용은 `study/docs/SpringBootJPA/활용2/N. 제목.md` 참고.
> 실습 환경: Spring Boot 3.5.16, Java 17, Hibernate 6.6.x, H2, `jakarta.persistence.*`
> (6장 「다음으로」는 강의 마무리 안내라 정리를 생략했다)

---

## 0. 전체 그림 — 이 강의는 두 문장이다

> **① 엔티티를 API 스펙에서 떼어낸다. (1장)**
> **② 떼어낸 DTO를 채우는 과정에서 나가는 쿼리 수를 줄인다. (3~5장)**

```
1장  엔티티 ─X─▶ API      DTO를 만든다
     ↓
2장  샘플 데이터 준비      주문 2건 × 상품 2건 (N+1이 눈에 보이도록)
     ↓
3장  xToOne 최적화        7 → 5 → 1
     ↓
4장  컬렉션 최적화         11 → 3 (행 뻥튀기·페이징 한계)
     ↓
5장  OSIV                커넥션 점유 시간과 커맨드/쿼리 분리
```

3장과 4장이 이 강의의 본체다. **매 단계 쿼리 수를 실제로 세는 것**이 학습의 핵심이었다.

---

## 1. 엔티티를 API에 노출하지 마라 (1장)

### 왜 안 되는가

| 문제 | 설명 |
|------|------|
| 스펙이 엔티티에 종속 | 엔티티 필드명만 바꿔도 **API 스펙이 깨진다** |
| 원치 않는 필드 노출 | 필드를 추가하면 **모든 API에 자동으로 딸려 나간다** |
| 검증 조건 충돌 | API마다 필수 값이 다른데 엔티티는 **공용 자산**이다 |
| 최상위 배열 | `List`를 그대로 반환하면 나중에 `count` 하나 못 붙인다 |

### 정답은 항상 DTO

```java
// 요청·응답 모두 별도 DTO
@PostMapping("/api/v2/members")
public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) { ... }

// 조회는 Result 로 한 번 감싼다
return new Result(collect.size(), collect);
```

⚠️ **`@JsonIgnore`는 해결책이 아니라 증상이다.** 엔티티에 붙이면 전역이라 다른 API를 막는다.

💡 **DTO의 진짜 이점** — 엔티티가 바뀌면 **컴파일 에러로 알려준다.** 런타임에 스펙이 조용히 바뀌지 않는다.

### 수정은 변경 감지로, 메서드는 CQS

```java
@Transactional
public void update(Long id, String name) {   // 반환 X
    Member member = memberRepository.findOne(id);
    member.setName(name);                    // 변경 감지가 UPDATE 실행
}
```

영속 엔티티를 반환하면 호출부에서 의도치 않게 변경할 수 있다. **커맨드는 반환하지 않는다.**

---

## 2. 성능을 세기 위한 준비 (2장)

| 항목 | 요점 |
|------|------|
| `@PostConstruct` + `@Transactional` | **같은 메서드에 붙이면 안 된다.** 초기화 콜백이 트랜잭션 AOP보다 먼저 돈다 |
| 빈 분리 | `@Transactional`은 **프록시 기반** — 자기 호출(self-invocation)은 적용되지 않는다 |
| 중첩 클래스 | `static`이어야 빈으로 등록된다 |
| cascade | `Order`의 `cascade = ALL`이 `delivery`·`orderItem`을 대신 저장 |
| cascade 범위 | **라이프사이클이 같고 소유자가 하나**일 때만. `Member`·`Item`은 공유되므로 금지 |
| 배송지 주소 | 회원 주소를 **복사해서 박제**한다. 회원이 이사해도 과거 주문은 그대로 |

💡 **샘플이 주문 2건 × 상품 2건이어야 하는 이유** — 1건이면 N+1도, 조인 행 뻥튀기도 **눈에 보이지 않는다.**

---

## 3. xToOne 조회 최적화 (3장) — 7 → 5 → 1

`Order → Member`, `Order → Delivery` 처럼 **컬렉션이 아닌** 연관관계가 대상이다.

| 버전 | 방식 | 쿼리 수 |
|------|------|--------|
| V1 | 엔티티 직접 노출 | **7** |
| V2 | 엔티티 → DTO 변환 | **5** |
| **V3** | **페치 조인** ⭐ | **1** |
| V4 | JPA에서 DTO 직접 조회 | 1 (SELECT 컬럼만 더 적음) |

### 지연 로딩 프록시와 Jackson

```
com.fasterxml.jackson.databind.exc.InvalidDefinitionException:
No serializer found for class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor
```

⚠️ **모듈 이름이 버전마다 다르다.** 부트 3.5 + Hibernate 6이면 **`Hibernate6Module`**
(강의의 `Hibernate5Module` 아님).

⚠️ **모듈은 에러만 막을 뿐 데이터를 가져다주지 않는다.** 초기화하지 않은 프록시는 전부 `null`로 나간다.
결국 **DTO로 옮기는 것 말고는 답이 없다.**

### 이 장에서 가장 인상 깊었던 두 가지

1. **JPQL `join`은 조인만 할 뿐 함께 조회하지 않는다.** SQL `select`에 `o1_0.*`만 나가서 프록시가 그대로 남는다. **`join fetch`** 여야 한다.
2. **Jackson은 필드가 아니라 getter 기준으로 속성을 만든다.** `getTotalPrice()`가 몰래 쿼리 2개를 더 만들었다.

⚠️ **`EAGER`로 바꾸면 더 나빠진다.** JPQL은 즉시 로딩을 무시하고 N+1을 그대로 만든다.
**모든 연관관계는 `LAZY`**, 필요할 때 페치 조인.

### V3 vs V4 트레이드오프

| | V3 (엔티티 → DTO) | V4 (DTO 직접 조회) |
|---|---|---|
| 쿼리 수 | 1 | 1 |
| SELECT 컬럼 | 13 | 7 |
| 재사용성 | **높음** (엔티티 조회라 어디서든) | 낮음 (그 API 전용) |
| 리포지토리 | 공용 | **분리** (`repository.order.simplequery`) |

**V3가 기본**이다. V4는 컬럼 수가 성능에 유의미할 만큼 클 때만.

---

## 4. 컬렉션 조회 최적화 (4장) — 11 → 3

`Order → OrderItem` 처럼 **1:N**이 대상이다. 3장의 해법을 그대로 쓸 수 없다.

| 버전 | 방식 | 쿼리 수 |
|------|------|--------|
| V1 / V2 | 엔티티 노출 / DTO 변환 | **11 / 11** |
| V3 | 컬렉션 페치 조인 | 1 (⚠️ **페이징 불가**) |
| **V3.1** | **ToOne 페치 조인 + 배치 사이즈** ⭐ | **3** |
| V4 | DTO 직접 조회 | 3 (1+N) |
| V5 | IN 절 + Map 매칭 | 2 (1+1) |
| V6 | 플랫 데이터 | 1 (⚠️ 중복 전송) |

### 1:N 조인은 row를 뻥튀기한다

주문 2건인데 조인 결과가 **4 row**가 된다. `distinct`로 애플리케이션에서 중복을 제거한다.

⚠️ **하이버네이트 6은 `distinct` 없이도 중복을 제거해 준다.** 강의(부트 2.x)와 다른 지점.

### 컬렉션 페치 조인의 두 가지 치명적 한계

1. **페이징 불가** — `firstResult/maxResults specified with collection fetch; applying in memory` 경고와 함께 **전부 메모리로 가져와** 페이징한다. 데이터가 많으면 OOM.
2. **컬렉션 페치 조인은 1개만** 가능하다.

### 그래서 V3.1이 실무 정답

> **ToOne 관계는 페치 조인, ToMany 관계는 배치 사이즈.**

```yaml
spring.jpa.properties.hibernate.default_batch_fetch_size: 100
```

**설정 딱 한 줄로 7 → 3.** 페이징도 된다.

💡 배치 IN 절이 `NULL`로 패딩되는 건 **SQL 문자열을 고정해 실행 계획 캐시를 재사용**하려는 최적화다.

⚠️ **쿼리 1번이 곧 빠름은 아니다.** V6는 쿼리 1번이지만 중복 데이터를 전송하고 페이징도 안 된다.

### 권장 순서 (강의 결론)

```
① 엔티티 조회 → DTO 변환          (V2)
② 페치 조인으로 최적화             (V3, V3.1)  ← 대부분 여기서 끝
③ 그래도 안 되면 DTO 직접 조회      (V4~V6)
④ 그래도 안 되면 네이티브 SQL / JdbcTemplate
```

**엔티티 조회 방식이 먼저인 이유** — 코드를 거의 손대지 않고 옵션만으로 최적화할 수 있다.
DTO 직접 조회는 성능을 얻는 대신 **코드가 그 API에 박제된다.**

---

## 5. OSIV와 커맨드/쿼리 분리 (5장)

### OSIV가 뭘 하나

`spring.jpa.open-in-view`의 **기본값은 `true`** 이고, 부트가 시작할 때 `warn` 로그를 남긴다.

| | 커넥션 유지 구간 | 지연 로딩 |
|---|---|---|
| **ON (기본)** | 최초 DB 접근 ~ **API 응답 종료** | 컨트롤러·뷰에서도 가능 |
| **OFF** | 트랜잭션 시작 ~ **트랜잭션 종료** | 트랜잭션 안에서만 |

지연 로딩은 영속성 컨텍스트가, 영속성 컨텍스트는 커넥션이 있어야 산다.
**ON의 편리함과 위험이 같은 원인에서 나온다.**

⚠️ **OSIV의 대가는 커넥션 점유 시간이다.** 컨트롤러에서 외부 API를 호출하는 동안에도
커넥션을 붙잡고 있어서, 실시간 트래픽이 많으면 **커넥션 고갈 → 장애**로 이어진다.

### 4장의 정답과 5장의 정답이 충돌한다

**V3.1의 배치 로딩도 결국 지연 로딩**이라, OSIV를 끄면 컨트롤러에서 터진다.

해법은 **커맨드와 쿼리를 분리**하는 것이다.

```java
@Service
@Transactional(readOnly = true)     // 조회 전용
public class OrderQueryService {    // 서비스 안이므로 트랜잭션이 살아 있다
    public List<OrderDto> ordersV3_1(...) { ... }
}
```

| 서비스 | 역할 |
|--------|------|
| `OrderService` | 커맨드 — 핵심 비즈니스 로직 |
| `OrderQueryService` | 쿼리 — 화면/API 전용 조회, 자주 바뀜 |

### 실무 선택 기준

> **실시간 트래픽 API는 OSIV OFF, ADMIN처럼 커넥션 경합이 적은 곳은 ON.**

---

## ✅ 전체 핵심 요약

| # | 핵심 |
|---|------|
| 1 | **엔티티를 API 요청·응답에 절대 쓰지 않는다.** 요청·응답 모두 DTO |
| 2 | 조회 결과는 `Result<T>`로 감싼다. 최상위 배열은 확장이 막힌다 |
| 3 | 수정은 **변경 감지**로, 커맨드 메서드는 값을 반환하지 않는다 (CQS) |
| 4 | **모든 연관관계는 `LAZY`.** `EAGER`는 N+1을 예측 불가능하게 만든다 |
| 5 | JPQL `join`은 함께 조회하지 않는다. **`join fetch`** 여야 한다 |
| 6 | 지연 로딩 프록시는 Jackson이 직렬화하지 못한다 → **DTO가 정답** |
| 7 | Jackson은 **getter 기준** — 계산 getter가 쿼리를 몰래 추가한다 |
| 8 | **컬렉션 페치 조인은 페이징 불가 + 1개만.** 행이 뻥튀기된다 |
| 9 | **ToOne은 페치 조인, ToMany는 `default_batch_fetch_size`** (100~1000) |
| 10 | 쿼리 수가 적다고 항상 빠른 게 아니다. 중복 전송·페이징 가능 여부를 같이 본다 |
| 11 | 최적화 순서: 엔티티 조회 → 페치 조인 → DTO 직접 조회 → 네이티브 SQL |
| 12 | OSIV는 **커넥션 점유 시간**이 대가. 실시간 API는 끄고 **커맨드/쿼리를 분리**한다 |

### 버전 차이 (강의는 부트 2.x)

| 항목 | 강의 | 내 환경 (부트 3.5 / Hibernate 6) |
|------|------|--------------------------------|
| 패키지 | `javax.*` | **`jakarta.*`** |
| Jackson 모듈 | `Hibernate5Module` | **`Hibernate6Module`** |
| 파라미터 로그 | `org.hibernate.type: trace` | **`org.hibernate.orm.jdbc.bind: trace`** |
| 컬렉션 페치 조인 `distinct` | 필수 | **불필요** (하이버네이트가 처리) |

> 다음 단계는 **스프링 데이터 JPA**와 **Querydsl**이다.
> 이 강의에서 손으로 짠 리포지토리와 JPQL 문자열이 각각 어떻게 줄어드는지 확인하게 된다.
