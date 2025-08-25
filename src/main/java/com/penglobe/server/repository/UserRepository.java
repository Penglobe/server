package com.penglobe.server.repository;
import com.penglobe.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일 기반 조회
    Optional<User> findByEmail(String email);
}
