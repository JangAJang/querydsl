package study.querydsl.domain;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.UserDto;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.domain.QMember.*;
import static study.querydsl.domain.QTeam.*;

@SpringBootTest
@Transactional
@Commit
public class QuerydslBasicTest {

    @Autowired
    private EntityManagerFactory emf;

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

        // ????????? ????????? ?????? ????????? ????????????. (?????? ??????, ?????? ??????)
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
    //?????? ????????? ??? ?????? ????????????
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

    @Test
    @DisplayName("")
    public void fetchJoin() throws Exception{
        //given
        em.flush();
        em.clear();

        //when
        Member findMember = query.selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        //then
        assertThat(emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam())).isTrue();
    }

    @Test
    @DisplayName("")
    public void subQuery() throws Exception{
        //given
        QMember memberSub = new QMember("memberSub");

        //when
        List<Member> fetch = query.selectFrom(member)
                .where(member.age.eq(//??????????????? ???????????? JPAExpressions.?????? ?????? ??????
                        select(memberSub.age.max())
                        .from(memberSub)
                )).fetch();

        //then
        assertThat(fetch).extracting("age").containsExactly(40);
    }

    @Test
    @DisplayName("")
    public void ?????????_??????_?????????_????????????_????????????() throws Exception{
        //given
        QMember memberSub = new QMember("memberSub");
        //when
        List<Member> moreThanAvg = query.selectFrom(member)
                .where(member.age.goe(select(memberSub.age.avg()).from(memberSub))).fetch();
        double avg = query.select(member.age.avg()).from(member).fetchOne();
        System.out.println("average = " + avg);
        //then
        for (Member member1 : moreThanAvg) {
            System.out.println(member1.getAge());
            assertThat((double)member1.getAge()).isGreaterThanOrEqualTo(avg);
        }
    }

    @Test
    @DisplayName("")
    public void ????????????_in() throws Exception{
        //given
        QMember sub = new QMember("sub");
        //when
        List<Member> result = query.selectFrom(member)
                .where(member.age.in(select(sub.age)
                        .from(sub)
                        .where(sub.age.gt(10))
                )).fetch();
        //then
        assertThat(result).extracting("age").containsExactly(20, 30, 40);
    }

    @Test
    @DisplayName("")
    public void ????????????_select() throws Exception{
        //given
        QMember sub = new QMember("sub");
        //when
        List<Tuple> result = query
                .select(member.username, member.age.divide(select(sub.age.avg()).from(sub)).multiply(100).as(member.age))
                .from(member)
                .fetch();
        //then
        for (Tuple tuple : result) {
            System.out.println(tuple.get(member.username) + ", " + tuple.get(member.age) + "%");
        }
    }

    @Test
    @DisplayName("")
    public void basicCase() throws Exception{
        //given

        //when
        List<String> fetch = query.select(member.age
                        .when(10).then("??????")
                        .when(20).then("?????????")
                        .otherwise("?????????"))
                .from(member)
                .fetch();
        //then
        assertThat(fetch).containsExactly("??????", "?????????", "?????????", "?????????");
    }
    
    @Test
    @DisplayName("")        
    public void complexCase() throws Exception{
        //given
        
        //when
        List<String> fetch = query.select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("?????????")
                        .when(member.age.between(21, 40)).then("??????")
                        .otherwise("??????")
                )
                .from(member)
                .fetch();
        //then
        assertThat(fetch).containsExactly("?????????", "?????????", "??????", "??????");
    }

    @Test
    @DisplayName("")        
    public void findDtoBySetter() throws Exception{
        //given

        //when
        List<MemberDto> result = query.select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        //Projections.bean??? ???????????? setter??? ???????????? ??????. ????????? setter??? ?????? ??? ????????? ?????? ??????.

        //then
        for (MemberDto memberDto : result) {
            System.out.println(memberDto.toString());
        }
    }

    @Test
    @DisplayName("")
    public void findDtoByField() throws Exception{
        //given

        //when
        List<MemberDto> result = query.select(Projections.fields(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        //Projections.bean??? ???????????? setter??? ???????????? ??????. ????????? setter??? ?????? ??? ????????? ?????? ??????.

        //then
        for (MemberDto memberDto : result) {
            System.out.println(memberDto.toString());
        }
    }

    @Test
    @DisplayName("")
    public void findDtoByConstructor() throws Exception{
        //given

        //when
        List<MemberDto> result = query.select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();
        //Projections.bean??? ???????????? setter??? ???????????? ??????. ????????? setter??? ?????? ??? ????????? ?????? ??????.

        //then
        for (MemberDto memberDto : result) {
            System.out.println(memberDto.toString());
        }
    }

    @Test
    @DisplayName("")
    public void findUserDto() throws Exception{
        //given
        QMember ages = new QMember("ages");
        //when
        List<UserDto> result = query.select(Projections.constructor(UserDto.class,
                        member.username.as("name"),
                        new CaseBuilder()
                                .when(member.age.between(0, 10)).then("?????????")
                                .when(member.age.between(11, 20)).then("??????")
                                .when(member.age.between(21, 30)).then("?????????")
                                .when(member.age.between(31, 40)).then("?????????")
                                .otherwise("??? ??????").as("ageField")
                ))
                .from(member)
                .fetch();

        //then
        for (UserDto userDto : result) {
            System.out.println(userDto.toString());
        }
    }
    
    @Test
    @DisplayName("")        
    public void findUserDto2() throws Exception{
        //given
        QMember sub = new QMember("sub");
        //when
        List<UserDto> result = query.select(Projections.constructor(UserDto.class,
                member.username.as("name"),
                ExpressionUtils.as(select(sub.age.max()).from(sub), "ageField")
        )).from(member).fetch();

        //then
        for (UserDto userDto : result) {
            System.out.println(userDto.toString());
        }
    }
    
    @Test
    @DisplayName("")        
    public void dynamic_BooleanBuilder() throws Exception{
        //given
        String username = "member1";
        int age = 10;

        //when
        List<Member> result = searchMember1(username, age);

        //then
        assertThat(result.size()).isEqualTo(1);

    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if(usernameCond != null){
            booleanBuilder.and(member.username.eq(usernameCond));
        }
        if(ageCond != null){
            booleanBuilder.and(member.age.eq(ageCond));
        }

        return query.selectFrom(member)
                .where(booleanBuilder)
                .fetch();
    }
    
    @Test
    @DisplayName("")        
    public void dynamic_Where??????() throws Exception{
        //given
        String username = "member1";
        int age = 10;

        //when
        List<Member> result = searchMember2(username, age);

        //then
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String nameCond, Integer ageCond) {
        return query.selectFrom(member)
                .where(usernameEq(nameCond), ageEq(ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String nameCond) {
        if(nameCond == null) return null;
        return member.username.eq(nameCond);
    }

    private BooleanExpression ageEq(Integer ageCond) {
        if(ageCond == null) return null;
        return member.age.eq(ageCond);
    }

    @Test
    @DisplayName("")        
    public void bulkUpdate() throws Exception{
        //given
        
        //when
        long count = query.update(member)
                .set(member.username, "?????????")
                .where(member.age.lt(21))
                .execute();
        em.flush();
        em.clear();
        //then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("")
    public void bulkAdd() throws Exception{
        //given
        List<Member> beforeMembers = query.selectFrom(member).fetch();
        //when
        query.update(member).set(member.age, member.age.add(1)).execute();
        List<Member> whileMember = query.selectFrom(member).fetch();
        em.flush();
        em.clear();
        //then
        List<Member> after = query.selectFrom(member).fetch();
        for(int index = 0; index < beforeMembers.size(); index++){
            assertThat(after.get(0).getAge() - beforeMembers.get(0).getAge()).isEqualTo(1);
            assertThat(whileMember.get(0).getAge() - beforeMembers.get(0).getAge()).isEqualTo(0);
        }
    }
}
