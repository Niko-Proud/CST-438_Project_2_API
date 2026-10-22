package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Protects the API as an OAuth2 resource server.
 *
 * The frontend sends a bearer token. This application validates that token
 * before any controller or repository method is reached. The frontend sends
 * Google's ID token, so the decoder validates Google's issuer and the token's
 * audience against the same Google client ID used by the frontend. Database-backed
 * admin and ownership checks remain in the controllers because those decisions
 * belong to application data, not to untrusted request JSON.
 *
 * Documentation:
 * https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
 */
@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${GOOGLE_CLIENT_ID:}")
    private String googleClientId;

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Builds a decoder without requiring Google metadata during application
     * startup. The client ID is optional for local mock-JWT tests but must be
     * configured in deployment for audience validation to be active.
     */
    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
        decoder.setJwtValidator(googleClientId.isBlank()
                ? issuerValidator
                : new AudienceAndIssuerValidator(issuerValidator, googleClientId));
        return decoder;
    }

    private static final class AudienceAndIssuerValidator implements OAuth2TokenValidator<Jwt> {
        private final OAuth2TokenValidator<Jwt> issuerValidator;
        private final String expectedAudience;

        private AudienceAndIssuerValidator(OAuth2TokenValidator<Jwt> issuerValidator, String expectedAudience) {
            this.issuerValidator = issuerValidator;
            this.expectedAudience = expectedAudience;
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token) {
            OAuth2TokenValidatorResult issuerResult = issuerValidator.validate(token);
            if (issuerResult.hasErrors()) {
                return issuerResult;
            }
            if (token.getAudience().contains(expectedAudience)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token", "Token audience does not match this API", null));
        }
    }
}
