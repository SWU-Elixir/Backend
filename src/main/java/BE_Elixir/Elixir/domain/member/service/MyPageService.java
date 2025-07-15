package BE_Elixir.Elixir.domain.member.service;

import BE_Elixir.Elixir.domain.achievement.entity.Achievement;
import BE_Elixir.Elixir.domain.achievement.entity.MemberAchievement;
import BE_Elixir.Elixir.domain.achievement.repository.AchievementRepository;
import BE_Elixir.Elixir.domain.achievement.repository.MemberAchievementRepository;
import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeAchievementRepository;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeRepository;
import BE_Elixir.Elixir.domain.member.dto.request.MemberProfileRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.request.SurveyRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.response.*;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeImageResponseDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.s3.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MyPageService {

    private final MemberRepository memberRepository;
    private final RecipeRepository recipeRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeAchievementRepository challengeAchievementRepository;
    private final RecipeEventRepository recipeEventRepository;
    private final S3Service s3Service;
    private final MemberAchievementRepository memberAchievementRepository;
    private final AchievementRepository achievementRepository;


    // 회원 정보 조회
    public MemberResponseDTO getMemberInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

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

    // 사용자 프로필 조회 (칭호, 닉네임, 프로필사진, 팔로워 수, 팔로잉 수)
    public MemberProfileResponseDTO getMemberProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberProfileResponseDTO.builder()
                .id(memberId)
                .nickname(member.getNickname())
                .title(member.getTitle())
                .profileUrl(member.getProfileUrl())
                .followerCount(member.getFollowers().size())
                .followingCount(member.getFollowings().size())
                .build();
    }

    // 프로필 수정 시, 얻은 칭호 목록 조회
    public List<String> getTitles(Long memberId) {
        // memberId 기반 챌린지 최종 달성 여부 조회
        List<Long> achievedChallengeIds = challengeAchievementRepository.findByMemberId(memberId).stream()
                .filter(ChallengeAchievement::isAllGoalsAchieved)
                .map(ChallengeAchievement::getChallengeId)
                .collect(Collectors.toList());

        // 챌린지 업적명 조회
        List<String> challengeAchievementNames = challengeRepository.findAllById(achievedChallengeIds).stream()
                .map(Challenge::getAchievementName)
                .toList();

        // memberId 기반 챌린지 외 업적 달성 여부 조회
        List<Long> completedAchievementIds = memberAchievementRepository.findByMemberIdAndCompleted(memberId, true).stream()
                .map(ma -> ma.getAchievement().getId())
                .toList();

        // 챌린지 외 업적명 조회
        List<String> achievementNames = achievementRepository.findAllById(completedAchievementIds).stream()
                .map(Achievement::getAchievementName)
                .toList();

        List<String> allTitles = new ArrayList<>();
        allTitles.addAll(achievementNames);
        allTitles.addAll(challengeAchievementNames);

        return allTitles;
    }

    // 로그인한 사용자 프로필 수정하기
    public MemberResponseDTO updateMemberProfile(Long memberId, MemberProfileRequestDTO dto, MultipartFile image) {
        // 기존 프로필 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 닉네임 수정
        if (dto.getNickname() != null) {
            member.setNickname(dto.getNickname());
        }

        // 칭호 수정
        if (dto.getTitle() != null) {
            member.setTitle(dto.getTitle());
        }

        // 프로필 사진 수정
        if (image != null && !image.isEmpty()) {
            // 기존 이미지 삭제
            if (member.getProfileUrl() != null) {
                s3Service.deleteS3(member.getProfileUrl(), "member");
            }
            // 새 이미지 업로드
            try {
                String imageUrl = s3Service.upload(image, "member");
                member.setProfileUrl(imageUrl);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
            }
        }

        // 젠더 수정
        if (dto.getGender() != null) {
            member.setGender(dto.getGender());
        }

        // 생년 수정
        if (dto.getBirthYear() != null) {
            member.setBirthYear(dto.getBirthYear());
        }

        memberRepository.save(member);

        return MemberResponseDTO.builder()
                .id(memberId)
                .email(member.getEmail())
                .nickname(member.getNickname())
                .title(member.getTitle())
                .profileUrl(member.getProfileUrl())
                .gender(member.getGender())
                .birthYear(member.getBirthYear())
                .build();
    }

    // 설문조사 결과 조회
    public SurveyResponseDTO getSurvey(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        return SurveyResponseDTO.builder()
                .memberId(memberId)
                .allergies(member.getAllergies())
                .mealStyles(member.getMealStyles())
                .recipeStyles(member.getRecipeStyles())
                .reasons(member.getReasons())
                .build();
    }

    // 설문조사 결과 수정
    public SurveyResponseDTO updateSurvey(Long memberId, SurveyRequestDTO dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<String> allergies = dto.getAllergies();
        if (allergies != null) {
            member.resetAllergies();
            member.applyAllergies(allergies);
        }

        // meal style 값 세팅
        List<String> mealStyles = dto.getMealStyles();
        if (mealStyles != null) {
            member.resetMealStyles();
            member.applyMealStyles(mealStyles);
        }

        // recipe style 값 세팅
        List<String> recipeStyles = dto.getRecipeStyles();
        if (recipeStyles != null) {
            member.resetRecipeStyles();
            member.applyRecipeStyles(recipeStyles);
        }

        // reason 값 세팅
        List<String> reasons = dto.getReasons();
        if (reasons != null) {
            member.resetReasons();
            member.applyReasons(reasons);
        }

        memberRepository.save(member);

        return SurveyResponseDTO.builder()
                .memberId(memberId)
                .allergies(member.getAllergies())
                .mealStyles(member.getMealStyles())
                .recipeStyles(member.getRecipeStyles())
                .reasons(member.getReasons())
                .build();
    }

    // 로그인한 사용자가 업로드한 모든 레시피 조회하기
    public List<RecipeImageResponseDTO> getMyRecipes(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Recipe> recipes = recipeRepository.findAllByMember(member);

        return recipes.stream()
                .map(recipe -> new RecipeImageResponseDTO(recipe.getId(), recipe.getImageUrl()))
                .collect(Collectors.toList());
    }

    // 로그인한 사용자가 스크랩한 레시피 조회하기
    public List<RecipeImageResponseDTO> getMyScrapRecipes(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<RecipeEvent> scraps = recipeEventRepository.findByMemberAndScrapFlagTrue(member);

        return scraps.stream()
                .map(event -> {
                    Recipe recipe = event.getRecipe();
                    return new RecipeImageResponseDTO(recipe.getId(), recipe.getImageUrl());
                })
                .toList();
    }

    // 다른 사용자가 업로드한 모든 레시피 조회하기
    public List<RecipeImageResponseDTO> getUserRecipes(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Recipe> recipes = recipeRepository.findAllByMember(member);

        return recipes.stream()
                .map(recipe -> new RecipeImageResponseDTO(recipe.getId(), recipe.getImageUrl()))
                .limit(9)
                .collect(Collectors.toList());
    }

    // 사용자의 모든 챌린지 업적 정보 조회
    public List<MemberChallengeResponseDTO> getAllChallengeAchievements(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

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

                    return new MemberChallengeResponseDTO(
                            challenge.getYear(),
                            challenge.getMonth(),
                            challenge.getAchievementName(),
                            imageUrl,
                            completed
                    );
                })
                .collect(Collectors.toList());
    }

    // 사용자가 달성한 챌린지 업적 최신 3개 조회하기
    public List<MemberChallengeResponseDTO> getTop3ChallengeAchievements(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<ChallengeAchievement> achievements = challengeAchievementRepository.findByMemberId(member.getId());

        // 챌린지 ID만 가져오기
        Set<Long> challengeIds = achievements.stream()
                .filter(a -> a.isStep4Goal1Achieved() && a.isStep4Goal2Achieved())
                .map(ChallengeAchievement::getChallengeId)
                .collect(Collectors.toSet());

        // challengeId에 해당하는 챌린지를 한 번에 조회
        List<Challenge> challenges = challengeRepository.findAllById(challengeIds);

        // challengeId → Challenge 매핑
        Map<Long, Challenge> challengeMap = challenges.stream()
                .collect(Collectors.toMap(Challenge::getId, c -> c));

        // 업적 필터 + 정렬 + DTO 변환
        List<MemberChallengeResponseDTO> top3Achievements = achievements.stream()
                .filter(a -> a.isStep4Goal1Achieved() && a.isStep4Goal2Achieved())
                .sorted((a1, a2) -> {
                    Challenge c1 = challengeMap.get(a1.getChallengeId());
                    Challenge c2 = challengeMap.get(a2.getChallengeId());
                    int compareYear = Integer.compare(c2.getYear(), c1.getYear());
                    return (compareYear != 0) ? compareYear : Integer.compare(c2.getMonth(), c1.getMonth());
                })
                .limit(3)
                .map(a -> {
                    Challenge challenge = challengeMap.get(a.getChallengeId());
                    return new MemberChallengeResponseDTO(
                            challenge.getYear(),
                            challenge.getMonth(),
                            challenge.getAchievementName(),
                            challenge.getAchievementImageUrl(), // 컬러 이미지
                            true
                    );
                })
                .collect(Collectors.toList());
        return top3Achievements;
    }


    // 사용자의 모든 업적 조회
    @Transactional
    public List<MemberAchievementResponseDTO> getAllMyStatsAchievements(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Achievement> allAchievements = achievementRepository.findAll(); // 총 18개
        List<MemberAchievement> existingAchievements = memberAchievementRepository.findAllByMember(member);

        // 이미 존재하는 업적 정리
        Map<Long, MemberAchievement> achievementMap = existingAchievements.stream()
                .collect(Collectors.toMap(ma -> ma.getAchievement().getId(), ma -> ma));

        List<MemberAchievementResponseDTO> result = new ArrayList<>();

        for (Achievement achievement : allAchievements) {
            MemberAchievement ma = achievementMap.get(achievement.getId());

            // 누락된 업적 자동 생성
            if (ma == null) {
                ma = new MemberAchievement();
                ma.setMember(member);
                ma.setAchievement(achievement);
                ma.setCurrentProgress(0);
                ma.setCompleted(false);
                memberAchievementRepository.save(ma);
            }

            result.add(MemberAchievementResponseDTO.from(achievement, ma));
        }

        return result;
    }


    // 사용자가 달성한 최신 업적 3개 조회
    public List<MemberAchievementResponseDTO> getTop3StatsAchievements(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<MemberAchievement> recent3Achievements = memberAchievementRepository
                .findTop3ByMemberAndCompletedTrueOrderByCompletedAtDescUpdatedAtDesc(member);

        return recent3Achievements.stream()
                .map(MemberAchievementResponseDTO::from)
                .collect(Collectors.toList());
    }


    // 사용자가 달성한 일반 업적과 챌린지 업적 통합 최신 3개 조회
    public List<MemberRecentAchievementDTO> getTop3AllAchievements(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<MemberRecentAchievementDTO> tempList = new ArrayList<>();

        // 달성한 일반 업적
        List<MemberAchievement> achievements = memberAchievementRepository
                .findTop3ByMemberAndCompletedTrueOrderByCompletedAtDescUpdatedAtDesc(member);

        for (MemberAchievement a : achievements) {
            tempList.add(new MemberRecentAchievementDTO(
                    a.getAchievement().getAchievementName(),
                    a.getAchievement().getAchievementImageUrl(),
                    true,
                    a.getCompletedAt()
            ));
        }

        // 달성한 챌린지 업적
        List<ChallengeAchievement> challengeAchievements = challengeAchievementRepository.findByMemberId(memberId);

        Set<Long> challengeIds = challengeAchievements.stream()
                .filter(a -> a.isStep4Goal1Achieved() && a.isStep4Goal2Achieved())
                .map(ChallengeAchievement::getChallengeId)
                .collect(Collectors.toSet());

        List<Challenge> challenges = challengeRepository.findAllById(challengeIds);

        Map<Long, Challenge> challengeMap = challenges.stream()
                .collect(Collectors.toMap(Challenge::getId, c -> c));

        for (ChallengeAchievement a : challengeAchievements) {
            if (a.isStep4Goal1Achieved() && a.isStep4Goal2Achieved()) {
                Challenge challenge = challengeMap.get(a.getChallengeId());
                LocalDateTime completedAt = LocalDateTime.of(challenge.getYear(), challenge.getMonth(), 1, 0, 0);

                tempList.add(new MemberRecentAchievementDTO(
                        challenge.getAchievementName(),
                        challenge.getAchievementImageUrl(),
                        true,
                        completedAt
                ));
            }
        }

        // 정렬 및 3개 추출
        return tempList.stream()
                .sorted(Comparator.comparing(MemberRecentAchievementDTO::getCompletedAt).reversed())
                .limit(3)
                .map(dto -> new MemberRecentAchievementDTO(
                        dto.getAchievementName(),
                        dto.getAchievementImageUrl(),
                        dto.isCompleted(),
                        dto.getCompletedAt()
                ))
                .collect(Collectors.toList());
    }
}
