package springdata.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import springdata.querydsl.dto.MemberSearchCondition;
import springdata.querydsl.entity.Member;
import springdata.querydsl.entity.QMember;
import springdata.querydsl.repository.support.Querydsl4RepositorySupport;

import java.util.List;

import static org.aspectj.util.LangUtil.isEmpty;
import static springdata.querydsl.entity.QMember.member;
import static springdata.querydsl.entity.QTeam.team;


@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {


    public MemberTestRepository(Class<?> domainClass) {
        super(Member.class);
    }

    public List<Member> basicSelect(){
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom(){
        return selectFrom(member)
                .fetch();
    }

    public Page<Member> applyPagination(MemberSearchCondition condition, Pageable pageable){
      return applyPagination(pageable,query ->
            query
            .selectFrom(member)
            .where(usernameEq(condition.getUsername()),
                    teamNameEq(condition.getTeamName()),
                    ageGoe(condition.getAgeGoe()),
                    ageLoe(condition.getAgeLoe()))
      );
    }
    public Page<Member> applyPagination2(MemberSearchCondition condition, Pageable pageable){
        return applyPagination(pageable,contentQuery -> contentQuery
                .selectFrom(member)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ),countQuery -> countQuery
                .select(member.id)
                .from(member)
                .leftJoin(member.team,team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                );
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
