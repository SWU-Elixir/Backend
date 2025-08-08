package BE_Elixir.Elixir.domain.challenge.service;

import BE_Elixir.Elixir.domain.challenge.dto.request.ChallengeRequestDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeResponseDTO;
import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeRepository;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import BE_Elixir.Elixir.global.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeService 테스트")
class ChallengeServiceTest {
    @InjectMocks
    ChallengeService challengeService;

    @Mock ChallengeRepository challengeRepository;
    @Mock S3Service s3Service;

    private ChallengeRequestDTO dto;
    private MultipartFile imageFile;
    private MultipartFile grayImageFile;

    @BeforeEach
    void setUp() {
        dto = new ChallengeRequestDTO();
        dto.setName("7월 밸런스 푸드 챌린지");
        dto.setDescription("1단계\n• 하루 한 끼 재철 식재료(열무, 깻잎, 가지, 블루베리)를 포함한 식사 기록\n...");
        dto.setPurpose("무더위가 본격화되는 7월, 뜨거운 햇볕과 높은 기온으로 인한 피로 누적과 입맛 저하를 극복하고, 건강한 여름나기를 위한 식습관이 필요합니다.");
        dto.setMonth(7);
        dto.setYear(2025);
        dto.setAchievementName("쿨밸런서");

        dto.setStep1Goal1Type("DIET_SEASONAL_ONCE");
        dto.setStep1Goal2Type("DIET_LUNCH");
        dto.setStep2Goal1Type("DIET_SEASONAL_ONCE");
        dto.setStep2Goal2Type("DIET_BREAKFAST");
        dto.setStep3Goal1Type("DIET_SEASONAL_ONCE");
        dto.setStep3Goal2Type("DIET_THREE_MEALS");
        dto.setStep4Goal1Type("RECIPE_SEASONAL_ONCE");
        dto.setStep4Goal2Type("DIET_60_A_MONTH");

        dto.setStep1Goal1Desc("하루 한 끼 재철 식재료를 포함한 식사 기록");
        dto.setStep1Goal2Desc("점심 챙겨 먹기");
        dto.setStep2Goal1Desc("하루 한 끼 재철 식재료를 포함한 식사 기록");
        dto.setStep2Goal2Desc("아침 챙겨 먹기");
        dto.setStep3Goal1Desc("하루 한 끼 재철 식재료를 포함한 식사 기록");
        dto.setStep3Goal2Desc("하루 3끼 식단 기록");
        dto.setStep4Goal1Desc("재철 식재료를 활용한 레시피 작성");
        dto.setStep4Goal2Desc("1달 동안 누적 60끼 식단 기록");


        imageFile = new MockMultipartFile("image", "color.png", "image/png", "dummy image content".getBytes());
        grayImageFile = new MockMultipartFile("grayImage", "gray.png", "image/png", "dummy gray image content".getBytes());
    }

    @Nested
    @DisplayName("챌린지 등록 테스트")
    class RegisterChallengeTests {

        @Test
        @DisplayName("성공: 이미지가 포함된 챌린지 등록")
        void registerChallenge_success_withImages() throws IOException {
            // given
            given(s3Service.upload(imageFile, "challenge/achievement-color")).willReturn("http://s3.com/color.png");
            given(s3Service.upload(grayImageFile, "challenge/achievement-gray")).willReturn("http://s3.com/gray.png");
            given(challengeRepository.save(any(Challenge.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            ChallengeResponseDTO response = challengeService.registerChallenge(dto, imageFile, grayImageFile);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAchievementImageUrl()).isEqualTo("http://s3.com/color.png");
            assertThat(response.getGrayAchievementImageUrl()).isEqualTo("http://s3.com/gray.png");
            then(s3Service).should(times(1)).upload(imageFile, "challenge/achievement-color");
            then(s3Service).should(times(1)).upload(grayImageFile, "challenge/achievement-gray");
            then(challengeRepository).should(times(1)).save(any(Challenge.class));
        }

        @Test
        @DisplayName("성공: 이미지 없이 챌린지 등록")
        void registerChallenge_success_withoutImages() throws IOException {
            // given
            given(challengeRepository.save(any(Challenge.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            ChallengeResponseDTO response = challengeService.registerChallenge(dto, null, null);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getAchievementImageUrl()).isNull();
            assertThat(response.getGrayAchievementImageUrl()).isNull();
            then(s3Service).should(never()).upload(any(), anyString());
            then(challengeRepository).should(times(1)).save(any(Challenge.class));
        }

        @Test
        @DisplayName("실패: 이미지 업로드 중 IOException 발생 시 예외 던짐")
        void registerChallenge_fail_uploadIOException() throws IOException {
            // given
            given(s3Service.upload(imageFile, "challenge/achievement-color")).willThrow(IOException.class);

            // when & then
            assertThatThrownBy(() -> challengeService.registerChallenge(dto, imageFile, grayImageFile))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.S3_UPLOAD_ERROR);

            then(s3Service).should(times(1)).upload(imageFile, "challenge/achievement-color");
            then(challengeRepository).should(never()).save(any());
        }
    }
}