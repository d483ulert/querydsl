package springdata.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;

import org.springframework.util.StringUtils;
import springdata.querydsl.dto.MemberSearchCondition;
import springdata.querydsl.dto.MemberTeamDto;
import springdata.querydsl.dto.QMemberTeamDto;
import springdata.querydsl.entity.Member;
import java.util.List;
import java.util.Optional;

import static org.aspectj.util.LangUtil.isEmpty;
import static org.springframework.util.StringUtils.hasText;
import static springdata.querydsl.entity.QMember.member;
import static springdata.querydsl.entity.QTeam.team;

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

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){
        BooleanBuilder builder = new BooleanBuilder();

        if(hasText(condition.getUsername())){
            builder.and(member.username.eq(condition.getUsername()));
        }

        if(hasText(condition.getTeamName())){
            builder.and(team.name.eq(condition.getTeamName()));
        }

        if(condition.getAgeGoe() != null){
            builder.and(member.age.goe(condition.getAgeGoe()));
        }

        if(condition.getAgeLoe() != null){
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team,team)
                .where(builder)
                .fetch();
    }

    //김영한이 좋아하는... 동적 쿼리와 성능 최적화 조회 - Where절 파라미터 사용
    public List<MemberTeamDto> search(MemberSearchCondition condition){

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team,team)
               .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();

    }

    private BooleanExpression usernameEq(String username) {
        return isEmpty(username) ? null : member.username.eq(username);
    }
    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }
    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
