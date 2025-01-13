package com.michal.openai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@ComponentScan(basePackages = { "com.michal.openai" })
@Configuration
@EnableWebSecurity
public class WebConfig implements WebMvcConfigurer {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf(csrf -> csrf.disable()) // Wyłącz CSRF (jeśli nie jest wymagane)
//
//            // Zezwól na dostęp do /chatgpt i /v1/** bez uwierzytelniania
//            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
//                .requestMatchers("/chatgpt").permitAll()  // Zezwól na dostęp do /chatgpt
//                .requestMatchers("/v1/**").permitAll()   // Zezwól na dostęp do /v1/**
//                .requestMatchers("/WEB-INF/views/**").permitAll()  // Zezwól na dostęp do zasobów JSP
//                .anyRequest().authenticated()  // Pozostałe żądania wymagają uwierzytelnienia
//            );
//
//        return http.build();
//    }
	   @Bean
	    public UserDetailsService userDetailsService() {
	        // Tworzymy użytkowników w pamięci
	        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
	        
	        userDetailsService.createUser(User.withUsername("user")
	                .password("{noop}user")  // {noop} oznacza, że nie używamy żadnej enkrypcji hasła
	                .roles("USER")  // Przypisujemy rolę USER
	                .build());
	        
	        userDetailsService.createUser(User.withUsername("admin")
	                .password("{noop}admin")  // Używamy proste hasło, bez szyfrowania
	                .roles("ADMIN")  // Przypisujemy rolę ADMIN
	                .build());
	        
	        return userDetailsService;
	   }
	   
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	http.csrf()
	        .disable()
	        .authorizeHttpRequests()
		        .requestMatchers("*chatgpt*").permitAll()
		        .requestMatchers("/WEB-INF/views/**").permitAll()
		        .anyRequest().authenticated()
		        .and()
		        .formLogin()
		        .permitAll()
		        ;
    	
    	return http.build();
	}    
	        
	        
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/chatgpt")
                .allowedOrigins("*") // Możesz ustawić konkretną domenę
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setViewClass(JstlView.class);
        bean.setPrefix("/WEB-INF/views/");
        bean.setSuffix(".jsp");

        return bean;
    }
    
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // Ustawienie prefiksu i sufiksu dla plików JSP
        registry.jsp("/WEB-INF/views/", ".jsp");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
          .addResourceHandler("/css/**", "/fonts/**", "/images/**", "/js/**", "/vendor/**")
          .addResourceLocations("/css/", "/fonts/", "/images/", "/js/", "/vendor/");
    }
}