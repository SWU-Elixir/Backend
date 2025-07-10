package BE_Elixir.Elixir.domain.member.repository;

import BE_Elixir.Elixir.domain.member.entity.Member;
import BE_Elixir.Elixir.global.enums.LoginType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT m.loginType FROM Member m WHERE m.email = :email")
    Optional<LoginType> findLoginTypeByEmail(String email);
}
