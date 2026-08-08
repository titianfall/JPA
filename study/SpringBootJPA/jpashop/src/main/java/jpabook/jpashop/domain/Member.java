package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter // 단 @Setter는 필요한 경우에만 작접 선언하여 사용하는 것이 올바르다.
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    // entity를 외부 api로 노출하면 안되는 2가지이유
    // if) 필요에 의해 엔티티에 password를 추가하였다.
    // 이때문에 1. 패스워드 노출, 2. api 스펙 변경 두가지 오류가 발생한다.
    // private String password;

    // @NotEmpty // 요구되는 스펙이 다를경우 문제가 됨 (null 허용 및 미허용)
    private String name;

    @Embedded
    private Address address;

    @JsonIgnore // json 변환에서 제외해줌
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
