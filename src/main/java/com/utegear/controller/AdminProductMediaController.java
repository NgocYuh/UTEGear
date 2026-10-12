package com.utegear.controller;

import com.utegear.dto.media.ProductImageOrderRequest;
import com.utegear.dto.media.ProductImageResponse;
import com.utegear.service.ProductMediaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/products/{productId}/images")
public class AdminProductMediaController {

    private final ProductMediaService service;

    public AdminProductMediaController(ProductMediaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImageResponse upload(
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String altText,
            @RequestParam(defaultValue = "false") boolean primary) {
        return service.upload(productId, file, altText, primary);
    }

    @PutMapping("/{imageId}")
    public ProductImageResponse update(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(required = false) String altText,
            @RequestParam(defaultValue = "false") boolean primary) {
        return service.update(productId, imageId, file, altText, primary);
    }

    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long productId, @PathVariable Long imageId) {
        service.delete(productId, imageId);
    }

    @PutMapping("/order")
    public List<ProductImageResponse> reorder(
            @PathVariable Long productId,
            @Valid @RequestBody ProductImageOrderRequest request) {
        return service.reorder(productId, request.imageIds());
    }
}
