package com.utegear.dto.consultant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public final class ConsultantDtos {

    private ConsultantDtos() {
    }

    public record ChatRequest(
            @NotBlank(message = "Nội dung câu hỏi là bắt buộc")
            @Size(max = 1000, message = "Nội dung không được vượt quá 1000 ký tự")
            String message,
            Long storeId) {
    }

    public record RecommendedProduct(
            Long id,
            String name,
            String slug,
            String categoryName,
            String categorySlug,
            String brandName,
            BigDecimal basePrice,
            BigDecimal salePrice,
            BigDecimal effectivePrice,
            String primaryImageUrl,
            int availableQuantity,
            int matchScore,
            String matchReason) {
    }

    public record StoreInfo(
            Long id,
            String name,
            String province,
            String district) {
    }

    public record ChatResponse(
            String reply,
            String detectedIntent,
            StoreInfo store,
            BigDecimal budget,
            BigDecimal totalEstimate,
            List<RecommendedProduct> products,
            List<String> nextSuggestions) {
    }

    public record QuickPrompt(
            String id,
            String title,
            String prompt,
            String icon) {
    }
}

