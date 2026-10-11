package com.utegear.dto.catalog;
import com.utegear.entity.Brand;
public record BrandSummary(Long id,String name,String slug){ public static BrandSummary from(Brand b){return new BrandSummary(b.getId(),b.getName(),b.getSlug());} }
