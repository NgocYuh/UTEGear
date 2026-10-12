package com.utegear.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utegear.dto.importer.ImportRowResult;
import com.utegear.dto.importer.ProductImportRecord;
import com.utegear.dto.importer.ProductImportReport;
import com.utegear.exception.ApiException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImportService {

    private final ObjectMapper objectMapper;
    private final ProductRowImporter rowImporter;
    private final int maxRows;

    public ProductImportService(
            ObjectMapper objectMapper,
            ProductRowImporter rowImporter,
            @Value("${crawler.import.max-rows:1000}") int maxRows) {
        this.objectMapper = objectMapper;
        this.rowImporter = rowImporter;
        this.maxRows = maxRows;
    }

    public ProductImportReport importJsonl(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IMPORT_FILE_REQUIRED", "File JSONL là bắt buộc");
        }
        List<ImportRowResult> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber > maxRows) {
                    throw new ApiException(
                            HttpStatus.BAD_REQUEST,
                            "IMPORT_ROW_LIMIT_EXCEEDED",
                            "File vượt quá giới hạn " + maxRows + " dòng");
                }
                if (line.isBlank()) {
                    results.add(ImportRowResult.failed(lineNumber, null, "Dòng dữ liệu bị rỗng"));
                    continue;
                }
                results.add(importLine(lineNumber, line));
            }
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IMPORT_FILE_UNREADABLE", "Không thể đọc file JSONL");
        }
        return ProductImportReport.from(results);
    }

    private ImportRowResult importLine(int lineNumber, String line) {
        ProductImportRecord record;
        try {
            record = objectMapper.readValue(line, ProductImportRecord.class);
        } catch (JsonProcessingException exception) {
            return ImportRowResult.failed(lineNumber, null, "JSON không hợp lệ");
        }
        try {
            Long productId = rowImporter.importRecord(record);
            return ImportRowResult.imported(lineNumber, productId, record.sourceUrl());
        } catch (ApiException exception) {
            return ImportRowResult.failed(lineNumber, record.sourceUrl(), exception.getMessage());
        } catch (RuntimeException exception) {
            return ImportRowResult.failed(lineNumber, record.sourceUrl(), "Không thể import dòng dữ liệu");
        }
    }
}
