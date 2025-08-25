package com.penglobe.server.repository;
import com.penglobe.server.domain.user.UserCounters;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCountersRepository extends JpaRepository<UserCounters, Long> {
}
