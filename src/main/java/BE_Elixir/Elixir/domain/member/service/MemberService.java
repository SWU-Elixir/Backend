package BE_Elixir.Elixir.domain.member.service;

import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeAchievementRepository;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeRepository;
import BE_Elixir.Elixir.domain.member.dto.request.SignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.response.MemberAchievementResponseDTO;
import BE_Elixir.Elixir.domain.member.dto.response.MemberResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.domain.recipe.dto.RecipeImageResponseDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.exception.OccupiedException;
import BE_Elixir.Elixir.global.redis.RedisService;
import BE_Elixir.Elixir.global.s3.S3Service;
import BE_Elixir.Elixir.global.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final RecipeRepository recipeRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeAchievementRepository challengeAchievementRepository;
    private final RecipeEventRepository recipeEventRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final S3Service s3Service;

    // 이메일 중복 체크
    public boolean isEmailDuplicated(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 회원가입 (USER 권한을 추가하여 데이터 추가)
    public Member signUp(SignUpRequestDTO request, MultipartFile profileImage) {
        List<String> roles = new ArrayList<>();
        roles.add("USER");

        try {
            // Member entity 값 세팅
            Member member = request.toEntity(
                    passwordEncoder.encode(request.getPassword()), roles
            );
            member.setRoles(roles);

            // 설문조사 결과 세팅
            // allergy 값 세팅
            List<String> allergies = request.getAllergies();
            if (allergies != null) {
                applyAllergies(member, allergies);
            }

            // meal style 값 세팅
            List<String> mealStyles = request.getMealStyles();
            if (mealStyles != null) {
                 applyMealStyles(member, mealStyles);
            }

            // recipe style 값 세팅
            List<String> recipeStyles = request.getRecipeStyles();
            if (recipeStyles != null) {
                applyRecipeStyles(member, recipeStyles);
            }

            // reason 값 세팅
            List<String> reasons = request.getReasons();
            if (reasons != null) {
                applyReasons(member, reasons);
            }

            // 프로필 이미지 업로드 및 url 세팅
            if (profileImage != null && !profileImage.isEmpty()) {
                String imageUrl = s3Service.upload(profileImage, "member");
                member.setProfileUrl(imageUrl);
            }

            return memberRepository.save(member);

        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().toUpperCase().contains("EMAIL_UNIQUE")) {
                throw new OccupiedException(ErrorCode.EXISTS_MEMBER);
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("회원가입 중 오류가 발생했습니다.");
        }
    }


    // 회원 탈퇴
    public void withdraw(String email, String accessToken, String refreshToken) {
        // Access Token 검증 및 블랙리스트 처리
        if (jwtProvider.validateToken(accessToken)) {
            redisService.addAccessTokenToBlacklist(accessToken);
            log.info("Access Token 블랙리스트 처리");
        } else {
            throw new RuntimeException("유효하지 않거나 만료된 Access Token");
        }

        // Refresh Token이 redis에 있는지 확인 및 제거
        if (refreshToken != null && redisService.isRefreshTokenValid(email, refreshToken)) {
            // redis에서 제거
            redisService.removeRefreshToken(email);
            log.info("Refresh Token 무효화");
        } else {
            throw new RuntimeException("유효하지 않거나 만료된 Refresh Token");
        }

        // 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. email: " + email));

        // S3 버킷에서 프로필 이미지 삭제
        s3Service.deleteS3(member.getProfileUrl(), "member");
        // 회원 삭제
        memberRepository.delete(member);
    }


    // 회원 정보 조회
    public MemberResponseDTO getMemberInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. email: " + email));

        // member 객체를 MemberResponseDTO 로 변환
        return MemberResponseDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .gender(member.getGender())
                .birthYear(member.getBirthYear())
                .profileUrl(member.getProfileUrl())
                .build();
    }


    // 설문조사 결과를 member 객체에 적용
    private void applyAllergies(Member member, List<String> allergies) {
        for (String allergy : allergies) {
            switch (allergy) {
                case "egg" -> member.setAllergyEgg(true);
                case "milk" -> member.setAllergyMilk(true);
                case "grain" -> member.setAllergyGrain(true);
                case "wheat_product" -> member.setAllergyWheatProduct(true);
                case "dairy" -> member.setAllergyDairy(true);
                case "buckwheat" -> member.setAllergyBuckwheat(true);
                case "peanut" -> member.setAllergyPeanut(true);
                case "soybean" -> member.setAllergySoybean(true);
                case "wheat" -> member.setAllergyWheat(true);
                case "mackerel" -> member.setAllergyMackerel(true);
                case "pork" -> member.setAllergyPork(true);
                case "peach" -> member.setAllergyPeach(true);
                case "tomato" -> member.setAllergyTomato(true);
                case "sulfite" -> member.setAllergySulfite(true);
                case "walnut" -> member.setAllergyWalnut(true);
                case "chicken" -> member.setAllergyChicken(true);
                case "beef" -> member.setAllergyBeef(true);
                case "squid" -> member.setAllergySquid(true);
                case "shellfish" -> member.setAllergyShellfish(true);
                case "oyster" -> member.setAllergyOyster(true);
                case "abalone" -> member.setAllergyAbalone(true);
                case "mussel" -> member.setAllergyMussel(true);
                case "pine_nut" -> member.setAllergyPineNut(true);
            }
        }
    }

    private void applyMealStyles(Member member, List<String> styles) {
        for (String style : styles) {
            switch (style) {
                case "meat_based" -> member.setMealStyleMeatBased(true);
                case "vegetable_based" -> member.setMealStyleVegetableBased(true);
                case "mixed" -> member.setMealStyleMixed(true);
            }
        }
    }

    private void applyRecipeStyles(Member member, List<String> styles) {
        for (String style : styles) {
            switch (style) {
                case "korean" -> member.setRecipeStyleKorean(true);
                case "chinese" -> member.setRecipeStyleChinese(true);
                case "japanese" -> member.setRecipeStyleJapanese(true);
                case "western" -> member.setRecipeStyleWestern(true);
                case "dessert" -> member.setRecipeStyleDessert(true);
                case "beverage_tea" -> member.setRecipeStyleBeverageTea(true);
                case "sauce_jam" -> member.setRecipeStyleSauceJam(true);
            }
        }
    }

    private void applyReasons(Member member, List<String> reasons) {
        for (String reason : reasons) {
            switch (reason) {
                case "antioxidant_boost" -> member.setReasonAntioxidantBoost(true);
                case "blood_sugar_control" -> member.setReasonBloodSugarControl(true);
                case "inflammation_reduction" -> member.setReasonInflammationReduction(true);
            }
        }
    }

    // 로그인한 사용자가 업로드한 모든 레시피 조회하기
    public List<RecipeImageResponseDTO> getMyRecipes(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new OccupiedException(ErrorCode.MEMBER_NOT_FOUND));

        List<Recipe> recipes = recipeRepository.findAllByMember(member);

        return recipes.stream()
                .map(recipe -> new RecipeImageResponseDTO(recipe.getId(), recipe.getImageUrl()))
                .collect(Collectors.toList());
    }

    // 로그인한 사용자가 스크랩한 레시피 조회하기
    public List<RecipeImageResponseDTO> getMyScrapRecipes(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new OccupiedException(ErrorCode.MEMBER_NOT_FOUND));

        List<RecipeEvent> scraps = recipeEventRepository.findByMemberAndScrapFlagTrue(member);

        return scraps.stream()
                .map(event -> {
                    Recipe recipe = event.getRecipe();
                    return new RecipeImageResponseDTO(recipe.getId(), recipe.getImageUrl());
                })
                .toList();
    }

    // 로그인한 사용자의 모든 챌린지 업적 정보 조회
    public List<MemberAchievementResponseDTO> getAllAchievements(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new OccupiedException(ErrorCode.MEMBER_NOT_FOUND));

        // 전체 챌린지 조회 (연도/월 기준)
        List<Challenge> allChallenges = challengeRepository.findAllOrderedByYearAndMonth();

        // 사용자의 챌린지 달성 정보 조회
        List<ChallengeAchievement> achievements = challengeAchievementRepository.findByMemberId(member.getId());

        Map<Long, ChallengeAchievement> achievementMap = achievements.stream()
                .collect(Collectors.toMap(
                        ChallengeAchievement::getChallengeId,
                        a -> a
                ));
        return allChallenges.stream()
                .map(challenge -> {
                    ChallengeAchievement achievement = achievementMap.get(challenge.getId());

                    // 달성여부
                    boolean completed = (achievement != null) && achievement.isStep4Goal1Achieved() && achievement.isStep4Goal2Achieved();

                    // 달성할 경우 컬러이미지, 달성하지 않았을 경우 흑백이미지
                    String imageUrl = completed ?
                            challenge.getAchievementImageUrl() :
                            challenge.getGrayAchievementImageUrl();

                    return new MemberAchievementResponseDTO(
                            challenge.getYear(),
                            challenge.getMonth(),
                            challenge.getAchievementName(),
                            imageUrl,
                            completed
                    );
                })
                .collect(Collectors.toList());
    }
}