package BE_Elixir.Elixir.domain.dietLog.service;

import BE_Elixir.Elixir.domain.dietLog.dto.DietLogRequestDTO;
import BE_Elixir.Elixir.domain.dietLog.entity.DietLog;
import BE_Elixir.Elixir.domain.dietLog.entity.DietLogIngredient;
import BE_Elixir.Elixir.domain.dietLog.repository.DietLogRepository;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.DietLogType;
import BE_Elixir.Elixir.global.s3.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DietLogService {

    private final DietLogRepository dietLogRepository;
    private final MemberRepository memberRepository;
    private final IngredientRepository ingredientRepository;
    private final S3Service s3Service;

    // 식단 기록하기
    public DietLog createDietLog(DietLogRequestDTO dto, Long memberId, MultipartFile image) {
        try {
            // 회원 조회
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. member id: " + memberId));

            // 식단 타입을 enum 타입으로 변환
            DietLogType typeEnum = DietLogType.valueOf(dto.getType().toUpperCase());

            // 객체 생성
            DietLog dietLog = dto.toEntity(typeEnum, member);

            // 식단 이미지 업로드 및 url 세팅
            if (image != null && !image.isEmpty()) {
                String imageUrl = s3Service.upload(image, "diet_log");
                log.info("이미지 S3에 업로드 성공 imageUrl: {}", imageUrl);
                dietLog.setImageUrl(imageUrl);
            }

            // Ingredient ID 목록으로 Ingredient 엔티티들 조회
            List<Ingredient> ingredients = ingredientRepository.findAllById(dto.getIngredientTagId());

            // DietLogIngredient 리스트 생성
            List<DietLogIngredient> dietLogIngredients = ingredients.stream()
                    .map(ingredient -> new DietLogIngredient(dietLog, ingredient))
                    .toList();

            // DietLog에 연관관계 설정
            dietLog.setIngredientTags(dietLogIngredients);

            return dietLogRepository.save(dietLog);

        } catch (Exception e) {
            throw new RuntimeException("식단 기록 중 오류가 발생했습니다.", e);
        }
    }

    // 식단 기록 삭제하기
    public void deleteDietLog(Long dietLogId, Long memberId) {

    }
}
