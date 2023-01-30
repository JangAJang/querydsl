package study.querydsl.domain;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberTest {
    
    @Autowired
    EntityManager em;
    
    @Test
    @DisplayName("")
    public void testEntity() throws Exception{
        //given
        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");
        em.persist(team1);
        em.persist(team2);

        Member member1 = new Member("member1", 10, team1);
        Member member2 = new Member("member2", 20, team1);
        Member member3 = new Member("member3", 30, team2);
        Member member4 = new Member("member4", 40, team2);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();
        //when
        List<Member> result = em.createQuery("select m from Member m", Member.class).getResultList();
        //then
        assertThat(result.get(0).toString()).isEqualTo(member1.toString());
        assertThat(result.get(1).toString()).isEqualTo(member2.toString());
        assertThat(result.get(2).toString()).isEqualTo(member3.toString());
        assertThat(result.get(3).toString()).isEqualTo(member4.toString());
    }
}
