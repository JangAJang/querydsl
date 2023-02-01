package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;
import study.querydsl.domain.QTeam;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.*;
import static study.querydsl.domain.QMember.*;
import static study.querydsl.domain.QTeam.*;

@Repository
public class MemberJpaRepository {

    private EntityManager em;
    private JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        queryFactory = new JPAQueryFactory(em);
    }

    //Create
    public void save(Member member){
        em.persist(member);
    }

    //Read
    public Member findById(Long id){
        return queryFactory.selectFrom(member).where(member.id.eq(id)).fetchOne();
    }

    public List<Member> findAll(){
        return queryFactory.selectFrom(member).fetch();
    }

    public List<Member> findByUsername(String username){
        return queryFactory.selectFrom(member).where(member.username.eq(username)).fetch();
    }

    //Update는 변경감지로 실행된다.
    //Delete
    public void deleteMember(Member deleteMember){
        queryFactory.delete(member).where(member.id.eq(deleteMember.getId())).execute();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                //.where(createBuilderForSearch(condition))
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        memberAgeGoe(condition.getAgeGoe()),
                        memberAgeLoe(condition.getAgeLoe()))
                .fetch();
    }

    private Predicate memberAgeLoe(Integer ageLoe) {
        if(ageLoe == null) return null;
        return member.age.loe(ageLoe);
    }

    private Predicate memberAgeGoe(Integer ageGoe) {
        if(ageGoe == null) return null;
        return member.age.goe(ageGoe);
    }

    private Predicate usernameEq(String username) {
        if(username == null || username.isEmpty() || username.isBlank()) return null;
        return member.username.eq(username);
    }

    private Predicate teamNameEq(String teamName) {
        if(teamName == null || teamName.isEmpty() || teamName.isBlank()) return null;
        return team.name.eq(teamName);
    }

    private BooleanBuilder createBuilderForSearch(MemberSearchCondition condition){
        BooleanBuilder builder = new BooleanBuilder();
        if(hasText(condition.getUsername())) builder.and(member.username.eq(condition.getUsername()));
        if(hasText(condition.getTeamName())) builder.and(team.name.eq(condition.getTeamName()));
        if(condition.getAgeGoe() != null) builder.and(member.age.goe(condition.getAgeGoe()));
        if(condition.getAgeLoe() != null) builder.and(member.age.loe(condition.getAgeLoe()));
        return builder;
    }
}
