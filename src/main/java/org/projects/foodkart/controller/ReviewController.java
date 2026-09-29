package org.projects.foodkart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.projects.foodkart.dto.request.ReviewRequest;
import org.projects.foodkart.dto.response.ApiResponse;
import org.projects.foodkart.dto.response.ReviewResponse;
import org.projects.foodkart.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.addReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Review submitted successfully", response));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByRestaurantId(@PathVariable Long restaurantId) {
        List<ReviewResponse> responses = reviewService.getReviewsByRestaurantId(restaurantId);
        return ResponseEntity.ok(ApiResponse.ok("Restaurant reviews retrieved successfully", responses));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByUserId(@PathVariable Long userId) {
        List<ReviewResponse> responses = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok("User reviews retrieved successfully", responses));
    }
}
