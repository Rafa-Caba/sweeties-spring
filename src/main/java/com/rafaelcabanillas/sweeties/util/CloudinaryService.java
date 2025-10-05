package com.rafaelcabanillas.sweeties.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
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

    // Initialize Cloudinary instance after properties are injected
    @PostConstruct
    public void init() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public Map uploadFile(MultipartFile file, String folder, String publicId) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicId,
                "overwrite", true,
                "resource_type", "auto",
                "transformation", "c_limit,w_1600,h_1600"
        );
        return cloudinary.uploader().upload(file.getBytes(), options);
    }

    public List<Map> uploadFiles(List<MultipartFile> files, String folder, String prefix) throws IOException {
        List<Map> results = new ArrayList<>();
        for (MultipartFile file : files) {
            String publicId = prefix + "_" + System.currentTimeMillis();
            results.add(uploadFile(file, folder, publicId));
        }
        return results;
    }

    public Map deleteFile(String publicId) throws IOException {
        return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
