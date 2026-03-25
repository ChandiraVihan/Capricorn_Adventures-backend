package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.AdventureSchedule;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdventureScheduleRepository extends JpaRepository<AdventureSchedule, Long> {

    @Query("""
            select s
            from AdventureSchedule s
            join fetch s.adventure a
            where s.id = :scheduleId
            """)
    Optional<AdventureSchedule> findByIdWithAdventure(@Param("scheduleId") Long scheduleId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from AdventureSchedule s
            join fetch s.adventure a
            where s.id = :scheduleId
            """)
    Optional<AdventureSchedule> findByIdForUpdateWithAdventure(@Param("scheduleId") Long scheduleId);
}
