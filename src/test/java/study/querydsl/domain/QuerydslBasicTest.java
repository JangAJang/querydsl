package study.querydsl.domain;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    private EntityManager em;

    @BeforeEach
    public void before(){
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
    }
    
    @Test
    @DisplayName("")
    public void startJPQL() throws Exception{
        //given
        //when
        Member find = em.createQuery("select m from Member m where m.username = :name", Member.class)
                .setParameter("name", "member1").getSingleResult();
        //then
        assertThat(find.getAge()).isEqualTo(10);
        assertThat(find.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("")
    public void startQuerydsl() throws Exception{
        //given
        JPAQueryFactory query = new JPAQueryFactory(em);
        QMember m = new QMember("m");
        //when
        Member find = query.select(m).from(m).where(m.username.eq("member1")).fetchOne();
        //then
        assertThat(find.getUsername()).isEqualTo("member1");
    }
}
