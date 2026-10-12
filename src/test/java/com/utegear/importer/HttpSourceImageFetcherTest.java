package com.utegear.importer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.utegear.exception.ApiException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HttpSourceImageFetcherTest {

    private final HttpSourceImageFetcher fetcher = new HttpSourceImageFetcher(
            HttpClient.newHttpClient(), Set.of("hstatic.net"), 1024);

    @Test
    void rejectsHttpImageBeforeNetworkAccess() {
        assertInvalid("http://product.hstatic.net/image.webp");
    }

    @Test
    void rejectsImageHostOutsideAllowlistBeforeNetworkAccess() {
        assertInvalid("https://example.com/image.webp");
    }

    private void assertInvalid(String url) {
        assertThatThrownBy(() -> fetcher.fetch(URI.create(url)))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("INVALID_SOURCE_IMAGE");
    }
}
