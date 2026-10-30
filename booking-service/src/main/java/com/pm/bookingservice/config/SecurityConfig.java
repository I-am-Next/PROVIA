package com.pm.bookingservice.config;

import com.pm.bookingservice.security.InternalServiceAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final String jwtSecret;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final InternalServiceAuthenticationFilter internalServiceAuthenticationFilter;

    public SecurityConfig(
            @Value("${jwt.secret}") String jwtSecret,
            JwtAuthEntryPoint jwtAuthEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            InternalServiceAuthenticationFilter internalServiceAuthenticationFilter
    ) {
        this.jwtSecret = jwtSecret;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.internalServiceAuthenticationFilter =
                internalServiceAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        /*
                         * Internal service-to-service endpoints.
                         *
                         * These requests do not carry a customer JWT.
                         * They must authenticate using the internal
                         * service authentication mechanism.
                         */
                        .requestMatchers("/internal/**")
                        .authenticated()

                        /*
                         * Client-facing booking endpoints require
                         * a valid user JWT.
                         *
                         * The API Gateway strips /api/v1 before
                         * forwarding the request.
                         */
                        .requestMatchers("/bookings/**")
                        .authenticated()

                        /*
                         * Everything else remains public unless
                         * protected elsewhere.
                         */
                        .anyRequest()
                        .permitAll()
                )

                /*
                 * Internal service authentication must run before
                 * the normal JWT authentication mechanism.
                 */
                .addFilterBefore(
                        internalServiceAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                )

                .exceptionHandling(exceptions ->
                        exceptions
                                .authenticationEntryPoint(
                                        jwtAuthEntryPoint
                                )
                                .accessDeniedHandler(
                                        jwtAccessDeniedHandler
                                )
                );

        return http.build();
    }

    /**
     * Converts the JWT "role" claim into a Spring Security ROLE_ authority.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String role =
                    jwt.getClaimAsString("role");

            if (role != null && !role.isBlank()) {

                return List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        )
                );
            }

            return List.of();
        });

        return converter;
    }

    /**
     * JWT decoder using the shared HMAC secret.
     */
    @Bean
    public JwtDecoder jwtDecoder() {

        SecretKey secretKey =
                new SecretKeySpec(
                        jwtSecret.getBytes(),
                        "HmacSHA256"
                );

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .build();
    }
}