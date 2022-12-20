package springdata.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.support.PageableExecutionUtils;
import springdata.querydsl.dto.MemberSearchCondition;
import springdata.querydsl.dto.MemberTeamDto;
import springdata.querydsl.dto.QMemberTeamDto;
import javax.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import springdata.querydsl.entity.Member;

import java.util.List;
import static org.aspectj.util.LangUtil.isEmpty;
import static springdata.querydsl.entity.QMember.member;
import static springdata.querydsl.entity.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory factory;

    public MemberRepositoryImpl(EntityManager em) {
        this.factory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search (MemberSearchCondition condition){

        return factory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();

    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> results = factory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults().getResults();

        long total = results.size();

        return new PageImpl<>(results,pageable,total);
    }

    @Override // 카운트쿼리와 content쿼리 따로 날림
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = factory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Member> countQuery = factory.select(member)
                .from(member)
                .leftJoin(member.team,team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );


        return PageableExecutionUtils.getPage(content,pageable,countQuery::fetchCount);
    }



    private BooleanExpression usernameEq (String username){
        return isEmpty(username) ? null : member.username.eq(username);
    }
    private BooleanExpression teamNameEq (String teamName){
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }
    private BooleanExpression ageGoe (Integer ageGoe){
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe (Integer ageLoe){
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
