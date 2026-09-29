package org.projects.foodkart.repository;

import org.projects.foodkart.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query("SELECT DISTINCT r FROM Restaurant r JOIN r.serviceablePinCodes p WHERE p = :pinCode AND r.isActive = true")
    List<Restaurant> findByServiceablePinCode(@Param("pinCode") String pinCode);

    List<Restaurant> findByIsActiveTrue();
}
