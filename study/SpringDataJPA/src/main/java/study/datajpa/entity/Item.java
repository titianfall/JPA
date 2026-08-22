package study.datajpa.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 식별자를 @GeneratedValue 없이 직접 할당하는 엔티티.
 *
 * SimpleJpaRepository.save() 는 isNew() 가 true 면 persist, false 면 merge 를 호출한다.
 * 기본 전략(식별자가 null / 0 이면 새 엔티티)만 믿으면 여기서는 id 가 이미 채워져 있으므로
 * 항상 merge -> 불필요한 select 가 한 번 더 나간다.
 * 그래서 Persistable 을 직접 구현해서 "새 엔티티" 판단 기준을 @CreatedDate 로 바꾼다.
 */
@Entity
@EntityListeners(AuditingEntityListener.class) // BaseEntity 를 상속하지 않으므로 직접 붙여야 @CreatedDate 가 동작한다
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item implements Persistable<String> {

    @Id
    private String id;

    @CreatedDate
    private LocalDateTime createdDate;

    public Item(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return createdDate == null; // persist 전에는 아직 Auditing 이 값을 채우지 않았으므로 null
    }
}
