package org.example.lv_backend.repository;

import org.example.lv_backend.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {
    List<Subscription> findByUserIdOrderByCreatedAtDesc(Long userId);

}
