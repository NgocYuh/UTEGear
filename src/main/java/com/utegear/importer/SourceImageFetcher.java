package com.utegear.importer;

import java.net.URI;

public interface SourceImageFetcher {
    SourceImage fetch(URI source);
}
