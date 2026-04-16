package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.AdventureSchedule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("""
            select s
            from AdventureSchedule s
            join fetch s.adventure a
            where s.id = :scheduleId
            """)
    Optional<AdventureSchedule> findByIdForUpdateWithAdventure(@Param("scheduleId") Long scheduleId);

                @Query("""
                                                select s
                                                from AdventureSchedule s
                                                join fetch s.adventure a
                                                where s.startDate >= :startDate
                                                        and s.startDate < :endDate
                                                order by s.startDate asc
                                                """)
                List<AdventureSchedule> findDashboardSchedulesBetween(@Param("startDate") LocalDateTime startDate,
                                                                                                                                                                                                                                         @Param("endDate") LocalDateTime endDate);

                @Query("""
                                                select s
                                                from AdventureSchedule s
                                                where s.adventure.id = :adventureId
                                                        and s.id <> :excludedScheduleId
                                                        and s.startDate > :now
                                                        and upper(s.status) = 'AVAILABLE'
                                                        and s.availableSlots >= :requiredSlots
                                                order by s.startDate asc
                                                """)
                List<AdventureSchedule> findRescheduleOptions(@Param("adventureId") Long adventureId,
                                                                                                                                                                                                        @Param("excludedScheduleId") Long excludedScheduleId,
                                                                                                                                                                                                        @Param("requiredSlots") Integer requiredSlots,
                                                                                                                                                                                                        @Param("now") java.time.LocalDateTime now);
}

