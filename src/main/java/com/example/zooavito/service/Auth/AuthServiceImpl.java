package com.example.zooavito.service.Auth;

import com.example.zooavito.service.Security.JwtTokenProvider;
import com.example.zooavito.model.User;
import com.example.zooavito.request.AuthRequest;
import com.example.zooavito.response.AuthResponse;
import com.example.zooavito.response.UserRegistrationResponse;
import com.example.zooavito.service.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    public AuthResponse authenticateUser(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername());

        UserRegistrationResponse userResponse = userService.buildUserResponse(user);

        return AuthResponse.builder()
                .token(jwt)
                .user(userResponse)
                .build();
    }
}
