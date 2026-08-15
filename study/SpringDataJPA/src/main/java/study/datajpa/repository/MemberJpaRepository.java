package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

// 순수 JPA 사용 클래스 - 변경감지를 사용하여 데이터를 수정하는 방식
@Repository
public class MemberJpaRepository {

    @PersistenceContext
    private EntityManager em;
    // 회원 저장 및 삭제
    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    // 변경감지로 대체
    // public void update(Member member) {...}

    public void delete(Member member) {
        em.remove(member);
    }

    // jpql을 사용하여 회원 전체 조회
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    // 단건 회원 조회(Optional)
    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member); // member가 (null)없을수도 있다.
    }

    // 단건 인원수 조회(count(*))
    public long count() {
        return em.createQuery("select count(m) from Member m", Long.class)
                .getSingleResult();
    }

    // 회원 단건 조회
    public Member find(Long id) {
        return em.find(Member.class, id);
    }
}
