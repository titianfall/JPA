package jpabook.jpashop.api;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /** v1: 엔티티 직접 노출
     * 3장의 simple-orders와 달리 컬렉션(Order → OrderItem)까지 딸려 나온다.
     *
     * 1. 지연 로딩 프록시는 Jackson이 직렬화하지 못하므로 전부 강제 초기화해야 한다.
     *   - xToOne(member, delivery)뿐 아니라 컬렉션 orderItems, 그 안의 item까지 건드려야 한다.
     * 2. 양방향 연관관계는 한쪽에 @JsonIgnore를 걸어 무한 루프를 끊어야 한다.
     *
     * 쿼리: order 1 + member N + delivery N + orderItem N + item M (최악의 경우)
     *
     * 엔티티가 그대로 API 스펙이 되므로 절대 사용하면 안 된다.
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        for (Order order : orders) {
            // 강제 초기화 Member, Delivey
            order.getMember().getName();
            order.getDelivery().getAddress();

            // 강제 초기화 OrderItem, Order
            List<OrderItem> orderItems = order.getOrderItems();
//            for (OrderItem orderItem : orderItems) {
//                orderItem.getItem().getName();
//            }
            orderItems.stream().forEach(orderItem -> orderItem.getItem().getName());
        }
        return orders;
    }

    /** v2: 엔티티를 DTO로 변환 (fetch join 미사용)
     * API 스펙 문제는 해결되지만 성능 문제는 그대로다.
     *
     * ⚠️ OrderDto 안의 List<OrderItem>을 그대로 두면 안 된다.
     *    엔티티가 DTO에 껍데기만 씌워진 채 그대로 노출되므로 OrderItemDto로 한 번 더 감싼다.
     *
     * 쿼리: order 1 + member N + delivery N + orderItem N + item M (v1과 동일)
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(toList());

        return collect;
    }

    @Getter
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        // private List<OrderItem> orderItems; // [] 형식이 아닌가?
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderStatus();

            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());

//            order.getOrderItems().stream().forEach(o -> o.getItem().getName());
//            orderItems = order.getOrderItems(); // 객체가 직접적으로 응답으로 노출되는 안좋은 케이스이다. >> DTO 반환
        }
    }

    @Getter
    static class OrderItemDto {
//        private Long orderItemId;
        private String itemName;
        private int itemPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            itemPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    /** v3: 엔티티를 DTO로 변환 - 컬렉션 페치 조인 최적화
     * 컬렉션까지 join fetch로 끌어와 쿼리를 1번으로 줄인다.
     *
     * ⚠️ 1:N 조인이라 DB row가 뻥튀기되고, 같은 Order가 OrderItem 수만큼 중복된다.
     *    JPQL의 distinct가 같은 식별자의 엔티티를 애플리케이션에서 걸러준다.
     *    (아래 println으로 중복 제거 후 같은 참조를 쓰는지 확인)
     *
     * ⚠️ 치명적 한계 — 페이징 불가.
     *    setFirstResult/setMaxResults를 걸면 하이버네이트가 전체를 메모리로 퍼올린 뒤 페이징한다.
     *    데이터가 많으면 OutOfMemory. 그래서 주석으로만 남겨 뒀다.
     *
     * ⚠️ 컬렉션 페치 조인은 1개만 가능하다. 둘 이상이면 N*M 곱집합이 되어 데이터 정합성이 깨진다.
     *
     * 쿼리: 1번
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();// fetchJoin > 일대다로 인한 데이터 중복발생
        for (Order order : orders) {
            System.out.println("order ref = " + order + " id = " + order.getId());
        }
        List<OrderDto> collect = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(toList());

        return collect;
    }

    /** v3.1: 엔티티를 DTO로 변환 - 페이징과 한계 돌파 (실무 권장)
     * 컬렉션 fetch join시 paging 불가 > 최악의 경우 장애(out of memeory)
     *
     * !한계 돌파!
     * 1. ToOne 관계를 모두 fetch join합니다. (ToOne은 row 수를 늘리지 않으므로 페이징에 영향 없음)
     * 2. 컬렉션은 LAZY 로딩으로 조회합니다.
     * 3. 지연 로딩 성능 최적화
     *   - hibernate.default_batch_fetch_size(글로벌) > 1 + n + m 에서 1 + 1 + 1로 최적화가 가능합니다.
     *   - @BatchSize (개별 최적화) 를 적용합니다.
     *
     * v3와 비교했을 때의 장점
     * - 쿼리 수는 1번 더 많지만, 중복 row가 없어 DB 데이터 전송량이 오히려 적다.
     * - 페이징이 가능하다.
     *
     * @Param("offset") 페이징 쿼리 시작점
     * @Param("limit") 페이징 쿼리 끝점
     *
     * @return 주문 정보(Order(Member + Delivery + OrderItems(OrderItem... + Item...))
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit)
    {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // 페이징 영향 x

        List<OrderDto> collect = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(toList());

        return collect;
    }

    /** v4: JPA에서 DTO 직접 조회
     * 컬렉션이 있을때의 DTO 변환 및 반환
     *
     * 단 조회마다 N + 1 문제가 2번이나 발생한다.
     *
     * row 수가 늘지 않는 ToOne(member, delivery)은 처음부터 join으로 함께 조회하고,
     * row 수를 늘리는 ToMany(orderItems)는 주문 건마다 별도 쿼리로 채운다.
     *
     * 쿼리: 루트 1번 + 컬렉션 N번 (1 + N)
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /** v5: JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화
     * v4의 1 + N을 1 + 1로 줄인다.
     *
     * 1. 루트(ToOne)를 먼저 조회해 orderId 목록을 뽑는다.
     * 2. 그 orderId들을 IN 절 한 방으로 던져 OrderItem을 전부 가져온다.
     * 3. orderId를 key로 Map을 만들어 메모리에서 매칭한다. (조회 O(1))
     *
     * 주문이 1000건이면 v4는 1 + 1000번, v5는 1 + 1번. 실무에서 체감 차이가 크다.
     *
     * 쿼리: 루트 1번 + 컬렉션 1번 (1 + 1)
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> orderV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /** v6: JPA에서  DTO로 직접 조회, 플랫 데이터 최적화
     * OrderFlatDto로 반환하게 되면 페이징이 가능은 하나 중복된 데이터를 페이징하게 되어 원치않은 결과가 나올수있다.
     * OrderQueryDto로 응답 형식을 일괄적으로 맞추고 싶을경우에는 중복을 거르는 긴 수작업 코드가 필요하다.
     *
     * 조인 결과를 한 줄짜리 flat 데이터로 한 번에 받아온 뒤, 애플리케이션에서 groupingBy로 다시 묶는다.
     * groupingBy의 key가 OrderQueryDto이므로 @EqualsAndHashCode(of = "orderId")가 반드시 필요하다.
     * (orderId 기준으로 같은 주문임을 판단해야 컬렉션이 하나로 합쳐진다)
     *
     * 단점
     * - 쿼리는 1번이지만 조인으로 중복 row가 그대로 넘어와 v5보다 더 느릴 수도 있다.
     * - Order 기준 페이징이 불가능하다.
     * - 위 스트림처럼 애플리케이션 추가 작업이 크다.
     *
     * 쿼리: 1번
     */
     @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> orderV6() {
         List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

         return flats.stream()
                 .collect(groupingBy(o -> new OrderQueryDto(
                                                                 o.getOrderId(),
                                                                 o.getName(),
                                                                 o.getOrderDate(),
                                                                 o.getOrderStatus(),
                                                                 o.getAddress()
                                 ), mapping(o -> new OrderItemQueryDto(
                                                                     o.getOrderId(),
                                                                     o.getItemName(),
                                                                     o.getOrderPrice(),
                                                                     o.getCount()), toList()
                                        ) // end new OrderitemQueryDto()
                                )// end groupingBy()
                 ).entrySet().stream()
                 .map(e -> new OrderQueryDto(
                         e.getKey().getOrderId(),
                         e.getKey().getName(),
                         e.getKey().getOrderDate(),
                         e.getKey().getOrderStatus(),
                         e.getKey().getAddress(),
                         e.getValue()
                        ) // end new OrderQueryDto()
                 ).collect(toList());
     }
}
