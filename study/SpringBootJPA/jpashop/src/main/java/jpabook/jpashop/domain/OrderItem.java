package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    private int orderPrice; // 상품 개당 가격
    private int count; // 주문 수량 (재고 아님!!)

    @JsonIgnore // 양방향 연관관계 — Order 로 되돌아가는 무한 루프 차단
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    // == 생성 메서드 == // - 할인이 구현될수 있기 때문에 orderPrice는
    public static OrderItem createOrderItem(Item item, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);

        // 주문시 재고 감소
        item.removeStock(count);
        return orderItem;
    }
    //== 비즈니스 로직 ==//

    /**
     * 주문 취소에 따른 상품 수량 원상복구
     */
    public void cancel() {
        getItem().addStock(count);
    }

    //== 조회 로직 ==//
    /**
     * 주문 상품 전체 가격 조회
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
