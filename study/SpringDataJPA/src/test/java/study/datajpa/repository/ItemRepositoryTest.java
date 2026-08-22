package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Item;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @PersistenceContext
    private EntityManager em;

    /**
     * Item 은 @GeneratedValue 없이 id 를 직접 할당한다.
     * Persistable 을 구현하지 않았다면 id 가 이미 있으므로 merge 가 호출되고,
     * merge 는 DB 를 먼저 select 해서 존재 여부를 확인한다(select 1번 + insert 1번).
     */
    @Test
    public void save() {
        Item item = new Item("ItemA");

        Item saved = itemRepository.save(item);

        // persist 는 넘긴 인스턴스를 그대로 영속화한다. merge 였다면 "복사본"이 돌아와서 다른 객체가 된다.
        assertThat(saved).isSameAs(item);
        assertThat(em.contains(item)).isTrue();
        // @CreatedDate 는 persist 직전(@PrePersist)에 채워지므로 저장 후에는 값이 있다.
        assertThat(saved.getCreatedDate()).isNotNull();
    }

    /**
     * isNew() 판단 기준이 createdDate 라는 것을 뒤집어서 확인한다.
     * 이미 저장돼서 createdDate 가 채워진 엔티티는 더 이상 새 엔티티가 아니다.
     */
    @Test
    public void isNew() {
        Item item = new Item("ItemB");
        assertThat(item.isNew()).isTrue(); // 저장 전 - createdDate == null

        itemRepository.save(item);
        em.flush();

        assertThat(item.isNew()).isFalse(); // 저장 후 - createdDate 채워짐 -> 다음 save 는 merge
    }
}
