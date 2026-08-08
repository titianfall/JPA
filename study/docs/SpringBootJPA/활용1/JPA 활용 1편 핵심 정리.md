# JPA 활용 1편 핵심 정리 — 전체 챕터 통합

> 김영한 「실전! 스프링 부트와 JPA 활용 1편」 01~07장 학습 정리 통합본.
> 각 장의 상세 내용은 `study/docs/SpringBootJPA/NN. 제목.md` 참고.
> 실습 환경: Spring Boot 3.5.16, Java 17, Gradle, H2, `jakarta.persistence.*`

---

## 0. 전체 그림

```
Controller ──(폼 객체/식별자)──▶ Service ──▶ Repository ──▶ DB
    │                              │
    └──(Model)──▶ 타임리프 뷰      └── 트랜잭션 경계
```

| 계층 | 책임 |
|------|------|
| **Controller** | 폼 객체를 받아 서비스에 위임, 뷰 이름 반환. **얇게 유지** |
| **Service** | 트랜잭션 경계. 엔티티를 조회해 **위임**할 뿐 로직을 갖지 않는다 |
| **Repository** | `EntityManager`로 DB 접근 |
| **Entity** | **비즈니스 로직을 갖는다** (도메인 모델 패턴) |

작업 순서는 항상 **엔티티 → 리포지토리 → 서비스 → 테스트 → 화면**.

---

## 1. 프로젝트 환경설정 (01장)

- 의존성: web, thymeleaf, data-jpa, h2, lombok, validation
- **`spring-boot-devtools`** — html 수정 후 `build → Recompile`이면 서버 재시작 없이 반영
- **p6spy** — 쿼리 파라미터를 실제 값으로 치환해 로그에 찍어준다

⚠️ **부트 3.x 차이** — 파라미터 로그는 `org.hibernate.orm.jdbc.bind: trace`. 강의(2.x)의 `org.hibernate.type: trace`는 동작하지 않는다.

💡 **p6spy 로그가 3줄로 보이는 이유** — ① 하이버네이트 로거(`?` 유지, `format_sql`로 들여쓰기) ② p6spy 원본 ③ p6spy 값 치환. 파라미터가 없는 쿼리는 ②·③이 글자까지 같아 **같은 쿼리가 두 번 실행된 것처럼** 보인다.

---

## 2. 도메인 분석 설계 (02장)

### 연관관계 매핑 원칙

| 원칙 | 이유 |
|------|------|
| **모든 연관관계는 지연 로딩(`LAZY`)** | 즉시 로딩(`EAGER`)은 예측 불가한 SQL과 N+1을 유발 |
| **`@ManyToOne`은 기본이 EAGER** → 반드시 `LAZY`로 변경 | `@OneToMany`는 기본이 LAZY라 그대로 둬도 된다 |
| **`@Enumerated(EnumType.STRING)`** | 기본값 `ORDINAL`은 중간에 상수를 추가하면 기존 데이터 의미가 깨진다 |
| **컬렉션은 필드에서 바로 초기화** | `null` 문제 방지, 하이버네이트가 내장 컬렉션으로 변경하는 것을 안전하게 유지 |

### 값 타입은 불변으로

```java
@Embeddable
@Getter                        // setter 없음
public class Address {
    protected Address() {}     // JPA 리플렉션·프록시용
    public Address(String city, String street, String zipcode) { ... }
}
```

생성자로만 만들고 `getter`로 꺼내기만 한다. 한 번 만들면 못 바꾼다.

⚠️ **`@Table(name = "orders")`** — `order`는 SQL 예약어라 테이블명을 바꿔야 한다.

---

## 3. 애플리케이션 구현 준비 (03장)

| 애노테이션 | 의미 |
|---|---|
| `@Service`, `@Repository` | 컴포넌트 스캔 대상 |
| `@Transactional(readOnly = true)` | 클래스 기본값. 읽기 전용 최적화 |
| `@Transactional` (메서드) | 쓰기 메서드에만 붙여 기본값을 덮어쓴다 |
| `@RequiredArgsConstructor` | `final` 필드 생성자 주입. **생성자 주입 권장** |

💡 **`EntityManager` 주입** — 부트에서는 `@PersistenceContext` 없이 `@Autowired`(또는 `@RequiredArgsConstructor` + `final`)로도 주입된다.

스프링 기초 개념은 [spring-study/issues](https://github.com/titianfall/spring-study/tree/main/issues) 참고.

---

## 4. 회원 / 상품 도메인 개발 (04~05장)

### 도메인 모델 패턴 — 이 강의의 관통 주제

| 패턴 | 로직 위치 |
|------|-----------|
| **도메인 모델 패턴** (이 프로젝트) | **엔티티**가 비즈니스 로직을 갖는다 |
| 트랜잭션 스크립트 패턴 | 엔티티는 데이터만, 로직은 전부 서비스에 |

재고는 `Item`이 스스로 책임진다.

```java
public void addStock(int quantity) { this.stockQuantity += quantity; }

public void removeStock(int quantity) {
    int restStock = this.stockQuantity - quantity;
    if (restStock < 0) throw new NotEnoughStockException("need more stock");
    this.stockQuantity = restStock;
}
```

⚠️ **`@Transactional` 안에서 setter/필드 변경만으로 UPDATE가 나간다** — 변경 감지(dirty checking). `em.update()`는 존재하지 않는다.

---

## 5. 주문 도메인 개발 (06장)

### cascade와 연관관계 편의 메서드

```java
@OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
private Delivery delivery;

@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
private List<OrderItem> orderItems = new ArrayList<>();
```

`orderRepository.save(order)` 한 번으로 `Delivery`·`OrderItem`이 함께 `persist`된다.

⚠️ **cascade는 아무 데나 쓰면 안 된다** — **소유자가 명확하고 생명주기를 함께하는** 관계에만. 여기선 `Order`만 `Delivery`·`OrderItem`을 참조하므로 안전하다.

### 정적 생성 메서드로 생성 지점을 강제

```java
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // new Order() 차단
public class Order {
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) { ... }
    public void cancel() { ... }          // 상태 변경 + 재고 복구
    public int getTotalPrice() { ... }
}
```

생성 규칙이 바뀌어도 이 메서드만 고치면 된다.

### 동적 쿼리 3가지

| 방식 | 평가 |
|------|------|
| **JPQL 문자열 조립** (`findAllByString`) | 동작하지만 오타·띄어쓰기에 취약 |
| **JPA Criteria** (`findAllByCriteria`) | 표준이고 컴파일 타임 안전하지만 **가독성 최악**. 실무 X |
| **Querydsl** | **실질적 정답**. 활용 2편에서 다룸 |

⚠️ `findAll(orderSearch)`는 **파라미터를 받기만 하고 쓰지 않는다.** 조건 없이 전체 조회하므로 검색이 동작하지 않는다.

---

## 6. 웹 계층 개발 (07장)

### 엔티티를 폼/API에 노출하지 마라

화면 요구사항(`@NotEmpty`, 화면 전용 필드)이 엔티티에 스며들면, 화면이 바뀔 때마다 **DB 테이블과 다른 화면까지** 흔들린다.

- 화면에는 **폼 객체**, API에는 **DTO**
- **API는 절대 엔티티를 반환하지 않는다** — 엔티티 변경 = API 스펙 변경 = 장애
- 단, 화면 로직 없는 단순 조회는 엔티티를 그대로 써도 실용적으로 무방

### 🔑 준영속 엔티티 — 이 챕터의 핵심

수정 폼에서 넘어온 데이터로 `new Book()` + `setId()` 한 객체는 **준영속 엔티티**다. `id`는 있지만 영속성 컨텍스트가 모르니 **변경 감지가 동작하지 않는다.**

| | 변경 감지 ✅ | 병합(merge) ⚠️ |
|---|---|---|
| 변경 범위 | **원하는 필드만** | **모든 필드** |
| 폼에 없는 필드 | 그대로 유지 | **`null`로 덮어씀** |

`ItemRepository.save()`가 `id`가 있으면 `em.merge()`로 빠지는 게 이 함정을 그대로 안고 있다.

**해결** — 컨트롤러에서 엔티티를 만들지 않고, 서비스에 **식별자와 변경할 데이터만** 넘긴다. 서비스가 영속 엔티티를 조회해 직접 변경하면 커밋 시점에 변경 감지로 UPDATE가 나간다. **`save()`는 부르지 않는다.**

파라미터가 많아지면 DTO로 묶고, 엔티티에는 `change()` 같은 의미 있는 메서드를 둔다. setter를 밖에서 호출하면 변경 지점이 흩어진다.

### 타임리프

| 문법 | 의미 |
|------|------|
| `th:object` / `th:field="*{name}"` | 폼 객체 지정 / `id`·`name`·`value` 자동 생성 |
| `#fields.hasErrors()` / `th:errors` | 검증 오류 표시 |
| `@{/items/{id}/edit (id=${item.id})}` | URL 표현식 |
| `${T(패키지.OrderStatus).values()}` | SpEL로 enum 상수 전체를 옵션에 |
| `${member.address?.city}` | Safe Navigation — `null`이면 NPE 대신 빈 칸 |

⚠️ **`~{...}`** — 부트 3.x의 타임리프는 프래그먼트 표현식을 `th:replace="~{fragments/header :: header}"`로 감싸야 한다.

💡 **`@Valid` + `BindingResult`** — 오류 시 예외 대신 **입력값이 유지된 채** 폼으로 되돌아간다. 폼 객체를 쓰는 첫 번째 이유.

💡 **조회는 GET, 상태 변경은 POST** — 주문 취소를 `<a href>`로 만들면 크롤러나 브라우저 prefetch가 주문을 취소해버린다.

---

## ✅ 관통하는 원칙 5가지

1. **엔티티는 지연 로딩** — `@ManyToOne`은 기본이 EAGER이므로 반드시 `LAZY`로 바꾼다.
2. **비즈니스 로직은 엔티티에** — 서비스는 조회·위임만 하는 얇은 계층 (도메인 모델 패턴).
3. **생성은 정적 생성 메서드로** — 기본 생성자를 `protected`로 막아 생성 지점을 강제한다.
4. **수정은 변경 감지로** — 영속 엔티티를 조회해 직접 변경. merge는 모든 필드를 덮어쓰므로 쓰지 않는다.
5. **엔티티를 화면·API에 노출하지 마라** — 폼 객체·DTO를 쓴다. 특히 API는 절대.

## 💡 조용히 실패하는 것들

이 강의를 따라가며 겪은 버그는 대부분 **예외도 안 나고 화면도 멀쩡한** 종류였다.

| 증상 | 원인 |
|------|------|
| 값이 화면에 안 뜸 | `th:text`를 `th:test`로 오타 — 타임리프는 모르는 `th:` 속성을 **그냥 무시** |
| 검색해도 결과가 그대로 | `findAll()`이 검색 조건을 받기만 하고 안 씀 |
| 폼에서 고쳐도 저장 안 됨 | `updateItem()`이 `Item` 공통 필드만 다뤄 `author`/`isbn`을 빼먹음 |
| 배송 상태가 `null` | `order()`에서 `setDeliveryStatus(READY)` 누락 |

**화면이 뜬다고 동작하는 게 아니다.** 입력한 값이 실제로 DB에 반영됐는지 p6spy 로그의 SQL로 확인하는 습관이 필요하다.

---

## 📌 다음 (활용 2편)

- **N+1 문제** — `orderList`의 `orderItems[0]`처럼 지연 로딩된 연관을 화면에서 건드리는 순간 발생
- **Querydsl** — 동적 쿼리의 실질적 정답
- **API 개발과 DTO 변환** — 엔티티를 반환하지 않는 실전 API 설계
