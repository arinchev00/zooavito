package com.example.zooavito.service;

public interface SecurityService {
    String findLoggedInFullName();
    void autoLogin(String fullName, String password);
}
