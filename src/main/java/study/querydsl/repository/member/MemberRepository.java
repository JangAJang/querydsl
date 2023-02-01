package study.querydsl.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl.domain.Member;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom{

    List<Member> findByUsername(String username);
}
