package com.example.gateway.user.Service;



import com.example.gateway.user.Dto.UserCreateDTO;
import com.example.gateway.user.Dto.UserResponseDTO;
import com.example.gateway.user.Entity.User;
import com.example.gateway.user.Enums.UserRole;
import com.example.gateway.user.Enums.UserStatus;
import com.example.gateway.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    @Transactional
    public UserResponseDTO createUser(UserCreateDTO createDTO) {
        // Check if email already exists
        if (userRepository.existsByEmail(createDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(createDTO.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        User user = new User();
        user.setFirstName(createDTO.getFirstName());
        user.setLastName(createDTO.getLastName());
        user.setEmail(createDTO.getEmail());
        user.setPhoneNumber(createDTO.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setPrimaryRole(UserRole.valueOf(createDTO.getPrimaryRole()));
        user.setStatus(UserStatus.ACTIVE);
        user.setImageUrl(null);




        User savedUser = userRepository.save(user);
        return mapToResponseDTO(savedUser);
    }






    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return mapToResponseDTO(user);
    }

    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return mapToResponseDTO(user);
    }

    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll(); // fetch all users without pagination
        return users.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }


    public Page<UserResponseDTO> getUsersByRole(UserRole role, Pageable pageable) {
        return userRepository.findByPrimaryRole(role, pageable)
                .map(this::mapToResponseDTO);
    }

    public Page<UserResponseDTO> getUsersByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserResponseDTO updateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (updateDTO.getFirstName() != null) {
            user.setFirstName(updateDTO.getFirstName());
        }
        if (updateDTO.getLastName() != null) {
            user.setLastName(updateDTO.getLastName());
        }
        if (updateDTO.getPhoneNumber() != null) {
            if (!updateDTO.getPhoneNumber().equals(user.getPhoneNumber()) &&
                    userRepository.existsByPhoneNumber(updateDTO.getPhoneNumber())) {
                throw new RuntimeException("Phone number already exists");
            }
            user.setPhoneNumber(updateDTO.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);
        return mapToResponseDTO(updatedUser);
    }

//    @Transactional
//    public void changePassword(Long id, UserResponseDTO passwordChangeDTO) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
//
//        if (!passwordEncoder.matches(passwordChangeDTO.getOldPassword(), user.getPasswordHash())) {
//            throw new RuntimeException("Old password is incorrect");
//        }
//
//        user.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
//        userRepository.save(user);
//    }

    @Transactional
    public UserResponseDTO updateUserStatus(Long id, UserStatus status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setStatus(status);
        User updatedUser = userRepository.save(user);
        return mapToResponseDTO(updatedUser);
    }



    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
       dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatus(String.valueOf(user.getStatus()));

        dto.setPrimaryRole(String.valueOf(user.getPrimaryRole()));

        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
