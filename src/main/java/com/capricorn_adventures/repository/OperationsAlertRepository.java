package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.OperationsAlert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationsAlertRepository extends JpaRepository<OperationsAlert, Long> {

    @Query("""
            select alert
            from OperationsAlert alert
            join fetch alert.schedule schedule
            join fetch schedule.adventure adventure
            where alert.resolved = false
              and schedule.id in :scheduleIds
            order by alert.createdAt desc
            """)
    List<OperationsAlert> findActiveAlertsForSchedules(@Param("scheduleIds") List<Long> scheduleIds);
}