package com.example.zooavito.service.User;

import com.example.zooavito.Exception.BusinessErrorType;
import com.example.zooavito.Exception.BusinessException;
import com.example.zooavito.model.Role;
import com.example.zooavito.model.User;
import com.example.zooavito.repository.RoleRepository;
import com.example.zooavito.repository.UserRepository;
import com.example.zooavito.request.UserRegistrationRequest;
import com.example.zooavito.response.UserRegistrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserRegistrationResponse registerUser(UserRegistrationRequest request) {

        if (findByEmail(request.getEmail()) != null) {
            throw new BusinessException(BusinessErrorType.EMAIL_ALREADY_EXISTS);
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(BusinessErrorType.PASSWORDS_DO_NOT_MATCH);
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setTelephoneNumber(request.getTelephoneNumber());
        user.setPassword(request.getPassword());

        save(user);

        return buildUserResponse(user);
    }

    @Override
    public UserRegistrationResponse buildUserResponse(User user) {
        Set<String> roleTitles = null;
        if (user.getRoles() != null) {
            roleTitles = user.getRoles().stream()
                    .map(Role::getTitle)
                    .collect(Collectors.toSet());
        }

        return UserRegistrationResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .telephoneNumber(user.getTelephoneNumber())
                .roles(roleTitles)
                .build();
    }
}