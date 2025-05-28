package BE_Elixir.Elixir.domain.member.service;

import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeAchievementRepository;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeRepository;
import BE_Elixir.Elixir.domain.member.dto.request.MemberProfileRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.request.SignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.request.SurveyRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.response.MemberAchievementResponseDTO;
import BE_Elixir.Elixir.domain.member.dto.response.MemberProfileResponseDTO;
import BE_Elixir.Elixir.domain.member.dto.response.MemberResponseDTO;
import BE_Elixir.Elixir.domain.member.dto.response.SurveyResponseDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.domain.recipe.dto.response.RecipeImageResponseDTO;
import BE_Elixir.Elixir.domain.recipe.entity.Recipe;
import BE_Elixir.Elixir.domain.recipe.entity.RecipeEvent;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeEventRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.exception.EmailVerificationCodeExpiredException;
import BE_Elixir.Elixir.global.exception.EmailVerificationCodeMismatchException;
import BE_Elixir.Elixir.global.exception.OccupiedException;
import BE_Elixir.Elixir.global.email.EmailService;
import BE_Elixir.Elixir.global.redis.RedisEmailVerificationService;
import BE_Elixir.Elixir.global.redis.RedisService;
import BE_Elixir.Elixir.global.redis.dto.EmailVerificationDTO;
import BE_Elixir.Elixir.global.s3.S3Service;
import BE_Elixir.Elixir.global.security.JwtProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.*;
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
    private final RedisEmailVerificationService redisMailVerificationService;
    private final S3Service s3Service;
    private final EmailService mailService;

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

    // 이메일 인증 요청하기
    public void sendVerificationCode(String email) throws MessagingException, UnsupportedEncodingException, JsonProcessingException {
        // 해당 이메일의 회원이 존재하는지 검증
        memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

        // 인증코드 만들기 및 메일 보내기
        String key = mailService.sendMail(email);

        // 메일 전송 시각 저장
        mailService.setMailSendTime(Instant.now());

        // redis에 인증 관련 정보 저장
        redisMailVerificationService.saveVerificationCode(email, key, mailService.getMailSendTime());

    }

    // 인증번호 검증하기
    public boolean verifyCode(String email, String code) throws JsonProcessingException {
        EmailVerificationDTO dto = redisMailVerificationService.getVerification(email);
        // 인증 유효, 유효하지 않음, 시간 초과

        // 인증 가능한 최대 시간 계산
        Instant time = dto.getEmailSendTime().plus(mailService.getValidityDuration());

        // 유효시간 초과
        if (Instant.now().isAfter(time)) {
            throw new EmailVerificationCodeExpiredException(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }
        // 코드 불일치
        if (!code.equals(dto.getCode())) {
            throw new EmailVerificationCodeMismatchException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        // Redis에 저장된 인증 정보 삭제
        redisMailVerificationService.deleteVerification(email);

        return true;
    }

    // 비밀번호 수정하기
    public void updatePassword(String email, String newPassword) {
        Member member = memberRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

        // 인코딩 및 비밀번호 설정
        member.setPassword(passwordEncoder.encode(newPassword));

        memberRepository.save(member);
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

    // 프로필 수정 시, 얻은 칭호 목록 조회
    public List<String> getTitles(Long memberId) {
        // memberId 기반 챌린지 최종 달성 여부 조회 및
        List<Long> achievedChallengeIds = challengeAchievementRepository.findByMemberId(memberId).stream()
                .filter(ChallengeAchievement::isAllGoalsAchieved)
                .map(ChallengeAchievement::getChallengeId)
                .collect(Collectors.toList());

        // 업적명 조회
        return challengeRepository.findAllById(achievedChallengeIds).stream()
                .map(Challenge::getAchievementName)
                .collect(Collectors.toList());
    }

    // 로그인한 사용자 프로필 수정하기
    public MemberResponseDTO updateMemberProfile(Long memberId, MemberProfileRequestDTO dto, MultipartFile image) throws IOException {
        // 기존 프로필 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. id: " + memberId));

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
            String imageUrl = s3Service.upload(image, "member");
            member.setProfileUrl(imageUrl);
        }

        // 젠더 수정
        if (dto.getGender() != null) {
            member.setGender(dto.getGender());
        }

        // 생년 수정
        if (dto.getBirthYear() != null) {
            member.setGender(dto.getGender());
        }

        memberRepository.save(member);

        return MemberResponseDTO.builder()
                .id(memberId)
                .nickname(member.getNickname())
                .title(member.getTitle())
                .profileUrl(member.getProfileUrl())
                .gender(member.getGender())
                .birthYear(member.getBirthYear())
                .build();
    }

    // 사용자 프로필 조회 (칭호, 닉네임, 프로필사진, 팔로워 수, 팔로잉 수)
    public MemberProfileResponseDTO getMemberProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new OccupiedException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberProfileResponseDTO.builder()
                .id(memberId)
                .nickname(member.getNickname())
                .title(member.getTitle())
                .profileUrl(member.getProfileUrl())
                .followerCount(member.getFollowers().size())
                .followingCount(member.getFollowings().size())
                .build();
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

    // 로그인한 사용자의 달성한 업적 최신 3개 조회하기
    public List<MemberAchievementResponseDTO> getTop3Achievements(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new OccupiedException(ErrorCode.MEMBER_NOT_FOUND));

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
        List<MemberAchievementResponseDTO> top3Achievements = achievements.stream()
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
                    return new MemberAchievementResponseDTO(
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

    // 설문조사 결과 조회
    public SurveyResponseDTO getSurvey(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new OccupiedException(ErrorCode.MEMBER_NOT_FOUND));

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
                .orElseThrow(() -> new OccupiedException(ErrorCode.MEMBER_NOT_FOUND));

        List<String> allergies = dto.getAllergies();
        if (allergies != null) {
            resetAllergies(member);
            applyAllergies(member, allergies);
        }

        // meal style 값 세팅
        List<String> mealStyles = dto.getMealStyles();
        if (mealStyles != null) {
            resetMealStyles(member);
            applyMealStyles(member, mealStyles);
        }

        // recipe style 값 세팅
        List<String> recipeStyles = dto.getRecipeStyles();
        if (recipeStyles != null) {
            resetRecipeStyles(member);
            applyRecipeStyles(member, recipeStyles);
        }

        // reason 값 세팅
        List<String> reasons = dto.getReasons();
        if (reasons != null) {
            resetReasons(member);
            applyReasons(member, reasons);
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



    // 설문조사 결과를 member 객체에 적용 - 알러지
    private void applyAllergies(Member member, List<String> allergies) {
        for (String allergy : allergies) {
            switch (allergy) {
                case "알류" -> member.setAllergy_알류(true);
                case "우유" -> member.setAllergy_우유(true);
                case "각류" -> member.setAllergy_각류(true);
                case "밀류" -> member.setAllergy_밀류(true);
                case "유제품" -> member.setAllergy_유제품(true);
                case "메밀" -> member.setAllergy_메밀(true);
                case "땅콩" -> member.setAllergy_땅콩(true);
                case "대두" -> member.setAllergy_대두(true);
                case "밀" -> member.setAllergy_밀(true);
                case "고등어" -> member.setAllergy_고등어(true);
                case "돼지고기" -> member.setAllergy_돼지고기(true);
                case "복숭아" -> member.setAllergy_복숭아(true);
                case "토마토" -> member.setAllergy_토마토(true);
                case "아황산류" -> member.setAllergy_아황산류(true);
                case "호두" -> member.setAllergy_호두(true);
                case "닭고기" -> member.setAllergy_닭고기(true);
                case "쇠고기" -> member.setAllergy_쇠고기(true);
                case "오징어" -> member.setAllergy_오징어(true);
                case "조개류" -> member.setAllergy_조개류(true);
                case "굴" -> member.setAllergy_굴(true);
                case "전복" -> member.setAllergy_전복(true);
                case "홍합" -> member.setAllergy_홍합(true);
                case "잣" -> member.setAllergy_잣(true);
            }
        }
    }

    // 설문조사 결과를 member 객체에 적용 - 식사 스타일
    private void applyMealStyles(Member member, List<String> styles) {
        for (String style : styles) {
            switch (style) {
                case "고기위주" -> member.setMealStyle_고기위주(true);
                case "채소위주" -> member.setMealStyle_채소위주(true);
                case "혼합식" -> member.setMealStyle_혼합식(true);
            }
        }
    }

    // 설문조사 결과를 member 객체에 적용 - 레시피 스타일
    private void applyRecipeStyles(Member member, List<String> styles) {
        for (String style : styles) {
            switch (style) {
                case "한식" -> member.setRecipeStyle_한식(true);
                case "중식" -> member.setRecipeStyle_중식(true);
                case "일식" -> member.setRecipeStyle_일식(true);
                case "양식" -> member.setRecipeStyle_양식(true);
                case "디저트" -> member.setRecipeStyle_디저트(true);
                case "음료_차" -> member.setRecipeStyle_음료_차(true);
                case "양념_소스_잼" -> member.setRecipeStyle_양념_소스_잼(true);
            }
        }
    }

    // 설문조사 결과를 member 객체에 적용 - 식단 이유
    private void applyReasons(Member member, List<String> reasons) {
        for (String reason : reasons) {
            switch (reason) {
                case "항산화강화" -> member.setReason_항산화강화(true);
                case "혈당조절" -> member.setReason_혈당조절(true);
                case "염증감소" -> member.setReason_염증감소(true);
            }
        }
    }

    // 알러지 필드 모두 false로 초기화
    private void resetAllergies(Member member) {
        member.setAllergy_알류(false);
        member.setAllergy_우유(false);
        member.setAllergy_각류(false);
        member.setAllergy_밀류(false);
        member.setAllergy_유제품(false);
        member.setAllergy_메밀(false);
        member.setAllergy_땅콩(false);
        member.setAllergy_대두(false);
        member.setAllergy_밀(false);
        member.setAllergy_고등어(false);
        member.setAllergy_돼지고기(false);
        member.setAllergy_복숭아(false);
        member.setAllergy_토마토(false);
        member.setAllergy_아황산류(false);
        member.setAllergy_호두(false);
        member.setAllergy_닭고기(false);
        member.setAllergy_쇠고기(false);
        member.setAllergy_오징어(false);
        member.setAllergy_조개류(false);
        member.setAllergy_굴(false);
        member.setAllergy_전복(false);
        member.setAllergy_홍합(false);
        member.setAllergy_잣(false);
    }

    // 식사 스타일 필드 초기화
    private void resetMealStyles(Member member) {
        member.setMealStyle_고기위주(false);
        member.setMealStyle_채소위주(false);
        member.setMealStyle_혼합식(false);
    }

    // 레시피 스타일 필드 초기화
    private void resetRecipeStyles(Member member) {
        member.setRecipeStyle_한식(false);
        member.setRecipeStyle_중식(false);
        member.setRecipeStyle_일식(false);
        member.setRecipeStyle_양식(false);
        member.setRecipeStyle_디저트(false);
        member.setRecipeStyle_음료_차(false);
        member.setRecipeStyle_양념_소스_잼(false);
    }

    // 이유 필드 초기화
    private void resetReasons(Member member) {
        member.setReason_항산화강화(false);
        member.setReason_혈당조절(false);
        member.setReason_염증감소(false);
    }

    // 다른 사용자가 업로드한 모든 레시피 조회하기
    public List<RecipeImageResponseDTO> getUserRecipes(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new OccupiedException(ErrorCode.MEMBER_NOT_FOUND));

        List<Recipe> recipes = recipeRepository.findAllByMember(member);

        return recipes.stream()
                .map(recipe -> new RecipeImageResponseDTO(recipe.getId(), recipe.getImageUrl()))
                .limit(9)
                .collect(Collectors.toList());
    }

    // 다른 사용자의 모든 챌린지 업적 정보 조회하기
    public List<MemberAchievementResponseDTO> getAllAchievementsByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new OccupiedException(ErrorCode.MEMBER_NOT_FOUND));

        List<Challenge> allChallenges = challengeRepository.findAllOrderedByYearAndMonth();
        List<ChallengeAchievement> achievements = challengeAchievementRepository.findByMemberId(member.getId());

        Map<Long, ChallengeAchievement> achievementMap = achievements.stream()
                .collect(Collectors.toMap(ChallengeAchievement::getChallengeId, a -> a));

        return allChallenges.stream()
                .map(challenge -> {
                    ChallengeAchievement achievement = achievementMap.get(challenge.getId());

                    boolean completed = (achievement != null)
                            && achievement.isStep4Goal1Achieved()
                            && achievement.isStep4Goal2Achieved();

                    String imageUrl = completed
                            ? challenge.getAchievementImageUrl()
                            : challenge.getGrayAchievementImageUrl();

                    return new MemberAchievementResponseDTO(
                            challenge.getYear(),
                            challenge.getMonth(),
                            challenge.getAchievementName(),
                            imageUrl,
                            completed
                    );
                }).collect(Collectors.toList());
    }

    // 다른 사용자의 최신 업적 3개 조회
    public List<MemberAchievementResponseDTO> getTop3AchievementsByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new OccupiedException(ErrorCode.MEMBER_NOT_FOUND));

        List<ChallengeAchievement> achievements = challengeAchievementRepository.findByMemberId(member.getId());

        Set<Long> challengeIds = achievements.stream()
                .filter(a -> a.isStep4Goal1Achieved() && a.isStep4Goal2Achieved())
                .map(ChallengeAchievement::getChallengeId)
                .collect(Collectors.toSet());

        List<Challenge> challenges = challengeRepository.findAllById(challengeIds);

        Map<Long, Challenge> challengeMap = challenges.stream()
                .collect(Collectors.toMap(Challenge::getId, c -> c));

        return achievements.stream()
                .filter(a -> a.isStep4Goal1Achieved() && a.isStep4Goal2Achieved())
                .sorted((a1, a2) -> {
                    Challenge c1 = challengeMap.get(a1.getChallengeId());
                    Challenge c2 = challengeMap.get(a2.getChallengeId());
                    int compareYear = Integer.compare(c2.getYear(), c1.getYear());
                    return (compareYear != 0)
                            ? compareYear
                            : Integer.compare(c2.getMonth(), c1.getMonth());
                })
                .limit(3)
                .map(a -> {
                    Challenge challenge = challengeMap.get(a.getChallengeId());
                    return new MemberAchievementResponseDTO(
                            challenge.getYear(),
                            challenge.getMonth(),
                            challenge.getAchievementName(),
                            challenge.getAchievementImageUrl(),
                            true
                    );
                })
                .collect(Collectors.toList());
    }
}