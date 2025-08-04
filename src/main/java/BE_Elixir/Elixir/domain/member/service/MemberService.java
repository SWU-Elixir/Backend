package BE_Elixir.Elixir.domain.member.service;

import BE_Elixir.Elixir.domain.auth.dto.response.TokenResponseDTO;
import BE_Elixir.Elixir.domain.member.dto.SocialSignUpDTO;
import BE_Elixir.Elixir.domain.member.dto.request.SignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.dto.request.SocialSignUpRequestDTO;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.LoginType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.email.EmailService;
import BE_Elixir.Elixir.global.redis.RedisEmailVerificationService;
import BE_Elixir.Elixir.global.redis.RedisAuthService;
import BE_Elixir.Elixir.global.redis.dto.EmailVerificationDTO;
import BE_Elixir.Elixir.global.s3.S3Service;
import BE_Elixir.Elixir.global.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberDetailsService memberDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RedisAuthService redisAuthService;
    private final RedisEmailVerificationService redisMailVerificationService;
    private final S3Service s3Service;
    private final EmailService mailService;
    // 이메일 중복 체크
    public boolean isEmailDuplicated(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 일반 회원용 회원가입 메소드
    public Member localSignUp(SignUpRequestDTO request, MultipartFile profileImage) {
        List<String> roles = new ArrayList<>();
        // USER 권한을 추가하여 데이터 추가
        roles.add("USER");

        try {
        // Member entity 값 세팅
        Member member = request.toEntity(
                passwordEncoder.encode(request.getPassword()), roles
        );
        member.setRoles(roles);
        member.setLoginType(LoginType.LOCAL);

        // 설문조사 결과 세팅
        // allergy 값 세팅
        List<String> allergies = request.getAllergies();
        if (allergies != null) {
            member.applyAllergies(allergies);
        }

        // meal style 값 세팅
        List<String> mealStyles = request.getMealStyles();
        if (mealStyles != null) {
            member.applyMealStyles(mealStyles);
        }

        // recipe style 값 세팅
        List<String> recipeStyles = request.getRecipeStyles();
        if (recipeStyles != null) {
            member.applyRecipeStyles(recipeStyles);
        }

        // reason 값 세팅
        List<String> reasons = request.getReasons();
        if (reasons != null) {
            member.applyReasons(reasons);
        }

        // 프로필 이미지 업로드 및 url 세팅
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String imageUrl = s3Service.upload(profileImage, "member");
                member.setProfileUrl(imageUrl);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
            }
        }

        return memberRepository.save(member);

    } catch (DataIntegrityViolationException e) {
        if (e.getMessage().toUpperCase().contains("EMAIL_UNIQUE")) {
            throw new CustomException(ErrorCode.EXISTS_MEMBER);
        }
        throw e;
        }
    }

    // 소셜 회원용 회원가입 메소드
    public SocialSignUpDTO socialSignUp(LoginType loginType, SocialSignUpRequestDTO request, MultipartFile profileImage) {
        // 소셜 회원이 맞는지 검증
        if (!loginType.isSocial()) {
            throw new CustomException(ErrorCode.LOGIN_TYPE_MISMATCH);
        }

        List<String> roles = new ArrayList<>();
        // USER 권한을 추가
        roles.add("USER");

        try {
            // Member entity 값 세팅
            Member member = request.toEntity(roles, loginType);
            member.setRoles(roles);

            // 설문조사 결과 세팅
            // allergy 값 세팅
            List<String> allergies = request.getAllergies();
            if (allergies != null) {
                member.applyAllergies(allergies);
            }

            // meal style 값 세팅
            List<String> mealStyles = request.getMealStyles();
            if (mealStyles != null) {
                member.applyMealStyles(mealStyles);
            }

            // recipe style 값 세팅
            List<String> recipeStyles = request.getRecipeStyles();
            if (recipeStyles != null) {
                member.applyRecipeStyles(recipeStyles);
            }

            // reason 값 세팅
            List<String> reasons = request.getReasons();
            if (reasons != null) {
                member.applyReasons(reasons);
            }

            // 프로필 이미지 업로드 및 url 세팅
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    String imageUrl = s3Service.upload(profileImage, "member");
                    member.setProfileUrl(imageUrl);
                } catch (IOException e) {
                    throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
                }
            } else if (request.getProfileImageUrl() != null) {
                member.setProfileUrl(request.getProfileImageUrl());
            }

            memberRepository.save(member);

            // JWT 토큰 발급
            // Spring Security Authentication 객체 생성
            MemberDetails memberDetails = memberDetailsService.loadUserByUsername(member.getEmail());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    memberDetails, "", memberDetails.getAuthorities()
            );

            // JWT 생성
            TokenResponseDTO tokenResponse = jwtProvider.generateToken(authentication);
            String refreshToken = tokenResponse.getRefreshToken();

            // Redis에 Refresh Token 저장
            redisAuthService.saveRefreshToken(member.getEmail(), refreshToken);
            log.info("[소셜 로그인] Refresh Token Redis에 저장: email={}, token={}", member.getEmail(), refreshToken);

            return SocialSignUpDTO.builder()
                    .member(member)
                    .tokenResponseDTO(tokenResponse)
                    .build();

        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().toUpperCase().contains("EMAIL_UNIQUE")) {
                throw new CustomException(ErrorCode.EXISTS_MEMBER);
            }
            throw e;
        }
    }

    // 이메일 인증 요청하기
    public void sendVerificationCode(String email) {
        // 해당 이메일의 회원이 존재하는지 검증
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 일반 가입 회원이 맞는지 검증
        if (member.getLoginType() != LoginType.LOCAL) {
            throw new CustomException(ErrorCode.EMAIL_REGISTERED_WITH_SOCIAL);
        }

        // 인증코드 만들기 및 메일 보내기
        String key = mailService.sendMail(email);

        // 메일 전송 시각 저장
        mailService.setMailSendTime(Instant.now());

        // redis에 인증 관련 정보 저장
        redisMailVerificationService.saveVerificationCode(email, key, mailService.getMailSendTime());
    }

    // 인증번호 검증하기
    public boolean verifyCode(String email, String code) {
        EmailVerificationDTO dto = redisMailVerificationService.getVerification(email);

        // 인증 가능한 최대 시간 계산
        Instant time = dto.getEmailSendTime().plus(mailService.getValidityDuration());

        // 유효시간 초과
        if (Instant.now().isAfter(time)) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }
        // 코드 불일치
        if (!code.equals(dto.getCode())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        // Redis에 저장된 인증 정보 삭제
        redisMailVerificationService.deleteVerification(email);

        return true;
    }

    // 비밀번호 수정하기
    public void updatePassword(String email, String newPassword) {
        Member member = memberRepository.findByEmail(email)
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 인코딩 및 비밀번호 설정
        member.setPassword(passwordEncoder.encode(newPassword));

        memberRepository.save(member);
    }

    // 회원 탈퇴
    public void withdraw(String email, String accessToken, String refreshToken) {
        // Access Token 검증 및 블랙리스트 처리
        if (jwtProvider.validateToken(accessToken)) {
            redisAuthService.addAccessTokenToBlacklist(accessToken);
            log.info("Access Token 블랙리스트 처리");
        } else {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        // Refresh Token이 redis에 있는지 확인 및 제거
        if (refreshToken != null && redisAuthService.isRefreshTokenValid(email, refreshToken)) {
            // redis에서 제거
            redisAuthService.removeRefreshToken(email);
            log.info("Refresh Token 무효화");
        } else {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // S3 버킷에서 프로필 이미지 삭제
        s3Service.deleteS3(member.getProfileUrl(), "member");

        // 회원 삭제
        memberRepository.delete(member);
    }
}