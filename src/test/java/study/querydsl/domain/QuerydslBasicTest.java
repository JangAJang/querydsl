package study.querydsl.domain;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
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
import static study.querydsl.domain.QTeam.*;

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

    @Test
    @DisplayName("")
    public void aggregation() throws Exception{
        //given
        List<Tuple> result = query.select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member).fetch();
        //when
        Tuple tuple = result.get(0);
        //then
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    @DisplayName("")
    //팀의 이름과 각 팀의 평균연령
    public void group() throws Exception{
        //given
        List<Tuple> result = query.select(team.name, member.age.avg())
                .from(member)
                .join(member.team)
                .groupBy(team.name)
                .fetch();
        //when
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15L);
        assertThat(teamB.get(member.age.avg())).isEqualTo(35L);
    }

    @Test
    @DisplayName("")
    public void joinTest() throws Exception{
        //given

        //when
        List<Member> result = query.selectFrom(member).leftJoin(member.team, team)
                .where(team.name.eq("teamA")).fetch();
        //then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).extracting("username")
                .containsExactly("member1", "member2");
    }

    @Test
    @DisplayName("")
    public void theta_join() throws Exception{
        //given
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));
        //when
        List<Member> result = query.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        //then
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    @Test
    @DisplayName("")
    public void join_on_filtering() throws Exception{
        //given

        //when
        List<Tuple> result = query.select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        //then
        for (Tuple tuple : result) {
            System.out.println(tuple.toString());
        }
    }
}
