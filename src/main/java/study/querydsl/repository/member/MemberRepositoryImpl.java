package study.querydsl.repository.member;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import study.querydsl.domain.QMember;
import study.querydsl.domain.QTeam;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import java.util.List;

import static study.querydsl.domain.QMember.*;
import static study.querydsl.domain.QTeam.*;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory query;

    public MemberRepositoryImpl(EntityManager em) {
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return query.select(new QMemberTeamDto(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe()),
                        teamNameEq(condition.getTeamName()))
                .fetch();
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

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        QueryResults<MemberTeamDto> result = query.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe()),
                        teamNameEq(condition.getTeamName()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<MemberTeamDto>(result.getResults(), pageable, result.getTotal());
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> result = getPageData(condition, pageable);
        long total = countTotal(condition);
        return new PageImpl<MemberTeamDto>(result, pageable, total);
    }

    private long countTotal(MemberSearchCondition condition) {
        return query
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(
                                condition.getUsername()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe()),
                        teamNameEq(condition.getTeamName()))
                .fetchCount();
    }

    private List<MemberTeamDto> getPageData(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> result = query.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe()),
                        teamNameEq(condition.getTeamName()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        return result;
    }
}
