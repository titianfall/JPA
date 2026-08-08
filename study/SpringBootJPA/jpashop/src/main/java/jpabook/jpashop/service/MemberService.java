package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 스프링에서 지원해주는 Transactional 이 기능이 다양함

import java.util.List;

// MemberRepository에 Delegation하는 클래스
@Service
@Transactional(readOnly = true) // jpa 모든 데이터 변경이나 어떤 로직들은 가급적이면 트랜잭션 안에서 다 실행 되어야한다.
// @AllArgsConstructor // 모든 필드를 가지고 생성자를 만들어줌
@RequiredArgsConstructor // final로 선언된 필드를 가지고 생성자를 만들어줌
public class MemberService {
    // 변경할 일이 없기 때문에 + 초기화 안할경우 compile 시점에 에러
    private final MemberRepository memberRepository;
//    @Autowired // Field Injection
//    private MemberRepository memberRepository;

//    @Autowired // Setter Injection
//    public void setMemberRepository(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }

//    @Autowired // Constructor Injection (생략 가능 - 생성자가 1개일 경우)
//    public MemberService(MemberRepository memberRepository) {
//        this.memberRepository = memberRepository;
//    }
    /**
     * 회원 가입
     * 1. 종복 회원 검사
     * 2. 회원 등록
     */
    @Transactional // 쓰기에서는 readOnly 옵션을 꺼준다.
    public Long join(Member member) {
        validateDuplicateMember(member); // 중복 회원 검증 로직
        memberRepository.save(member);
        return member.getId(); // CQS
    }

    private void validateDuplicateMember(Member member) {
        // EXCEPTION
        List<Member> findMember = memberRepository.findByName(member.getName());
        if(!findMember.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }

    }

    // 조회하는 곳에서는 Transactional(readOnly = true)를 넣어주면 내부적으로 최적화 해준다.
    // 회원 전체 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    // 회원 단건 조회
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    // 회원 이름 업데이트 Put(/api/v2/members/{id})
    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);
        // 변경감지 사용 <> return member;
        // member를 return 해도 좋지만 영속 상태가 끊기기 때문에 유의해야한다.
        // id만 반환해서 다시 찾아서 쓸수있도록 하는것도 괜찮다.
    }
}
