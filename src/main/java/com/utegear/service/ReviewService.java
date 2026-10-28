package com.utegear.service;

import com.utegear.dto.review.ReviewDtos;
import com.utegear.entity.Product;
import com.utegear.entity.Review;
import com.utegear.entity.ReviewImage;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.OrderRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.ReviewRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public ReviewDtos.ReviewSummary getReviewSummary(Long productId, User currentUser) {
        if (!productRepository.existsById(productId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm");
        }

        List<Review> reviews = reviewRepository.findByProductIdWithUserAndImages(productId);
        Double avgRatingRaw = reviewRepository.calculateAverageRating(productId);
        double avgRating = avgRatingRaw != null ? Math.round(avgRatingRaw * 10.0) / 10.0 : 0.0;
        int totalReviews = reviews.size();

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        for (Review r : reviews) {
            distribution.put(r.getRating(), distribution.getOrDefault(r.getRating(), 0L) + 1);
        }

        boolean userEligible = false;
        boolean userReviewed = false;
        String userIneligibilityReason = null;

        if (currentUser == null) {
            userIneligibilityReason = "Vui lòng đăng nhập để gửi đánh giá sản phẩm.";
        } else {
            userReviewed = reviewRepository.existsByUserIdAndProductId(currentUser.getId(), productId);
            if (userReviewed) {
                userIneligibilityReason = "Bạn đã gửi đánh giá cho sản phẩm này rồi.";
            } else {
                boolean hasCompletedPurchase = orderRepository.hasCompletedOrderWithProduct(currentUser.getId(), productId);
                if (!hasCompletedPurchase) {
                    userIneligibilityReason = "Chỉ khách hàng đã mua sản phẩm này và hoàn tất đơn hàng mới có thể gửi đánh giá.";
                } else {
                    userEligible = true;
                }
            }
        }

        List<ReviewDtos.ReviewItem> reviewItems = reviews.stream()
                .map(this::toReviewItem)
                .toList();

        return new ReviewDtos.ReviewSummary(
                avgRating,
                totalReviews,
                distribution,
                userEligible,
                userReviewed,
                userIneligibilityReason,
                reviewItems
        );
    }

    @Transactional
    public ReviewDtos.ReviewItem createReview(User user, Long productId, ReviewDtos.CreateReviewRequest request) {
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Vui lòng đăng nhập để gửi đánh giá");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Không tìm thấy sản phẩm"));

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new ApiException(HttpStatus.CONFLICT, "REVIEW_ALREADY_EXISTS", "Bạn đã gửi đánh giá cho sản phẩm này rồi");
        }

        boolean hasCompletedPurchase = orderRepository.hasCompletedOrderWithProduct(user.getId(), productId);
        if (!hasCompletedPurchase) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "NOT_PURCHASED", "Chỉ khách hàng đã mua sản phẩm này và hoàn tất đơn hàng mới có thể gửi đánh giá");
        }

        if (request.comment() == null || request.comment().trim().length() < 50) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "COMMENT_TOO_SHORT", "Nội dung đánh giá phải có tối thiểu 50 ký tự");
        }

        Review review = new Review(product, user, request.rating(), request.comment().trim(), true);
        if (request.imageUrls() != null) {
            for (String url : request.imageUrls()) {
                if (url != null && !url.trim().isEmpty()) {
                    review.addImage(url.trim());
                }
            }
        }

        Review saved = reviewRepository.save(review);

        // Update Product average rating and review count
        Double avg = reviewRepository.calculateAverageRating(productId);
        long count = reviewRepository.countByProductId(productId);
        BigDecimal avgBd = BigDecimal.valueOf(avg != null ? avg : 0.0).setScale(2, RoundingMode.HALF_UP);
        product.updateRating(avgBd, (int) count);
        productRepository.save(product);

        return toReviewItem(saved);
    }

    private ReviewDtos.ReviewItem toReviewItem(Review review) {
        String displayName = maskName(review.getUser().getFullName());
        List<String> imageUrls = review.getImages() != null ?
                review.getImages().stream().map(ReviewImage::getImageUrl).toList() : List.of();

        return new ReviewDtos.ReviewItem(
                review.getId(),
                review.getProduct().getId(),
                displayName,
                review.getUser().getAvatarUrl(),
                review.getRating(),
                review.getComment(),
                review.isVerifiedPurchase(),
                review.getCreatedAt(),
                imageUrls
        );
    }

    private String maskName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "Khách hàng UTEGear";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            sb.append(parts[i]).append(" ");
        }
        String last = parts[parts.length - 1];
        if (last.length() > 1) {
            sb.append(last.charAt(0)).append("***");
        } else {
            sb.append(last);
        }
        return sb.toString();
    }
}

