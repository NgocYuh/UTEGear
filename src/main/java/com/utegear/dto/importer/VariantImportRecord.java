package com.utegear.dto.importer;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Map;

public record VariantImportRecord(
        String sku,
        @JsonProperty("option_values") Map<String, String> optionValues,
        BigDecimal price,
        @JsonProperty("is_default") Boolean defaultVariant) {
}
