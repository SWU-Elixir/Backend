package BE_Elixir.Elixir.domain.achievement.service;

import BE_Elixir.Elixir.domain.achievement.dto.AchievementCategoryDTO;
import BE_Elixir.Elixir.domain.achievement.dto.AchievementProgressDTO;
import BE_Elixir.Elixir.domain.achievement.dto.AllAchievementStatusResponseDTO;
import BE_Elixir.Elixir.domain.achievement.entity.Achievement;
import BE_Elixir.Elixir.domain.achievement.entity.MemberAchievement;
import BE_Elixir.Elixir.domain.achievement.repository.MemberAchievementRepository;
import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.domain.member.entity.MemberDetails;
import BE_Elixir.Elixir.domain.member.repository.MemberRepository;
import BE_Elixir.Elixir.global.enums.AchievementType;
import BE_Elixir.Elixir.global.exception.CustomException;
import BE_Elixir.Elixir.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AchievementService {
    private final MemberAchievementRepository memberAchievementRepository;
    private final MemberRepository memberRepository;

    // 업적 통합 조회
    public AllAchievementStatusResponseDTO getMyAchievements(MemberDetails memberDetails) {
        Member member = memberRepository.findByEmail(memberDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<MemberAchievement> achievements = memberAchievementRepository.findAllByMember(member);

        Map<AchievementType, AchievementCategoryDTO> result = new HashMap<>();

        for (MemberAchievement ma : achievements) {
            AchievementType type = ma.getAchievement().getType();

            // 해당 타입이 아직 result에 없으면 새로 추가
            result.putIfAbsent(type, new AchievementCategoryDTO(0, new ArrayList<>()));

            AchievementCategoryDTO category = result.get(type);

            // currentValue 갱신 (가장 높은 값 사용)
            if (ma.getCurrentProgress() > category.getCurrentValue()) {
                category.setCurrentValue(ma.getCurrentProgress());
            }

            Achievement a = ma.getAchievement();
            AchievementProgressDTO dto = new AchievementProgressDTO(
                    a.getId(),
                    a.getAchievementName(),
                    a.getLevel(),
                    a.getTargetValue(),
                    ma.isCompleted(),
                    ma.isCompleted() ? null : ma.getCurrentProgress(),
                    ma.isCompleted() ? ma.getCompletedAt() : null
            );

            category.getAchievements().add(dto);
        }

        return new AllAchievementStatusResponseDTO(result);
    }
}
