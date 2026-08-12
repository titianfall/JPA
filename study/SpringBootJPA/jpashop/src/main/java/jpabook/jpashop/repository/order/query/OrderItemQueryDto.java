package jpabook.jpashop.repository.order.query;

import lombok.Data;

@Data
public class OrderItemQueryDto {
    // ⚠️ 생성자에 들어오는 값은 oi.order.id — 상품 ID가 아니라 주문 ID다.
    //    v5에서 orderId 기준으로 그룹핑(Map의 key)하기 위해 필요하다.
    private Long orderId;
    private String itemName;
    private int orderPrice;
    private int count;

    public OrderItemQueryDto(Long orderId, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }
}
