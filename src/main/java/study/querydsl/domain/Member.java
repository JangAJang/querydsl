package study.querydsl.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"username", "age"})
public class Member {

    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private int age;
    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public void changeUsername(String username){
        this.username = username;
    }

    public void changeTeam(Team team){
        if(this.team != null) this.team.getMembers().remove(this);
        this.team = team;
        team.getMembers().add(this);
    }

    public void increaseAge(int age){
        this.age += age;
    }
}
