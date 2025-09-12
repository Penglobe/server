package com.penglobe.server.repository;

import com.penglobe.server.domain.payment.PaymentIntent;
import com.penglobe.server.domain.payment.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;


import java.util.Optional;


public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {
    Optional<PaymentIntent> findByMerchantUid(String merchantUid);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentIntent p where p.merchantUid=:mu")
    Optional<PaymentIntent> lockByMerchantUid(@Param("mu") String merchantUid);
}