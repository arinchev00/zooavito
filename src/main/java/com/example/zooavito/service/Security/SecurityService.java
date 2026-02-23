package com.example.zooavito.service.Security;

public interface SecurityService {
    String findLoggedInEmail();
    void autoLogin(String email, String password);
}
