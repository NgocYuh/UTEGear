package com.utegear.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.utegear.exception.ApiException;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CloudinaryMediaStorage implements MediaStorage {

    private final Cloudinary cloudinary;

    public CloudinaryMediaStorage(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public UploadedMedia upload(MultipartFile file, String folder) {
        try {
            return upload(file.getBytes(), file.getContentType(), file.getOriginalFilename(), folder);
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ApiException apiException) {
                throw apiException;
            }
            throw uploadFailed();
        }
    }

    @Override
    public UploadedMedia upload(byte[] content, String contentType, String originalFilename, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    content,
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "overwrite", false));
            Object secureUrl = result.get("secure_url");
            Object publicId = result.get("public_id");
            if (secureUrl == null || publicId == null) {
                throw uploadFailed();
            }
            return new UploadedMedia(secureUrl.toString(), publicId.toString());
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ApiException apiException) {
                throw apiException;
            }
            throw uploadFailed();
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "image", "invalidate", true));
        } catch (IOException | RuntimeException exception) {
            throw new ApiException(
                    HttpStatus.BAD_GATEWAY,
                    "MEDIA_UPLOAD_FAILED",
                    "Không thể xóa ảnh trên Cloudinary");
        }
    }

    private ApiException uploadFailed() {
        return new ApiException(
                HttpStatus.BAD_GATEWAY,
                "MEDIA_UPLOAD_FAILED",
                "Không thể tải ảnh lên Cloudinary");
    }
}
