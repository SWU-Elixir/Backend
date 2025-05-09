package BE_Elixir.Elixir.domain.challenge.event.listener;

import BE_Elixir.Elixir.domain.challenge.entity.Challenge;
import BE_Elixir.Elixir.domain.challenge.entity.ChallengeAchievement;
import BE_Elixir.Elixir.domain.challenge.event.events.DietLogEvent;
import BE_Elixir.Elixir.domain.challenge.event.events.RecipeEvent;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeAchievementRepository;
import BE_Elixir.Elixir.domain.challenge.repository.ChallengeRepository;
import BE_Elixir.Elixir.domain.dietLog.repository.DietLogRepository;
import BE_Elixir.Elixir.domain.ingredient.entity.Ingredient;
import BE_Elixir.Elixir.domain.ingredient.repository.IngredientRepository;
import BE_Elixir.Elixir.domain.recipe.repository.RecipeRepository;
import BE_Elixir.Elixir.global.enums.ChallengeGoalType;
import BE_Elixir.Elixir.global.enums.DietLogType;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChallengeEventListener {

    private final ChallengeRepository challengeRepository;
    private final ChallengeAchievementRepository challengeAchievementRepository;
    private final IngredientRepository ingredientRepository;
    private final DietLogRepository dietLogRepository;
    private final RecipeRepository recipeRepository;

    private List<ChallengeGoalType> parseGoalTypes(Challenge challenge) {
        return List.of(
                ChallengeGoalType.valueOf(challenge.getStep1Goal1Type()),
                ChallengeGoalType.valueOf(challenge.getStep1Goal2Type()),
                ChallengeGoalType.valueOf(challenge.getStep2Goal1Type()),
                ChallengeGoalType.valueOf(challenge.getStep2Goal2Type()),
                ChallengeGoalType.valueOf(challenge.getStep3Goal1Type()),
                ChallengeGoalType.valueOf(challenge.getStep3Goal2Type()),
                ChallengeGoalType.valueOf(challenge.getStep4Goal1Type()),
                ChallengeGoalType.valueOf(challenge.getStep4Goal2Type())
        );
    }

    @EventListener
    @Transactional
    public void handleEvent(Object event) {
        Long memberId = null;

        // 이벤트가 DietLogEvent일 경우
        if (event instanceof DietLogEvent) {
            memberId = ((DietLogEvent) event).getMemberId();
        }

        // 이벤트가 RecipeCreatedEvent일 경우
        else if (event instanceof RecipeEvent) {
            memberId = ((RecipeEvent) event).getMemberId();
        }

        // 해당 사용자의 현재 진행 중인 챌린지 조회 (없으면 예외 발생)
        Challenge challenge = challengeRepository.findCurrentByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 챌린지가 없습니다."));

        // 챌린지에 대한 사용자의 달성 상태 정보 조회 (없으면 예외 발생)
        ChallengeAchievement achievement = challengeAchievementRepository
                .findByChallengeIdAndMemberId(challenge.getId(), memberId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지 달성 정보가 없습니다."));

        // 해당 챌린지가 열린 시간 (이 시간 이후의 기록만 유효)
        LocalDateTime openedAt = achievement.getOpenedAt();

        // 챌린지 단계별 목표 유형들 (총 8개) 가져오기
        List<ChallengeGoalType> goalTypes = parseGoalTypes(challenge);

        // 각 목표의 활성화 상태를 리스트로 구성 (false인 항목은 평가하지 않음)
        List<Boolean> activeFlags = List.of(
                achievement.isStep1Goal1Active(), achievement.isStep1Goal2Active(),
                achievement.isStep2Goal1Active(), achievement.isStep2Goal2Active(),
                achievement.isStep3Goal1Active(), achievement.isStep3Goal2Active(),
                achievement.isStep4Goal1Active(), achievement.isStep4Goal2Active()
        );

        // 각 목표의 달성 여부를 업데이트하는 setter 메서드 리스트
        List<Consumer<Boolean>> setters = List.of(
                achievement::setStep1Goal1Achieved, achievement::setStep1Goal2Achieved,
                achievement::setStep2Goal1Achieved, achievement::setStep2Goal2Achieved,
                achievement::setStep3Goal1Achieved, achievement::setStep3Goal2Achieved,
                achievement::setStep4Goal1Achieved, achievement::setStep4Goal2Achieved
        );

        // 8개의 목표를 순회하며 활성화된 목표에 대해 조건 충족 여부 판단
        for (int i = 0; i < goalTypes.size(); i++) {
            if (!activeFlags.get(i)) continue; // 비활성 목표는 스킵

            // 현재 목표 유형
            ChallengeGoalType goalType = goalTypes.get(i);
            // 달성 결과를 설정할 setter
            Consumer<Boolean> resultSetter = setters.get(i);

            // 목표 달성 조건을 검사하고, 결과를 setter로 업데이트
            handleGoal(goalType, memberId, openedAt, resultSetter);
        }

        // 변경된 달성 정보를 저장소에 저장 (DB 반영)
        challengeAchievementRepository.save(achievement);
    }


    private void handleGoal(ChallengeGoalType goalType, Long memberId, LocalDateTime openedAt, Consumer<Boolean> resultSetter) {
        boolean achieved = false;

        // 해당 사용자의 현재 진행 중인 챌린지 조회 (없으면 예외 발생)
        Challenge challenge = challengeRepository.findCurrentByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 챌린지가 없습니다."));


        // 챌린지 월 가져오기
        int month = challenge.getMonth();

        // 제철 식재료 가져오기
        List<Ingredient> ingredients = ingredientRepository.findByChallengeMonth(month);
        List<String> ingredientNames = ingredients.stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());

        switch (goalType) {
            case DIET_BREAKFAST ->
                    achieved = dietLogRepository.existsByMemberIdAndTypeAndTimeAfter(memberId, DietLogType.아침, openedAt);
            case DIET_LUNCH ->
                    achieved = dietLogRepository.existsByMemberIdAndTypeAndTimeAfter(memberId, DietLogType.점심, openedAt);
            case DIET_THREE_MEALS -> {
                boolean hasBreakfast = dietLogRepository.existsByMemberIdAndTypeAndTimeAfter(memberId, DietLogType.아침, openedAt);
                boolean hasLunch = dietLogRepository.existsByMemberIdAndTypeAndTimeAfter(memberId, DietLogType.점심, openedAt);
                boolean hasDinner = dietLogRepository.existsByMemberIdAndTypeAndTimeAfter(memberId, DietLogType.저녁, openedAt);
                achieved = hasBreakfast && hasLunch && hasDinner;
            }
            case DIET_SEASONAL_ONCE -> {
                // 사용자의 식단에 포함된 식재료 목록
                List<String> dietIngredients = dietLogRepository.findIngredientsByMemberIdAndTimeAfter(memberId, openedAt);

                // 식단에 사용된 식재료와 제철 식재료 비교
                boolean hasSeasonalIngredient = dietIngredients.stream()
                        .anyMatch(ingredientNames::contains);

                achieved = hasSeasonalIngredient;
            }
            case RECIPE_SEASONAL_ONCE -> {
                // 레시피에 제철 식재료가 포함된 경우
                List<String> recipeIngredients = recipeRepository.findIngredientsByMemberIdAndTimeAfter(memberId, openedAt);

                // 레시피에 사용된 식재료와 제철 식재료 비교
                boolean hasSeasonalIngredient = recipeIngredients.stream()
                        .anyMatch(ingredientNames::contains);

                achieved = hasSeasonalIngredient;
            }
            case DIET_60_A_MONTH -> {
                // 누적 식단 기록은 openedAt 관계없이!
                int count = dietLogRepository.countByMemberIdThisMonth(memberId);
                achieved = count >= 60;
            }
        }

        resultSetter.accept(achieved);
    }
}
