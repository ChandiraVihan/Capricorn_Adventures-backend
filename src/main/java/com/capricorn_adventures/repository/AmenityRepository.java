package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {

}
