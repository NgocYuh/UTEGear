package com.utegear.view;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

class ProductContentTemplateTest {
    private String render(String json) throws Exception {
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix("src/main/resources/templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        Context context = new Context();
        context.setVariable("product", Map.of("name", "Sản phẩm mẫu",
                "specifications", new ObjectMapper().readTree(json)));
        return engine.process("fragments/product/product-specs", context);
    }

    @Test
    void rendersTechnicalTableWithRowHeadersAndHidesCrawlerMetadata() throws Exception {
        String html = render("""
                {"technicalSpecifications":{"Kích thước":"900 x 400 x 5 mm","Chất liệu":"Cordura"},
                 "sourceName":"crawler-internal","sourceUrl":"https://source.test/private-marker","crawledAt":"2026-10-15"}
                """);
        assertThat(html).contains("<table", "scope=\"row\"", "Kích thước", "900 x 400 x 5 mm", "Cordura");
        assertThat(html).doesNotContain("crawler-internal", "private-marker", "crawledAt", "đang được cập nhật");
    }

    @Test
    void escapesSourceLabelsAndValuesInsteadOfExecutingHtml() throws Exception {
        String html = render("""
                {"technicalSpecifications":{"<script>alert(1)</script>":"<img src=x onerror=alert(2)>"}}
                """);
        assertThat(html).contains("&lt;script&gt;", "&lt;img");
        assertThat(html).doesNotContain("<script>", "<img src=x");
    }

    @Test
    void showsEmptyStateForProvenanceOnlyAndEmptyTechnicalData() throws Exception {
        for (String json : new String[]{"{}", "{\"sourceUrl\":\"private-marker\"}", "{\"technicalSpecifications\":{}}"}) {
            assertThat(render(json)).contains("Thông số kỹ thuật đang được cập nhật.")
                    .doesNotContain("<table", "private-marker");
        }
    }
}
