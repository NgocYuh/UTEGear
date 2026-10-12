package com.utegear.media;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorage {
    UploadedMedia upload(MultipartFile file, String folder);

    UploadedMedia upload(byte[] content, String contentType, String originalFilename, String folder);

    void delete(String publicId);
}
