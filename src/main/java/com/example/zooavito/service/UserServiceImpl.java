package com.example.zooavito.service;

import com.example.zooavito.model.Role;
import com.example.zooavito.model.User;
import com.example.zooavito.repository.RoleRepository;
import com.example.zooavito.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findById(1L).orElseThrow(() -> new RuntimeException("Role not found"));
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Override
    public User findByFullName(String fullName) {
        return userRepository.findByFullName(fullName);
    }
}
