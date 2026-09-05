package org.example.bookapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.bookapp.security.RestAccessDeniedHandler;
import org.example.bookapp.security.RestAuthenticationEntryPoint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return new RestAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return new RestAccessDeniedHandler(objectMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth

                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/authors", "/api/authors/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/api/books/*/borrow").authenticated()

                .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")

                .requestMatchers(HttpMethod.PATCH, "/api/books/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")

                .requestMatchers("/api/authors/**").hasRole("ADMIN")

                .anyRequest().authenticated())

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .httpBasic(httpBasic ->
                        httpBasic.authenticationEntryPoint(authenticationEntryPoint));

        return http.build();
    }

}