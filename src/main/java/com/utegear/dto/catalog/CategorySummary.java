package com.utegear.dto.catalog;
import com.utegear.entity.Category;
public record CategorySummary(Long id,String name,String slug,int sortOrder){ public static CategorySummary from(Category c){return new CategorySummary(c.getId(),c.getName(),c.getSlug(),c.getSortOrder());} }
