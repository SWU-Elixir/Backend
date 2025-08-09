package BE_Elixir.Elixir.domain.challenge.service;

import BE_Elixir.Elixir.domain.challenge.dto.request.ChallengeRequestDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeDetailResponseDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeListResponseDTO;
import BE_Elixir.Elixir.domain.challenge.dto.response.ChallengeResponseDTO;
import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeRepository;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeService 테스트")
class ChallengeServiceTest {
    @InjectMocks
    ChallengeService challengeService;

    @Mock ChallengeRepository challengeRepository;
    @Mock IngredientRepository ingredientRepository;
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
        @DisplayName("예외: 이미지 업로드 중 IOException 발생 시 예외 던짐")
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

    @Nested
    @DisplayName("연도별 챌린지 조회 테스트")
    class GetChallengesByYearTests {

        @Test
        @DisplayName("성공: 해당 연도 챌린지 리스트 반환")
        void getChallengesByYear_success() {
            // given
            Challenge challenge1 = new Challenge();
            Challenge challenge2 = new Challenge();
            List<Challenge> challenges = List.of(challenge1, challenge2);

            given(challengeRepository.findByYear(2023)).willReturn(challenges);

            // when
            List<ChallengeListResponseDTO> responses = challengeService.getChallengesByYear(2023);

            // then
            assertThat(responses).hasSize(2);
            then(challengeRepository).should(times(1)).findByYear(2023);
        }

        @Test
        @DisplayName("성공: 해당 연도 챌린지가 없으면 빈 리스트 반환")
        void getChallengesByYear_empty() {
            // given
            given(challengeRepository.findByYear(2023)).willReturn(List.of());

            // when
            List<ChallengeListResponseDTO> responses = challengeService.getChallengesByYear(2023);

            // then
            assertThat(responses).isEmpty();
            then(challengeRepository).should(times(1)).findByYear(2023);
        }
    }

    @Nested
    @DisplayName("챌린지 상세 조회 테스트")
    class GetChallengeDetailTests {

        @Test
        @DisplayName("성공: 챌린지 상세와 제철 식재료 이름 리스트 반환")
        void getChallengeDetail_success() {
            // given
            Challenge challenge = new Challenge();
            challenge.setId(1L);
            challenge.setMonth(5);
            challenge.setName("7월 챌린지");

            Ingredient ingredient1 = new Ingredient();
            ingredient1.setName("감자");
            Ingredient ingredient2 = new Ingredient();
            ingredient2.setName("고구마");

            given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));
            given(ingredientRepository.findByChallengeMonth(5)).willReturn(List.of(ingredient1, ingredient2));

            // when
            ChallengeDetailResponseDTO response = challengeService.getChallengeDetail(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("7월 챌린지");
            assertThat(response.getIngredients()).containsExactly("감자", "고구마");

            then(challengeRepository).should(times(1)).findById(1L);
            then(ingredientRepository).should(times(1)).findByChallengeMonth(5);
        }

        @Test
        @DisplayName("예외: 챌린지가 없으면 예외 발생")
        void getChallengeDetail_notFound() {
            // given
            given(challengeRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> challengeService.getChallengeDetail(1L))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CHALLENGE_NOT_FOUND);

            then(challengeRepository).should(times(1)).findById(1L);
            then(ingredientRepository).should(never()).findByChallengeMonth(anyInt());
        }
    }
}