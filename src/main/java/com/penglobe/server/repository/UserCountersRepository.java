package com.penglobe.server.repository;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UserCountersRepository extends JpaRepository<UserCounters, Long> {
}
