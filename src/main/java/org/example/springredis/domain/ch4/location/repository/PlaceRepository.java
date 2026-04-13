package org.example.springredis.domain.ch4.location.repository;

import org.example.springredis.domain.ch4.location.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
}
