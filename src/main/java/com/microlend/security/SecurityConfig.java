package com.microlend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserDetailsService userDetailsService,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                          CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                    // --- DEVELOPMENT ONLY: Temporary public access for Phase F1 UI testing ---
                    .requestMatchers("/", "/login", "/register", "/css/**", "/js/**").permitAll()
                    .requestMatchers("/customer/**", "/officer/**").permitAll()
                    // -------------------------------------------------------------------------
                    .requestMatchers("/api/auth/register", "/api/auth/login", "/api/health", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/api/customers/**").hasRole("CUSTOMER")
                    .requestMatchers("/api/loan-applications/**").hasRole("CUSTOMER")
                    .requestMatchers("/api/eligibility/**").hasRole("CUSTOMER")
                    .requestMatchers("/api/loans/*/amortization**").hasRole("CUSTOMER")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/loans/*/disbursement**").hasRole("LOAN_OFFICER")
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/loans/*/disbursement**").hasRole("LOAN_OFFICER")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/loans/*/repayments**").hasRole("LOAN_OFFICER")
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/loans/*/repayments**").hasAnyRole("CUSTOMER", "LOAN_OFFICER")
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/loans/*/repayment-summary**").hasAnyRole("CUSTOMER", "LOAN_OFFICER")
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/loans/*/overdue**").hasAnyRole("CUSTOMER", "LOAN_OFFICER")
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/loans/{id}/settlement-quote").hasAnyRole("CUSTOMER", "LOAN_OFFICER")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/loan-officer/applications/{id}/prepayments").hasRole("LOAN_OFFICER")
                    .requestMatchers("/api/notifications/**").hasAnyRole("CUSTOMER", "LOAN_OFFICER")
                    .requestMatchers("/api/loan-officer/**").hasRole("LOAN_OFFICER")
                    .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
