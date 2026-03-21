package service.CSFC.CSFC_auth_service.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import service.CSFC.CSFC_auth_service.common.security.AuthorizationFilter;
import service.CSFC.CSFC_auth_service.common.security.CustomerUserDetailsService;

import java.util.List;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthorizationFilter authorizationFilter)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ADD THIS
                .authorizeHttpRequests(request -> {
                    request.requestMatchers(
                                    "/api/auth-service/auth/register",
                                    "/api/auth-service/auth/login",
                                    "/api/auth-service/auth/refresh",
                                    "/api/auth-service/password/forgot",
                                    "/api/auth-service/password/reset",
                                    "/api/auth-service/password/verify-otp",
                                    "/v3/api-docs/**",
                                    "/v3/api-docs.yaml",
                                    "/swagger-ui/**",
                                    "/swagger-ui.html",
                                    "/swagger-resources/**",
                                    "/webjars/**",
                                    "/api/auth-service/public/**"
                                    ).permitAll()
                            .anyRequest().authenticated();
                })
                .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    // Cấu hình AuthenticationManager để sử dụng CustomerUserDetailsService và
    // PasswordEncoder
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
            CustomerUserDetailsService customerUserDetailsService) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder
                .userDetailsService(customerUserDetailsService)
                .passwordEncoder(passwordEncoder());

        return authBuilder.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Accept ALL origins (correct way)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Accept ALL methods
        configuration.setAllowedMethods(List.of("*"));

        // Accept ALL headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow cookies / Authorization header
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
