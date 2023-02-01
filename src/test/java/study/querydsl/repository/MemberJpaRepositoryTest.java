package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.Team;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("")
    public void basicTest() throws Exception{
        //given
        Member member = new Member("testMember", 10);

        //when
        memberJpaRepository.save(member);

        //then
        assertThat(memberJpaRepository.findById(member.getId())).isEqualTo(member);
        assertThat(memberJpaRepository.findAll()).containsExactly(member);
        assertThat(memberJpaRepository.findByUsername("testMember")).containsExactly(member);

        memberJpaRepository.deleteMember(member);
        assertThat(memberJpaRepository.findAll().size()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("")        
    public void searchTest() throws Exception{
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
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);
        //then
        assertThat(result.get(0).getUsername()).isEqualTo("member4");
    }
}
