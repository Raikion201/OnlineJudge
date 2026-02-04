package com.userservice.service;

import com.userservice.dto.request.UserRequest;
import com.userservice.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse getUserById(Long id);

    UserResponse getUserByUsername(String username);

    UserResponse getUserByEmail(String email);

    UserResponse getUserByKeycloakId(String keycloakId);

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(Long id, UserRequest request);

    UserResponse getUserMe(String keycloakId);

    UserResponse updateUserMe(String keycloakId, UserRequest request);

    void deleteUser(Long id);
}
