package study.querydsl.domain;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.domain.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    private EntityManager em;

    private JPAQueryFactory query;

    @BeforeEach
    public void before(){
        query = new JPAQueryFactory(em);
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
        QMember m1 = new QMember("m1");
        //when
        Member find = query
                .select(m1).from(m1)
                .where(m1.username.eq("member1"))
                .fetchOne();
        //then
        assertThat(find.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("")
    public void search() throws Exception{
        //given

        //when
        Member findMember = query.selectFrom(member)
                .where(member.age.eq(10), (member.username.eq("member1")))
                .fetchOne();
        //then
        assertThat(findMember.getUsername()).isEqualTo("member1");
        assertThat(findMember.getAge()).isEqualTo(10);
    }
    
    @Test
    @DisplayName("")        
    public void resultFetch() throws Exception{
        //given
//        List<Member> fetch = query.selectFrom(member).fetch();
//
//        Member fetchOne = query.selectFrom(member).fetchOne();
//
//        Member fetchFirst = query.select(member).fetchFirst();

        // 하나의 코드로 쿼리 두개가 실행된다. (전체 조회, 개수 조회)
        QueryResults<Member> fetchResults = query.selectFrom(member).fetchResults();
        fetchResults.getTotal();
        fetchResults.getResults();

        long count = query.selectFrom(member).fetchCount();
        //when
        
        //then
        System.out.println(fetchResults.getTotal());
        System.out.println(count);
        assertThat(fetchResults.getTotal()).isEqualTo(count);
    }

    @Test
    @DisplayName("")        
    public void sort() throws Exception{
        //given
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
        em.flush();
        em.clear();
        //when
        List<Member> result = query.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast()).fetch();

        //then
        assertThat(result.get(0).getUsername()).isEqualTo("member5");
        assertThat(result.get(1).getUsername()).isEqualTo("member6");
        assertThat(result.get(2).getUsername()).isNull();
    }

    @Test
    @DisplayName("")
    public void paging1() throws Exception{
        //given

        //when
        List<Member> members = query.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        //then
        assertThat(members.size()).isEqualTo(2);

    }
}
