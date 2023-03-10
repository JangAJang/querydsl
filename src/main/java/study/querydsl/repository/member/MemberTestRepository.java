package study.querydsl.repository.member;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;
import study.querydsl.domain.QTeam;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.repository.Querydsl4Repository;

import java.util.List;

import static study.querydsl.domain.QMember.*;
import static study.querydsl.domain.QTeam.team;

@Repository
public class MemberTestRepository extends Querydsl4Repository {

    public MemberTestRepository(){
        super(Member.class);
    }

    public List<Member> select(){
        return select(member).from(member).fetch();
    }

    public List<Member> basicSelectFrom(){
        return selectFrom(member).fetch();
    }

    public Page<Member> searchPageByApplyPage(MemberSearchCondition memberSearchCondition, Pageable pageable){
        JPAQuery<Member> members = selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(memberSearchCondition.getUsername()),
                        ageLoe(memberSearchCondition.getAgeLoe()),
                        ageGoe(memberSearchCondition.getAgeGoe()),
                        teamNameEq(memberSearchCondition.getTeamName()));
        List<Member> fetch = getQuerydsl().applyPagination(pageable, members).fetch();
        return PageableExecutionUtils.getPage(fetch, pageable, members::fetchCount);
    }

    //위의 메서드와 동일한 기능을 가진다.
    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable){
        return applyPagination(pageable, query->
                query
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe()),
                        teamNameEq(condition.getTeamName())
                ));
    }

    public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable){
        return applyPagination(pageable, contentQuery->
                contentQuery
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe()),
                        teamNameEq(condition.getTeamName())
                ),
                countQuery->
                countQuery.select(member.id).from(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                                ageLoe(condition.getAgeLoe()),
                                ageGoe(condition.getAgeGoe()),
                                teamNameEq(condition.getTeamName())
                        ));
    }

    private Predicate teamNameEq(String teamName) {
        if(teamName == null || teamName.isEmpty() || teamName.isBlank()) return null;
        return team.name.eq(teamName);
    }

    private Predicate ageGoe(Integer ageGoe) {
        if(ageGoe == null) return null;
        return member.age.goe(ageGoe);
    }

    private Predicate ageLoe(Integer ageLoe) {
        if(ageLoe == null) return null;
        return member.age.loe(ageLoe);
    }

    private Predicate usernameEq(String username) {
        if(username == null || username.isEmpty() || username.isBlank()) return null;
        return member.username.eq(username);
    }
}
