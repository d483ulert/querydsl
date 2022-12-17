package springdata.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import springdata.querydsl.entity.Member;
import java.util.List;
import java.util.Optional;

import static springdata.querydsl.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public void save(Member member){
        em.persist(member);
    }

    public Optional<Member> findById(Long id){
       Member findMember = em.find(Member.class,id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m",Member.class).getResultList();
    }

    public List<Member> findAllQuerydsl(){
        return queryFactory
                .selectFrom(member)
                .fetch();
    }



    public List<Member> findByUsername(String username){
        return em.createQuery("select m from Member m where username =:username",Member.class)
                .setParameter("username",username)
                .getResultList();
    }

    public List<Member> findByUsernameQuerydsl(String username){
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }
}
