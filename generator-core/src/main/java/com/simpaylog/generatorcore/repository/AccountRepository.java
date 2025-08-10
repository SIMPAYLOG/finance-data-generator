package com.simpaylog.generatorcore.repository;

import com.simpaylog.generatorcore.entity.Account;
import com.simpaylog.generatorcore.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findAccountByUser_IdAndType(Long userId, AccountType type);
}
