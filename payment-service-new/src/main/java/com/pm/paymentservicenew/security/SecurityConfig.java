package com.pm.paymentservicenew.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final InternalServiceAuthenticationFilter internalServiceAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            InternalServiceAuthenticationFilter internalServiceAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
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
                         * Public endpoints
                         */
                        .requestMatchers(
                                "/api/webhooks/paystack"
                        ).permitAll()

                        /*
                         * Internal service-to-service endpoints
                         *
                         * These requests must be authenticated
                         * by InternalServiceAuthenticationFilter.
                         */
                        .requestMatchers(
                                "/api/internal/**"
                        ).authenticated()

                        /*
                         * Everything else requires authentication
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * Both custom filters are positioned relative to
                 * Spring Security's registered filter.
                 *
                 * Internal authentication runs first.
                 */
                .addFilterBefore(
                        internalServiceAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}