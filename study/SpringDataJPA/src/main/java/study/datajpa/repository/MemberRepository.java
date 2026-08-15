package study.datajpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.datajpa.entity.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 공통으로 만드는 것이 불가능한 예 - 이 하나를 위해 내가 지원하는 모든 기능을 구현하는것이 맞는가?
    List<Member> findByUsername(String username);
}
