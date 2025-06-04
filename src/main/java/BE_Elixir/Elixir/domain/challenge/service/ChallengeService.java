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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final S3Service s3Service;
    private final IngredientRepository ingredientRepository;

    // 챌린지 등록하기
    @Transactional
    public ChallengeResponseDTO registerChallenge(
            ChallengeRequestDTO dto,
            MultipartFile image,
            MultipartFile grayImage
    ) {
        Challenge challenge = ChallengeRequestDTO.from(dto);

        // 업적 컬러 이미지
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = s3Service.upload(image, "challenge/achievement-color");
                challenge.setAchievementImageUrl(imageUrl);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
            }
        }

        // 업적 흑백 이미지
        if (grayImage != null && !grayImage.isEmpty()) {
            try {
                String grayImageUrl = s3Service.upload(grayImage, "challenge/achievement-gray");
                challenge.setGrayAchievementImageUrl(grayImageUrl);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
            }
        }

        challengeRepository.save(challenge);

        return new ChallengeResponseDTO(challenge);
    }
    
    // 연도 별 챌린지 조회하기
    @Transactional(readOnly = true)
    public List<ChallengeListResponseDTO> getChallengesByYear(int year) {
        List<Challenge> challenges = challengeRepository.findByYear(year);
        return challenges.stream()
                .map(ChallengeListResponseDTO::new)
                .toList();
    }

    // 특정 챌린지 상세조회하기
    @Transactional(readOnly = true)
    public ChallengeDetailResponseDTO getChallengeDetail(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_NOT_FOUND));

        // 챌린지 month
        int month = challenge.getMonth();
        // 제철 식재료
        List<Ingredient> ingredients = ingredientRepository.findByChallengeMonth(month);
        List<String> ingredientNames = ingredients.stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());
        return new ChallengeDetailResponseDTO(challenge, ingredientNames);
    }
}