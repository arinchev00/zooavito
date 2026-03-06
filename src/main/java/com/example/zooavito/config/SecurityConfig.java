package com.example.zooavito.config;

import com.example.zooavito.service.Security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF (для REST API не нужен)
                .csrf(csrf -> csrf.disable())

                // Stateless (без сессий) - используем JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Настройка авторизации
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты (не требуют токена)
                        .requestMatchers(
                                // Swagger UI
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",

                                // Публичные API
                                "/v1/api/auth",
                                "/v1/api/registration"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/v1/api/announcement/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/api/categories/**").permitAll()

                        // Эндпоинты с объявлениями - ТРЕБУЮТ АУТЕНТИФИКАЦИИ!
                        .requestMatchers(HttpMethod.POST, "/v1/api/announcement/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/v1/api/announcement/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/v1/api/announcement/**").authenticated()

                        // Админские эндпоинты
                        .requestMatchers("/v1/api/admin/**").hasRole("ADMIN")

                        // Категории - создание/обновление/удаление только для ADMIN
                        .requestMatchers(HttpMethod.POST, "/v1/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/v1/api/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/api/categories/**").hasRole("ADMIN")

                        // Все остальные требуют аутентификации
                        .anyRequest().authenticated()
                )

                // Добавляем JWT фильтр
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Отключаем formLogin (не нужен для REST)
                .formLogin(form -> form.disable())

                // Отключаем httpBasic
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}