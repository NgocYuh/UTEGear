package com.utegear.dto.importer;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record ProductImportRecord(
        @JsonProperty("source_name") String sourceName,
        @JsonProperty("source_url") String sourceUrl,
        @JsonProperty("crawled_at") String crawledAt,
        String name,
        String description,
        String brand,
        String category,
        @JsonProperty("base_price") BigDecimal basePrice,
        @JsonProperty("image_urls") List<String> imageUrls,
        List<VariantImportRecord> variants) {
}
