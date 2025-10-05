package com.rafaelcabanillas.sweeties.controller;

import com.rafaelcabanillas.sweeties.dto.*;
import com.rafaelcabanillas.sweeties.service.UserService;
import com.rafaelcabanillas.sweeties.util.CloudinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    // GET all users
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // POST create user (optionally with image)
    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestPart("user") CreateUserDTO userDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        String imageUrl = "";
        String imagePublicId = "";
        if (image != null && !image.isEmpty()) {
            String publicId = "usuario_" + userDTO.getUsername() + "_" + System.currentTimeMillis();
            Map result = cloudinaryService.uploadFile(image, "sweeties-crochet/users", publicId);
            imageUrl = result.get("secure_url").toString();
            imagePublicId = result.get("public_id").toString();
        }
        UserDTO saved = userService.createUser(userDTO, imageUrl, imagePublicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PATCH or PUT update user (optionally with new image)
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestPart("user") UpdateUserDTO userDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        String imageUrl = "";
        String imagePublicId = "";
        if (image != null && !image.isEmpty()) {
            String publicId = "usuario_" + userDTO.getUsername() + "_" + System.currentTimeMillis();
            Map result = cloudinaryService.uploadFile(image, "sweeties-crochet/users", publicId);
            imageUrl = result.get("secure_url").toString();
            imagePublicId = result.get("public_id").toString();
        }
        UserDTO updated = userService.updateUser(id, userDTO, imageUrl, imagePublicId);
        return ResponseEntity.ok(updated);
    }

    // PATCH just profile picture
    @PatchMapping("/{id}/profile-pic")
    public ResponseEntity<?> uploadProfilePic(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image
    ) throws IOException {
        String publicId = "usuario_" + id + "_" + System.currentTimeMillis();
        Map result = cloudinaryService.uploadFile(image, "sweeties-crochet/users", publicId);
        String imageUrl = result.get("secure_url").toString();
        String imagePublicId = result.get("public_id").toString();
        userService.updateUserImage(id, imageUrl, imagePublicId);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl, "imagePublicId", imagePublicId));
    }

    // DELETE user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) throws IOException {
        UserDTO user = userService.getUserById(id); // fetch to get publicId
        // Optionally: Delete image from Cloudinary
        if (user.getImagePublicId() != null && !user.getImagePublicId().isEmpty()) {
            cloudinaryService.deleteFile(user.getImagePublicId());
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted"));
    }
}
