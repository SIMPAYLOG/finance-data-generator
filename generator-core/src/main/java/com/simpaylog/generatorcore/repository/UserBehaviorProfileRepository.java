package com.simpaylog.generatorcore.repository;

import com.simpaylog.generatorcore.entity.UserBehaviorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBehaviorProfileRepository extends JpaRepository<UserBehaviorProfile, Long> {

    @Modifying
    @Query("DELETE FROM UserBehaviorProfile")
    void deleteAllProfiles();
}
