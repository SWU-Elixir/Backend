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


    // allergy fields
    @Setter private boolean allergyEgg = false;
    @Setter private boolean allergyMilk = false;
    @Setter private boolean allergyGrain = false;
    @Setter private boolean allergyWheatProduct = false;
    @Setter private boolean allergyDairy = false;
    @Setter private boolean allergyBuckwheat = false;
    @Setter private boolean allergyPeanut = false;
    @Setter private boolean allergySoybean = false;
    @Setter private boolean allergyWheat = false;
    @Setter private boolean allergyMackerel = false;
    @Setter private boolean allergyPork = false;
    @Setter private boolean allergyPeach = false;
    @Setter private boolean allergyTomato = false;
    @Setter private boolean allergySulfite = false;
    @Setter private boolean allergyWalnut = false;
    @Setter private boolean allergyChicken = false;
    @Setter private boolean allergyBeef = false;
    @Setter private boolean allergySquid = false;
    @Setter private boolean allergyShellfish = false;
    @Setter private boolean allergyOyster = false;
    @Setter private boolean allergyAbalone = false;
    @Setter private boolean allergyMussel = false;
    @Setter private boolean allergyPineNut = false;

    // meal style fields
    @Setter private boolean mealStyleMeatBased = false;
    @Setter private boolean mealStyleVegetableBased = false;
    @Setter private boolean mealStyleMixed = false;

    // recipe style fields
    @Setter private boolean recipeStyleKorean = false;
    @Setter private boolean recipeStyleChinese = false;
    @Setter private boolean recipeStyleJapanese = false;
    @Setter private boolean recipeStyleWestern = false;
    @Setter private boolean recipeStyleDessert = false;
    @Setter private boolean recipeStyleBeverageTea = false;
    @Setter private boolean recipeStyleSauceJam = false;

    // reason fields
    @Setter private boolean reasonAntioxidantBoost = false;
    @Setter private boolean reasonBloodSugarControl = false;
    @Setter private boolean reasonInflammationReduction = false;
}