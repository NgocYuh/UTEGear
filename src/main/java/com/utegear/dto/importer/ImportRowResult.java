package com.utegear.dto.importer;

public record ImportRowResult(
        int line,
        String status,
        Long productId,
        String sourceUrl,
        String message) {

    public static ImportRowResult imported(int line, Long productId, String sourceUrl) {
        return new ImportRowResult(line, "IMPORTED", productId, sourceUrl, null);
    }

    public static ImportRowResult failed(int line, String sourceUrl, String message) {
        return new ImportRowResult(line, "FAILED", null, sourceUrl, message);
    }
}
