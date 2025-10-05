package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.dto.*;
import com.rafaelcabanillas.sweeties.service.ItemService;
import com.rafaelcabanillas.sweeties.util.CloudinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final CloudinaryService cloudinaryService;

    // GET all items
    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    // GET item by ID
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    // POST create item (main image + sprites)
    @PostMapping
    public ResponseEntity<ItemDTO> createItem(
            @Valid @RequestPart("item") CreateItemDTO itemDTO,
            @RequestPart("image") MultipartFile mainImage,
            @RequestPart(value = "sprites", required = false) List<MultipartFile> sprites
    ) throws IOException {
        String mainPublicId = "item_" + itemDTO.getName() + "_" + System.currentTimeMillis();
        Map mainUpload = cloudinaryService.uploadFile(mainImage, "sweeties-crochet/items", mainPublicId);

        String imageUrl = mainUpload.get("secure_url").toString();
        String imagePublicId = mainUpload.get("public_id").toString();

        List<String> spriteUrls = new ArrayList<>();
        List<String> spritePublicIds = new ArrayList<>();
        if (sprites != null && !sprites.isEmpty()) {
            List<Map> spriteUploads = cloudinaryService.uploadFiles(sprites, "sweeties-crochet/items", "sprite");
            for (Map m : spriteUploads) {
                spriteUrls.add(m.get("secure_url").toString());
                spritePublicIds.add(m.get("public_id").toString());
            }
        }

        ItemDTO saved = itemService.createItem(itemDTO, imageUrl, imagePublicId, spriteUrls, spritePublicIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT update item (main image and sprites, optional)
    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestPart("item") UpdateItemDTO itemDTO,
            @RequestPart(value = "image", required = false) MultipartFile mainImage,
            @RequestPart(value = "sprites", required = false) List<MultipartFile> sprites
    ) throws IOException {
        // Fetch current item for old image/sprite cleanup
        ItemDTO current = itemService.getItemById(id);

        String imageUrl = "";
        String imagePublicId = "";
        if (mainImage != null && !mainImage.isEmpty()) {
            // Delete old main image if present
            if (current.getImagePublicId() != null && !current.getImagePublicId().isEmpty()) {
                cloudinaryService.deleteFile(current.getImagePublicId());
            }
            String mainPublicId = "item_" + itemDTO.getName() + "_" + System.currentTimeMillis();
            Map mainUpload = cloudinaryService.uploadFile(mainImage, "sweeties-crochet/items", mainPublicId);
            imageUrl = mainUpload.get("secure_url").toString();
            imagePublicId = mainUpload.get("public_id").toString();
        }

        List<String> spriteUrls = new ArrayList<>();
        List<String> spritePublicIds = new ArrayList<>();
        if (sprites != null && !sprites.isEmpty()) {
            // Delete old sprites if present
            if (current.getSpritesPublicIds() != null && !current.getSpritesPublicIds().isEmpty()) {
                for (String pid : current.getSpritesPublicIds()) {
                    cloudinaryService.deleteFile(pid);
                }
            }
            List<Map> spriteUploads = cloudinaryService.uploadFiles(sprites, "sweeties-crochet/items", "sprite");
            for (Map m : spriteUploads) {
                spriteUrls.add(m.get("secure_url").toString());
                spritePublicIds.add(m.get("public_id").toString());
            }
            // **Merge the new sprites into the DTO so the service picks them up**
            itemDTO.setSprites(spriteUrls);
            itemDTO.setSpritesPublicIds(spritePublicIds);
        }

        ItemDTO updated = itemService.updateItem(id, itemDTO, imageUrl, imagePublicId, spriteUrls, spritePublicIds);
        return ResponseEntity.ok(updated);
    }

    // DELETE item (and images from Cloudinary)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) throws IOException {
        ItemDTO item = itemService.getItemById(id); // will throw 404 if not found
        // Remove main image
        if (item.getImagePublicId() != null && !item.getImagePublicId().isEmpty()) {
            cloudinaryService.deleteFile(item.getImagePublicId());
        }
        // Remove sprites
        if (item.getSpritesPublicIds() != null && !item.getSpritesPublicIds().isEmpty()) {
            for (String pid : item.getSpritesPublicIds()) {
                cloudinaryService.deleteFile(pid);
            }
        }
        itemService.deleteItem(id);
        return ResponseEntity.ok(Map.of("message", "Item deleted"));
    }
}
