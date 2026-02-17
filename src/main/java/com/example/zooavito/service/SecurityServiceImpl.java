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
        Object userDetails = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if(userDetails instanceof UserDetails){
            return ((UserDetails) userDetails).getUsername();
        }
        return null;
    }

    @Override
    public void autoLogin(String fullName, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(fullName);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
        authenticationManager.authenticate(authenticationToken);
        if(authenticationToken.isAuthenticated()){
            SecurityContextHolder.getContext().setAuthentication((authenticationToken));
            logger.debug(String .format("Автологин успешно выполнен пользователем ", fullName));
        }
    }
}
