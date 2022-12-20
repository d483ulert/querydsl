package springdata.querydsl.repository;

import springdata.querydsl.dto.MemberSearchCondition;
import springdata.querydsl.dto.MemberTeamDto;
import java.util.List;
public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);

}
