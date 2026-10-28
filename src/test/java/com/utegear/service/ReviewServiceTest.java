package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.review.ReviewDtos;
import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.Review;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.OrderRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.ReviewRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ReviewServiceTest {

    private ReviewRepository reviewRepository;
    private ProductRepository productRepository;
    private OrderRepository orderRepository;
    private ReviewService reviewService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        productRepository = mock(ProductRepository.class);
        orderRepository = mock(OrderRepository.class);
        reviewService = new ReviewService(reviewRepository, productRepository, orderRepository);

        user = new User("Nguyễn Văn An", "an@example.test", null, "hash");
        ReflectionTestUtils.setField(user, "id", 1L);

        Category category = new Category("Bàn phím", "ban-phim", null, 0);
        product = new Product("Bàn phím cơ AKKO", "akko-v3", category, null, new BigDecimal("1500000"), null);
        ProductVariant variant = new ProductVariant("AKKO-DEFAULT", Map.of(), true, BigDecimal.ZERO);
        product.addVariant(variant);
        ReflectionTestUtils.setField(product, "id", 10L);
    }

    @Test
    void getReviewSummaryReturnsEligibleForPurchasedUser() {
        when(productRepository.existsById(10L)).thenReturn(true);
        when(reviewRepository.findByProductIdWithUserAndImages(10L)).thenReturn(List.of());
        when(reviewRepository.calculateAverageRating(10L)).thenReturn(0.0);
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(false);
        when(orderRepository.hasCompletedOrderWithProduct(1L, 10L)).thenReturn(true);

        var summary = reviewService.getReviewSummary(10L, user);

        assertThat(summary.userEligible()).isTrue();
        assertThat(summary.userReviewed()).isFalse();
        assertThat(summary.userIneligibilityReason()).isNull();
    }

    @Test
    void createReviewRejectsShortComment() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(false);
        when(orderRepository.hasCompletedOrderWithProduct(1L, 10L)).thenReturn(true);

        var request = new ReviewDtos.CreateReviewRequest(5, "Hàng tốt quá", List.of());

        assertThatThrownBy(() -> reviewService.createReview(user, 10L, request))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("COMMENT_TOO_SHORT");
    }

    @Test
    void createReviewRejectsUnpurchasedUser() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(false);
        when(orderRepository.hasCompletedOrderWithProduct(1L, 10L)).thenReturn(false);

        String longComment = "Bàn phím gõ rất êm ái, switch mượt mà, cảm giác gõ rất nảy và âm thanh nghe rất đầm tay!";
        var request = new ReviewDtos.CreateReviewRequest(5, longComment, List.of());

        assertThatThrownBy(() -> reviewService.createReview(user, 10L, request))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("NOT_PURCHASED");
    }

    @Test
    void createReviewSucceedsAndUpdatesProductAverageRating() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUserIdAndProductId(1L, 10L)).thenReturn(false);
        when(orderRepository.hasCompletedOrderWithProduct(1L, 10L)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 100L);
            return r;
        });
        when(reviewRepository.calculateAverageRating(10L)).thenReturn(5.0);
        when(reviewRepository.countByProductId(10L)).thenReturn(1L);

        String longComment = "Bàn phím gõ rất êm ái, switch mượt mà, cảm giác gõ rất nảy và âm thanh nghe rất đầm tay!";
        var request = new ReviewDtos.CreateReviewRequest(5, longComment, List.of("https://res.cloudinary.com/demo/image.png"));

        var result = reviewService.createReview(user, 10L, request);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.rating()).isEqualTo(5);
        assertThat(result.comment()).isEqualTo(longComment);
        assertThat(result.imageUrls()).containsExactly("https://res.cloudinary.com/demo/image.png");
        verify(productRepository).save(product);
        assertThat(product.getReviewCount()).isEqualTo(1);
    }
}

