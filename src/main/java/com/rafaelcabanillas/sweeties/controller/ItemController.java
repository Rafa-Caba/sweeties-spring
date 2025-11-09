package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.dto.CreateItemDTO;
import com.rafaelcabanillas.sweeties.dto.ItemDTO;
import com.rafaelcabanillas.sweeties.dto.UpdateItemDTO;
import com.rafaelcabanillas.sweeties.service.ItemService;
import com.rafaelcabanillas.sweeties.util.CloudinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CloudinaryService cloudinaryService;

    /* ------------------------- PUBLIC READ ------------------------- */

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    /* ------------------------- ADMIN WRITE ------------------------- */

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemDTO> createItem(
            @Valid @RequestPart("item") CreateItemDTO itemDTO,
            @RequestPart("image") MultipartFile mainImage,
            @RequestPart(value = "sprites", required = false) List<MultipartFile> sprites
    ) throws IOException {

        ensureImage(mainImage);

        // --- main image ---
        String mainPublicId = "item_" + safeSlug(itemDTO.getName()) + "_" + System.currentTimeMillis();
        Map<String, Object> mainUpload;
        try {
            mainUpload = cloudinaryService.uploadFile(mainImage, "sweeties-crochet/items", mainPublicId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary upload (image) failed: " + e.getMessage(), e);
        }
        String imageUrl = Objects.toString(mainUpload.get("secure_url"), null);
        String imagePublicId = Objects.toString(mainUpload.get("public_id"), null);

        // --- sprites (optional) ---
        List<String> spriteUrls = new ArrayList<>();
        List<String> spritePublicIds = new ArrayList<>();
        if (sprites != null && !sprites.isEmpty()) {
            // keep only non-empty image parts
            List<MultipartFile> cleanSprites = new ArrayList<>();
            for (MultipartFile f : sprites) {
                if (f != null && !f.isEmpty()) {
                    ensureImage(f);
                    cleanSprites.add(f);
                }
            }
            if (!cleanSprites.isEmpty()) {
                List<Map<String, Object>> ups;
                try {
                    ups = cloudinaryService.uploadFiles(cleanSprites, "sweeties-crochet/items", "sprite");
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary upload (sprites) failed: " + e.getMessage(), e);
                }
                for (Map<String, Object> m : ups) {
                    spriteUrls.add(Objects.toString(m.get("secure_url"), null));
                    spritePublicIds.add(Objects.toString(m.get("public_id"), null));
                }
            }
        }

        // pass MUTABLE lists to service
        ItemDTO saved = itemService.createItem(
                itemDTO,
                imageUrl,
                imagePublicId,
                new ArrayList<>(spriteUrls),
                new ArrayList<>(spritePublicIds)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestPart("item") UpdateItemDTO itemDTO,
            @RequestPart(value = "image", required = false) MultipartFile mainImage,
            @RequestPart(value = "sprites", required = false) List<MultipartFile> sprites
    ) throws IOException {
        return ResponseEntity.ok(handleUpsert(id, itemDTO, mainImage, sprites, true));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ItemDTO> patchItem(
            @PathVariable Long id,
            @Valid @RequestPart("item") UpdateItemDTO itemDTO,
            @RequestPart(value = "image", required = false) MultipartFile mainImage,
            @RequestPart(value = "sprites", required = false) List<MultipartFile> sprites
    ) throws IOException {
        return ResponseEntity.ok(handleUpsert(id, itemDTO, mainImage, sprites, false));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> deleteItem(@PathVariable Long id) throws IOException {
        ItemDTO item = itemService.getItemById(id); // 404 if not found

        // best-effort deletes (don’t fail the request if Cloudinary has issues)
        if (item.getImagePublicId() != null && !item.getImagePublicId().isBlank()) {
            try { cloudinaryService.deleteFile(item.getImagePublicId(), true); } catch (Exception ignored) {}
        }
        if (item.getSpritesPublicIds() != null) {
            for (String pid : item.getSpritesPublicIds()) {
                if (pid != null && !pid.isBlank()) {
                    try { cloudinaryService.deleteFile(pid, true); } catch (Exception ignored) {}
                }
            }
        }

        itemService.deleteItem(id);
        return ResponseEntity.ok(Map.of("message", "Item deleted"));
    }

    /* --------------------------- helpers --------------------------- */

    /**
     * Shared logic for PUT (replace semantics) and PATCH (partial update).
     */
    private ItemDTO handleUpsert(
            Long id,
            UpdateItemDTO dto,
            MultipartFile mainImage,
            List<MultipartFile> sprites,
            boolean isPut
    ) throws IOException {

        ItemDTO current = itemService.getItemById(id); // used for cleanup and naming fallbacks

        // normalize incoming parts
        if (mainImage != null && mainImage.isEmpty()) mainImage = null;

        List<MultipartFile> cleanSprites = null;
        if (sprites != null) {
            cleanSprites = new ArrayList<>();
            for (MultipartFile f : sprites) {
                if (f != null && !f.isEmpty()) cleanSprites.add(f);
            }
            if (cleanSprites.isEmpty()) cleanSprites = null; // treat as not provided
        }

        String imageUrl = null;
        String imagePublicId = null;
        List<String> spriteUrls = null;
        List<String> spritePublicIds = null;

        // --- main image (optional) ---
        if (mainImage != null) {
            ensureImage(mainImage);

            if (current.getImagePublicId() != null && !current.getImagePublicId().isBlank()) {
                try {
                    cloudinaryService.deleteFile(current.getImagePublicId(), true);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary delete (image) failed: " + e.getMessage(), e);
                }
            }

            String slugBase = (dto.getName() != null && !dto.getName().isBlank()) ? dto.getName() : current.getName();
            Map<String, Object> upload;
            try {
                upload = cloudinaryService.uploadFileAutoId(mainImage, "sweeties-crochet/items", "item_" + slugBase);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary upload (image) failed: " + e.getMessage(), e);
            }
            imageUrl = Objects.toString(upload.get("secure_url"), null);
            imagePublicId = Objects.toString(upload.get("public_id"), null);
        }

        // --- sprites (optional) ---
        if (cleanSprites != null) {
            for (MultipartFile f : cleanSprites) ensureImage(f);

            if (current.getSpritesPublicIds() != null && !current.getSpritesPublicIds().isEmpty()) {
                try {
                    cloudinaryService.deleteFiles(current.getSpritesPublicIds(), true);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary delete (sprites) failed: " + e.getMessage(), e);
                }
            }

            List<Map<String, Object>> ups;
            try {
                String slugBase = (dto.getName() != null && !dto.getName().isBlank()) ? dto.getName() : current.getName();
                ups = cloudinaryService.uploadFiles(cleanSprites, "sweeties-crochet/items", "sprite_" + slugBase);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary upload (sprites) failed: " + e.getMessage(), e);
            }

            spriteUrls = new ArrayList<>(ups.size());
            spritePublicIds = new ArrayList<>(ups.size());
            for (Map<String, Object> m : ups) {
                spriteUrls.add(Objects.toString(m.get("secure_url"), null));
                spritePublicIds.add(Objects.toString(m.get("public_id"), null));
            }

            // mirror into DTO so service replaces the lists
            dto.setSprites(new ArrayList<>(spriteUrls));
            dto.setSpritesPublicIds(new ArrayList<>(spritePublicIds));
        } else if (isPut && sprites != null && sprites.isEmpty()) {
            // client explicitly sent empty array on PUT → clear sprites
            spriteUrls = new ArrayList<>();
            spritePublicIds = new ArrayList<>();
            dto.setSprites(new ArrayList<>());
            dto.setSpritesPublicIds(new ArrayList<>());

            // also delete existing from Cloudinary
            if (current.getSpritesPublicIds() != null && !current.getSpritesPublicIds().isEmpty()) {
                try {
                    cloudinaryService.deleteFiles(current.getSpritesPublicIds(), true);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary delete (sprites) failed: " + e.getMessage(), e);
                }
            }
        }

        return itemService.updateItem(id, dto, imageUrl, imagePublicId, spriteUrls, spritePublicIds);
    }

    private void ensureImage(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image/* uploads are allowed");
        }
    }

    private String safeSlug(String s) {
        if (s == null) return "item";
        return s.trim().toLowerCase().replaceAll("[^a-z0-9-_]+", "-");
    }
}
