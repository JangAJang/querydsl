package study.querydsl.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("")
    public void basicTest() throws Exception{
        //given
        Member member = new Member("testMember", 10);

        //when
        memberRepository.save(member);

        //then
        assertThat(memberRepository.findById(member.getId()).get()).isEqualTo(member);
        assertThat(memberRepository.findAll()).containsExactly(member);
        assertThat(memberRepository.findByUsername("testMember")).containsExactly(member);

        memberRepository.delete(member);
        assertThat(memberRepository.findAll().size()).isEqualTo(0);
    }
}