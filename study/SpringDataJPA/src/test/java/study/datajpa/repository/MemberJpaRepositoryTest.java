package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
// @Rollback(false) // DB를 눈으로 확인할 때만 잠깐 켠다. 켜두면 데이터가 쌓여서 개수 검증이 깨진다.
class MemberJpaRepositoryTest {

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Autowired
    TeamJpaRepository teamJpaRepository;

    @Test
    public void testMember() {
        // given
        Member member = new Member("memberA");
        Member saved = memberJpaRepository.save(member);

        // when
        Member findMember = memberJpaRepository.find(saved.getId());

        // then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        // 단건 조회 검증
        Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
        Member findMember2 = memberJpaRepository.findById(member2.getId()).get();

        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        // 리스트 조회 검증
        List<Member> all = memberJpaRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        // 카운트 검증
        long count = memberJpaRepository.count();
        assertThat(count).isEqualTo(2);

        // 삭제 검증
        memberJpaRepository.delete(member1);
        memberJpaRepository.delete(member2);

        long deletedCount = memberJpaRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    // JPA @NamedQuery 직접 사용
    @Test
    public void findByUsername() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        List<Member> findMembers = memberJpaRepository.findByUsername("member1");
        Member member = findMembers.get(0);

        assertThat(member.getUsername()).isEqualTo(member1.getUsername());
    }

    // @Query
    @Test
    public void findUser() {
        Member member1 = new Member("member1", 10);
        memberJpaRepository.save(member1);

        List<Member> user = memberJpaRepository.findUser(member1.getUsername(), member1.getAge());
        Member member2 = user.get(0);
        assertThat(member2.getUsername()).isEqualTo(member1.getUsername());
    }

    // @Query, 값, DTO 조회
    @Test
    public void findUsernameList() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);
        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        List<String> usernameList = memberJpaRepository.findUsernameList();
        assertThat(usernameList.size()).isEqualTo(2);
    }

    @Test
    public void findMemberDto() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamJpaRepository.save(teamA);
        teamJpaRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        List<MemberDto> memberDto = memberJpaRepository.findMemberDto();
        assertThat(memberDto.size()).isEqualTo(2);
    }

    @Test
    public void findMembers() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        Member member = memberJpaRepository.findMembers(member1.getUsername());
        assertThat(member.getUsername()).isEqualTo(member1.getUsername());
    }

    // 컬렉션 파라미터 바인딩
    @Test
    public void findByNames() {
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);

        List<Member> byNames = memberJpaRepository.findByNames();
        assertThat(byNames.size()).isEqualTo(2);
    }

    @Test
    public void findByPage() throws Exception{
        Member member1 = new Member("member1", 10);
        Member member2 = new Member("member2", 10);
        Member member3 = new Member("member3", 10);
        Member member4 = new Member("member4", 10);
        Member member5 = new Member("member5", 10);

        memberJpaRepository.save(member1);
        memberJpaRepository.save(member2);
        memberJpaRepository.save(member3);
        memberJpaRepository.save(member4);
        memberJpaRepository.save(member5);

        List<Member> members = memberJpaRepository.findByPage(10, 0, 3);
        long totalCount = memberJpaRepository.totalCount(10);

        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);
    }

    // 메소드 이름으로 쿼리 생성
    @Test
    public void findByUsernameAndAgeGreaterThan() {
        memberJpaRepository.save(new Member("AAA", 10));
        memberJpaRepository.save(new Member("AAA", 20));

        List<Member> result = memberJpaRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
    }

    // 벌크성 수정 쿼리
    @Test
    public void bulkUpdate() {
        memberJpaRepository.save(new Member("member1", 10));
        memberJpaRepository.save(new Member("member2", 19));
        memberJpaRepository.save(new Member("member3", 20));
        memberJpaRepository.save(new Member("member4", 21));
        memberJpaRepository.save(new Member("member5", 40));

        int resultCount = memberJpaRepository.bulkAgePlus(20);

        assertThat(resultCount).isEqualTo(3); // 20, 21, 40
    }
}
