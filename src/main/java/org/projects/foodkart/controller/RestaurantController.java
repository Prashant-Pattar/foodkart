package org.projects.foodkart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.projects.foodkart.dto.request.RestaurantRegisterRequest;
import org.projects.foodkart.dto.request.UpdateMenuRequest;
import org.projects.foodkart.dto.request.UpdatePinCodesRequest;
import org.projects.foodkart.dto.response.ApiResponse;
import org.projects.foodkart.dto.response.RestaurantResponse;
import org.projects.foodkart.service.RestaurantService;
import org.projects.foodkart.strategy.SelectionStrategyType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RestaurantResponse>> registerRestaurant(@Valid @RequestBody RestaurantRegisterRequest request) {
        RestaurantResponse response = restaurantService.registerRestaurant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Restaurant registered successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurantById(@PathVariable Long id) {
        RestaurantResponse response = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.ok("Restaurant retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAllRestaurants() {
        List<RestaurantResponse> responses = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(ApiResponse.ok("Restaurants retrieved successfully", responses));
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updateMenu(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuRequest request) {
        RestaurantResponse response = restaurantService.updateMenu(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Restaurant menu updated successfully", response));
    }

    @PutMapping("/{id}/pincodes")
    public ResponseEntity<ApiResponse<RestaurantResponse>> updatePinCodes(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePinCodesRequest request) {
        RestaurantResponse response = restaurantService.updatePinCodes(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Restaurant serviceable pin codes updated successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> searchRestaurants(
            @RequestParam String pinCode,
            @RequestParam(required = false, defaultValue = "HIGHEST_RATING") SelectionStrategyType strategy,
            @RequestParam(required = false) String item) {
        List<RestaurantResponse> responses = restaurantService.searchRestaurants(pinCode, strategy, item);
        return ResponseEntity.ok(ApiResponse.ok("Serviceable restaurants fetched successfully", responses));
    }
}
