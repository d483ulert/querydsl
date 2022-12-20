package springdata.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import springdata.querydsl.dto.MemberSearchCondition;
import springdata.querydsl.dto.MemberTeamDto;
import springdata.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
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
        System.out.println(condition + "\n************");

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
