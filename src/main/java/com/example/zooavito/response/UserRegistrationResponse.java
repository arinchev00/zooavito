package com.example.zooavito.response;

import com.example.zooavito.model.Role;
import com.example.zooavito.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class UserRegistrationResponse {
    private Long id;
    private String fullName;
    private String email;
    private String telephoneNumber;
    private Set<String> roles;
}