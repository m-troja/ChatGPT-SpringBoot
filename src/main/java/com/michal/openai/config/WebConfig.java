package com.michal.openai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@EnableWebSecurity
@EnableWebMvc
@Configuration
	public class WebConfig implements WebMvcConfigurer  {

	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/css/**", "/fonts/**", "/images/**", "/js/**", "/vendor/**").permitAll()
            .requestMatchers("/favicon.ico", "/error").permitAll()  

            .requestMatchers("/chatgpt", "/", "/WEB-INF/jsp/**", "/v1/slack", "/v1/jira/**", "/v1/jira/issue").permitAll()
            .requestMatchers("/admin/**").permitAll()
            .requestMatchers("/authorize", "/oauth2callback").permitAll()
            .requestMatchers("/swagger").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .anyRequest().authenticated()
        );

    return http.build();
	}    
	        
	@Bean
	public ViewResolver viewResolver() {
		InternalResourceViewResolver bean = new InternalResourceViewResolver();
		bean.setViewClass(JstlView.class);
		bean.setPrefix("/WEB-INF/jsp/");
		bean.setSuffix(".jsp");
		
		return bean;
	}
//
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
          .addResourceHandler("/css/**", "/fonts/**", "/images/**", "/js/**", "/vendor/**")
       //   .addResourceLocations("/css/", "/fonts/", "/images/", "/js/", "/vendor/")
          .addResourceLocations("classpath:/public/css/", "classpath:/public/images/", "classpath:/public/", "classpath:/public/js/", "classpath:/public/vendor/");
    }
    

}