package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.AdventureCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdventureCategoryRepository extends JpaRepository<AdventureCategory, Long> {

    Optional<AdventureCategory> findByIdAndIsActiveTrue(Long id);

        List<AdventureCategory> findByIsActiveTrueOrderByNameAsc();

    @Query("""
            select c.id as categoryId,
                   c.name as categoryName,
                   c.thumbnailUrl as thumbnailUrl,
                   count(distinct case when upper(s.status) = 'AVAILABLE' and s.availableSlots > 0 then a.id else null end) as adventureCount
            from AdventureCategory c
            left join Adventure a on a.category = c
            left join a.schedules s
            where c.isActive = true
            group by c.id, c.name, c.thumbnailUrl
            order by c.name asc
            """)
    List<AdventureCategoryCountProjection> findActiveCategoriesWithAdventureCounts();

    @Query("""
            select c.id as categoryId,
                   c.name as categoryName,
                   c.thumbnailUrl as thumbnailUrl,
                   count(distinct case when upper(s.status) = 'AVAILABLE' and s.availableSlots > 0 then a.id else null end) as adventureCount
            from AdventureCategory c
            left join Adventure a on a.category = c
            left join a.schedules s
            where c.isActive = true
              and (:excludedCategoryId is null or c.id <> :excludedCategoryId)
            group by c.id, c.name, c.thumbnailUrl
            having count(distinct case when upper(s.status) = 'AVAILABLE' and s.availableSlots > 0 then a.id else null end) > 0
            order by count(distinct case when upper(s.status) = 'AVAILABLE' and s.availableSlots > 0 then a.id else null end) desc, c.name asc
            """)
    List<AdventureCategoryCountProjection> findSuggestedCategories(@Param("excludedCategoryId") Long excludedCategoryId);

    @Query("""
            select c
            from AdventureCategory c
            where c.isActive = true and lower(c.name) = lower(:categoryName)
            """)
    Optional<AdventureCategory> findActiveByCategoryName(@Param("categoryName") String categoryName);
}
