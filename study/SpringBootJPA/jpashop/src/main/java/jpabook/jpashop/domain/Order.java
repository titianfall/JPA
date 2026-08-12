package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 주문을 취소하면 배송이 취소되므로 주문을 주인관계로 설정하였다.
    // Order 시에 Delivery 자동 persist 설정
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    // Order에 저장 > OrderItem에 자동저장(persist 생략)
    @BatchSize(size = 100) // ToMany는 필드에 추가
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    private LocalDateTime orderDate;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus orderStatus; // order or cancel

    // === 연관관계 편의 메서드 ===
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery){
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    // == 생성 메서드 == // - 앞으로 생성시에도 해당 부분만 수정하면 된다.
//    protected Order() {
//
//    }
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setOrderStatus(OrderStatus.ORDER); // 첫 주문은 주문 상태로 강제
        order.setOrderDate(LocalDateTime.now());

        return order;
    }

    // == 비즈니스 로직 == //
    /**
     * 주문 취소
     * 1. 배송이 완료되었을 경우에는 주문 취소가 불가능하다.
     * 2. 주문 취소에 따른 주문 개수 원상복구
     */
    public void cancel() {
        if(delivery.getDeliveryStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setOrderStatus(OrderStatus.CANCEL);
        for(OrderItem orderItem : orderItems) {
            orderItem.cancel(); // 상품 개수 원상 복구
        }
    }

    // == 조회 로직 == //
    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
        // return orderItems.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }
}
