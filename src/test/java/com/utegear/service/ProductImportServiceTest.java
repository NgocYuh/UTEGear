package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utegear.dto.importer.ProductImportRecord;
import com.utegear.dto.importer.ProductImportReport;
import com.utegear.exception.ApiException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ProductImportServiceTest {

    private ProductRowImporter rowImporter;
    private ProductImportService service;

    @BeforeEach
    void setUp() {
        rowImporter = mock(ProductRowImporter.class);
        service = new ProductImportService(new ObjectMapper(), rowImporter, 100);
    }

    @Test
    void reportsSuccessfulAndFailedLinesWithoutStoppingTheFile() {
        when(rowImporter.importRecord(any(ProductImportRecord.class)))
                .thenReturn(101L)
                .thenThrow(new ApiException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "SKU_DUPLICATE",
                        "SKU đã tồn tại"));
        String json = validJson("SKU-01") + "\n" + "{not-json}" + "\n" + validJson("SKU-02");

        ProductImportReport report = service.importJsonl(file(json));

        assertThat(report.totalRows()).isEqualTo(3);
        assertThat(report.importedRows()).isEqualTo(1);
        assertThat(report.failedRows()).isEqualTo(2);
        assertThat(report.rows()).extracting("status")
                .containsExactly("IMPORTED", "FAILED", "FAILED");
        assertThat(report.rows().get(1).message()).isEqualTo("JSON không hợp lệ");
        assertThat(report.rows().get(2).message()).isEqualTo("SKU đã tồn tại");
    }

    @Test
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> service.importJsonl(file("")))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("IMPORT_FILE_REQUIRED");
    }

    @Test
    void rejectsFileAboveConfiguredRowLimit() {
        ProductImportService limited = new ProductImportService(new ObjectMapper(), rowImporter, 1);

        assertThatThrownBy(() -> limited.importJsonl(file(validJson("SKU-01") + "\n" + validJson("SKU-02"))))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("IMPORT_ROW_LIMIT_EXCEEDED");
    }

    private MockMultipartFile file(String content) {
        return new MockMultipartFile(
                "file", "products.jsonl", "application/x-ndjson", content.getBytes(StandardCharsets.UTF_8));
    }

    private String validJson(String sku) {
        return """
                {"source_name":"gearvn","source_url":"https://gearvn.com/products/sample",\
                "crawled_at":"2026-10-12T08:34:07Z","name":"Sample","brand":"E-Dra",\
                "category":"ban-phim","base_price":1000000,\
                "image_urls":["https://product.hstatic.net/sample.webp"],\
                "variants":[{"sku":"%s","option_values":{},"price":1000000,"is_default":true}]}
                """.formatted(sku).replace("\\\n", "").strip();
    }
}
