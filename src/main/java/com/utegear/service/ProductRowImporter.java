package com.utegear.service;

import com.utegear.dto.importer.ProductImportRecord;

public interface ProductRowImporter {
    Long importRecord(ProductImportRecord record);
}
