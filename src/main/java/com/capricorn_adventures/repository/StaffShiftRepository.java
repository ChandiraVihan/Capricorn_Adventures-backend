package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.StaffShift;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffShiftRepository extends JpaRepository<StaffShift, Long> {

    @Query("""
            select shift
            from StaffShift shift
            join fetch shift.staff staff
            where shift.shiftStartAt <= :now
              and (shift.shiftEndAt is null or shift.shiftEndAt > :now)
            order by shift.department asc, shift.shiftStartAt asc
            """)
    List<StaffShift> findCurrentShifts(@Param("now") LocalDateTime now);

    @Query("""
            select shift
            from StaffShift shift
            join fetch shift.staff staff
            where shift.shiftStartAt >= :dayStart
              and shift.shiftStartAt < :nextDayStart
            order by shift.shiftStartAt asc
            """)
    List<StaffShift> findShiftsStartingWithinDay(@Param("dayStart") LocalDateTime dayStart,
                                                 @Param("nextDayStart") LocalDateTime nextDayStart);
}
