package com.utegear.controller;

import com.utegear.dto.review.ReviewDtos;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ReviewDtos.ReviewSummary getReviews(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return reviewService.getReviewSummary(productId, principal != null ? principal.getUser() : null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDtos.ReviewItem createReview(
            @PathVariable Long productId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ReviewDtos.CreateReviewRequest request) {
        return reviewService.createReview(principal.getUser(), productId, request);
    }
}

