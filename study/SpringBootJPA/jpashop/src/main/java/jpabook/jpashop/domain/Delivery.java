package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "DELIVERY_ID")
    private Long id;

    @Embedded
    private Address address;

    @Enumerated(value = EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @JsonIgnore // 양방향 연관관계 — Order 로 되돌아가는 무한 루프 차단
    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;
}
