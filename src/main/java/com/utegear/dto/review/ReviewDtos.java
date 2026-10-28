package com.utegear.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record CreateReviewRequest(
            @NotNull(message = "Số sao đánh giá không được để trống")
            @Min(value = 1, message = "Đánh giá tối thiểu 1 sao")
            @Max(value = 5, message = "Đánh giá tối đa 5 sao")
            Integer rating,

            @NotBlank(message = "Nội dung đánh giá không được để trống")
            @Size(min = 50, max = 2000, message = "Nội dung đánh giá phải có tối thiểu 50 ký tự")
            String comment,

            List<String> imageUrls
    ) {}

    public record ReviewItem(
            Long id,
            Long productId,
            String userName,
            String userAvatar,
            int rating,
            String comment,
            boolean verifiedPurchase,
            OffsetDateTime createdAt,
            List<String> imageUrls
    ) {}

    public record ReviewSummary(
            double averageRating,
            int totalReviews,
            Map<Integer, Long> distribution,
            boolean userEligible,
            boolean userReviewed,
            String userIneligibilityReason,
            List<ReviewItem> reviews
    ) {}
}

