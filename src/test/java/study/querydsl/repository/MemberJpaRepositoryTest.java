package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;

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
}
