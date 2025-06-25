package com.simpaylog.generatorcore.repository;

import com.simpaylog.generatorcore.entity.IncomeLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeLevelRepository extends JpaRepository<IncomeLevel, Long> {
}
