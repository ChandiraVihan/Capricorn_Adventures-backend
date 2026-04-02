package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.CancellationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CancellationPolicyRepository extends JpaRepository<CancellationPolicy, Long> {
    Optional<CancellationPolicy> findByCategory(String category);
}
