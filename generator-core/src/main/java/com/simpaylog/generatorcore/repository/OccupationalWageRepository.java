package com.simpaylog.generatorcore.repository;

import com.simpaylog.generatorcore.entity.OccupationalWage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OccupationalWageRepository extends JpaRepository<OccupationalWage, Long> {
}
