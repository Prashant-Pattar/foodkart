package org.projects.foodkart.service;

import org.projects.foodkart.dto.request.ReviewRequest;
import org.projects.foodkart.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse addReview(ReviewRequest request);
    List<ReviewResponse> getReviewsByRestaurantId(Long restaurantId);
    List<ReviewResponse> getReviewsByUserId(Long userId);
}
