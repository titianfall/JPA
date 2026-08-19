package study.datajpa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
@NamedQuery(
        name = "Member.findByUsername",
        query = "select m from Member m where m.username = :username"
)
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

//    protected Member() {}

    public Member(String username){
        this(username, 0);
    }

    public Member(String username, int age){
        this(username, age, null);
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if(team != null){
            changeTeam(team);
        }
    }

    // 연관관계 편의 메서드(db와 객체를 동일하게 만들어줌) - 팀변경 + 회원 추가
    public void changeTeam(Team team){
        this.team = team; // 나의 팀을 변경
        team.getMembers().add(this); // 해당 팀에 나를 추가
    }

}
