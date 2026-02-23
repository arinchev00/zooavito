package com.example.zooavito.service.User;

import com.example.zooavito.model.User;

public interface UserService {
    void save(User user);
    User findByEmail(String email);
}
