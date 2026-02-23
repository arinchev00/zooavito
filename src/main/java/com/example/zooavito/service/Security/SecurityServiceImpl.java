package com.example.zooavito.service.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class SecurityServiceImpl implements SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private HttpServletRequest request;

    @Override
    public String findLoggedInEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return null;
    }

    @Override
    public void autoLogin(String email, String password) {
        try {
            logger.info("=== ПОПЫТКА АВТОЛОГИНА ===");
            logger.info("Email: {}", email);

            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(email, password);

            logger.info("Токен создан");

            Authentication authentication = authenticationManager.authenticate(token);

            logger.info("Аутентификация прошла, isAuthenticated: {}", authentication.isAuthenticated());

            if (authentication.isAuthenticated()) {
                // Устанавливаем в контекст
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // КРИТИЧЕСКИ ВАЖНО: сохраняем в сессию!
                HttpSession session = request.getSession(true);
                session.setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext()
                );

                logger.info("✅ АВТОЛОГИН УСПЕШЕН для: {}", email);
                logger.info("Session ID: {}", session.getId());
            }
        } catch (Exception e) {
            logger.error("ОШИБКА автологина: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}