package org.projects.foodkart.service.impl;

import lombok.RequiredArgsConstructor;
import org.projects.foodkart.dto.request.UserRegisterRequest;
import org.projects.foodkart.dto.request.UserUpdateRequest;
import org.projects.foodkart.dto.response.UserResponse;
import org.projects.foodkart.entity.User;
import org.projects.foodkart.exception.BadRequestException;
import org.projects.foodkart.exception.ResourceNotFoundException;
import org.projects.foodkart.repository.UserRepository;
import org.projects.foodkart.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BadRequestException("User already exists with phone number: " + request.getPhoneNumber());
        }

        User user = User.builder()
                .name(request.getName())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .pinCode(request.getPinCode())
                .build();

        User saved = userRepository.save(user);
        return UserResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return UserResponse.fromEntity(findUserEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUserEntityById(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            String newPhone = request.getPhoneNumber().trim();
            if (!newPhone.equals(user.getPhoneNumber()) && userRepository.existsByPhoneNumber(newPhone)) {
                throw new BadRequestException("User already exists with phone number: " + newPhone);
            }
            user.setPhoneNumber(newPhone);
        }

        if (request.getPinCode() != null && !request.getPinCode().isBlank()) {
            user.setPinCode(request.getPinCode().trim());
        }

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserEntityById(id);
        userRepository.delete(user);
    }
}
