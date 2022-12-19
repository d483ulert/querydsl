package springdata.querydsl.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {
    private String username;
    private String teamName;
    private Integer ageGoe; //크거나 같거나
    private Integer ageLoe; // 작거나 같거나

}
