package BE_Elixir.Elixir.domain.member.entity;

import BE_Elixir.Elixir.domain.follow.entity.Follow;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true, nullable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;  // 인코딩된 비밀번호

    @Column(nullable = false, unique = true)
    private String nickname;

    @Setter private String profileUrl;
    private String gender;
    private Integer birthYear;

    // 회원이 팔로잉하는 경우
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followings = new ArrayList<>();

    // 회원을 팔로우하는 경우
    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Follow> followers = new ArrayList<>();

    // 업적
    private String title;

    @Setter
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    // 알러지 정보
    @Setter private Boolean allergy_알류;
    @Setter private Boolean allergy_우유;
    @Setter private Boolean allergy_각류;
    @Setter private Boolean allergy_밀류;
    @Setter private Boolean allergy_유제품;
    @Setter private Boolean allergy_메밀;
    @Setter private Boolean allergy_땅콩;
    @Setter private Boolean allergy_대두;
    @Setter private Boolean allergy_밀;
    @Setter private Boolean allergy_고등어;
    @Setter private Boolean allergy_돼지고기;
    @Setter private Boolean allergy_복숭아;
    @Setter private Boolean allergy_토마토;
    @Setter private Boolean allergy_아황산류;
    @Setter private Boolean allergy_호두;
    @Setter private Boolean allergy_닭고기;
    @Setter private Boolean allergy_쇠고기;
    @Setter private Boolean allergy_오징어;
    @Setter private Boolean allergy_조개류;
    @Setter private Boolean allergy_굴;
    @Setter private Boolean allergy_전복;
    @Setter private Boolean allergy_홍합;
    @Setter private Boolean allergy_잣;

    // meal style fields
    @Setter private Boolean mealStyle_고기_위주 = false;
    @Setter private Boolean mealStyle_채소_위주 = false;
    @Setter private Boolean mealStyle_혼합식 = false;

    // recipe style fields
    @Setter private Boolean recipeStyle_한식 = false;
    @Setter private Boolean recipeStyle_중식 = false;
    @Setter private Boolean recipeStyle_일식 = false;
    @Setter private Boolean recipeStyle_양식 = false;
    @Setter private Boolean recipeStyle_디저트 = false;
    @Setter private Boolean recipeStyle_음료_차 = false;
    @Setter private Boolean recipeStyle_양념_소스_잼 = false;

    // reason fields
    @Setter private boolean reason_항산화강화 = false;
    @Setter private boolean reason_혈당조절 = false;
    @Setter private boolean reason_염증감소 = false;
}