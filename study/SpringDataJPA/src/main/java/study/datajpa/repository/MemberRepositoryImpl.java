package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import study.datajpa.entity.Member;

import java.util.List;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final EntityManager em;
    
    @Override
    public List<Member> findMemberCustom() {
        return List.of();
    }
}
