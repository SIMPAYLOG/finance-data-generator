package com.simpaylog.generatorcore.repository;

import com.simpaylog.generatorcore.dto.*;
import com.simpaylog.generatorcore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Modifying
    @Query("DELETE FROM User")
    void deleteAllUsers();

    @Query("SELECT new com.simpaylog.generatorcore.dto.GenderStats(" +
            "       CAST(SUM(CASE WHEN u.gender = 'M' THEN 1 ELSE 0 END) as int), " +
            "       CAST(SUM(CASE WHEN u.gender = 'F' THEN 1 ELSE 0 END) as int)) " +
            "FROM User u")
    GenderStats analyzeGender();

    @Query("SELECT new com.simpaylog.generatorcore.dto.AgeStats(u.age, COUNT(u)) " +
            "FROM User u GROUP BY u.age ORDER BY u.age")
    List<AgeStats> analyzeAgeGroup();

    @Query("SELECT new com.simpaylog.generatorcore.dto.OccupationCodeStats(u.occupationCode, COUNT(u)) " +
            "FROM User u GROUP BY u.occupationCode ORDER BY COUNT(u) DESC")
    List<OccupationCodeStats> analyzeOccupation();

    @Query("SELECT NEW com.simpaylog.generatorcore.dto.UserSimpleIdInfo(u.name, u.gender, u.age, u.userBehaviorProfile.preferenceId, u.occupationName) " +
            "FROM User u")
    List<UserSimpleIdInfo> findAllSimpleInfo();

    Page<User> findAllByOrderByNameAsc(Pageable pageable);
}
