package com.michal.openai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebSecurity
@EnableWebMvc
@Configuration
	public class WebConfig implements WebMvcConfigurer  {

	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/slack").permitAll()
            .requestMatchers("/api/v1/test/**").permitAll()
            .requestMatchers("/api/v1/jira/**").permitAll()
            .requestMatchers("/api/v1/users/**").permitAll()
            .requestMatchers("/api/v1/github/user/**").permitAll()
            .requestMatchers("/api/v1/admin/clear-database").permitAll()
            .requestMatchers("/swagger").permitAll()
            .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
            .anyRequest().authenticated()
        );
    return http.build();
	}    
}