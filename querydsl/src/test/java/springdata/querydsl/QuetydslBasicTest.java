package springdata.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import springdata.querydsl.entity.Member;
import springdata.querydsl.entity.Team;
import javax.persistence.EntityManager;
import static org.assertj.core.api.Assertions.assertThat;
import static springdata.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QuetydslBasicTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory; //동시성 문제 없음. 멀티쓰레드에서 아무문제없음. 트랜잭션 단위에 따라 사용됨

    @BeforeEach
    public void before(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1  = new Member("member1",10,teamA);
        Member member2  = new Member("member2",20,teamA);
        Member member3  = new Member("member3",30,teamB);
        Member member4  = new Member("member4",40,teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL(){
        //member을 찾아라  jpql
        Member findMember = em.createQuery("select m from Member m where m.username =: username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void startQuerydsl(){
        //member = QMember임
        Member findmember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        System.out.println("************"+findmember);
        assertThat(findmember.getUsername()).isEqualTo("member1");

    }

}
