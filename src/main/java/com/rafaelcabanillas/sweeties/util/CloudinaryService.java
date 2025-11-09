package com.rafaelcabanillas.sweeties.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.Transformation;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class CloudinaryService {

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    /* ---------------------------- Upload (single) ---------------------------- */

    /** Upload with explicit publicId. */
    public Map<String, Object> uploadFile(
            MultipartFile file,
            String folder,
            String publicId
    ) throws IOException {
        return uploadFile(file, folder, publicId, 1600, 1600);
    }

    /** Upload with explicit publicId and size options. */
    public Map<String, Object> uploadFile(
            MultipartFile file,
            String folder,
            String publicId,
            int maxWidth,
            int maxHeight
    ) throws IOException {

        Map<String, Object> options = new HashMap<>();
        options.put("folder", folder);
        options.put("public_id", publicId);
        options.put("overwrite", true);
        options.put("invalidate", true);
        options.put("resource_type", "image");

        // ✅ Use Cloudinary's Transformation builder (most reliable)
        Transformation tx = new Transformation()
                .width(maxWidth)
                .height(maxHeight)
                .crop("limit");

        options.put("transformation", tx);

        options.remove("transformation");
        return cloudinary.uploader().upload(file.getBytes(), options);
    }

    /** Upload and auto-generate a safe publicId with a prefix/slug. */
    public Map<String, Object> uploadFileAutoId(
            MultipartFile file,
            String folder,
            String prefixSlug
    ) throws IOException {
        String publicId = buildPublicId(prefixSlug);
        return uploadFile(file, folder, publicId);
    }

    /* ---------------------------- Upload (bulk) ----------------------------- */

    /** Bulk upload with auto-generated ids (prefix applied to each). */
    public List<Map<String, Object>> uploadFiles(
            List<MultipartFile> files,
            String folder,
            String prefixSlug
    ) throws IOException {
        List<Map<String, Object>> results = new ArrayList<>(files.size());
        for (MultipartFile f : files) {
            results.add(uploadFileAutoId(f, folder, prefixSlug));
        }
        return results;
    }

    /* ----------------------------- Deletions -------------------------------- */

    public Map<String, Object> deleteFile(String publicId) throws IOException {
        return deleteFile(publicId, true); // default invalidate=true (CDN purge)
    }

    public Map<String, Object> deleteFile(String publicId, boolean invalidate) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap("invalidate", invalidate);
        return cloudinary.uploader().destroy(publicId, options);
    }

    /** Bulk delete by publicIds. */
    public List<Map<String, Object>> deleteFiles(Collection<String> publicIds, boolean invalidate) throws IOException {
        List<Map<String, Object>> results = new ArrayList<>();
        for (String pid : publicIds) {
            if (pid != null && !pid.isBlank()) {
                results.add(deleteFile(pid, invalidate));
            }
        }
        return results;
    }

    /* ------------------------------ Utilities ------------------------------- */

    /** Rename (move) an asset’s publicId (within/between folders). */
    public Map<String, Object> rename(String fromPublicId, String toPublicId, boolean overwrite) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap("overwrite", overwrite, "invalidate", true);
        return cloudinary.uploader().rename(fromPublicId, toPublicId, options);
    }

    /** Try to extract a publicId from a standard Cloudinary URL. */
    public @Nullable String extractPublicIdFromUrl(String url) {
        if (url == null || url.isBlank()) return null;
        // Handles URLs like: https://res.cloudinary.com/<cloud>/image/upload/v<ver>/<folder>/.../<name>.<ext>
        // Strip query params
        String clean = url.split("\\?")[0];
        int uploadIdx = clean.indexOf("/upload/");
        if (uploadIdx < 0) return null;

        String tail = clean.substring(uploadIdx + "/upload/".length());
        // Remove version segment v123456 if present
        tail = tail.replaceFirst("^v\\d+/", "");
        // Remove extension: keep everything up to last "."
        int dot = tail.lastIndexOf('.');
        String noExt = dot > 0 ? tail.substring(0, dot) : tail;
        // publicId is everything after /upload/ minus extension
        return noExt;
    }

    /** Build a robust publicId: slug + timestamp + short-uuid. */
    public String buildPublicId(String prefixSlug) {
        String slug = safeSlug(prefixSlug);
        String shortUuid = UUID.randomUUID().toString().substring(0, 8);
        return slug + "_" + System.currentTimeMillis() + "_" + shortUuid;
    }

    public String safeSlug(String s) {
        if (s == null) return "asset";
        return s.trim().toLowerCase().replaceAll("[^a-z0-9-_]+", "-");
    }
}
