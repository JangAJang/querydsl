package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;

import java.util.List;
import java.util.Optional;

import static study.querydsl.domain.QMember.*;

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
}
