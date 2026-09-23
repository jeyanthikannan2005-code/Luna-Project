package com.lunette.gifts.repository;

import com.lunette.gifts.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByPhone(String phone);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<UserAccount> findByRole(String role);
    List<UserAccount> findByRoleOrderByCreatedAtDesc(String role);
}
