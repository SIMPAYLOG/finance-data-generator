package com.simpaylog.generatorcore.repository;

import com.simpaylog.generatorcore.dto.UserInfoDto;
import com.simpaylog.generatorcore.dto.analyze.AgeStat;
import com.simpaylog.generatorcore.dto.analyze.GenderStat;
import com.simpaylog.generatorcore.dto.analyze.OccupationCodeStat;
import com.simpaylog.generatorcore.entity.User;
import com.simpaylog.generatorcore.entity.dto.TransactionUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Modifying
    @Query("DELETE FROM User")
    void deleteAllUsers();

    @Query("SELECT new com.simpaylog.generatorcore.dto.analyze.GenderStat(" +
            "       CAST(SUM(CASE WHEN u.gender = 'M' THEN 1 ELSE 0 END) as int), " +
            "       CAST(SUM(CASE WHEN u.gender = 'F' THEN 1 ELSE 0 END) as int)) " +
            "FROM User u " +
            "WHERE u.sessionId = :sessionId")
    GenderStat analyzeGender(String sessionId);

    @Query("SELECT new com.simpaylog.generatorcore.dto.analyze.AgeStat(u.age, COUNT(u)) " +
            "FROM User u " +
            "WHERE u.sessionId = :sessionId " +
            "GROUP BY u.age ORDER BY u.age")
    List<AgeStat> analyzeAgeGroup(String sessionId);

    @Query("SELECT new com.simpaylog.generatorcore.dto.analyze.OccupationCodeStat(u.occupationCode, COUNT(u)) " +
            "FROM User u " +
            "WHERE u.sessionId = :sessionId " +
            "GROUP BY u.occupationCode ORDER BY COUNT(u) DESC")
    List<OccupationCodeStat> analyzeOccupation(String sessionId);

    @Query("SELECT NEW com.simpaylog.generatorcore.dto.UserInfoDto(u.name, u.gender, u.age, u.userBehaviorProfile.preferenceId, u.occupationName) " +
            "FROM User u " +
            "WHERE u.sessionId = :sessionId")
    List<UserInfoDto> findAllSimpleInfo(String sessionId);

    @Query("SELECT NEW com.simpaylog.generatorcore.entity.dto.TransactionUserDto(" +
            "u.id, " +
            "u.sessionId," +
            "u.decile, " +
            "u.balance, " +
            "p.preferenceId, " +
            "p.wageType, " +
            "p.autoTransferDayOfMonth, " +
            "p.activeHours, " +
            "p.incomeValue)" +
            "FROM User u JOIN u.userBehaviorProfile p " +
            "WHERE u.sessionId = :sessionId")
    List<TransactionUserDto> findAllTransactionUserDtosBySessionId(String sessionId);

    Page<User> findAllBySessionIdOrderByName(Pageable pageable, String sessionId);

    void deleteUsersBySessionId(String sessionId);

    long countUsersBySessionId(String sessionId);

    Optional<User> findUserBySessionIdAndId(String sessionId, Long userId);

    @Query("SELECT u.id FROM User u WHERE u.age = :ageGroup AND u.sessionId = :sessionId")
    List<Long> findUserIdsByAgeGroup(
            @Param("ageGroup") int ageGroup,
            @Param("sessionId") String sessionId
    );
}
