package org.projects.foodkart.repository;

import org.projects.foodkart.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    public Restaurant createRestaurant(Restaurant restaurant);
    public Restaurant findRestaurantById(long id);
    public Restaurant updateRestaurantById(long id, Restaurant restaurant);
    public void deleteRestaurantById(long id);
    public List<Restaurant> findAllRestaurants();
}
