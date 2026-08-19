package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import study.datajpa.dto.MemberDto;
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

    // @NamedQuery
    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    // @Query
    public List<Member> findUser(String username, int age) {
        return em.createQuery("select m from Member m where m.username = :username and m.age = :age", Member.class)
                .setParameter("username", username)
                .setParameter("age", age)
                .getResultList();
    }

    // @Query, Value, DTO
    public List<String> findUsernameList() {
        return em.createQuery("select m.username from Member m", String.class)
                .getResultList();
    }

    public List<MemberDto> findMemberDto() {
        return em.createQuery("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name)" +
                        " from Member m join m.team t", MemberDto.class)
                .getResultList();
    }

    // 파라미터 바인딩
    public Member findMembers(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getSingleResult();
    }

    // 컬렉션 파라미터 바인딩
    public List<Member> findByNames() {
        return em.createQuery("select m from Member m where m.username in :names", Member.class)
                .setParameter("names", findUsernameList())
                .getResultList();
    }

    // 반환 타입
    public List<Member> findByPage(int age, int offset, int limit) {
        return em.createQuery("select m from Member m where m.age = :age order by m.username desc",  Member.class)
                .setParameter("age", age)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long totalCount(int age) {
        return em.createQuery("select count(*) from Member m where m.age = :age", Long.class)
                .setParameter("age", age)
                .getSingleResult();
    }

    // 메소드 이름으로 쿼리 생성 - 스프링 데이터 JPA 는 이 8줄이 선언 한 줄로 끝난다
    public List<Member> findByUsernameAndAgeGreaterThan(String username, int age) {
        return em.createQuery("select m from Member m where m.username = :username and m.age > :age", Member.class)
                .setParameter("username", username)
                .setParameter("age", age)
                .getResultList();
    }

    // 벌크성 수정 쿼리 - 영속성 컨텍스트를 무시하고 DB 에 직접 UPDATE 를 날린다
    public int bulkAgePlus(int age) {
        return em.createQuery("update Member m set m.age = m.age + 1 where m.age >= :age")
                .setParameter("age", age)
                .executeUpdate(); // 영향받은 행 수
    }
}
