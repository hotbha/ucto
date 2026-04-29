package com.ucto.backend.repository;

import com.ucto.backend.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserId(Long userId);
    Optional<Subscription> findByChargebeeSubscriptionId(String chargebeeSubscriptionId);
}
