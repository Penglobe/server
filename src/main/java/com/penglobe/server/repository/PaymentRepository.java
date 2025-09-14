package com.penglobe.server.repository;

import com.penglobe.server.domain.user.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<com.penglobe.server.domain.user.Payment, Long> {
    Optional<Payment> findByMerchantUid(String merchantUid);
}
