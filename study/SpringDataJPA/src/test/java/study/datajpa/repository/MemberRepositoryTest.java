package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
// @Rollback(false) // DB를 눈으로 확인할 때만 잠깐 켠다. 켜두면 데이터가 쌓여서 개수 검증이 깨진다.
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void testMember() {
        
        // memberRepository: class jdk.proxy1.$Proxy149
        System.out.println("memberRepository: " + memberRepository.getClass());


        Member member = new Member("memberA");
        Member saved = memberRepository.save(member);

        Optional<Member> byId = memberRepository.findById(saved.getId());
        Member findMember = byId.get();

        assertThat(findMember.getId()).isEqualTo(saved.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    // @NamedQuery
    @Test
    public void findByUsername() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> member3 = memberRepository.findByUsername("member1");
        Member member = member3.get(0);

        assertThat(member.getUsername()).isEqualTo(member1.getUsername());
    }

    @Test
    public void findUser() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> user = memberRepository.findUser(member1.getUsername(), member1.getAge());
        Member member = user.get(0);

        assertThat(member.getUsername()).isEqualTo(member1.getUsername());
    }

    @Test
    public void findUsernameList() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);
        memberRepository.save(member1);
        memberRepository.save(member2);

        List<String> usernameList = memberRepository.findUsernameList();
        assertThat(usernameList.size()).isEqualTo(2);
    }

    @Test
    public void findMemberDto() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);

        memberRepository.save(member1);
        memberRepository.save(member2);
        List<MemberDto> memberDto = memberRepository.findMemberDto();
        MemberDto memberDto1 = memberDto.get(0);
        MemberDto memberDto2 = memberDto.get(1);
        assertThat(memberDto1.getId()).isEqualTo(member1.getId());
        assertThat(memberDto2.getId()).isEqualTo(member2.getId());
    }

    @Test
    public void findMembers() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);

        memberRepository.save(member1);
        memberRepository.save(member2);

        Member members = memberRepository.findMembers("member1");
        assertThat(members.getId()).isEqualTo(member1.getId());
    }

    // 컬렉션 파라미터 바인딩
    @Test
    public void findByNames() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);

        memberRepository.save(member1);
        memberRepository.save(member2);

        List<Member> byNames = memberRepository.findByNames(memberRepository.findUsernameList());
        assertThat(byNames.size()).isEqualTo(2);
    }

    @Test
    public void findByAge() throws Exception {
        // given
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);
        Member member3 = new Member("member3", 10);
        Member member4 = new Member("member4", 10);
        Member member5 = new Member("member5", 10);

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
        memberRepository.save(member5);

        // when
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> page = memberRepository.findByAge(10, pageRequest);
        Page<MemberDto> dtoPage = page.map(m -> new MemberDto(m.getId(), m.getUsername(), null));
        List<Member> content = page.getContent();

        // then
        assertThat(content.size()).isEqualTo(3); // 조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5); // 전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); // 페이지 번호 수
        assertThat(page.getTotalPages()).isEqualTo(2); // 전체 페이지 번호
        assertThat(page.isFirst()).isTrue(); // 첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는가?
    }

    @Test
    public void findMemberAllCountBy() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);

        memberRepository.save(member1);
        memberRepository.save(member2);

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> members = memberRepository.findmemberAllCountBy(pageRequest);
        List<Member> content = members.getContent();

        assertThat(content.size()).isEqualTo(2);
        assertThat(members.getTotalElements()).isEqualTo(2);
        assertThat(members.getNumber()).isEqualTo(0);
        assertThat(members.getTotalPages()).isEqualTo(1);
        assertThat(members.isFirst()).isTrue();
        assertThat(members.hasNext()).isFalse();
    }

    // 메소드 이름으로 쿼리 생성
    @Test
    public void findByUsernameAndAgeGreaterThan() {
        memberRepository.save(new Member("AAA", 10));
        memberRepository.save(new Member("AAA", 20));

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }

    // Slice / List : count 쿼리를 날리지 않는다
    @Test
    public void sliceAndList() {
        for (int i = 1; i <= 5; i++) {
            memberRepository.save(new Member("member" + i, 10));
        }
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Slice<Member> slice = memberRepository.findSliceByAge(10, pageRequest);
        List<Member> list = memberRepository.findListByAge(10, pageRequest);

        assertThat(slice.getContent().size()).isEqualTo(3);
        assertThat(slice.hasNext()).isTrue(); // limit + 1 로 다음 페이지 존재만 판단
        assertThat(list.size()).isEqualTo(3);
    }

    // 벌크성 수정 쿼리
    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        Member member5 = memberRepository.save(new Member("member5", 40));

        int resultCount = memberRepository.bulkAgePlus(20);

        assertThat(resultCount).isEqualTo(3);

        // clearAutomatically = true 라 영속성 컨텍스트가 비워졌고, DB 에서 다시 읽어온다.
        // 이 옵션이 없으면 1차 캐시의 과거 값(40)이 그대로 나온다.
        Member found = memberRepository.findById(member5.getId()).get();
        assertThat(found.getAge()).isEqualTo(41);
    }

    // N+1 과 @EntityGraph
    @Test
    public void findMemberLazy() {
        Team teamA = teamRepository.save(new Team("teamA"));
        Team teamB = teamRepository.save(new Team("teamB"));
        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member2", 20, teamB));

        em.flush();
        em.clear();

        // findAll 은 @EntityGraph(attributePaths = {"team"}) 로 오버라이드해 뒀다 -> 쿼리 1번
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            assertThat(Hibernate.isInitialized(member.getTeam())).isTrue(); // 프록시가 아니라 이미 로딩됨
        }

        em.clear();
        assertThat(memberRepository.findMemberFetchJoin().size()).isEqualTo(2);
        em.clear();
        assertThat(memberRepository.findMemberEntityGraph().size()).isEqualTo(2);
        em.clear();
        assertThat(memberRepository.findEntityGraphByUsername("member1").size()).isEqualTo(1);
    }

    // 쿼리 힌트 - readOnly 라 변경 감지가 동작하지 않는다
    @Test
    public void queryHint() {
        memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        Member member = memberRepository.findReadOnlyByUsername("member1");
        member.setUsername("member2");
        em.flush(); // UPDATE 쿼리가 나가지 않는다

        em.clear();
        assertThat(memberRepository.findReadOnlyByUsername("member1")).isNotNull();
    }

    @Test
    public void lock() {
        memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        List<Member> result = memberRepository.findLockByUsername("member1"); // select ... for update
        assertThat(result.size()).isEqualTo(1);
    }
}
