package study.querydsl.repository.member;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import study.querydsl.domain.QMember;
import study.querydsl.domain.QTeam;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import java.util.List;

import static study.querydsl.domain.QMember.*;
import static study.querydsl.domain.QTeam.*;

@RequiredArgsConstructor
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
                .where(
                        usernameEq(condition.getUsername()),
                        ageLoe(condition.getAgeLoe()),
                        ageGoe(condition.getAgeGoe()),
                        teamNameEq(condition.getTeamName())
                        )
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


}
