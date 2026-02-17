package com.example.zooavito.service;

import com.example.zooavito.model.User;

public interface UserService {
    void save(User user);
    User findByFullName(String fullName);
}
