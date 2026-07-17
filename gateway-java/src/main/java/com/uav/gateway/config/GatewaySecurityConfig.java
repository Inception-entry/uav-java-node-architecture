package com.uav.gateway.config;

import com.uav.gateway.security.GatewaySecurityErrorHandler;
import com.uav.gateway.security.KeycloakRealmRoleConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class GatewaySecurityConfig {

    @Bean
    @ConditionalOnProperty(
            name = "gateway.security.enabled",
            havingValue = "false",
            matchIfMissing = true
    )
    public SecurityWebFilterChain permitAllSecurityWebFilterChain(
            ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .anyExchange().permitAll()
                )
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            name = "gateway.security.enabled",
            havingValue = "true"
    )
    public SecurityWebFilterChain jwtSecurityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder jwtDecoder,
            Converter<Jwt, Mono<AbstractAuthenticationToken>>
                    jwtAuthenticationConverter,
            GatewaySecurityErrorHandler errorHandler) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler)
                )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(
                                "/actuator/health",
                                "/actuator/info",
                                "/api/health",
                                "/socket.io/**"
                        ).permitAll()
                        .pathMatchers("/actuator/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/**")
                        .hasAnyRole("ADMIN", "OPERATOR", "VIEWER")
                        .pathMatchers(HttpMethod.HEAD, "/api/**")
                        .hasAnyRole("ADMIN", "OPERATOR", "VIEWER")
                        .pathMatchers("/api/**")
                        .hasAnyRole("ADMIN", "OPERATOR")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler)
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder)
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                )
                .build();
    }

    @Bean
    @ConditionalOnProperty(
            name = "gateway.security.enabled",
            havingValue = "true"
    )
    public Converter<Jwt, Mono<AbstractAuthenticationToken>>
            jwtAuthenticationConverter(
                    @Value("${gateway.security.client-id:uav-web}")
                    String clientId) {
        KeycloakRealmRoleConverter roleConverter =
                new KeycloakRealmRoleConverter(clientId);
        ReactiveJwtAuthenticationConverter authenticationConverter =
                new ReactiveJwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                jwt -> Flux.fromIterable(roleConverter.convert(jwt))
        );
        return authenticationConverter;
    }

    @Bean
    @ConditionalOnProperty(
            name = "gateway.security.enabled",
            havingValue = "true"
    )
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${gateway.security.jwk-set-uri:}")
            String jwkSetUri,
            @Value("${gateway.security.issuer-uri:}")
            String issuerUri,
            @Value("${gateway.security.audience:}")
            String audience) {
        if (!StringUtils.hasText(jwkSetUri)) {
            throw new IllegalStateException(
                    "启用 Gateway 鉴权时必须配置 GATEWAY_JWT_JWK_SET_URI"
            );
        }
        if (!StringUtils.hasText(issuerUri)) {
            throw new IllegalStateException(
                    "启用 Gateway 鉴权时必须配置 GATEWAY_JWT_ISSUER_URI"
            );
        }

        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();
        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(issuerUri);
        if (StringUtils.hasText(audience)) {
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                    issuerValidator,
                    audienceValidator(audience)
            ));
        } else {
            decoder.setJwtValidator(issuerValidator);
        }
        return decoder;
    }

    private OAuth2TokenValidator<Jwt> audienceValidator(String audience) {
        return jwt -> {
            if (jwt.getAudience().contains(audience)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "访问令牌不包含所需 audience: " + audience,
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        };
    }
}
