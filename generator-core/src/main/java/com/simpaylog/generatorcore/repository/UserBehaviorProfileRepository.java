package com.simpaylog.generatorcore.repository;

import com.simpaylog.generatorcore.entity.UserBehaviorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserBehaviorProfileRepository extends JpaRepository<UserBehaviorProfile, Long> {

    @Modifying
    @Query("DELETE FROM UserBehaviorProfile")
    void deleteAllProfiles();

    @Query("SELECT u.locationId FROM UserBehaviorProfile u WHERE u.id = :id")
    int findLocationIdById(@Param("id") Long id);
}
