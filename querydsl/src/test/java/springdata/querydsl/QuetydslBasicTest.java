package springdata.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import springdata.querydsl.entity.Member;
import springdata.querydsl.entity.QMember;
import springdata.querydsl.entity.QTeam;
import springdata.querydsl.entity.Team;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static springdata.querydsl.entity.QMember.member;
import static springdata.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuetydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory = new JPAQueryFactory(em);//동시성 문제 없음. 멀티쓰레드에서 아무문제없음. 트랜잭션 단위에 따라 사용됨
    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);

        System.out.println("********************초기화");
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

        em.flush();
        em.clear();
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

      /*  JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember m = new QMember("m");
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))//파라미터 바인딩 처리
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");*/

        Member findmember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        System.out.println("************"+findmember);
        assertThat(findmember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void search(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch(){
        //리스트로가져옴
        List<Member> list = queryFactory
                .selectFrom(member)
                .fetch();

        //단건
        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();
        // limit 1과 똑같음
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();
        System.out.println("****results"+results);
        System.out.println("****content"+content);

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();
    }
    /*
    * 회원 정렬 순서
    * 1.회원 나이 내림차순(dssc)
    * 2.회원 이름 올림차순(asc)
    * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
    * */
    @Test
    public void sort(){
        em.persist(new Member(null,100));
        em.persist(new Member("member6",100));
        em.persist(new Member("member7",100));

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();

    }

    @Test
    public void Paging1(){
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    //count쿼리 한번, content쿼리 한 번 나가는 fetchResult
    @Test
    public void Paging2(){
        QueryResults<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(result.getTotal()).isEqualTo(4);
        assertThat(result.getLimit()).isEqualTo(2);
        assertThat(result.getOffset()).isEqualTo(1);
        assertThat(result.getResults().size()).isEqualTo(2);
    }

    @Test
    //집합
    public void aggregation(){
        //쿼리 dsl 제공 튜플
        List<Tuple> result = queryFactory.select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                ).from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple*****"+tuple);
        }
        Tuple tuple =result.get(0);

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);

    }

    /**
     *  팀의 이름과 각 팀의 평균 연령을 구하라.
     * */
    @Test
    public void groupby(){
        List<Tuple> result = queryFactory.select(
                        team.name,
                        member.age.avg()
                ).from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    /*
    * 팀 A에 소속된 모든 회원
    * */
    @Test
    public void join(){
        //연관관계가 없어도 조인할 수 있음.
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1","member2");

    }

    /**
     * 세타조인 (연관관계가 없는 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA","teamB");
    }

    /*
    *  회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인,회원은 모두 조회
    *  JPQL: select m,t from Member m left join m.team t on t.name = 'teamA'
    * */
    @Test
    public void join_on_filtering(){
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team) //id값 매칭
                .on(team.name.eq("teamA"))
                .fetch();
        for(Tuple tuple: result){
            System.out.println("tuple: "+tuple);
        }
    }

    /**
     * 연관관계 없는 엔티티 외부 조인
     *  예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
     *  JPQL: select m, t from Member m left join team t on m.username = t.name
     *  SQL: select m.*, t.* from member m left join team t on m.username= t.name
     * */

    @Test
    public void join_on_no_relation(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name)) //id값 매칭x 막조인
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("***tuple"+tuple);
        }
    }
}
