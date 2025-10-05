package com.rafaelcabanillas.sweeties.service;

import com.rafaelcabanillas.sweeties.dto.*;

import java.util.List;

public interface UserService {
    UserDTO createUser(CreateUserDTO dto, String imageUrl, String imagePublicId);
    UserDTO getUserById(Long id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UpdateUserDTO dto, String imageUrl, String imagePublicId);
    void updateUserImage(Long id, String imageUrl, String imagePublicId);
    void deleteUser(Long id);
    UserDTO getProfile(Long userId);
}