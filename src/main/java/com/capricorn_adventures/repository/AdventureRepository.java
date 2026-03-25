package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.Adventure;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdventureRepository extends JpaRepository<Adventure, Long> {

    @Query("""
            select distinct a
            from Adventure a
            join a.category c
            join a.schedules s
            where c.isActive = true
              and upper(s.status) = 'AVAILABLE'
              and s.availableSlots > 0
              and (:categoryId is null or c.id = :categoryId)
              and (:minPrice is null or a.basePrice >= :minPrice)
              and (:maxPrice is null or a.basePrice <= :maxPrice)
            order by a.name asc
            """)
    List<Adventure> findBrowseAdventures(@Param("categoryId") Long categoryId,
                                         @Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice);
}
