package com.utegear.controller;

import com.utegear.dto.importer.ProductImportReport;
import com.utegear.service.ProductImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/products/import")
public class AdminProductImportController {

    private final ProductImportService service;

    public AdminProductImportController(ProductImportService service) {
        this.service = service;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ProductImportReport importProducts(@RequestPart("file") MultipartFile file) {
        return service.importJsonl(file);
    }
}
