package com.utegear.dto.importer;

import java.util.List;

public record ProductImportReport(
        int totalRows,
        int importedRows,
        int failedRows,
        List<ImportRowResult> rows) {

    public static ProductImportReport from(List<ImportRowResult> rows) {
        int imported = (int) rows.stream().filter(row -> "IMPORTED".equals(row.status())).count();
        return new ProductImportReport(rows.size(), imported, rows.size() - imported, List.copyOf(rows));
    }
}
