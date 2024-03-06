package com.camcorderio.camcorderio.config.security;

import com.camcorderio.camcorderio.config.SuccessHandler;
import com.camcorderio.camcorderio.exceptions.UserDisabledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SuccessHandler successHandler;
    private final CustomUserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(SuccessHandler successHandler, CustomUserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder) {
        this.successHandler = successHandler;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(
                        authorizeRequests -> authorizeRequests
                                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                                .requestMatchers("/user/**").hasAnyAuthority("USER")
                                .requestMatchers("/**", "/login", "/signup", "/verify-otp","/assets","/product/{productId}","/product/").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(form ->
                        form
                                .loginPage("/login").permitAll()
                                .successHandler(successHandler)
                                .failureHandler((request, response, exception) -> {
                                    if (exception instanceof AuthenticationException) {
                                        AuthenticationException authException = (AuthenticationException) exception;

                                        if (authException.getCause() instanceof UserDisabledException) {
                                            request.getSession().setAttribute("loginError", "Your account is disabled. Please contact support.");
                                        } else {
                                            request.getSession().setAttribute("loginError", "Invalid Email or Password");
                                        }
                                    }
                                    response.sendRedirect("/login");
                                })
                )
                .sessionManagement(
                        httpSecuritySessionManagementConfigurer ->
                                httpSecuritySessionManagementConfigurer
                                        .maximumSessions(1)
                                        .maxSessionsPreventsLogin(true)
                                        .expiredUrl("/login?sessionExpired")
                )
                .logout(
                        log -> log
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-CSRF-TOKEN");
        return repository;
    }

}