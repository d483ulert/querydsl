package springdata.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import springdata.querydsl.dto.MemberDto;
import springdata.querydsl.dto.QMemberDto;
import springdata.querydsl.dto.UserDto;
import springdata.querydsl.entity.Member;
import springdata.querydsl.entity.QMember;
import springdata.querydsl.entity.Team;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
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

    //조인 - 페치 조인
    @PersistenceUnit
    EntityManagerFactory emf;
    @Test
    public void NofetchJoin(){
        em.flush();
        em.clear();

        Member result = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("***Result"+result);

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(result.getTeam());//초기화 된 Entity인지 아닌지 알려줌
        assertThat(loaded).as("페치조인미적용").isFalse();
    }

    @Test
    public void UsefetchJoin(){
        em.flush();
        em.clear();

        Member result = queryFactory
                .selectFrom(member)
                .join(member.team,team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("***Result"+result);

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(result.getTeam());//초기화 된 Entity인지 아닌지 알려줌
        assertThat(loaded).as("페치조인미적용").isTrue();
    }
    
    //서브쿼리
    /*
    * 나이가 가장 많은 회원 조회
    * */
    @Test
    public void subQuery(){
        //서브쿼리이기 때문에 밖의  QMember와 ALias가 겹치면 안된다. 다르게선언
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(40);
    }

    /*
     * 나이가 평균 이상인 회원
     * */
    @Test
    public void subQueryGoe(){
        //서브쿼리이기 때문에 밖의  QMember와 ALias가 겹치면 안된다. 다르게선언
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(  //goe 크거나 혹은 같다
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(30,40);
    }

    /*
     * 나이가 평균 이상인 회원
     * */
    @Test
    public void subQueryIn(){
        //서브쿼리이기 때문에 밖의  QMember와 ALias가 겹치면 안된다. 다르게선언
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(  //goe 크거나 혹은 같다
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result)
                .extracting("age")
                .containsExactly(20,30,40);
    }

    @Test
    public void selectSubQuery(){
        QMember memberSub = new QMember("memberSub");
        List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("***tuple"+tuple);
        }
    }

    //from절 서브쿼리(인라인뷰)는 지원하지않는다.
    //서브쿼리를 join으로 바꿔서사용.
    //애플리케이션에서 쿼리를 2번 분리해서 실행
    // native SQL을 사용


    //케이스문
    @Test
    public void basicCase(){
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("*******ss"+s);
        }
    }

    @Test
    public void complexCase(){
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("20대")
                        .when(member.age.between(21, 40)).then("노인")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("ssss****"+s);
        }
    }

    //상수 문자 더하기
    @Test
    public void constant(){
        List<Tuple> result
                = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("*****result"+result);
        }
    }
    @Test
    public void concat(){
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("ssss***"+s);
        }
    }

    //프로젝션과 결과반환-기본
    @Test
    public void simpleProjection(){
        //String,int,Member 등 한번에 조회
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("sssssss: "+s);
        }
    }

    @Test
    public void tupleProejction(){
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            int age = tuple.get(member.age);
            System.out.println("username: "+username);
            System.out.println("age: "+age);
        }

        //Tuple로 받아도 결국 DTO로 반환해서 내보낼것.
    }

    //프로젝션 결과 반환 -DTO조회
    @Test
    public void findDtoBySetter(){ //setter방법 setter를 통해서 값이 들어감.
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("******"+memberDto);
        }
    }

    @Test
    public void findDtoByField(){ //필드방법 바로 filed에 꽂힘
        List<MemberDto> result = queryFactory  
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("******"+memberDto);
        }
    }

    @Test
    public void findDtoByConstructor(){ //생성자방식  호출 타입이 맞아야함.
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("ㅎㅎㅎㅎ"+memberDto);
        }
    }

    @Test
    public void findUserDto(){ //필드방법 바로 filed에 꽂힘 필드명,타입이 맞아야함
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("******"+userDto);
        }
    }

    @Test
    public void findUserDtoSubQuery(){ //필드방법 바로 filed에 꽂힘 필드명,타입이 맞아야함
        QMember memberSub = new QMember("memberSub");


        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub),"age")
                ))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("******"+userDto);
        }
    }

    @Test
    public void findDtoByQueryProjection(){
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("ggggg"+memberDto);
        }
    }

    //동적 쿼리 - BooleanBuilder 사용
    @Test
    public void dynamicQuery_BooleanBulder(){
        String usernameParam = "member1";
        Integer ageParam= 10;

        List<Member> result = searchMember1(usernameParam,ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if(usernameCond != null){
            booleanBuilder.and(member.username.eq(usernameCond));
        }

        if(ageCond != null){
            booleanBuilder.and(member.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(member)
                .where(booleanBuilder)
                .fetch();
    }

}
