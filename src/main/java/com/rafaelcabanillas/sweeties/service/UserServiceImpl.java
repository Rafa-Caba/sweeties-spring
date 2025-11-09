package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.*;
import com.rafaelcabanillas.sweeties.exception.ResourceNotFoundException;
import com.rafaelcabanillas.sweeties.model.User;
import com.rafaelcabanillas.sweeties.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDTO createUser(CreateUserDTO dto, String imageUrl, String imagePublicId) {
        User user = User.builder()
                .name(dto.getName())
                .username(dto.getUsername().toLowerCase())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole() != null ? User.Role.valueOf(dto.getRole().toUpperCase()) : User.Role.GUEST)
                .bio(dto.getBio())
                .imageUrl(imageUrl)
                .imagePublicId(imagePublicId)
                .build();

        userRepository.save(user);
        return toUserDTO(user);
    }

    @Override
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id).map(this::toUserDTO)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no existe"));
    }

    @Override
    public UserDTO getByUsername(String username) {
        return userRepository.findByUsername(username.toLowerCase())
                .map(this::toUserDTO)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no existe"));
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::toUserDTO).toList();
    }

    @Override
    public UserDTO updateUser(Long id, UpdateUserDTO dto, String imageUrl, String imagePublicId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no existe"));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getUsername() != null) user.setUsername(dto.getUsername().toLowerCase());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPassword() != null) user.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (dto.getRole() != null) user.setRole(User.Role.valueOf(dto.getRole().toUpperCase()));
        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (imageUrl != null && !imageUrl.isEmpty()) user.setImageUrl(imageUrl);
        if (imagePublicId != null && !imagePublicId.isEmpty()) user.setImagePublicId(imagePublicId);

        userRepository.save(user);
        return toUserDTO(user);
    }

    @Override
    public void updateUserImage(Long id, String imageUrl, String imagePublicId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El usuario no existe"));
        user.setImageUrl(imageUrl);
        user.setImagePublicId(imagePublicId);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("El usuario no existe");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO getProfile(Long userId) {
        return getUserById(userId);
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name().toLowerCase())
                .bio(user.getBio())
                .imageUrl(user.getImageUrl())
                .imagePublicId(user.getImagePublicId())
                .build();
    }
}
