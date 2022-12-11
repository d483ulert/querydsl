package springdata.querydsl.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter @Setter
@ToString(of ={"id","username","age"})
public class Member {

    @Id
    @GeneratedValue
    @Column(name="member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_id")  //연관관계주인
    private Team team;


    public Member(String username, int age, Team team){
        this.username = username;
        this.age = age;
        if(team != null){
            changeTeam(team);
        }
    }

    public Member(String username,int age){
        this(username,age,null);
    }

    public Member(String username){
        this(username,0);
    }

    public void changeTeam(Team team){
        this.team=team;
        team.getMembers().add(this);
    }

}
