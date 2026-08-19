package study.datajpa.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    // 공통으로 만드는 것이 불가능한 예 - 이 하나를 위해 내가 지원하는 모든 기능을 구현하는것이 맞는가?
    List<Member> findByUsername(@Param("username") String username);

    // @Query
    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    // Query, Value, DTO
    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name)" +
            " from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username = :username")
    Member findMembers(@Param("username") String username);

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    Page<Member> findByAge(int age, Pageable pageable);

    @Query(value = "select m from Member m",
            countQuery = "select count(m.username) from Member m")
    Page<Member> findmemberAllCountBy(Pageable pageable);

    // 메소드 이름으로 쿼리 생성 - 순수 JPA 8줄이 선언 한 줄로 끝난다
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    // count 쿼리 없음. limit + 1 을 조회해서 다음 페이지 존재만 판단한다
    Slice<Member> findSliceByAge(int age, Pageable pageable);

    // count 쿼리 없음. 결과만
    List<Member> findListByAge(int age, Pageable pageable);

    // 벌크성 수정 쿼리 - @Modifying 없으면 QueryExecutionRequestException
    // clearAutomatically = true : 벌크 실행 직후 영속성 컨텍스트 초기화 (기본값 false)
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    // N+1 해결 ① JPQL 페치 조인
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    // N+1 해결 ② @EntityGraph - 페치 조인의 간편 버전 (LEFT OUTER JOIN)
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    // 메서드 이름으로 쿼리 + 엔티티 그래프 (제일 편하다)
    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(String username);

    // 읽기 전용 힌트 - 변경 감지용 스냅샷을 만들지 않아 UPDATE 가 안 나간다
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String username);
}
