package com.example.zooavito.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Data
public class SecurityConfig {

    @Autowired
    private final UserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11); // strength = 11
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig,
            UserDetailsService userDetailsService
    ) throws Exception {

        // Получаем passwordEncoder из контекста (через @Bean)
        BCryptPasswordEncoder encoder = passwordEncoder();

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(
                userDetailsService
        );

        return provider::authenticate;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/welcome").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/welcome")
                        .failureUrl("/login?error")
                        .usernameParameter("email")
                        .passwordParameter("password")
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                );

        return http.build();
    }
}
