package com.utegear.importer;

import com.utegear.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class HttpSourceImageFetcher implements SourceImageFetcher {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final HttpClient client;
    private final Set<String> allowedHosts;
    private final long maxFileSize;

    @Autowired
    public HttpSourceImageFetcher(
            @Value("${crawler.import.allowed-image-hosts:product.hstatic.net,res.cloudinary.com}") String hosts,
            @Value("${cloudinary.max-file-size:5242880}") long maxFileSize) {
        this(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .build(),
                parseHosts(hosts),
                maxFileSize);
    }

    HttpSourceImageFetcher(HttpClient client, Set<String> allowedHosts, long maxFileSize) {
        this.client = client;
        this.allowedHosts = Set.copyOf(allowedHosts);
        this.maxFileSize = maxFileSize;
    }

    @Override
    public SourceImage fetch(URI source) {
        validateSource(source);
        HttpRequest request = HttpRequest.newBuilder(source)
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "UTEGearInitialDataImporter/0.1")
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throw invalidImage("Nguồn ảnh trả về HTTP " + response.statusCode());
            }
            String contentType = response.headers().firstValue("Content-Type")
                    .map(value -> value.split(";", 2)[0].trim().toLowerCase(Locale.ROOT))
                    .orElse("");
            if (!ALLOWED_TYPES.contains(contentType)) {
                throw invalidImage("Nguồn ảnh không phải JPEG, PNG hoặc WebP");
            }
            byte[] content;
            try (InputStream input = response.body()) {
                content = input.readNBytes(Math.toIntExact(maxFileSize + 1));
            }
            if (content.length == 0 || content.length > maxFileSize) {
                throw invalidImage("Nguồn ảnh rỗng hoặc vượt quá dung lượng cho phép");
            }
            return new SourceImage(content, contentType, filename(source, contentType));
        } catch (IOException exception) {
            throw invalidImage("Không thể đọc nguồn ảnh");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw invalidImage("Tác vụ tải ảnh bị gián đoạn");
        }
    }

    private void validateSource(URI source) {
        String host = source.getHost() == null ? "" : source.getHost().toLowerCase(Locale.ROOT);
        boolean allowed = allowedHosts.stream()
                .anyMatch(allowedHost -> host.equals(allowedHost) || host.endsWith("." + allowedHost));
        if (!"https".equalsIgnoreCase(source.getScheme()) || !allowed) {
            throw invalidImage("URL ảnh phải dùng HTTPS và thuộc host đã cho phép");
        }
    }

    private String filename(URI source, String contentType) {
        String path = source.getPath();
        String name = path == null ? "" : path.substring(path.lastIndexOf('/') + 1);
        if (!name.isBlank()) {
            return name;
        }
        return "product." + switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    private ApiException invalidImage(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_SOURCE_IMAGE", message);
    }

    private static Set<String> parseHosts(String hosts) {
        return Arrays.stream(hosts.split(","))
                .map(String::trim)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}
