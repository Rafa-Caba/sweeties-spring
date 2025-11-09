package com.rafaelcabanillas.sweeties.util;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeleteFromCloudinary {

    private final CloudinaryService cloudinaryService;

    /** Delete by publicId, returns Cloudinary response map. */
    public Map<String, Object> byPublicId(String publicId) throws IOException {
        return cloudinaryService.deleteFile(publicId, true);
    }

    /** Delete by URL (extracts publicId internally). No-op if URL is null/invalid. */
    public @Nullable Map<String, Object> byUrl(@Nullable String url) throws IOException {
        String pid = cloudinaryService.extractPublicIdFromUrl(url);
        if (pid == null || pid.isBlank()) return null;
        return cloudinaryService.deleteFile(pid, true);
    }
}
