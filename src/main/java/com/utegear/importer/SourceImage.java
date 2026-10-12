package com.utegear.importer;

public record SourceImage(byte[] content, String contentType, String originalFilename) {
}
