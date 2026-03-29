package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.TrailRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrailRouteRepository extends JpaRepository<TrailRoute, Long> {
    Optional<TrailRoute> findByAdventureId(Long adventureId);
    boolean existsByAdventureId(Long adventureId);
    void deleteByAdventureId(Long adventureId);
}