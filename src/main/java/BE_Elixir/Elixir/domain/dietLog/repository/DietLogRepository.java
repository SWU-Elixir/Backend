package BE_Elixir.Elixir.domain.dietLog.repository;

import BE_Elixir.Elixir.domain.dietLog.entity.DietLog;
import BE_Elixir.Elixir.global.enums.DietLogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DietLogRepository extends JpaRepository<DietLog, Long> {

    // 특정 사용자의 특정 날짜 식단 전체 조회
    List<DietLog> findAllByMemberIdAndTimeBetween(Long memberId, LocalDateTime start, LocalDateTime end);

    // 최근 N일간 특정 멤버의 식단 로그 조회
    List<DietLog> findByMemberIdAndTimeAfter(Long memberId, LocalDateTime from);

    // 특정 사용자의 월별 식단 점수 조회
    @Query("SELECT d FROM DietLog d WHERE d.member.id = :memberId AND d.time BETWEEN :start AND :end")
    List<DietLog> findByMemberIdAndMonthBetween(
            @Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 챌린지 목표 조건 확인
    // - 특정 식사 종류(아침/점심/저녁) 기록
    boolean existsByMemberIdAndTypeAndTimeAfter(Long memberId, DietLogType type, LocalDateTime time);
    // - 사용자가 기록한 식단에서 사용된 식재료 이름을 가져오는 메서드
    @Query("SELECT DISTINCT dli.ingredient.name " +
            "FROM DietLog dl " +
            "JOIN dl.ingredientTags dli " +
            "WHERE dl.member.id = :memberId AND dl.time > :openedAt")
    List<String> findIngredientsByMemberIdAndTimeAfter(@Param("memberId") Long memberId, @Param("openedAt") LocalDateTime openedAt);
    // - 이번 달에 기록한 식단의 개수
    @Query("SELECT COUNT(d) FROM DietLog d WHERE d.member.id = :memberId AND d.time BETWEEN :startOfMonth AND :endOfMonth")
    int countDietLogsInMonth(@Param("memberId") Long memberId,
                             @Param("startOfMonth") LocalDateTime startOfMonth,
                             @Param("endOfMonth") LocalDateTime endOfMonth);


}