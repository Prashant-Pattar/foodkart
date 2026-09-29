package org.projects.foodkart.service.impl;

import lombok.RequiredArgsConstructor;
import org.projects.foodkart.dto.request.ReviewRequest;
import org.projects.foodkart.dto.response.ReviewResponse;
import org.projects.foodkart.entity.Restaurant;
import org.projects.foodkart.entity.Review;
import org.projects.foodkart.entity.User;
import org.projects.foodkart.repository.RestaurantRepository;
import org.projects.foodkart.repository.ReviewRepository;
import org.projects.foodkart.service.RestaurantService;
import org.projects.foodkart.service.ReviewService;
import org.projects.foodkart.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserService userService;
    private final RestaurantService restaurantService;

    @Override
    @Transactional
    public ReviewResponse addReview(ReviewRequest request) {
        User user = userService.findUserEntityById(request.getUserId());
        Restaurant restaurant = restaurantService.findRestaurantEntityById(request.getRestaurantId());

        Review review = Review.builder()
                .user(user)
                .restaurant(restaurant)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update restaurant average rating and count
        double currentAvg = (restaurant.getAverageRating() != null) ? restaurant.getAverageRating() : 0.0;
        int currentCount = (restaurant.getRatingCount() != null) ? restaurant.getRatingCount() : 0;

        double updatedAvg = ((currentAvg * currentCount) + request.getRating()) / (currentCount + 1);
        double roundedAvg = Math.round(updatedAvg * 10.0) / 10.0;

        restaurant.setAverageRating(roundedAvg);
        restaurant.setRatingCount(currentCount + 1);
        restaurantRepository.save(restaurant);

        return ReviewResponse.fromEntity(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByRestaurantId(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
