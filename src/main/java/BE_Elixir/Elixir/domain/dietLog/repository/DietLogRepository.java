package BE_Elixir.Elixir.domain.dietLog.repository;

import BE_Elixir.Elixir.domain.dietLog.entity.DietLog;
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
    @Query("SELECT d FROM DietLog d WHERE d.member.id = :memberId AND MONTH(d.time) = :month AND YEAR(d.time) = :year")
    List<DietLog> findByMemberIdAndYearAndMonth(
            @Param("memberId") Long memberId,
            @Param("month") int month,
            @Param("year") int year
    );

}