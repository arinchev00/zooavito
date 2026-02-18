package com.example.zooavito.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService{

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public String findLoggedInFullName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(principal instanceof UserDetails){
            return ((UserDetails) principal).getUsername();
        }
        return null;
    }

    @Override
    public void autoLogin(String fullName, String password) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(fullName, password);

            authenticationManager.authenticate(authenticationToken);

            if(authenticationToken.isAuthenticated()){
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                logger.debug(String.format("Автологин успешно выполнен пользователем %s", fullName));
            }
        } catch (Exception e) {
            logger.error("Ошибка при автологине: " + e.getMessage());
        }
    }
}
