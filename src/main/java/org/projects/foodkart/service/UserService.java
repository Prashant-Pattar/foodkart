package org.projects.foodkart.service;

import org.projects.foodkart.dto.request.UserRegisterRequest;
import org.projects.foodkart.dto.request.UserUpdateRequest;
import org.projects.foodkart.dto.response.UserResponse;
import org.projects.foodkart.entity.User;

import java.util.List;

public interface UserService {
    UserResponse registerUser(UserRegisterRequest request);
    UserResponse getUserById(Long id);
    User findUserEntityById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}
