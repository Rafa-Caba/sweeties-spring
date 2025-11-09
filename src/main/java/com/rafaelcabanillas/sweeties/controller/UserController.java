package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.dto.CreateUserDTO;
import com.rafaelcabanillas.sweeties.dto.UpdateUserDTO;
import com.rafaelcabanillas.sweeties.dto.UserDTO;
import com.rafaelcabanillas.sweeties.service.UserService;
import com.rafaelcabanillas.sweeties.util.CloudinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    // ---------- READ ----------

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ---------- CREATE ----------

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestPart("user") CreateUserDTO userDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {

        String imageUrl = null;
        String imagePublicId = null;

        if (image != null && !image.isEmpty()) {
            ensureImage(image);
            // username is required in CreateUserDTO, safe to use
            String safeTag = Optional.ofNullable(userDTO.getUsername())
                    .map(String::toLowerCase)
                    .filter(s -> !s.isBlank())
                    .orElse(String.valueOf(System.currentTimeMillis()));

            String publicId = "usuario_" + safeTag + "_" + System.currentTimeMillis();
            Map<?, ?> result = cloudinaryService.uploadFile(image, "sweeties-crochet/users", publicId);

            imageUrl = Objects.toString(result.get("secure_url"), null);
            imagePublicId = Objects.toString(result.get("public_id"), null);
        }

        UserDTO saved = userService.createUser(userDTO, imageUrl, imagePublicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ---------- UPDATE (PUT/PATCH share the same handler) ----------

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestPart("user") UpdateUserDTO userDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        return ResponseEntity.ok(upsertUser(id, userDTO, image));
    }

    @PatchMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDTO> patchUser(
            @PathVariable Long id,
            @Valid @RequestPart("user") UpdateUserDTO userDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        return ResponseEntity.ok(upsertUser(id, userDTO, image));
    }

    // ---------- UPDATE PROFILE PICTURE ONLY ----------

    @PatchMapping(
            value = "/{id}/profile-pic",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> uploadProfilePic(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image
    ) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }
        ensureImage(image);

        String publicId = "usuario_" + id + "_" + System.currentTimeMillis();
        Map<?, ?> result = cloudinaryService.uploadFile(image, "sweeties-crochet/users", publicId);
        String imageUrl = Objects.toString(result.get("secure_url"), null);
        String imagePublicId = Objects.toString(result.get("public_id"), null);

        userService.updateUserImage(id, imageUrl, imagePublicId);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl, "imagePublicId", imagePublicId));
    }

    // ---------- DELETE ----------

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) throws IOException {
        UserDTO user = userService.getUserById(id); // to get existing imagePublicId
        if (user.getImagePublicId() != null && !user.getImagePublicId().isEmpty()) {
            cloudinaryService.deleteFile(user.getImagePublicId());
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }

    // ---------- PRIVATE HELPERS ----------

    private UserDTO upsertUser(Long id, UpdateUserDTO userDTO, MultipartFile image) throws IOException {
        String imageUrl = null;      // null => service won't overwrite
        String imagePublicId = null; // null => service won't overwrite

        if (image != null && !image.isEmpty()) {
            ensureImage(image);

            // If username isn't provided in this update, fall back to id
            String safeTag = Optional.ofNullable(userDTO.getUsername())
                    .map(String::toLowerCase)
                    .filter(s -> !s.isBlank())
                    .orElse(String.valueOf(id));

            String publicId = "usuario_" + safeTag + "_" + System.currentTimeMillis();
            Map<?, ?> result = cloudinaryService.uploadFile(image, "sweeties-crochet/users", publicId);

            imageUrl = Objects.toString(result.get("secure_url"), null);
            imagePublicId = Objects.toString(result.get("public_id"), null);
        }

        return userService.updateUser(id, userDTO, imageUrl, imagePublicId);
    }

    private void ensureImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image/* uploads are allowed");
        }
    }
}