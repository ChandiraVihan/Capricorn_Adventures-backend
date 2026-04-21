package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.RoomServiceOrder;
import com.capricorn_adventures.entity.RoomServiceOrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomServiceOrderRepository extends JpaRepository<RoomServiceOrder, Long> {

    @Query("""
            select distinct o
            from RoomServiceOrder o
            left join fetch o.assignedStaff s
            where o.status in :activeStatuses
              and (:floor is null or o.floorNumber = :floor)
              and (:minRoom is null or o.roomNumber >= :minRoom)
              and (:maxRoom is null or o.roomNumber <= :maxRoom)
            order by o.placedAt desc
            """)
    List<RoomServiceOrder> findActiveOrdersForDashboard(
            @Param("activeStatuses") List<RoomServiceOrderStatus> activeStatuses,
            @Param("floor") Integer floor,
            @Param("minRoom") Integer minRoom,
            @Param("maxRoom") Integer maxRoom);

    List<RoomServiceOrder> findByPlacedAtBetweenOrderByPlacedAtAsc(LocalDateTime start, LocalDateTime end);

    @Query("""
            select o
            from RoomServiceOrder o
            where o.status in :activeStatuses
              and o.lastStatusUpdatedAt <= :cutoff
              and o.staleAlertedAt is null
            """)
    List<RoomServiceOrder> findUnalertedStaleOrders(
            @Param("activeStatuses") List<RoomServiceOrderStatus> activeStatuses,
            @Param("cutoff") LocalDateTime cutoff);
}
