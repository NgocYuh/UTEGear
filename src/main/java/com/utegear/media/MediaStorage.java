package com.utegear.media;

import org.springframework.web.multipart.MultipartFile;

public interface MediaStorage {
    UploadedMedia upload(MultipartFile file, String folder);

    void delete(String publicId);
}
